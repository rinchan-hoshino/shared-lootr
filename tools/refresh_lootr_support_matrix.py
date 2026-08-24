#!/usr/bin/env python3
"""Regenerate the checked-in Lootr compatibility target matrix from Modrinth.

CurseForge additionally marks the 26.1 artifact as compatible with 26.1.1;
that official compatibility alias is included explicitly below.
"""
from __future__ import annotations

import json
from pathlib import Path
from urllib.request import Request, urlopen

PROJECT_ID = "EltpO5cN"
OUTPUT = Path(__file__).resolve().parents[1] / "support" / "lootr-matrix.json"
LOADER_ORDER = {"forge": 0, "neoforge": 1, "fabric": 2}


def fetch_json(url: str):
    request = Request(url, headers={"User-Agent": "Shared-Lootr-support-matrix/1"})
    with urlopen(request) as response:
        return json.load(response)


def era(game_version: str) -> str:
    if game_version == "1.12.2":
        return "legacy_1_12"
    if game_version == "22w24a" or game_version.startswith(("1.16", "1.17", "1.18", "1.19", "1.20")):
        return "chest_data"
    if game_version.startswith("1.21"):
        return "saved_data"
    if game_version.startswith("26."):
        return "inventory_store"
    raise ValueError(f"Unclassified Minecraft version: {game_version}")


def release_line(game_version: str) -> str:
    aliases = {
        "1.16.4": "1.16.5",
        "1.18": "1.18.1",
        "22w24a": "1.19.2",
        "1.19": "1.19.2",
        "1.19.1": "1.19.2",
        "1.20": "1.20.1",
        "1.21.2": "1.21.3",
        "26.1.1": "26.1",
    }
    return aliases.get(game_version, game_version)


def main() -> None:
    project = fetch_json(f"https://api.modrinth.com/v2/project/{PROJECT_ID}")
    versions = fetch_json(f"https://api.modrinth.com/v2/project/{PROJECT_ID}/version")

    labels = list(project["game_versions"])
    labels.insert(labels.index("26.1") + 1, "26.1.1")
    latest: dict[tuple[str, str], dict] = {}
    for version in versions:
        for game_version in version["game_versions"]:
            for loader in version["loaders"]:
                key = game_version, loader
                if key not in latest or version["date_published"] > latest[key]["date_published"]:
                    latest[key] = version

    targets = []
    for game_version in labels:
        source_version = "26.1" if game_version == "26.1.1" else game_version
        pairs = sorted(
            ((loader, version) for (label, loader), version in latest.items() if label == source_version),
            key=lambda pair: LOADER_ORDER[pair[0]],
        )
        if not pairs:
            raise RuntimeError(f"No official Lootr release found for {game_version}")
        for loader, version in pairs:
            file = next(item for item in version["files"] if item["primary"])
            targets.append(
                {
                    "gameVersion": game_version,
                    "loader": loader,
                    "stability": "snapshot" if game_version == "22w24a" else "release",
                    "era": era(game_version),
                    "releaseLine": release_line(game_version),
                    "lootr": {
                        "projectId": PROJECT_ID,
                        "versionId": version["id"],
                        "versionNumber": version["version_number"],
                        "fileName": file["filename"],
                        "sha512": file["hashes"]["sha512"],
                    },
                    "evidence": (
                        "curseforge:361276:26.1-build-declares-26.1.1"
                        if game_version == "26.1.1"
                        else f"modrinth:{version['id']}"
                    ),
                    "status": "planned",
                    "proof": [],
                }
            )

    label_count = len({target["gameVersion"] for target in targets})
    if label_count != 33 or len(targets) != 60:
        raise RuntimeError(f"Expected 33 labels and 60 cells, got {label_count} and {len(targets)}")

    document = {
        "schemaVersion": 1,
        "generatedAt": "2026-08-24",
        "coverageRule": "Every Minecraft-version and loader combination listed by Lootr on Modrinth, plus CurseForge's 26.1.1 compatibility alias; snapshots are included.",
        "sources": {
            "modrinthProject": "https://modrinth.com/mod/lootr",
            "modrinthProjectId": PROJECT_ID,
            "curseForgeForgeNeoForgeProjectId": 361276,
            "curseForgeFabricProjectId": 615106,
            "upstreamSource": "https://github.com/LootrMinecraft/Lootr",
        },
        "labelCount": label_count,
        "cellCount": len(targets),
        "targets": targets,
    }
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_text(json.dumps(document, indent=2) + "\n", encoding="utf-8")


if __name__ == "__main__":
    main()
