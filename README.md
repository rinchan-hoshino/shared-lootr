# Shared Lootr

Shared Lootr is a small NeoForge addon for Lootr.

## Behavior

- Lootr containers still use Lootr's generated loot, lid handling, opened state, and visual marker updates.
- All players open the same stored loot inventory for each Lootr container instead of receiving separate per-player inventories.
- Once a Lootr container has been opened, clients treat it as opened for the visual unopened-particle marker instead of showing unopened feedback per player.

## Supported target

- Minecraft 1.21.1
- NeoForge 21.1.x
- Lootr 1.21.1-1.11.37.120 or compatible later 1.21.1 builds

## Build

```bash
./gradlew :neoforge:build
```
