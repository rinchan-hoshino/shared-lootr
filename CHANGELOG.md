# Changelog

## 1.1.0

### Changed

- Delegate container conversion, block entities, menus, rendering, models, textures, particles, packets and persistence to Lootr's native implementation.
- Share one canonical Lootr inventory globally while retaining existing per-player copies and selecting populated legacy contents on first shared access.
- Share and persist Lootr's opened visual state globally.

### Fixed

- Target Lootr 1.11.37's current inventory and open-handler descriptors.
- Broadcast the opened state when a shared container is reopened.
- Keep Jade from reporting generated shared loot as ungenerated after the Lootr container has been opened.
