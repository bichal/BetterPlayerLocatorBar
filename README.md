# Better Player Locator Bar

[![Version](https://badgen.net/badge/version/1.1.0/blue)](https://github.com/bichal/BetterPlayerLocatorBar/releases/tag/v1.1.0)  
[![License](https://badgen.net/badge/license/CC-BY-NC-4.0/grey)](LICENSE)  
[![Modrinth](https://badgen.net/badge/Platform/Modrinth/green)](https://modrinth.com/mod/bplb)  
[![CurseForge](https://badgen.net/badge/Platform/CurseForge/orange)](https://www.curseforge.com/minecraft/mc-mods/better-player-locator-bar)  
[![Issues](https://badgen.net/badge/Issues/GitHub/red)](https://github.com/bichal/BetterPlayerLocatorBar/issues)

Better Player Locator Bar is a Fabric mod for Minecraft 1.21–1.21.1. It shows nearby players in a HUD bar above the XP bar. You can customize icons, borders, fades, and more.

---

## 🎯 Features

- HUD bar with icons representing nearby players and death markers
- Customizable icon style: `default`, `minimal`, `mojang`, `bowtie`
- Icon border styles: `rounded` or `squared`
- Option to show player heads
- Vertical arrows when height difference is significant
- Distance-based fading (configurable start, end, alpha min/max)
- Smooth interpolation using easing (`easeInOutQuad`)
- Asset detection: scans included textures in `sprites/hud/`
- Client/server sync via handshake and position payloads; fallback to local mode
- Config UI (via `F8`), preview, per-player overrides (color, icon, border, head)
- Death markers persistence and cleanup

---

## 📸 Screenshots

![HUD](https://github.com/user-attachments/assets/5d6d6bc1-5097-4b5c-b510-6e7ffeea6be9)  
![HUD with names](https://github.com/user-attachments/assets/f590d482-1618-4b24-aa97-cc2086653c3d)

---

## Requirements

- Minecraft 1.21 – 1.21.1
- Fabric Loader ≥ 0.14.2
- Java 21
- Fabric API

---

## Installation

1. Install Fabric Loader and Fabric API
2. Place `bplb-1.1.0.jar` into your `mods/` folder
3. Run Minecraft

---

## Usage

- Hold `Tab` to show names (optional)
- Press `F8` to open the mod configuration screen
- HUD adjusts automatically if icons are present
- Mod auto-switches between synchronized (server) and local mode

---

## Configuration options (important)

- `modEnabled` — enable/disable mod
- `maxVisibleIcons` — how many icons to render
- `position_update_rate_ticks` — update frequency
- `lerp_speed` — interpolation speed
- `icon_size`, `head_size` — sizes
- `dot_type`, `arrow_type`, `icon_border_style`, `icon_border_type` — visuals
- `fade_start_distance`, `fade_end_distance`, `fade_alpha_min`, `fade_alpha_max`
- `inherit_border_color` — border inherits darker tint
- `death_marker_type`, `death_marker_border_type`, `death_marker_inherit_border_color`

These are exposed via the `Config` class and in the UI (`ConfigScreen`).

---

## Architecture summary

- `Main` / `BetterPlayerLocatorBar`
- `client/` (HUD, keybinds, asset scanning, rendering)
- `network/` (payload definitions)
- `server/` (server broadcasting)
- `config/` (config class + UI)
- `resources/` (fabric.mod.json, mixins, assets, languages)

The `sprites/hud/` folder includes all icon, border, arrow, tag, and death marker assets (see earlier file listing).

---

## Network API overview

- `HandshakePayload` — indicates server mod presence and OP rights
- `PositionUpdatePayload` — sends lists of player positions, new and disconnected players
- Client receives and updates caches; if server drops updates, client switches to local mode

---

## Known Issues & Limitations

- If `usercache.json` is missing, some name resolution may fail
- Fallback to default skin (“steve”) if texture override fails
- Some combinations of armor/helmet may break head rendering logic
- Dependence on Fabric API versions; use latest

---

## License

CC-BY-NC-4.0

---

## Links

- Repository: https://github.com/bichal/BetterPlayerLocatorBar
- Issues: https://github.com/bichal/BetterPlayerLocatorBar/issues
- Modrinth: https://modrinth.com/mod/bplb
- CurseForge: https://www.curseforge.com/minecraft/mc-mods/better-player-locator-bar  
