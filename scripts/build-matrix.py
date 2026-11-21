#!/usr/bin/env python3
"""Build, audit, and run all 3 hard verification gates across Shared Lootr 2.0.2 targets."""

from __future__ import annotations

import argparse
import json
import os
import shutil
import subprocess
import sys
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
PROFILES_PATH = ROOT / "profiles.json"
DIST_DIR = ROOT / "build" / "dist"
MAX_JAR_BYTES = 100 * 1024

EXPECTED_LANGS = [
    "assets/shared_lootr/lang/en_us.json",
    "assets/shared_lootr/lang/zh_cn.json",
    "assets/shared_lootr/lang/zh_tw.json",
    "assets/shared_lootr/lang/ja_jp.json",
]

EXPECTED_SERVICES = {
    "1.21.1": "META-INF/services/noobanidus.mods.lootr.common.api.team.ITeamResolver",
    "26.1.2": "META-INF/services/noobanidus.mods.lootr.common.api.interfaces.team.ITeamResolver",
    "26.2": "META-INF/services/noobanidus.mods.lootr.common.api.interfaces.processor.ITeamResolver",
    "26.3": "META-INF/services/noobanidus.mods.lootr.common.api.interfaces.processor.ITeamResolver",
}

FORBIDDEN_ENTRIES = (
    "rinlib",
    "com/terraformersmc/modmenu/",
)


JAVA_ENV_KEYS = (
    "JAVA_17_HOME",
    "JAVA_21_HOME",
    "JAVA_25_HOME",
    "JAVA_HOME_17_X64",
    "JAVA_HOME_21_X64",
    "JAVA_HOME_25_X64",
    "JAVA_HOME_17_arm64",
    "JAVA_HOME_21_arm64",
    "JAVA_HOME_25_arm64",
)


def resolve_java_home(java_version: int) -> str | None:
    for key in (
        f"JAVA_{java_version}_HOME",
        f"JAVA_HOME_{java_version}_X64",
        f"JAVA_HOME_{java_version}_arm64",
    ):
        val = os.environ.get(key)
        if val and Path(val).is_dir():
            return val
    return None


def run_gradle(args: list[str], java_version: int | None = None) -> None:
    env = os.environ.copy()
    if java_version is not None:
        # fabric-loom 1.14+ requires Gradle itself to run on Java 21+, while Java 17 is used via toolchain.
        daemon_java = max(java_version, 21)
        java_home = resolve_java_home(daemon_java)
        if java_home:
            env["JAVA_HOME"] = java_home
            env["PATH"] = f"{Path(java_home) / 'bin'}{os.pathsep}{env.get('PATH', '')}"

    from_env = ",".join(JAVA_ENV_KEYS)
    cmd = [
        str(ROOT / "gradlew"),
        "--no-daemon",
        "--console=plain",
        f"-Dorg.gradle.java.installations.fromEnv={from_env}",
        *args,
    ]
    print(f">>> {' '.join(cmd)}", flush=True)
    subprocess.run(cmd, cwd=ROOT, env=env, check=True)


def find_primary_jar(mc_version: str, loader: str) -> Path:
    libs_dir = ROOT / "build" / "profiles" / mc_version / loader / "libs"
    candidates = sorted(
        p for p in libs_dir.glob("*.jar")
        if not p.name.endswith(("-sources.jar", "-dev.jar", "-javadoc.jar"))
    )
    if len(candidates) != 1:
        raise RuntimeError(f"Expected 1 primary jar in {libs_dir}, found: {[p.name for p in candidates]}")
    return candidates[0]


