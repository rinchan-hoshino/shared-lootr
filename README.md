# Shared Lootr

Shared Lootr is a small NeoForge addon for Lootr.

## Behavior

- All players see and modify the same stored contents for each Lootr container.
- Preserves Lootr's container conversion, loot generation, menu opening, lid behavior, and opener tracking.
- The first open broadcasts Lootr's persistent `LootrHasBeenOpened` marker to every tracking client, so unopened visual feedback is shared instead of per-player.

## Supported target

- Minecraft 1.21.1
- NeoForge 21.1.x
- Lootr `lootr-neoforge-1.21.1-1.11.37.120.jar`

## Build

```bash
./gradlew test :neoforge:build
```
