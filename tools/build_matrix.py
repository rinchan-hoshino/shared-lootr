#!/usr/bin/env python3
"""Build supported release cells with pinned dependencies and isolated outputs."""
import argparse
from concurrent.futures import ThreadPoolExecutor, as_completed
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import zipfile

ROOT = Path(__file__).resolve().parents[1]


def source_fingerprint():
    files = subprocess.check_output(
        ['git', 'ls-files', '-co', '--exclude-standard', '-z', 'adapters', 'versions', 'support',
         'gradle.properties', 'gradle'], cwd=ROOT).decode().split('\0')
    digest = hashlib.sha256()
    for name in sorted(set(files)):
        path = ROOT / name
        if name and path.is_file():
            digest.update(name.encode() + b'\0' + path.read_bytes())
    return digest.hexdigest()


def build_target(target, output):
    fingerprint = source_fingerprint()
    game, loader = target['minecraft'], target['loader']
    directory = ROOT / 'versions' / target['source_line']
    legacy = game == '1.12.2'
    task = 'build' if legacy else f':{loader}:build'
    wrapper = directory / 'gradlew'
    if not wrapper.exists():
        wrapper = ROOT / 'gradlew'
    command = [str(wrapper), '-p', str(directory), task, f'-Pprofile={game}', f'-Ploader={loader}',
               '--project-cache-dir', str(ROOT / 'build/project-cache' / f'{game}-{loader}'),
               '--console=plain', '--no-daemon', '-Dorg.gradle.internal.http.connectionTimeout=15000',
               '-Dorg.gradle.internal.http.socketTimeout=15000']
    env = os.environ.copy()
    java = '8' if legacy else ('25' if game.startswith('26.') else '21')
    if env.get(f'JAVA_{java}_HOME'):
        env['JAVA_HOME'] = env[f'JAVA_{java}_HOME']
    java_paths = [value for key, value in env.items() if key.startswith('JAVA_') and key.endswith('_HOME')]
    if java_paths:
        command.append('-Porg.gradle.java.installations.paths=' + ','.join(java_paths))
    cell = output / f'{game}-{loader}'
    cell.mkdir(exist_ok=True)
    print(f'Building {game} {loader}', flush=True)
    with (cell / 'build.log').open('w') as log:
        result = subprocess.run(command, cwd=directory, env=env, stdout=log, stderr=subprocess.STDOUT)
    record = dict(target, build_exit_code=result.returncode, artifacts=[],
                  source_fingerprint=fingerprint if fingerprint == source_fingerprint() else None)
    if result.returncode == 0:
        libs = ROOT / 'build/profiles' / game / ('root' if legacy else loader) / 'libs'
        for artifact in libs.glob('*.jar'):
            if any(word in artifact.name for word in ['-sources', '-dev', '-javadoc', '-local-probe']):
                continue
            with zipfile.ZipFile(artifact) as jar:
                forbidden = [name for name in jar.namelist() if name.endswith('.class')
                             and ('Harness' in name or 'sharedlootrprobe' in name)]
                if forbidden:
                    raise RuntimeError(f'Test code leaked into {artifact}: {forbidden}')
            destination = cell / artifact.name
            shutil.copy2(artifact, destination)
            record['artifacts'].append({'file': str(destination),
                                       'sha256': hashlib.sha256(destination.read_bytes()).hexdigest()})
        if not record['artifacts']:
            record['build_exit_code'] = 1
    print(f'{game} {loader}: {"built" if record["build_exit_code"] == 0 else "FAILED"}', flush=True)
    return record


def valid_result(record, fingerprint):
    return record.get('source_fingerprint') == fingerprint and record['build_exit_code'] == 0 and record['artifacts'] and all(
        Path(artifact['file']).is_file() and hashlib.sha256(Path(artifact['file']).read_bytes()).hexdigest()
        == artifact['sha256'] for artifact in record['artifacts'])


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--profile')
    parser.add_argument('--loader', choices=['fabric', 'forge', 'neoforge'])
    parser.add_argument('--resume', action='store_true', help='Reuse successful builds only when sources and artifact hashes match')
    parser.add_argument('--jobs', type=int, default=1)
    parser.add_argument('--output', type=Path, default=ROOT / 'build/matrix')
    args = parser.parse_args()
    if args.jobs < 1:
        parser.error('--jobs must be positive')
    targets = json.loads((ROOT / 'support/release-targets.json').read_text())['targets']
    targets = [target for target in targets if (not args.profile or target['minecraft'] == args.profile)
               and (not args.loader or target['loader'] == args.loader)]
    if not targets:
        parser.error('No supported release target matches the filters')
    output = args.output.resolve()
    output.mkdir(parents=True, exist_ok=True)
    result_file = output / 'results.json'
    results = json.loads(result_file.read_text()) if args.resume and result_file.exists() else []
    fingerprint = source_fingerprint()
    results = [result for result in results if valid_result(result, fingerprint)]
    completed = {(result['minecraft'], result['loader']) for result in results}
    targets = [target for target in targets if (target['minecraft'], target['loader']) not in completed]
    with ThreadPoolExecutor(max_workers=args.jobs) as pool:
        futures = [pool.submit(build_target, target, output) for target in targets]
        for future in as_completed(futures):
            results.append(future.result())
            temporary = output / 'results.json.tmp'
            temporary.write_text(json.dumps(results, indent=2) + '\n')
            temporary.replace(result_file)
    return int(any(result['build_exit_code'] for result in results))


if __name__ == '__main__':
    raise SystemExit(main())
