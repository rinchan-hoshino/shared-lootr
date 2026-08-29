from pathlib import Path
import unittest


REPOSITORY_ROOT = Path(__file__).resolve().parents[1]
IGNORED_DIRECTORY_NAMES = {".git", ".gradle", "build"}
SMOKE_ACTIVATION_PROPERTY = "sharedLootr" + ".smoke"
SMOKE_HARNESS_SYMBOL = "Smoke" + "Harness"


def is_main_source(path: Path) -> bool:
    parts = path.relative_to(REPOSITORY_ROOT).parts
    return any(parts[index : index + 2] == ("src", "main") for index in range(len(parts) - 1))


def iter_main_source_files():
    for path in sorted(REPOSITORY_ROOT.rglob("*")):
        if not path.is_file():
            continue
        relative_parts = path.relative_to(REPOSITORY_ROOT).parts
        if any(part in IGNORED_DIRECTORY_NAMES for part in relative_parts):
            continue
        if is_main_source(path):
            yield path


def iter_gradle_config_files():
    for path in sorted(REPOSITORY_ROOT.rglob("*")):
        if not path.is_file():
            continue
        relative_parts = path.relative_to(REPOSITORY_ROOT).parts
        if any(part in IGNORED_DIRECTORY_NAMES for part in relative_parts):
            continue
        if path.name == "gradle.properties" or path.name.endswith((".gradle", ".gradle.kts")):
            yield path


def find_line_references(paths, forbidden_texts):
    references = []
    scanned_lines = 0
    for path in paths:
        with path.open("r", encoding="utf-8", errors="replace") as source:
            for line_number, line in enumerate(source, start=1):
                scanned_lines += 1
                for forbidden_text in forbidden_texts:
                    if forbidden_text in line:
                        references.append(
                            f"{path.relative_to(REPOSITORY_ROOT)}:{line_number}: {forbidden_text}"
                        )
    return scanned_lines, references


class ReleaseSourceContractTest(unittest.TestCase):
    def test_production_sources_contain_no_harness_source(self):
        harness_sources = [
            path.relative_to(REPOSITORY_ROOT)
            for path in iter_main_source_files()
            if "Harness" in path.name
        ]
        self.assertEqual([], harness_sources)

    def test_production_sources_contain_no_smoke_activation(self):
        main_sources = list(iter_main_source_files())
        scanned_lines, references = find_line_references(
            main_sources,
            (SMOKE_ACTIVATION_PROPERTY, SMOKE_HARNESS_SYMBOL),
        )
        self.assertGreater(scanned_lines, 0, "No production source lines were scanned")
        self.assertEqual([], references)

    def test_gradle_configs_contain_no_smoke_activation_property(self):
        gradle_configs = list(iter_gradle_config_files())
        scanned_lines, references = find_line_references(
            gradle_configs,
            (SMOKE_ACTIVATION_PROPERTY,),
        )
        self.assertGreater(scanned_lines, 0, "No Gradle configuration lines were scanned")
        self.assertEqual([], references)


    def test_legacy_forge_inventory_redirect_targets_the_player_overload(self) -> None:
        source = (REPOSITORY_ROOT / "versions/chest-data-legacy-direct/forge/src/main/java/dev/rinchan/sharedlootr/mixin/ForgeChestDataMixin.java").read_text()
        self.assertIn("getInventory(Lnet/minecraft/server/level/ServerPlayer;)", source)
        self.assertNotIn("getInventory(Ljava/util/UUID;)", source)

    def test_loader_mixins_are_registered_by_release_resources(self) -> None:
        for module in REPOSITORY_ROOT.glob("versions/*/*"):
            if module.name not in {"fabric", "forge", "neoforge"}:
                continue
            source_root = module / "src/main/java"
            if not source_root.is_dir():
                continue
            configs = []
            for path in (module / "src/main/resources").glob("*.mixins.json"):
                configs.append(path.read_text())
            registered_text = "\n".join(configs)
            for mixin in source_root.rglob("*Mixin.java"):
                self.assertIn(f'"{mixin.stem}"', registered_text, str(mixin))


if __name__ == "__main__":
    unittest.main()
