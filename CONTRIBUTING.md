# Contributing

Thanks for helping. Dev : Dr. Newman

## Ground rules

- Keep it small. This mod does one job; new features should be optional and off the hot path.
- Pure logic goes in `core/` (no Minecraft imports) and needs unit tests.
- Minecraft-specific code stays in `client/`. Prefer official Fabric API hooks over mixins and reflection.
- No telemetry, network requests or bundled third-party code or assets.

## Setup

1. Install JDK 25.
2. `./gradlew build` to compile and run the tests, `./gradlew runClient` to try it in a development client.

## Pull requests

- One change per pull request, with a short description of what and why.
- Run `./gradlew build` before submitting.
- For visual changes, include a screenshot.
- By contributing you agree that your work is released under the project's MIT license.

## Manual test checklist

Before a release, check in a development client: coordinates (including negative), all eight directions, day counter against `/time query day`, copy by key and by button, settings screen sliders, F3 hiding, GUI scales 1–4, a Nether round trip, death row after `/kill`, and a corrupted config file.
