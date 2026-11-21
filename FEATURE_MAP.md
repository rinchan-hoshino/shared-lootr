# Shared Lootr 2.0.2 Feature Map

## Architecture Overview

Shared Lootr 2.0.2 uses a single unified multi-profile Gradle build (`profiles.json`) targeting **10 golden artifacts** across 5 Minecraft versions (`1.20.1`, `1.21.1`, `26.1.2`, `26.2`, `26.3`) under package `dev.rinchan.sharedlootr`.

| Module / Layer | Responsibility | Key Files |
| :--- | :--- | :--- |
| **Core (`common/src/main`)** | Constants, zero-dependency `SimpleToml` config, pure `LootPoolResolver`, optional reflective `FtbTeamsBridge`, `PlayerPoolResolver` | `SharedLootr.java`, `SharingMode.java`, `SimpleToml.java`, `SharedLootrConfig.java`, `LootPoolResolver.java`, `FtbTeamsBridge.java`, `PlayerPoolResolver.java` |
| **1.20.1 (`v120` + `fabric/src/v120` + `forge/src/main`)** | Native 1.20.1 `SharedLootrConfigScreen`, server-safe UUID-projection Mixins + isolated `mixin.client` visual Mixins for Lootr `0.7.35.x` | `SharedLootrConfigScreen.java`, `Fabric*Mixin.java`, `Forge*Mixin.java`, `SharedLootrForgeClient.java` |
| **1.21.1 (`v121`)** | Official `noobanidus.mods.lootr.common.api.team.ITeamResolver` (`ResourceLocation`) + `LootrTeamConfigMixin` + 1.21.1 `SharedLootrConfigScreen` | `SharedTeamResolver.java`, `LootrTeamConfigMixin.java`, `META-INF/services/...team.ITeamResolver` |
| **26.1.2 (`v26` + `v26_1_2`)** | Official `noobanidus.mods.lootr.common.api.interfaces.team.ITeamResolver` (`Identifier`) + `LootrTeamConfigMixin` + 26.x `SharedLootrConfigScreen` | `SharedTeamResolver.java`, `LootrTeamConfigMixin.java`, `META-INF/services/...interfaces.team.ITeamResolver` |
| **26.2 & 26.3 (`v26` + `v26_2`)** | Official `noobanidus.mods.lootr.common.api.interfaces.processor.ITeamResolver` (`Identifier`) + shared `v26` bridge & UI | `SharedTeamResolver.java`, `META-INF/services/...interfaces.processor.ITeamResolver` |
| **UI & Localization** | Client-isolated Mod Menu (`fabric.client`), Forge `SharedLootrForgeClient`, NeoForge `SharedLootrNeoForgeClient`, 4-language JSON | `fabric/client/SharedLootrModMenu.java`, `forge/client/SharedLootrForgeClient.java`, `neoforge/client/SharedLootrNeoForgeClient.java`, `lang/{en_us,zh_cn,zh_tw,ja_jp}.json` |
| **Verification (3 Hard Gates)** | Packaged-JAR real-runtime GameTest + Config Screen lifecycle, >=1,000 fixed-seed Property fuzzing, Architecture bytecode/JAR guardrails, Git hooks & containerized runner | `SharedLootrPackagedJarGameTests.java`, `SharedLootrConfigScreenTest.java`, `SharedLootrPropertyTest.java`, `ArchitectureContractTest.java`, `scripts/verify.sh`, `Dockerfile.verify`, `.githooks/pre-push` |

## Pool Resolution Invariants

1. `sharingMode = "GLOBAL"` -> always `00000000-0000-0000-0000-000000000001`.
2. `sharingMode = "TEAMS"`:
   - Active FTB party/server team -> FTB Team UUID (takes priority over vanilla team; personal FTB player teams are treated as solo).
   - Vanilla scoreboard `PlayerTeam` -> `UUID.nameUUIDFromBytes("shared_lootr:vanilla_team:" + teamName)`.
   - Solo player (no team):
     - `soloPlayersSharePool = false` (default) -> `player.getUUID()` (private per-player pool).
     - `soloPlayersSharePool = true` -> deterministic `SOLO_POOL_UUID`.
3. Client-side resolution mirrors server-side resolution via FTB Teams client manager and synced vanilla scoreboard teams, keeping unopened/opened Lootr container visuals aligned in both modes.

## Verification Gates & Commands

Install the local pre-push hook once per clone, and run the unified 3-gate verifier locally or in the reproducible container:

```bash
./scripts/setup-hooks.sh
./scripts/verify.sh [--container]
```

| Gate | Scope | Direct Command |
| :--- | :--- | :--- |
| **Gate 1: Packaged-JAR Real-Runtime E2E & Screen Lifecycle** | Boots real MC `1.21.1` NeoForge `gameTestServer` loading the packaged `shared_lootr-neoforge-2.0.0+1.21.1.jar` + upstream `Lootr` JAR; verifies `GLOBAL` shared loot, `SOLO` isolated loot, non-sneak break block, sneak break allow, `refresh` & `decay` hooks, plus headless `SharedLootrConfigScreen` init/reset/save/close | `./gradlew -Pminecraft_version=1.21.1 -Ploader=neoforge :neoforge:runE2eGameTestServer` |
| **Gate 2: Fixed-Seed Property Invariant Fuzzing** | `SharedLootrPropertyTest`: 2,000 random player/team topologies (determinism, symmetry, priority, isolation) + 1,500 TOML round-trip & fuzz trials | `./gradlew -Pminecraft_version=1.21.1 -Ploader=neoforge :neoforge:test --tests dev.rinchan.sharedlootr.SharedLootrPropertyTest` |
| **Gate 3: Architecture Contract Guardrails** | `ArchitectureContractTest`: JVM constant-pool scan enforcing zero client/GUI references in non-`client` classes, bidirectional Mixin closure (`*.mixins.json` <-> JAR & `@Mixin` source), zero `rinlib` / `modmenu` stub leaks across built JARs | `./gradlew -Pminecraft_version=1.21.1 -Ploader=neoforge :neoforge:test --tests dev.rinchan.sharedlootr.ArchitectureContractTest` |
| **All-in-One Matrix & Full Check** | Builds all 10 target JARs, runs unit + property + screen + architecture suites across `build/dist/*.jar`, and runs Packaged-JAR `runE2eGameTestServer` | `python3 scripts/build-matrix.py` (or `./gradlew -Pminecraft_version=1.21.1 -Ploader=neoforge :neoforge:check`) |
