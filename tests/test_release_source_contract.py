import json
import re
from collections import Counter
from pathlib import Path
import unittest


REPOSITORY_ROOT = Path(__file__).resolve().parents[1]
IGNORED_DIRECTORY_NAMES = {".git", ".gradle", "build"}
SMOKE_ACTIVATION_PROPERTY = "sharedLootr" + ".smoke"
SMOKE_HARNESS_SYMBOL = "Smoke" + "Harness"


def is_main_source(path: Path) -> bool:
    parts = path.relative_to(REPOSITORY_ROOT).parts
    return parts[0] == "adapters" or any(parts[index : index + 2] == ("src", "main") for index in range(len(parts) - 1))


def module_sources(module):
    manifest = json.loads((REPOSITORY_ROOT / "support/source-sets.json").read_text())
    return [REPOSITORY_ROOT / "adapters" / name for name in manifest["modules"][module]]


def module_source(module, filename):
    return next(path for path in module_sources(module) if path.name == filename)


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
        direct = module_source("chest-data-legacy-direct/forge", "ForgeChestDataMixin.java").read_text()
        self.assertIn("getInventory(Lnet/minecraft/server/level/ServerPlayer;)", direct)
        self.assertNotIn("getInventory(Ljava/util/UUID;)", direct)

        shared = module_source("chest-data-legacy/forge", "ForgeChestDataMixin.java").read_text()
        self.assertIn("getInventory(Lnet/minecraft/server/level/ServerPlayer;)", shared)
        self.assertIn("getInventory(Ljava/util/UUID;)", shared)

    def test_forge_minecart_mixins_cover_the_production_method_name(self) -> None:
        for path in (REPOSITORY_ROOT / "adapters").rglob("ForgeMinecart*Mixin.java"):
            source = path.read_text()
            if "startSeenByPlayer" in source:
                self.assertIn('{"startSeenByPlayer", "m_6457_"}', source, str(path))
                self.assertNotIn("m_5856_", source, str(path))

    def test_loader_mixins_are_registered_by_release_resources(self) -> None:
        manifest = json.loads((REPOSITORY_ROOT / "support/source-sets.json").read_text())
        for module in manifest["modules"]:
            directory = REPOSITORY_ROOT / "versions" / module
            resource_dirs = [directory / "src/main/resources",
                             directory.parent / "shared/src/main/resources"]
            registered = "\n".join(path.read_text() for root in resource_dirs
                                   for path in root.glob("*.mixins.json"))
            for source in module_sources(module):
                if "@Mixin(" in source.read_text():
                    self.assertIn(f'"{source.stem}"', registered, str(source))

    def test_every_fabric_release_descriptor_declares_fabric_api(self) -> None:
        descriptors = list(REPOSITORY_ROOT.glob("versions/*/fabric/src/main/resources/fabric.mod.json"))
        self.assertGreater(len(descriptors), 0)
        for path in descriptors:
            metadata = json.loads(path.read_text())
            self.assertEqual(1, len({"fabric", "fabric-api"} & metadata.get("depends", {}).keys()), str(path))

    def test_every_parameterized_forge_jar_binds_file_jar_version(self) -> None:
        builds = list(REPOSITORY_ROOT.glob("versions/*/forge/build.gradle"))
        self.assertGreater(len(builds), 0)
        for path in builds:
            source = path.read_text()
            self.assertIn("'Implementation-Version': project.version.toString()", source, str(path))

    def test_legacy_mixin_dependency_is_declared_to_the_loader(self) -> None:
        annotation = module_source("legacy-1.12.2", "SharedLootr.java")
        descriptor = REPOSITORY_ROOT / "versions/legacy-1.12.2/src/main/resources/mcmod.info"
        self.assertIn("required-after:mixinbooter", annotation.read_text())
        self.assertIn("mixinbooter", json.loads(descriptor.read_text())[0]["requiredMods"])

    def test_source_composition_is_complete_and_unambiguous(self):
        manifest = json.loads((REPOSITORY_ROOT / "support/source-sets.json").read_text())
        used = set()
        for module in manifest["modules"]:
            names = []
            for path in module_sources(module):
                source = path.read_text()
                package = re.search(r"package ([\w.]+);", source).group(1)
                names.append(package + "." + path.stem)
                used.add(path)
            self.assertEqual(len(names), len(set(names)), module)
        self.assertEqual(used, set((REPOSITORY_ROOT / "adapters").rglob("*.java")))
        counts = Counter(path.read_bytes() for path in used)
        self.assertTrue(all(count == 1 for count in counts.values()), "Duplicate production source")

    def test_every_release_target_selects_a_source_module(self):
        targets = json.loads((REPOSITORY_ROOT / "support/release-targets.json").read_text())["targets"]
        self.assertEqual(56, len(targets))
        self.assertEqual(31, len({target["minecraft"] for target in targets}))
        for target in targets:
            module = target["source_line"]
            if module != "legacy-1.12.2":
                module += "/" + target["loader"]
            self.assertTrue(module_sources(module))
            self.assertTrue(target["rinlib"]["version_id"])
            self.assertTrue(target["lootr"]["version_id"])

    def test_identical_lootr_releases_share_core_source_files(self):
        targets = json.loads((REPOSITORY_ROOT / "support/release-targets.json").read_text())["targets"]
        groups = {}
        for target in targets:
            module = target["source_line"]
            if module != "legacy-1.12.2":
                module += "/" + target["loader"]
            core = {path for path in module_sources(module) if "integration" not in path.parts}
            key = (target["loader"], target["lootr"]["version_id"])
            if key in groups:
                self.assertEqual(groups[key], core, str(key))
            groups[key] = core

    def test_profiles_use_the_canonical_release_version(self):
        for path in (REPOSITORY_ROOT / "versions").glob("*/build.gradle"):
            source = re.sub(r"minecraft\s*\{[^}]*\}", "", path.read_text())
            self.assertNotRegex(source, r"(?m)^\s*version\s*=\s*[\"']\d", str(path))
        for path in (REPOSITORY_ROOT / "versions").glob("*/gradle.properties"):
            self.assertNotRegex(path.read_text(), r"(?m)^mod_version=", str(path))


if __name__ == "__main__":
    unittest.main()
