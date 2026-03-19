# RealSize

**Scales every Minecraft mob to its accurate real-world size.**

A spider becomes the tiny creature it actually is. A horse stands at proper height. An elder guardian looms like the ocean titan it should be.

No client install required — drop it on your Fabric server and every player sees accurate sizes automatically.

---

## Compatibility

| | |
|---|---|
| **Minecraft** | 1.21.1 – 1.21.11 |
| **Loader** | Fabric |
| **Environment** | Server-side (works in singleplayer too) |
| **Fabric API** | Required |

---

## What It Does

Applies `EntityAttributes.GENERIC_SCALE` to each mob on load, scaling hitbox and model together based on real-world measurements.

| Mob | Real Height | Scale |
|-----|------------|-------|
| Spider | ~2.5cm body | 0.26× |
| Bee | ~1.5cm | 0.25× |
| Bat | ~6cm | 0.24× |
| Rabbit | ~25cm | 0.50× |
| Wolf | ~80cm shoulder | 0.88× |
| Horse | ~1.6m shoulder | 1.05× |
| Iron Golem | ~2.7m | 1.20× |
| Elder Guardian | enormous | 1.35× |
| Dolphin | ~2.5m long | 1.30× |

Over 60 mobs rescaled. Full rationale and real-world references in [`RealSizeMod.java`](src/main/java/xyz/pyrehaven/realsize/RealSizeMod.java).

---

## Features

- **Accurate proportions** — real-world shoulder/body heights used as reference
- **Server-side only** — no client mod needed, works in singleplayer too
- **Hitbox + model scale together** — uses the native `GENERIC_SCALE` attribute added in 1.20.5
- **Small mob visibility** — tracking range boosted for tiny mobs so they never cull at normal view distances
- **Large mob terrain fix** — step height boosted for mobs over 1.10× so they can navigate terrain naturally
- **Hard scale limits** — floor 0.22× (anything smaller is invisible), cap 1.45× (prevents ceiling clipping)
- **1.21.11 mobs supported** — Nautilus and Zombie Nautilus included via runtime registry lookup

---

## Installation

1. Download the latest `.jar` from [Releases](https://github.com/phred2026-cyber/realsize-mod/releases)
2. Drop it in your server's `mods/` folder (alongside Fabric API)
3. Restart — done

---

## Links

- 🔥 [PyreHaven Discord](https://discord.gg/tZ6Hx2ETA3) — support, feedback, hang out
- 📦 [Modrinth](https://modrinth.com/mod/realsize) — download page
- 🌐 [PyreHaven](https://pyrehaven.xyz) — the Minecraft server this was built for

---

*Built by [PyreHaven](https://pyrehaven.xyz) — chaotic worlds, safe community.*
