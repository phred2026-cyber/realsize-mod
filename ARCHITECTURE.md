# RealSize Structure

RealSize keeps version-neutral configuration and sizing policy in `common`. Each Minecraft target
owns its Fabric bootstrap, packaged resources, Minecraft attribute mutation, and the two tracking
hooks whose mapped names differ between versions. Both targets call the same shared policy; neither
gets a private copy of the rules.

```text
realsize-mod/
├── common/
│   ├── build.gradle
│   │   # Builds version-neutral Java and the two shared behavior suites.
│   └── src/
│       ├── main/java/xyz/pyrehaven/realsize/
│       │   ├── RealSizeConfig.java
│       │   │   # Immutable configuration values, built-in defaults, and the default entity scale map.
│       │   ├── RealSizeConfigStore.java
│       │   │   # Sole owner of config/realsize.json creation/loading/merging and the accepted
│       │   │   # process-wide current config read by platform adapters and static mixin boundaries.
│       │   └── ScalePolicy.java
│       │       # Sole owner of 1.0 fallback selection, clamping, tracking, and step decisions.
│       └── test/java/xyz/pyrehaven/realsize/
│           ├── ConfigurationTest.java
│           │   # Default-file, partial override, invalid-value, malformed-file, and immutability risks.
│           └── ScalingPolicyTest.java
│               # Selection, clamp boundaries, tracking thresholds, step thresholds, and neutral entities.
├── mc121/
│   ├── build.gradle
│   │   # Fabric Loom adapter and packaging for Minecraft 1.21.1 through 1.21.11.
│   └── src/main/
│       ├── java/xyz/pyrehaven/realsize/
│       │   ├── RealSizeMod.java
│       │   │   # Minecraft 1.21 composition root: loads config and registers the entity-load callback.
│       │   ├── EntityScaleApplier.java
│       │   │   # Sole 1.21 mutation owner for released add-if-missing scale and step modifiers.
│       │   └── mixin/
│       │       ├── EntityTypeTrackingMixin.java
│       │       │   # Raises the configured tiny-mob chunk range through shared ScalePolicy decisions.
│       │       └── TrackedEntityDistanceMixin.java
│       │           # Raises the configured tiny-mob block distance through shared ScalePolicy decisions.
│       └── resources/
│           ├── fabric.mod.json
│           │   # 1.21 Fabric metadata, dependencies, entrypoint, icon, and mixin declaration.
│           ├── realsize.mixins.json
│           │   # Required 1.21 server mixins and fail-fast injection settings.
│           └── assets/realsize/icon.png
│               # Packaged RealSize icon generated from the shared branding source.
├── mc2612/
│   ├── build.gradle
│   │   # Fabric Loom adapter and packaging for Minecraft 26.2.
│   └── src/main/
│       ├── java/xyz/pyrehaven/realsize/
│       │   ├── RealSizeMod.java
│       │   │   # Minecraft 26.2 composition root: loads config and registers the entity-load callback.
│       │   ├── EntityScaleApplier.java
│       │   │   # Sole 26.2 mutation owner for released add-if-missing scale and step modifiers.
│       │   └── mixin/
│       │       ├── EntityTypeTrackingMixin.java
│       │       │   # Raises the configured tiny-mob chunk range through shared ScalePolicy decisions.
│       │       └── TrackedEntityDistanceMixin.java
│       │           # Raises the configured tiny-mob block distance through shared ScalePolicy decisions.
│       └── resources/
│           ├── fabric.mod.json
│           │   # 26.2 Fabric metadata, dependencies, entrypoint, icon, and mixin declaration.
│           ├── realsize.mixins.json
│           │   # Required 26.2 server mixins and fail-fast injection settings.
│           └── assets/realsize/icon.png
│               # Packaged RealSize icon generated from the shared branding source.
├── ARCHITECTURE.md
│   # This proposed file tree. Implementation and verification must match it.
├── FEATURES.md
│   # Accepted behavior contract and explicit baseline defects for the clean rebuild.
├── README.md
│   # Installation, configuration, supported versions, and current rebuild status.
├── LICENSE
├── .gitignore
│   # Keeps Gradle output, IDE state, and local runtime files out of source.
├── branding/generate_icon.py
│   # Sole branding source; deterministically regenerates both byte-identical packaged icons.
├── build.gradle
│   # Root aggregate build and verification tasks for common plus both targets.
├── gradle.properties
│   # Minecraft, Fabric, Java, artifact, and preserved release-version properties.
├── settings.gradle
│   # Declares the common, mc121, and mc2612 modules and plugin repositories.
├── gradlew
└── gradle/
    ├── minecraft/identity-official-26.2.jar
    │   # Pinned official-name mapping input consumed by the Minecraft 26.2 build.
    └── wrapper/
        ├── gradle-wrapper.jar
        └── gradle-wrapper.properties
```

Generated output, caches, IDE state, local runtime files, and compatibility copies do not belong in
this tree. A Minecraft-specific file may translate mappings and APIs, but it may not redefine config,
scale, tracking, or step-height policy.
