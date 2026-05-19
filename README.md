# Shared Lootr

Shared Lootr is a small NeoForge addon for Lootr.

## Behavior

- Keeps the official Lootr jar unchanged.
- All players see and modify the same stored contents for each Lootr container.
- Lootr still owns container conversion, loot generation, menu opening, lid behavior, opener tracking, and update packets.
- If `LootrHasBeenOpened` is true, the client treats the container as opened for Lootr's unopened visual feedback.

## Supported target

- Minecraft 1.21.1
- NeoForge 21.1.x
- Lootr `lootr-neoforge-1.21.1-1.11.37.120.jar`

## Build

```bash
./gradlew :neoforge:build
```
