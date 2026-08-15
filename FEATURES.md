# RealSize Feature Contract

## Status

This file preserves RealSize's accepted product behavior for the clean implementation. The current
branch is structural groundwork only: it compiles the owners named in `ARCHITECTURE.md`, but the
entrypoints deliberately stop startup and none of the gameplay features or executable tests are
implemented. Do not deploy or publish jars from this groundwork.

## Runtime scope

- RealSize is server-authoritative and supports dedicated Fabric servers and integrated singleplayer.
- Players do not need RealSize installed on their clients.
- The repository produces separate jars for Minecraft 1.21.1 through 1.21.11 and Minecraft 26.2.
- Both jars use the same configuration schema, defaults, scale policy, modifier IDs, and observable
  behavior. Platform adapters may differ only where Minecraft mappings or APIs differ.
- Configuration is loaded once during startup. Changes take effect after a restart.

## Configuration

`RealSizeConfigStore` is the only owner of `config/realsize.json`.

- A missing file creates its parent directory and writes a complete, pretty-printed default config.
- A partial existing document inherits omitted scalar fields and omitted entity entries from defaults.
- Unknown JSON fields are ignored.
- An empty document uses the complete defaults.
- Gson's released coercion remains part of this rebuild: numeric JSON strings such as `"0.3"` and
  `"12"` are accepted for numeric fields. Unknown fields and duplicate keys follow Gson 2.13.1
  behavior; registry IDs are not validated against Minecraft while loading.
- Malformed JSON or incompatible value types propagate Gson's syntax/type exception without adding
  the config path. Directory, read, and write I/O failures are wrapped in `IllegalStateException`
  with the config path and cause. RealSize must not silently run after either failure form.
- The loaded entity scale map is immutable to consumers.
- Entity IDs must be non-blank and scale overrides must be positive. Invalid map entries are ignored
  in favor of that entity's built-in default or the normal scale fallback.
- `floor`, `cap`, `trackingRangeThreshold`, and `stepHeightBoostThreshold` must be positive.
- `minTrackingRangeChunks` and `minTrackingDistanceBlocks` must be positive integers.
- `stepHeightBoostAmount` may be zero but not negative.
- Invalid values use their field defaults. Validation is limited to the comparisons above; the
  released parser has no additional finite-number check. If the resulting `cap` is below `floor`,
  `cap` becomes equal to `floor`.

| Field | Default | Ownership contract |
|---|---:|---|
| `floor` | `0.22` | Lowest effective scale after clamping |
| `cap` | `1.45` | Highest effective scale after clamping |
| `trackingRangeThreshold` | `0.60` | Effective scales below this receive tracking help |
| `minTrackingRangeChunks` | `10` | Minimum entity-type tracking range for tiny mobs |
| `minTrackingDistanceBlocks` | `128` | Minimum effective server tracking distance for tiny mobs |
| `stepHeightBoostThreshold` | `1.10` | Effective scales above this receive extra step height |
| `stepHeightBoostAmount` | `0.5` | Added step-height attribute value for qualifying mobs |
| `entityScales` | built-in map below | Positive per-entity overrides keyed by registry ID |

Built-in entity scales:

```text
minecraft:spider=0.26                 minecraft:cave_spider=0.20
minecraft:silverfish=0.22             minecraft:endermite=0.22
minecraft:bee=0.25                    minecraft:bat=0.24
minecraft:frog=0.28                   minecraft:tadpole=0.22
minecraft:axolotl=0.55                minecraft:tropical_fish=0.22
minecraft:pufferfish=0.43             minecraft:cod=0.50
minecraft:salmon=0.70                 minecraft:squid=0.40
minecraft:glow_squid=0.40             minecraft:nautilus=0.40
minecraft:zombie_nautilus=0.40        minecraft:turtle=0.55
minecraft:chicken=0.64                minecraft:parrot=0.45
minecraft:rabbit=0.50                 minecraft:cat=0.45
minecraft:fox=0.50                    minecraft:armadillo=0.38
minecraft:allay=0.42                  minecraft:vex=0.30
minecraft:ocelot=0.64                 minecraft:goat=0.65
minecraft:wolf=0.88                   minecraft:pig=0.95
minecraft:sheep=0.75                  minecraft:panda=0.92
minecraft:polar_bear=1.05             minecraft:hoglin=0.85
minecraft:zoglin=0.85                 minecraft:donkey=0.88
minecraft:mule=0.93                   minecraft:horse=1.05
minecraft:skeleton_horse=1.05         minecraft:zombie_horse=1.05
minecraft:llama=0.92                  minecraft:trader_llama=0.92
minecraft:camel=1.10                  minecraft:sniffer=0.94
minecraft:dolphin=1.30                minecraft:guardian=0.70
minecraft:elder_guardian=1.35         minecraft:piglin_brute=1.05
minecraft:iron_golem=1.20             minecraft:ravager=1.15
```

