<h1 align="center">Better Player Locator Bar</h1>

<p align="center">
<img src="https://img.shields.io/badge/Minecraft-1.21_--_26.3-3c8527?style=for-the-badge" alt="Minecraft 1.21 to 26.3">
<img src="https://img.shields.io/badge/Fabric_and_NeoForge-Client_and_Server-e8710a?style=for-the-badge" alt="Fabric and NeoForge, client and server">
<img src="https://img.shields.io/badge/Type-HUD_mod-2f6db8?style=for-the-badge" alt="HUD mod">
</p>

> **Better Player Locator Bar shows where your friends are in a HUD bar above the XP bar.** Pick the icons, borders, fades and names you like, and set a different look for each player.

<!-- publish:only modrinth -->
<p align="center">
<a href="https://modrinth.com/mod/bplb"><img src="https://img.shields.io/badge/Download_on-Modrinth-1bd96a?style=for-the-badge&logo=modrinth&logoColor=white" alt="Download on Modrinth"></a>
</p>
<!-- /publish:only -->

<!-- publish:only curseforge -->
<p align="center">
<a href="https://www.curseforge.com/minecraft/mc-mods/better-player-locator-bar"><img src="https://img.shields.io/badge/Download_on-CurseForge-f16436?style=for-the-badge&logo=curseforge&logoColor=white" alt="Download on CurseForge"></a>
</p>
<!-- /publish:only -->

<!-- publish:only github -->
<p align="center">
<a href="https://github.com/bichal/BetterPlayerLocatorBar/releases"><img src="https://img.shields.io/badge/Download_on-GitHub-24292f?style=for-the-badge&logo=github&logoColor=white" alt="Download on GitHub"></a>
</p>
<!-- /publish:only -->

<p align="center">
<img src="./docs/media/TwoPlayerBarPreview.gif" alt="Two players on the HUD bar" width="100%"/>
</p>

## What it does

- Shows nearby players and death markers as icons on a HUD bar.
- Lets you choose the icon style (`default`, `minimal`, `mojang` or `bowtie`), the border (`rounded` or `squared`), and whether names and heads show.
- Adds vertical arrows when a player is much higher or lower than you.
- Fades players with distance, and moves the icons smoothly.
- Has a config screen (`F8` in game, or through Mod Menu) with a preview and per-player overrides for colour, icon and border.
- Keeps death markers between sessions and cleans them up later.

It works with the server too: when the server has the mod, every player's position comes from the server, however far away they are.

## Requirements

- Minecraft 1.21 to 1.21.11 and 26.1 to 26.3.
- Fabric: Loader 0.16 or newer (0.19 or newer for 26.x) and Fabric API. [Mod Menu](https://modrinth.com/mod/modmenu) is optional.
- NeoForge: 21.1 or newer for the matching Minecraft version. NeoForge has no 1.21.6, 1.21.7 or 1.21.9 build.
- Java 21 up to 1.21.11, Java 25 from 26.1.

**How it works on each version:**

- **1.21 to 1.21.5** have no locator bar of their own, so the mod adds one. On a server with the mod, positions come from the server. Otherwise only players inside your render distance are shown, and the settings screen says which mode you are in.
- **1.21.6 and newer** already have Mojang's locator bar. The mod uses the vanilla waypoints and only draws them with your icons, colours, fading and name plates, so it adds no second bar and no extra network traffic. The server does not need the mod. If it has it, the server does less work per player movement; `-Dbplb.optimizeWaypoints=false` turns that off.

## Installation

1. Install your loader (and Fabric API on Fabric).
2. Put `better-player-locator-bar-<version>+<minecraft>-<loader>.jar` into your `mods/` folder.
3. Run Minecraft.

## Learn more

<p align="center">
<a href="https://github.com/bichal/BetterPlayerLocatorBar/issues"><img src="https://img.shields.io/badge/Found_a_bug-Report_it-d73a4a?style=for-the-badge&logo=github&logoColor=white" alt="Report a bug"></a>
<a href="https://github.com/bichal/BetterPlayerLocatorBar/tree/main/docs/changelogs"><img src="https://img.shields.io/badge/Whats_new-Changelog-2f81f7?style=for-the-badge&logo=github&logoColor=white" alt="Changelog"></a>
</p>

<!-- publish:off -->

## Building

Clone [BichalBuildLogic](https://github.com/bichal/BichalBuildLogic) next to this repository as `bichal-build-logic`
(Gradle 9.8 on Java 25; the build downloads the Java 21 toolchain it needs for 1.21.x):

```bash
git clone https://github.com/bichal/BichalBuildLogic.git bichal-build-logic
cd BetterPlayerLocatorBar
./gradlew buildAndCollect                      # every version and loader, jars in build/libs/<version>/
./gradlew :1.21.1:build :1.21.1-fabric:build   # one version: NeoForge is the plain name, Fabric ends in -fabric
./gradlew :1.21.1-fabric:runClient             # dev client (-Pbplb.quickplay=<host> joins a server)
```

The versions, their ranges and the renamed-class replacements are in `mod.stonecutter.properties.toml`.
Loader code is only in `net.bichal.bplb.platform.<loader>`; the rest uses Mojang names and no loader API.

<!-- /publish:off -->

<!-- bichal:license -->
## License

| Content | License |
|---|---|
| Source code | [CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/), see [`LICENSE-CC`](./LICENSE-CC) |
| Artwork, branding and documentation | [CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/), see [`LICENSE-CC`](./LICENSE-CC) |

**Source code:** credit Bichal, no commercial use without permission, and share derivatives under the same license.

**Artwork:** credit Bichal, no commercial use without permission, and share derivatives under the same license.

See [`LICENSE`](./LICENSE) for the full summary.
<!-- /bichal:license -->

<!-- publish:on -->

---

<!-- bichal:footer -->
Code: **CC BY-NC-SA 4.0** · Art: **CC BY-NC-SA 4.0** · Made by **Bichal**
<!-- /bichal:footer -->
