#!/usr/bin/env python3
"""Run a local physical-container test, stop, restart, and verify saved inventory state."""
import argparse
import json
import os
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]



def probe_for(target):
    game, loader = target['minecraft'], target['loader']
    if game.startswith('26.'):
        return 'inventory-store'
    if game == '1.21.11':
        return 'saved-data-identifier'
    if target['source_line'].startswith('saved-data'):
        return 'saved-data'
    if game == '1.16.5':
        return 'chest-data-1.16'
    if game == '1.17.1':
        return 'chest-data-forge-1.17'
    if loader == 'fabric' and game in ['1.20', '1.20.1']:
        return 'chest-data-fabric-1.20'
    family = 'chest-data-fabric' if loader == 'fabric' else 'chest-data-forge'
    return family + ('-modern' if game in ['1.19.3', '1.19.4', '1.20', '1.20.1', '1.20.4'] else '')


def players_for(target):
    game, loader = target['minecraft'], target['loader']
    if game.startswith('26.'):
        return '26-neoforge' if loader == 'neoforge' else '26'
    if game == '1.20.4':
        return '1.20.4-neoforge' if loader == 'neoforge' else '1.20.4'
    if game in ['1.19', '1.19.1', '1.19.2', '22w24a']:
        return 'signed'
    if target['source_line'].startswith('chest-data'):
        return 'none' if game == '1.16.5' else 'classic'
    return 'modern'


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--profile', required=True)
    parser.add_argument('--loader', required=True, choices=['fabric', 'forge', 'neoforge'])
    parser.add_argument('--without-jade', action='store_true', help='Test required dependencies only')
    parser.add_argument('--port', type=int, default=25585)
    parser.add_argument('--probe', help='Override the automatically selected Lootr API probe')
    parser.add_argument('--lootr-version-id', help='Test a specific Modrinth Lootr version instead of the release pin')
    parser.add_argument('--output', type=Path, default=ROOT / 'build/runtime')
    args = parser.parse_args()
    targets = json.loads((ROOT / 'support/release-targets.json').read_text())['targets']
    target = next((t for t in targets if t['minecraft'] == args.profile and t['loader'] == args.loader), None)
    if target is None:
        parser.error('No supported release target matches this Minecraft version and loader')
    if args.profile == '1.12.2':
        parser.error('Use the standalone production-JAR procedure in runtime-tests/README.md for 1.12.2')
    args.probe = args.probe or probe_for(target)
    project = ROOT / 'versions' / target['source_line']
    instance = args.output.resolve() / f'{args.profile}-{args.loader}'
    instance.mkdir(parents=True, exist_ok=True)
    if (instance / 'world').exists():
        parser.error('Use an empty output directory for a new test; previous evidence is retained')
    (instance / 'eula.txt').write_text('eula=true\n')
    (instance / 'server.properties').write_text(f'online-mode=false\nserver-ip=127.0.0.1\nserver-port={args.port}\n'
        'level-type=minecraft:flat\ngenerate-structures=false\nview-distance=2\nsimulation-distance=2\n'
        'spawn-protection=0\nsync-chunk-writes=true\n')
    wrapper = project / 'gradlew'
    if not wrapper.exists():
        wrapper = ROOT / 'gradlew'
    env = os.environ.copy()
    java = '25' if args.profile.startswith('26.') else '21'
    if env.get(f'JAVA_{java}_HOME'):
        env['JAVA_HOME'] = env[f'JAVA_{java}_HOME']
    players = players_for(target)
    forge_entrypoint = {'1.16.5': 'forge', '1.17.1': 'forge-1.17'}.get(args.profile, 'forge-modern')
    command = [str(wrapper), '-p', str(project), f':{args.loader}:runServer',
        f'-Pprofile={args.profile}', f'-Ploader={args.loader}',
        '--project-cache-dir', str(ROOT / 'build/project-cache' / f'{args.profile}-{args.loader}'),
        '-I', str(ROOT / f'runtime-tests/probe/{args.loader}.init.gradle'),
        f'-Dprobe.sources={ROOT / "runtime-tests/probe" / args.probe}',
        f'-Dprobe.instance={instance}', f'-Dprobe.players={players}',
        f'-Dprobe.forgeEntrypoint={forge_entrypoint}', '--console=plain']
    if args.loader == 'forge':
        command.extend(['-x', 'downloadAssets'])  # Dedicated servers do not use client sound/texture assets.
    if args.lootr_version_id:
        command.extend(['-I', str(ROOT / 'runtime-tests/probe/lootr.init.gradle'),
                        f'-Dprobe.lootrVersion={args.lootr_version_id}'])
    if args.without_jade:
        command.append('-DsharedLootr.omitJadeRuntime=true')
    java_paths = [value for key, value in env.items() if key.startswith('JAVA_') and key.endswith('_HOME')]
    if java_paths:
        command.append('-Porg.gradle.java.installations.paths=' + ','.join(java_paths))
    record = {'minecraft': args.profile, 'loader': args.loader, 'jade_excluded': args.without_jade,
              'lootr_version_id': args.lootr_version_id or target['lootr']['version_id'], 'phases': [],
              'scope': 'Server-world container inventory, two ServerPlayer objects, persistence and clear; no network clients'}
    for phase in [1, 2]:
        with (instance / f'phase{phase}.log').open('w') as log:
            result = subprocess.run(command, env=env, stdout=log, stderr=subprocess.STDOUT)
        text = (instance / f'phase{phase}.log').read_text()
        passed = result.returncode == 0 and f'SHARED_LOOTR_PHASE{phase}_PASS' in text and 'SHARED_LOOTR_RUNTIME_FAIL' not in text
        record['phases'].append({'phase': phase, 'passed': passed, 'exit_code': result.returncode})
        (instance / 'result.json').write_text(json.dumps(record, indent=2) + '\n')
        print(f'{args.profile} {args.loader} phase {phase}: {"PASS" if passed else "FAIL"}', flush=True)
        if not passed:
            return 1
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
