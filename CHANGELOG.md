# Changelog

## 1.3.2

- Declared the required Fabric API and legacy MixinBooter dependencies in release metadata.
- Added the implementation version to legacy Forge manifests so `${file.jarVersion}` resolves correctly.
- Corrected the Minecraft 1.18–1.19.2 Forge chest-data redirect to the `UUID` overload that actually performs the inventory map lookup.
- Does not change the shared-loot gameplay contract or supported matrix.

## 1.3.1

- Removed internal smoke harnesses and their startup/property activation from production JARs across every supported source line.
- Preserved the harness implementations as test-only sources.
- Upgraded the repository Gradle wrapper to 9.5.1 for the current Loom profiles.

## 1.3.0

- Published the chest-data, saved-data, and inventory-store compatibility matrix across supported Minecraft loaders and versions.
