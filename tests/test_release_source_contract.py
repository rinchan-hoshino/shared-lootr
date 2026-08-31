import json
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


    def test_legacy_forge_inventory_redirect_targets_each_provider_shape(self) -> None:
        direct = (REPOSITORY_ROOT / "versions/chest-data-legacy-direct/forge/src/main/java/dev/rinchan/sharedlootr/mixin/ForgeChestDataMixin.java").read_text()
        self.assertIn("getInventory(Lnet/minecraft/server/level/ServerPlayer;)", direct)
        self.assertNotIn("getInventory(Ljava/util/UUID;)", direct)

        shared = (REPOSITORY_ROOT / "versions/chest-data-legacy/forge/src/main/java/dev/rinchan/sharedlootr/mixin/ForgeChestDataMixin.java").read_text()
        self.assertIn("getInventory(Lnet/minecraft/server/level/ServerPlayer;)", shared)
        self.assertIn("getInventory(Ljava/util/UUID;)", shared)

    def test_forge_minecart_mixins_cover_the_production_method_name(self) -> None:
        for path in REPOSITORY_ROOT.glob("versions/*/forge/src/main/java/**/ForgeMinecart*Mixin.java"):
            source = path.read_text()
            if "startSeenByPlayer" in source:
                self.assertIn('{"startSeenByPlayer", "m_6457_"}', source, str(path))
                self.assertNotIn("m_5856_", source, str(path))

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

    def test_every_fabric_release_descriptor_declares_fabric_api(self) -> None:
        descriptors = list(REPOSITORY_ROOT.glob("versions/*/fabric/src/main/resources/fabric.mod.json"))
        self.assertGreater(len(descriptors), 0)
        for path in descriptors:
            metadata = json.loads(path.read_text())
            self.assertIn("fabric-api", metadata.get("depends", {}), str(path))

    def test_every_parameterized_forge_jar_binds_file_jar_version(self) -> None:
        builds = list(REPOSITORY_ROOT.glob("versions/*/forge/build.gradle"))
        self.assertGreater(len(builds), 0)
        for path in builds:
            source = path.read_text()
            self.assertIn("'Implementation-Version': project.version.toString()", source, str(path))

    def test_legacy_mixin_dependency_is_declared_to_the_loader(self) -> None:
        annotation = REPOSITORY_ROOT / "versions/legacy-1.12.2/src/main/java/dev/rinchan/sharedlootr/SharedLootr.java"
        descriptor = REPOSITORY_ROOT / "versions/legacy-1.12.2/src/main/resources/mcmod.info"
        self.assertIn("required-after:mixinbooter", annotation.read_text())
        self.assertIn("mixinbooter", json.loads(descriptor.read_text())[0]["requiredMods"])


if __name__ == "__main__":
    unittest.main()