def audit_jar(jar_path: Path, mc_version: str, loader: str) -> None:
    size = jar_path.stat().st_size
    if size > MAX_JAR_BYTES:
        raise RuntimeError(f"{jar_path.name} is {size} bytes (> {MAX_JAR_BYTES} bytes limit)")

    with zipfile.ZipFile(jar_path, "r") as zf:
        names = set(zf.namelist())
        if loader == "forge":
            if "pack.mcmeta" not in names:
                raise RuntimeError(f"{jar_path.name} is missing pack.mcmeta")
            pack_meta = json.loads(zf.read("pack.mcmeta").decode("utf-8"))
            if pack_meta.get("pack", {}).get("pack_format") != 15:
                raise RuntimeError(f"{jar_path.name} pack.mcmeta must have pack_format == 15, got {pack_meta}")
        elif "pack.mcmeta" in names:
            pack_meta = json.loads(zf.read("pack.mcmeta").decode("utf-8"))
            if pack_meta.get("pack", {}).get("pack_format") == 15:
                raise RuntimeError(f"{jar_path.name} must NOT contain pack_format == 15 pack.mcmeta")

    for forbidden in FORBIDDEN_ENTRIES:
        leaked = [n for n in names if forbidden in n]
        if leaked:
            raise RuntimeError(f"{jar_path.name} contains forbidden entries ({forbidden}): {leaked[:5]}")

    required = ["icon.png", "shared_lootr.mixins.json", *EXPECTED_LANGS]
    if loader == "fabric":
        required.append("fabric.mod.json")
    elif loader == "forge":
        required.append("META-INF/mods.toml")
    elif loader == "neoforge":
        required.append("META-INF/neoforge.mods.toml")

    service_entry = EXPECTED_SERVICES.get(mc_version)
    if service_entry:
        required.append(service_entry)

    missing = [req for req in required if req not in names]
    if missing:
        raise RuntimeError(f"{jar_path.name} is missing required entries: {missing}")

    print(f"  [OK] {jar_path.name} ({size:,} bytes)", flush=True)


def main() -> int:
    parser = argparse.ArgumentParser(description="Build, audit, and verify Shared Lootr matrix.")
    parser.add_argument("--mc", help="Only build a specific Minecraft version (e.g. 1.21.1)")
    parser.add_argument("--loader", help="Only build a specific loader (fabric/forge/neoforge)")
    parser.add_argument("--skip-tests", action="store_true", help="Skip JUnit 5 and Packaged-JAR GameTest gates")
    args = parser.parse_args()

    profiles = json.loads(PROFILES_PATH.read_text(encoding="utf-8"))

    if not args.mc and not args.loader and DIST_DIR.exists():
        shutil.rmtree(DIST_DIR)
    DIST_DIR.mkdir(parents=True, exist_ok=True)
    built: list[Path] = []

    for mc_version, spec in profiles.items():
        if args.mc and mc_version != args.mc:
            continue
        loaders = [l for l in spec["loaders"] if not args.loader or l == args.loader]
        if not loaders:
            continue

        tasks = [t for loader in loaders for t in (f":{loader}:clean", f":{loader}:assemble")]
        loader_prop = loaders[0] if len(loaders) == 1 else "all"
        run_gradle([f"-Pminecraft_version={mc_version}", f"-Ploader={loader_prop}", *tasks], java_version=spec["java"])

        for loader in loaders:
            jar = find_primary_jar(mc_version, loader)
            audit_jar(jar, mc_version, loader)
            dest = DIST_DIR / jar.name
            shutil.copy2(jar, dest)
            built.append(dest)

    if not args.skip_tests:
        # Run all 3 hard verification gates against freshly built JARs in build/dist/:
        # - JUnit 5 Unit + >=1,000 Property Fuzz + SharedLootrConfigScreen Lifecycle + ArchitectureContractTest
        # - Packaged-JAR real-runtime NeoForge 1.21.1 GameTestServer (against shared_lootr-neoforge-2.0.0+1.21.1.jar)
        run_gradle(
            [
                "-Pminecraft_version=1.21.1",
                "-Ploader=neoforge",
                ":neoforge:test",
                ":neoforge:runE2eGameTestServer",
                "--rerun-tasks",
            ],
            java_version=profiles["1.21.1"]["java"],
        )

    print(f"\nAll {len(built)} target JAR(s) built, audited, and verified in {DIST_DIR}:", flush=True)
    for jar in built:
        print(f"  - {jar.name} ({jar.stat().st_size:,} bytes)")
    return 0


if __name__ == "__main__":
    sys.exit(main())
