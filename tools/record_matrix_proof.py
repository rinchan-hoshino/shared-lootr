#!/usr/bin/env python3
"""Record a proved support-matrix cell without changing its upstream dependency lock."""

from __future__ import annotations

import argparse
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MATRIX = ROOT / "support" / "lootr-matrix.json"


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("game_version")
    parser.add_argument("loader", choices=("forge", "fabric", "neoforge"))
    parser.add_argument("status")
    parser.add_argument("proof", nargs="+")
    args = parser.parse_args()

    document = json.loads(MATRIX.read_text(encoding="utf-8"))
    matches = [
        target
        for target in document["targets"]
        if target["gameVersion"] == args.game_version and target["loader"] == args.loader
    ]
    if len(matches) != 1:
        raise SystemExit(f"expected one matrix cell, found {len(matches)}")

    target = matches[0]
    target["status"] = args.status
    target["proof"] = args.proof
    MATRIX.write_text(json.dumps(document, indent=2) + "\n", encoding="utf-8")


if __name__ == "__main__":
    main()
