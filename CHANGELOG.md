# Changelog

## 1.2.0

### Changed

- Make the shared inventory's existence the single server-side source of truth for generated loot, the opened appearance, and Jade's generated-loot state.
- Keep Lootr's native block event and NBT only as the client-side projection of that shared state.
- Remove the extra reopen broadcast fallback.

### Breaking

- Stop migrating or selecting legacy per-player Lootr inventories. Existing worlds must reset Lootr's saved data or continue using 1.1.x.

## 1.1.1

### Fixed

- Align packaged mod metadata with the repository's GPL-3.0-or-later license.

## 1.1.0

### Changed

- Delegate container conversion, block entities, menus, rendering, models, textures, particles, packets and persistence to Lootr's native implementation.
- Share one canonical Lootr inventory globally while retaining existing per-player copies and selecting populated legacy contents on first shared access.
- Share and persist Lootr's opened visual state globally.

### Fixed

- Target Lootr 1.11.37's current inventory and open-handler descriptors.
- Broadcast the opened state when a shared container is reopened.
- Keep Jade from reporting generated shared loot as ungenerated after the Lootr container has been opened.
