# Shared Lootr

<p align="center"><img src="docs/shared-lootr-icon.png" alt="An open Lootr chest shared by two Steves and Alex" width="320" /></p>

Play Lootr, but share the loot.

<!-- PROJECT_PAGE_START -->

I like the improvements Lootr brings to loot chests:

- Distinct appearances for loot chests and ordinary chests
- Visual markers showing whether a loot chest has been opened
- Statistics tracking how many loot containers have been opened
- Unique trophies, and more

The one feature I don't like is each player having their own loot container. To me, sharing or competing for loot is a big part of the fun of adventuring in Minecraft. That's why I made this mod for my own private modpack.

## Requirements

Install the following mods as dependencies:

- Lootr
- RinLib

## How it works

When Lootr reads or creates a container's inventory, Shared Lootr replaces the player's UUID used as the inventory owner with a fixed UUID: `00000000-0000-0000-0000-000000000001` (`new UUID(0L, 1L)` in the code). This makes every player access the same inventory within that container.

<!-- PROJECT_PAGE_END -->
