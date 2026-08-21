# Shared Lootr

Shared Lootr keeps Lootr's gold-trimmed world-generation containers while replacing its per-player semantics with one server-global state per container.

## Behavior

- Lootr still converts and renders its gold-trimmed chest, barrel, shulker, decorated-pot, and brushable container variants.
- Each converted container generates exactly one loot inventory from the first legitimate opener's loot context.
- Every later player opens that same persisted inventory; no player UUID owns a second loot roll.
- The server persists one `hasBeenOpened` marker and broadcasts its first transition to every tracking client.
- Client rendering answers `hasClientOpened(...)` only from that global marker and ignores the queried player UUID.
- Existing saves with player-keyed inventories migrate one existing inventory to the global owner on first access.
- Physical lid animation and concurrent viewers remain Lootr-managed.

## Compatibility target

- Minecraft `1.21.1`
- NeoForge `21.1.248`
- Lootr `1.21.1-1.11.37.122`
- Java `21`

## Build

```bash
./gradlew :neoforge:build
```

The NeoForge artifact is written to `neoforge/build/libs/`.
