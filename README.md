<h1 align="center">Better Player Locator Bar</h1>

<p align="center">
    <a href="https://github.com/bichal/BetterPlayerLocatorBar/releases/tag/v1.1.1"><img src="https://img.shields.io/badge/Version%201%2E1%2E1-2F6DB8?logo=openjdk&logoColor=fff&style=for-the-badge" alt="Version"/></a>
    <a href="LICENSE"><img src="https://img.shields.io/badge/CC%E2%80%93BY%E2%80%93NC%E2%80%93SA%E2%80%934%2E0-1A1A1A?logo=creativecommons&logoColor=fff&style=for-the-badge" alt="License"/></a>
    <a href="https://modrinth.com/mod/bplb"><img src="https://img.shields.io/badge/Modrinth-00AF5C?logo=modrinth&logoColor=fff&style=for-the-badge" alt="Modrinth"/></a>
    <a href="https://www.curseforge.com/minecraft/mc-mods/better-player-locator-bar"><img src="https://img.shields.io/badge/CurseForge-F16436?logo=curseforge&logoColor=fff&style=for-the-badge" alt="CurseForge"/></a>
    <a href="https://github.com/bichal/BetterPlayerLocatorBar/issues"><img src="https://img.shields.io/badge/Have%20An%20Issue%3F-BE3939?logo=theconversation&logoColor=fff&style=for-the-badge" alt="Issues"/></a>
</p>

Better Player Locator Bar is a Fabric and NeoForge mod. It shows players in a HUD bar above the XP bar. You can customize icons, borders, fades, and more.

## 🎯 Features

- HUD bar with icons representing nearby players and death markers
- Customizable icon style: `default`, `minimal`, `mojang`, `bowtie (shift + click)`
- Icon border styles: `rounded` or `squared`
- Option to show player names and heads
- Vertical arrows when height difference is significant by player camera or position
- Distance-based fading (configurable start, end, alpha min/max)
- Smooth interpolation using easing
- Asset detection: scans included textures in `sprites/hud/`
- Client/server sync via handshake and position payloads; fallback to local mode
- Config UI (`F8` in game or use `ModMenu`), preview, per-player overrides (color, icon, border...)
- Death markers persistence and cleanup

## 📸 Gallery
<p align="center">
    <blockquote>A preview of how you would see other players with the mod</blockquote>
    <img src="https://github.com/user-attachments/assets/5d6d6bc1-5097-4b5c-b510-6e7ffeea6be9" alt="HUD" width="100%"/>
    <blockquote>A preview of a player on the HUD</blockquote>
    <img src="./media/OnePlayerBarPreview.gif" alt="OnePlayerBarPreview" width="100%"/>
    <blockquote>A preview of two players on the HUD</blockquote>
    <img src="./media/TwoPlayerBarPreview.gif" alt="TwoPlayerBarPreview" width="100%"/>
    <blockquote>This would be a preview of the settings screen so you can customize it as much as you want</blockquote>
    <img src="./media/ConfigScreenPreview.gif" alt="ConfigScreenPreview" width="100%"/>
    <blockquote>Now you can customize your friends icons</blockquote>
    <img src="./media/PlayerCustomPreview.gif" alt="PlayerCustomPreview" width="100%"/>
</p>

## Requirements

| Minecraft | Fabric | NeoForge |
|---|---|---|
| 1.21 - 1.21.1 | Loader ≥ 0.16, Fabric API | NeoForge 21.1+ |
| 1.21.2 - 1.21.3 | Loader ≥ 0.16, Fabric API | NeoForge 21.3+ |
| 1.21.4 | Loader ≥ 0.16, Fabric API | NeoForge 21.4+ |

Java 21 on every version. [Mod Menu](https://modrinth.com/mod/modmenu) is optional on Fabric; on NeoForge the settings are in the mod list.
How it works:

- **Server has the mod (server mode):** every player's position comes from the server, however far away, for as long as you stay connected.
- **Server without the mod (local mode):** only players your game can see (inside render distance) are shown.

The settings screen shows which mode you are in.

## Installation

1. Install your loader (and Fabric API on Fabric)
2. Put `better-player-locator-bar-<version>+<minecraft>-<loader>.jar` into your `mods/` folder
3. Run Minecraft

## Building

One Gradle project per Minecraft version and loader, built from the same sources with [Stonecutter](https://stonecutter.kikugie.dev/):

```
./gradlew :1.21.1-fabric:build        # one version, one loader
./gradlew build                       # everything
./gradlew :1.21.1-fabric:runClient    # dev client (also :1.21.1-neoforge:runClient)
```

The shared code uses Mojang names and no loader API; each loader only has its entrypoints in
`net.bichal.bplb.platform.<loader>`. Add a version in `settings.gradle.kts` and `versions/<version>-<loader>/gradle.properties`.
## License

The contents of this repository and mod, are licensed under a <a href="LICENSE">Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License</a>. ([Canonical URL](https://creativecommons.org/licenses/by-nc-sa/4.0/))