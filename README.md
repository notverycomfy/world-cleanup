<div align="center">

<img src="world-cleanup-icon.png" alt="World Cleanup grass block logo" width="160">

# World Cleanup

### Keep dropped items fair without keeping them forever.

Intelligent, configurable dropped-item cleanup that reduces lag without punishing players.

[Download on Modrinth](https://modrinth.com/mod/world-cleanup) · [Download on CurseForge](https://www.curseforge.com/minecraft/mc-mods/world-cleanup) · [Report an issue](https://github.com/notverycomfy/world-cleanup/issues)

![Minecraft 26.1.2 and 26.2](https://img.shields.io/badge/Minecraft-26.1.2%20%7C%2026.2-62B47A)
![NeoForge and Fabric](https://img.shields.io/badge/Loaders-NeoForge%20%7C%20Fabric-E46A2C)
![All Rights Reserved](https://img.shields.io/badge/License-All%20Rights%20Reserved-9B59B6)

</div>

> Official JAR downloads are distributed through Modrinth and CurseForge. GitHub contains source code only.

## Highlights

- Valuable items stay longer while common blocks disappear sooner
- Player-thrown items and mob drops use separate timers
- Items near a recent player death receive temporary protection
- Dense farms can clean eligible drops more aggressively
- Timers, detection settings, and item categories are configurable in game

Configuration is saved between launches. Server owners can tune the rules for their worlds.

## Versions

The repository follows the same branch layout as Crop Helper:

| Minecraft | Loader | Branch |
| --- | --- | --- |
| 26.1.2 | NeoForge | `main` |
| 26.1.2 | Fabric | [`codex/fabric-26.1.2`](https://github.com/notverycomfy/world-cleanup/tree/codex/fabric-26.1.2) |
| 26.2 | Fabric | [`codex/fabric-26.2`](https://github.com/notverycomfy/world-cleanup/tree/codex/fabric-26.2) |
| 26.2 | NeoForge | [`codex/neoforge-26.2`](https://github.com/notverycomfy/world-cleanup/tree/codex/neoforge-26.2) |

Fabric requires Fabric API. Mod Menu is recommended for its in-game configuration button.

## Build

Use `./gradlew build` from the branch matching the target loader and Minecraft version. Generated development JARs appear in `build/libs` and are not distributed through GitHub.

## License

Copyright © 2026 notverycomfy. All Rights Reserved. See [`LICENSE`](LICENSE).
