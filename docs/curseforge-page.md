# Shared Lootr

**One Lootr chest. One shared inventory for the whole team.**

Lootr normally gives each player a separate inventory when they open the same container. Shared Lootr changes that rule: every player now interacts with the same server-owned inventory for that Lootr container.

![How Shared Lootr changes a Lootr container](https://raw.githubusercontent.com/rinchan-hoshino/shared-lootr/main/docs/assets/shared-lootr-overview.png)

## What changes in game

- Everyone sees and takes items from the same container inventory.
- When one player removes an item, it is gone for everyone.
- The container's opened appearance and information overlays follow that shared state.
- Existing legacy per-player keys are left untouched but ignored; inventories are not merged or silently migrated.

This is intended for cooperative servers and modpacks that want Lootr containers to behave as team-shared loot instead of one reward per player.

## Requirements

Install the matching versions of:

- **Lootr**
- **RinLib**
- **Shared Lootr** for your Minecraft version and loader

Do not mix files built for different Minecraft versions or loaders. Use the dependency information attached to each download.

## Published compatibility

The current release covers **56 published Minecraft/loader combinations**, from Minecraft 1.12.2 Forge through the current Fabric and NeoForge lines. The Files tab is the source of truth for the exact file matching your instance.

Supported families include:

- Forge: 1.12.2, 1.16.5, 1.17.1, 1.18–1.20.1
- Fabric: 1.18 through current supported releases, including the listed snapshot build
- NeoForge: 1.20.4 through current supported releases

## Known upstream gaps

No Shared Lootr file is published for these combinations because the corresponding locked Lootr build fails before Shared Lootr can load:

- Minecraft 1.16.4 Forge
- Minecraft 1.19.3 Forge
- Minecraft 1.19.4 Forge
- Minecraft 1.20.2 Forge

These gaps are not presented as supported versions.

## Reporting a problem

When reporting an issue, include:

- Minecraft version
- Loader and loader version
- Shared Lootr, Lootr, and RinLib versions
- Whether the issue occurs with a newly placed Lootr container
- A minimal reproduction without unrelated mods when possible

Source and issue tracker: https://github.com/rinchan-hoshino/shared-lootr
