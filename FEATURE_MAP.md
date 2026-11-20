# Shared Lootr 2.0.0 Feature Map

## Architecture Overview

Shared Lootr 2.0.0 uses a single unified multi-profile Gradle build (`profiles.json`) targeting **8 golden artifacts** across 4 Minecraft versions (`1.20.1`, `1.21.1`, `26.1.2`, `26.2`) under package `dev.rinchan.sharedlootr`.

| Module / Layer | Responsibility | Key Files |
| :--- | :--- | :--- |
| **Core (`common/src/main`)** | Constants, zero-dependency `SimpleToml` config, pure `LootPoolResolver`, optional reflective `FtbTeamsBridge`, `PlayerPoolResolver` | `SharedLootr.java`, `SharingMode.java`, `SimpleToml.java`, `SharedLootrConfig.java`, `LootPoolResolver.java`, `FtbTeamsBridge.java`, `PlayerPoolResolver.java` |
| **1.20.1 (`v120` + `fabric/src/v120` + `forge/src/main`)** | Native 1.20.1 `SharedLootrConfigScreen` + minimal UUID-projection Mixins for Lootr `0.7.35.x` (no `ITeamResolver`) | `SharedLootrConfigScreen.java`, `Fabric*Mixin.java`, `Forge*Mixin.java` |
| **1.21.1 (`v121`)** | Official `noobanidus.mods.lootr.common.api.team.ITeamResolver` (`ResourceLocation`) + `LootrTeamConfigMixin` + 1.21.1 `SharedLootrConfigScreen` | `SharedTeamResolver.java`, `LootrTeamConfigMixin.java`, `META-INF/services/...team.ITeamResolver` |
| **26.1.2 (`v26` + `v26_1_2`)** | Official `noobanidus.mods.lootr.common.api.interfaces.team.ITeamResolver` (`Identifier`) + `LootrTeamConfigMixin` + 26.x `SharedLootrConfigScreen` | `SharedTeamResolver.java`, `LootrTeamConfigMixin.java`, `META-INF/services/...interfaces.team.ITeamResolver` |
| **26.2 (`v26` + `v26_2`)** | Official `noobanidus.mods.lootr.common.api.interfaces.processor.ITeamResolver` (`Identifier`) + shared `v26` bridge & UI | `SharedTeamResolver.java`, `META-INF/services/...interfaces.processor.ITeamResolver` |
| **UI & Localization** | Mod Menu (Fabric compile-time stub), Forge `ConfigScreenHandler`, NeoForge `IConfigScreenFactory`, 4-language JSON | `SharedLootrModMenu.java`, `SharedLootrForge.java`, `SharedLootrNeoForgeClient.java`, `lang/{en_us,zh_cn,zh_tw,ja_jp}.json` |
| **Verification** | JUnit 5 invariant tests + 8-target build & JAR audit script | `LootPoolResolverTest.java`, `SharedLootrConfigTest.java`, `scripts/build-matrix.py` |

## Pool Resolution Invariants

1. `sharingMode = "GLOBAL"` → always `00000000-0000-0000-0000-000000000001`.
2. `sharingMode = "TEAMS"`:
   - Active FTB party/server team → FTB Team UUID (takes priority over vanilla team; personal FTB player teams are treated as solo).
   - Vanilla scoreboard `PlayerTeam` → `UUID.nameUUIDFromBytes("shared_lootr:vanilla_team:" + teamName)`.
   - Solo player (no team):
     - `soloPlayersSharePool = false` (default) → `player.getUUID()` (private per-player pool).
     - `soloPlayersSharePool = true` → deterministic `SOLO_POOL_UUID`.
3. Client-side resolution mirrors server-side resolution via FTB Teams client manager and synced vanilla scoreboard teams, keeping unopened/opened Lootr container visuals aligned in both modes.
