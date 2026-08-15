# RealSize

RealSize is being rebuilt from a clean architecture. This branch is structural groundwork only.
It compiles so the proposed owners and dual-target build can be reviewed, but its entrypoints
intentionally stop startup and no gameplay is active.

Do not deploy or publish these jars.

- `ARCHITECTURE.md` defines the proposed file tree and ownership boundaries.
- `FEATURES.md` preserves the accepted configuration and gameplay behavior for the rebuild.
- The repository still targets Minecraft 1.21.1 through 1.21.11 and Minecraft 26.2 with separate
  Fabric jars.

Build the groundwork with:

```bash
./gradlew clean build
```