An entity absent from both the loaded map and the built-in map selects fallback scale `1.0`.
The entity-load mutation path treats that raw fallback as neutral and exits before clamping, but the
tracking hooks clamp the same fallback through global bounds before comparing the tracking threshold.

## Entity scaling

- RealSize acts only on living entities when the server loads them.
- `ScalePolicy` selects a configured value or fallback `1.0` and clamps selected values inclusively
  between `floor` and `cap` when the caller requests an effective scale.
- `EntityScaleApplier` is the sole Minecraft mutation owner for its target. It applies the effective
  scale through a persistent `realsize:scale` attribute modifier using add-multiplied-base semantics
  and value `effectiveScale - 1.0`.
- A raw configured or fallback scale of exactly `1.0` exits before clamping or either modifier path.
- Each modifier is added only when that specific attribute exists and no modifier with RealSize's ID
  is already present. Existing owned modifiers are not replaced or removed, so repeated loads do not
  stack duplicates but persisted entities may retain values from an older configuration.
- Non-living entities are left alone. A missing scale or step-height attribute skips only that
  attribute's mutation; it does not suppress the other attribute path.

## Tiny-mob tracking

- A living entity qualifies when its configured or fallback scale, after clamping, is strictly below
  `trackingRangeThreshold`.
- The entity-type tracking hook raises, but never lowers, the returned chunk tracking range to
  `minTrackingRangeChunks`.
- The tracked-entity hook raises, but never lowers, the effective block tracking distance to
  `minTrackingDistanceBlocks`.
- Both hooks ask `ScalePolicy`; mixins do not read configuration or calculate scale independently.
- Mixin injections are required and fail loudly when a supported Minecraft target no longer exposes
  the mapped return hook. Silent loss of tiny-mob visibility is not an accepted compatibility mode.
- The entity-type hook has no entity instance and applies its policy to every entity type, including
  fallback `1.0` and configured nonliving IDs. Scale mutation and effective-distance enforcement
  remain living-entity behavior. This released distinction is explicit and covered by tests.

## Large-mob step height

- A living entity qualifies when its effective clamped scale is strictly above
  `stepHeightBoostThreshold`.
- `EntityScaleApplier` adds one persistent `realsize:step_height` attribute modifier using add-value
  semantics and the configured `stepHeightBoostAmount`.
- An existing `realsize:step_height` modifier is left untouched rather than replaced or removed.
- A missing step-height attribute skips only step-height mutation; an available scale attribute is
  still handled independently.

The previous README said “at or above” while the released implementation used a strict `>` check.
This contract preserves the actual released threshold. Changing it to inclusive is a separate gameplay
decision and test, not cleanup hidden inside the rebuild.

## Released quirks preserved pending a feature decision

Add-if-missing persistent modifiers, the early raw-`1.0` return, tracking's clamped `1.0` fallback,
Gson coercion, duplicate-key handling, missing registry validation, and the absence of explicit
finite-number validation are preserved here because this checkpoint changes structure, not gameplay.
Replacing them with modifier reconciliation, stricter parsing, or an explicit unmanaged-entity policy
requires a separately accepted feature decision and regression tests.

## Lifecycle and failure contract

- Config must load successfully before any entity-load callback is registered.
- Startup logs the number of configured entity scale entries without printing the whole operator config.
- `RealSizeConfigStore` publishes and retains the single process-wide accepted config. Every scale,
  tracking, and step-height decision reads that state through `ScalePolicy`; platform adapters and
  mixins do not keep parallel config or decision state.
- No fallback config is installed after startup has accepted a config.
- A config failure, unsupported required mixin target, or broken composition invariant stops startup
  loudly. RealSize must not appear healthy while one of its advertised systems is absent.
- A successful implementation is not complete until shared config/policy tests pass for both packaged
  targets and server verification confirms scaling, tiny tracking, and large-mob movement behavior.
