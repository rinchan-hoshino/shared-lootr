<!-- PROJECT_PAGE_START -->
<p align="center">
  <img src="docs/artwork/icon.png" alt="Shared Lootr icon" width="128" height="128">
</p>

<h1 align="center">Shared Lootr</h1>

**Shared Lootr** keeps everything players love about [Lootr](https://modrinth.com/mod/lootr) — anti-griefing break protection, unopened golden highlight visuals, vanilla chest advancements, and decay/refresh mechanics — while preventing multiplayer servers from duplicating dungeon loot by player count. Instead of generating an extra copy of every chest for every player who walks by, each Lootr container shares one inventory globally across the server (`GLOBAL`, default) or per team (`TEAMS`). Out of the box, it stays 100% save-compatible with Shared Lootr 1.x while giving co-op groups native FTB Teams and vanilla scoreboard team sharing.

---

### Sharing Modes (`config/shared_lootr.toml`)

- **`GLOBAL` (Default)** — Every player resolves to the fixed shared pool UUID `00000000-0000-0000-0000-000000000001`. One player looting a chest updates the inventory and opened visual state for everyone, preserving full 1.x compatibility.
- **`TEAMS`** — Teammates share a single loot pool while separate teams keep independent pools:
  1. If **FTB Teams** is installed and the player belongs to a party or server team, Shared Lootr uses that FTB Team UUID (zero hard dependency).
  2. Otherwise, if the player belongs to a vanilla scoreboard `PlayerTeam`, Shared Lootr deterministically derives `UUID.nameUUIDFromBytes("shared_lootr:vanilla_team:" + teamName)`.
  3. Players not in any team keep their own private pool by default (`soloPlayersSharePool = false`), or can opt into a shared solo pool (`soloPlayersSharePool = true`).

Configure in-game via **Mod Menu** (Fabric) or the native **Mods → Config** button (Forge / NeoForge), or by editing `config/shared_lootr.toml`.

### Supported Matrix (8 Targets)

| Minecraft | Fabric | Forge | NeoForge | Integration |
| :--- | :---: | :---: | :---: | :--- |
| **26.2** | ✅ | — | ✅ | Official Lootr `ITeamResolver` (`1.24.41+`) |
| **26.1.2** | ✅ | — | ✅ | Official Lootr `ITeamResolver` (`1.23.38+`) |
| **1.21.1** | ✅ | — | ✅ | Official Lootr `ITeamResolver` (`1.11.38+`) |
| **1.20.1** | ✅ | ✅ | — | Minimal UUID projection Mixin (`0.7.35.x`) |

### Credits

Special thanks to **noobanidus** (author of [Lootr](https://github.com/LootrMinecraft/Lootr)) for designing and shipping the official `ITeamResolver` ServiceLoader API on Minecraft `1.21.1`, `26.1.2`, and `26.2`, making Shared Lootr 2.0 dramatically lighter and future-proof.

<!-- PROJECT_PAGE_END -->

---

### Building from Source

```bash
python3 scripts/build-matrix.py
```

All 8 target JARs are compiled, unit-tested, audited, and copied to `build/dist/`.
