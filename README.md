# CoordHUD

**Dev : Dr. Newman**

A small, good-looking HUD for Minecraft Java Edition (Fabric) that shows your coordinates, the direction you are facing and the world day, with a one-click copy button. It runs entirely on the client, so there is nothing to install on a server.

```
┌──────────────────────────────────┐
│ ⌖ X: 248   Y: 72   Z: -136   ⧉  │
│ ◎ Facing: NE                45°  │
│ ☀ Day: 24                        │
│ ▯ Nether: 31, -17                │
│ ☠ Death: 48 blocks NW            │
└──────────────────────────────────┘
```
(Layout sketch only; the real HUD uses small pixel icons.)

## Features

- Live **X / Y / Z** block coordinates, correct for negative values (block -1 starts at -1.0, not 0).
- **8-way compass** (N, NE, E, SE, S, SW, W, NW) plus **heading in degrees**.
- **World day counter** (Day 1 by default, or Day 0 like `/time query day`).
- **Copy button** and **keybind** that put `248, 72, -136` on your clipboard, with a "Copied!" confirmation.
- **Nether ⇄ Overworld** coordinate row, shown automatically in those dimensions.
- **Last death** row with distance and direction, for example `48 blocks NW, 12 up`.
- Three looks: **Glass** (rounded, translucent), **Minecraft** (vanilla tooltip style) and **Flat**.
- **In-game settings screen** with live preview: size, opacity, corners, position, accent colour and every row.
- No telemetry, no network access, no changes to the world or to gameplay.

## Requirements

| | Version |
|---|---|
| Minecraft Java Edition | 26.3 |
| Fabric Loader | 0.20.1 or newer |
| Fabric API | 0.165.0+26.3 or newer |
| Java | 25 (already required by Minecraft 26.3) |

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for Minecraft 26.3.
2. Put [Fabric API](https://modrinth.com/mod/fabric-api) and this mod's `.jar` into your `mods` folder.
3. Start the game with the Fabric profile.

## Using it

| Action | How |
|---|---|
| Open settings | Press **H** (rebind in *Options → Controls → Key Binds*) |
| Copy coordinates while playing | Press **K** (rebindable) |
| Copy coordinates with the mouse | Open chat (`T`) and click the small copy icon |

Changes made in the settings screen are visible immediately and saved when you close it.

## Configuration file

`config/coordhud.properties` is created on first launch. You can also edit it by hand; changes are picked up within a couple of seconds, no restart needed.

| Key | Values | Default |
|---|---|---|
| `enabled` | true / false | true |
| `showCoordinates`, `showDirection`, `showHeading`, `showDay`, `showCopyIcon` | true / false | true |
| `hideWhenScreenOpen` | hide while a non-chat screen is open | false |
| `hideWithDebugScreen` | hide while F3 is open | true |
| `anchor` | TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT | TOP_LEFT |
| `offsetX`, `offsetY` | 0–1000 GUI pixels from the anchored corner | 4, 4 |
| `scale` | HUD size in percent, 50–300 | 100 |
| `backgroundOpacity` | 0–100 | 60 |
| `style` | GLASS, MINECRAFT, CLASSIC | GLASS |
| `cornerRadius` | 0–8 (GLASS only) | 4 |
| `accentColor` | RRGGBB hex | 7FC8E8 |
| `showIcons`, `coloredAxes` | row icons, coloured X/Y/Z | true |
| `coordinateFormat` | BLOCK, DECIMAL | BLOCK |
| `directionStyle` | CARDINAL (N/E/S/W), EIGHT_WAY | EIGHT_WAY |
| `firstDay` | 0 or 1 | 1 |
| `showDimensionConversion` | Nether/Overworld row | true |
| `showLastDeath` | last death row | true |
| `deathStyle` | DISTANCE, COORDS, BOTH | DISTANCE |

Invalid values fall back to their default one by one, so the rest of your settings are kept. An unreadable file is renamed to `*.broken` and defaults are restored.

## Compatibility

- Built and run on **Minecraft 26.3**, Fabric Loader 0.20.1, Fabric API 0.165.0+26.3.
- The mod uses Fabric API's normal HUD hook and ordinary GUI drawing. It has no mixins and does not touch rendering internals, so it is designed to coexist with Sodium and other client mods.
- **Sodium:** not yet tested. Reports are welcome.
- It never sends packets and works on any server, including servers without the mod.

## Building from source

You need **JDK 25**.

```
git clone https://github.com/drnewman7/coordhud.git
cd coordhud
./gradlew build          # Windows: gradlew.bat build
```

The release jar is `build/libs/coordhud-<version>.jar` (the `-sources` jar is for developers). Other tasks: `./gradlew runClient` starts a development client, `./gradlew test` runs the unit tests.

Project layout:

- `core/` is plain Java with no Minecraft classes: coordinate and compass maths, day calculation, layout, config, the panel shapes. It has unit tests.
- `client/` is the thin Minecraft layer: HUD element, settings screen, keybinds, clipboard.

## Known limitations

- The Glass theme is a translucent panel with highlights. It does not blur or bend the world behind it.
- The copy button is clickable while chat is open; during normal play use the keybind.
- The death distance is horizontal; in another dimension the row shows coordinates and the dimension name.
- Minecraft 26.x changes its internals often, so a new Minecraft release may need a small update.

## Bugs and contributions

Please open an issue with your Minecraft, Fabric Loader, Fabric API and mod versions, your other mods, and `logs/latest.log` or the crash report. Pull requests are welcome; see [CONTRIBUTING.md](CONTRIBUTING.md).

## License and credit

Released under the [MIT License](LICENSE). You may use, modify and redistribute it, but the copyright notice and license text must stay with every copy or substantial portion. Please keep the credit to **Dr. Newman** when you fork or redistribute.

*Not an official Minecraft product. Not approved by or associated with Mojang or Microsoft.*

Dev : Dr. Newman
