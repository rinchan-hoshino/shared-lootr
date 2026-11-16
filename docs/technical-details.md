# Shared Lootr

Shared Lootr is a thin compatibility layer for Lootr. Lootr remains the sole owner of container conversion, block entities, menus, renderers, models, textures, particles, packets, and persistence; this project changes only inventory ownership from per-player to one shared inventory per container.

## Behavior

- Lootr converts supported generated chests, trapped chests, barrels, shulker boxes, minecarts, and tagged custom inventories through its native paths.
- Every player sees and modifies the same inventory for a given Lootr container.
- Newer Lootr adapters derive opened state from shared inventory existence. Older ChestData adapters project Lootr's native opener markers onto the shared UUID; their lifecycle follows the corresponding Lootr API.
- Lootr's native block events and NBT carry opened state to clients; Shared Lootr defines no network packet.
- Lootr's native conversion still turns each half of a vanilla double chest into a separate single Lootr chest.
- No vanilla container mixin, custom block entity, renderer, model, texture, particle, or packet is provided here.

## Incompatible upgrade

Shared Lootr 1.2.0 does not migrate per-player inventories created by Lootr or Shared Lootr 1.1.x. When 1.2.0 is installed in an existing world:

- Existing player-owned inventory entries remain stored but are not used.
- The first player to open each affected container creates a new shared inventory from its loot table.
- As a result, previously collected loot can become available again.

## Source layout

Shared Lootr embeds the shared-owner state contract (`SharedOwnerState`), including the stable UUID and map operations, with no external RinLib dependency. Lootr owns container persistence and inventory generation. Shared Lootr's adapters connect those contracts to each supported Lootr API.

Production Java lives in `adapters/`. `support/source-sets.json` selects existing source files at compile time for each loader module; matching implementations share one physical source file. API families cover legacy 1.12.2, ChestData, LootrSavedData, and LootrInventoryStore. Loader and Minecraft API differences remain in their adapters.

`support/release-targets.json` lists the 56 supported loader/Minecraft combinations across 31 Minecraft versions and pins their Lootr releases. The root `gradle.properties` is the only Shared Lootr release version setting.

## Build

```bash
# Source and release metadata checks
python3 -m unittest discover -s tests -v

# One Minecraft version, one loader
./gradlew build -Pprofile=1.21.1 -Ploader=neoforge

# Complete release matrix
./gradlew build
```

Artifacts, SHA-256 hashes, source fingerprints, and build logs are collected under `build/matrix/`. Build outputs and Gradle project caches are isolated by Minecraft version and loader. Use `-Pjobs=2` for two parallel builds. Each profile uses its compatible Gradle wrapper. Set `JAVA_8_HOME`, `JAVA_16_HOME`, `JAVA_17_HOME`, `JAVA_21_HOME`, and `JAVA_25_HOME` when those JDKs are installed outside standard discovery locations.

Build results and source checks do not replace local server or client behavior tests. Runtime scenarios are defined under `runtime-tests/scenarios/`.
