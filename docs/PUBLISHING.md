# Publishing guide

Dev : Dr. Newman

## Before the first release

- [ ] `./gradlew build` passes (tests included) and `build/libs/coordhud-1.0.0.jar` exists.
- [ ] The jar was tested in a normal Fabric install (not only `runClient`), with and without Sodium.
- [ ] Open the jar as a zip: `fabric.mod.json` shows version 1.0.0, `depends` lists only fabricloader, minecraft, java and fabric-api, the icon is present, and no Fabric or Minecraft classes are bundled.
- [ ] Update the Compatibility section of the README with what you actually tested.
- [ ] Set the date in `CHANGELOG.md`.
- [ ] Take real in-game screenshots (default HUD, each theme, settings screen, copy confirmation, a negative-coordinate view).

## GitHub

1. Create an empty repository named `coordhud` (no README, no license; they are in the project).
2. From the project folder: `git init -b main`, `git add .`, `git commit -m "Initial release"`, `git remote add origin <url>`, `git push -u origin main`.
3. Tag and release: `git tag v1.0.0`, `git push origin v1.0.0`, then *Releases → Draft a new release*, pick the tag, paste the notes below, attach `coordhud-1.0.0.jar`.
4. Add topics: `minecraft`, `fabric`, `fabric-mod`, `hud`, `coordinates`.

## Modrinth

Create a project and fill in:

- **Name:** CoordHUD, **slug:** `newman-coordinates-hud`
- **Summary:** A compact, good-looking HUD with coordinates, an 8-way compass, the world day, death-point distance and a one-click copy button.
- **Environment:** client required, server unsupported. **Loader:** Fabric. **Game version:** 26.3.
- **License:** MIT. **Dependency:** Fabric API (required).
- **Links:** source and issues on GitHub. **Icon:** `docs/icon-512.png`.
- **Description:** use the README feature list and controls.
- Check Modrinth's current content rules before submitting.

## CurseForge

Same name, summary, description, icon, license and dependency (Fabric API). Upload the jar for Minecraft 26.3 / Fabric / Java 25. Check CurseForge's current project requirements before submitting.

## Release notes (1.0.0)

First public release. Coordinates, 8-way compass with degrees, world day, Nether/Overworld row, death-point distance and direction, copy button and keybind, Glass / Minecraft / Flat themes, and an in-game settings screen (H).
