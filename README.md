# Shared Lootr

Shared Lootr keeps generated vanilla loot chests globally shared while reusing Lootr's own chest materials for their visual state.

## Behavior

- Generated vanilla chests retain vanilla one-inventory loot generation, so all players see the same remaining contents.
- The chest stores one global opened marker and broadcasts its first transition to tracking clients.
- Unopened and opened chests use Lootr's own current or old chest materials according to Lootr's texture setting.
- Trapped loot chests use Lootr's matching trapped variants.
- No custom icon, billboard, overlay, particle, or texture is rendered.
- Ordinary player-placed chests remain vanilla.

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
