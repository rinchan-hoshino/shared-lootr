# Changelog

## 2.0.0

### Highlights
- **Dual Sharing Modes (`GLOBAL` + `TEAMS`)**: Added zero-dependency `SimpleToml` configuration at `config/shared_lootr.toml`. Defaults to `sharingMode = "GLOBAL"` (fixed UUID `00000000-0000-0000-0000-000000000001`, 100% compatible with 1.x saves), and adds `sharingMode = "TEAMS"` with optional reflective FTB Teams support, deterministic vanilla scoreboard team UUIDs, and configurable `soloPlayersSharePool = false`.
- **Official Lootr `ITeamResolver` Adoption**: Migrated `1.21.1`, `26.1.2`, and `26.2` completely onto Lootr's native `ITeamResolver` `ServiceLoader` SPI paired with a single thin `LootrTeamConfigMixin` bridge, removing ~50 legacy invasive container mixins and all legacy `1.12.2`–`1.20.4` / `rinlib` adapters.
- **8-Target Golden Matrix**: Converged builds onto `1.20.1` (Fabric/Forge), `1.21.1` (Fabric/NeoForge), `26.1.2` (Fabric/NeoForge), and `26.2` (Fabric/NeoForge) under unified package `dev.rinchan.sharedlootr`.
- **In-Game Config UI & 4-Language Localization**: Added native `SharedLootrConfigScreen` with Mod Menu (Fabric compile-only stub) and Forge/NeoForge config screen registration, plus full `en_us`, `zh_cn`, `zh_tw`, and `ja_jp` translations.
- **New `64x64` Pixel-Art Badge Icon**: Replaced the legacy icon with a crisp `64x64` (upscaled 8x to `512x512`) pixel-art golden Lootr chest and interlocking rings emblem.

## 1.3.3
- Supported legacy and flagged Lootr provider opening APIs.
- Embedded `SharedOwnerState` and began migrating to Lootr `ITeamResolver`.
