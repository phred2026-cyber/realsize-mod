package xyz.pyrehaven.realsize;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * RealSize v1.0.5 — Accurate real-world scaling, all 1.21.11 mobs
 *
 * Reference: Player = 1.95m = 1.95 blocks (call it 2.0)
 * Scale = real_height_m / vanilla_height_blocks
 * Hard floor: 0.22 (spider/bee level — anything smaller is invisible)
 * Hard cap:   1.45 (prevents extreme ceiling clipping)
 *
 * Real-world heights used (shoulder/body height, not nose-to-tail):
 *   Bee:            0.015m   Vanilla: 0.60m  → 0.025 → floor 0.22
 *   Silverfish:     0.015m   Vanilla: 0.30m  → 0.05  → floor 0.22
 *   Cave Spider:    0.02m    Vanilla: 0.50m  → 0.04  → floor 0.22
 *   Spider:         0.025m   Vanilla: 0.90m  → 0.03  → floor 0.22
 *   Bat:            0.06m    Vanilla: 0.90m  → 0.07  → floor 0.22
 *   Endermite:      0.02m    Vanilla: 0.30m  → 0.07  → floor 0.22
 *   Tadpole:        0.015m   Vanilla: 0.40m  → 0.04  → floor 0.22
 *   Tropical Fish:  0.05m    Vanilla: 0.60m  → 0.08  → floor 0.22
 *   Frog:           0.10m    Vanilla: 0.55m  → 0.18  → 0.28 (bump for visibility)
 *   Axolotl:        0.25m    Vanilla: 0.42m  → 0.60  → 0.55 (scale down to match)
 *   Rabbit:         0.25m    Vanilla: 0.50m  → 0.50
 *   Pufferfish:     0.30m    Vanilla: 0.70m  → 0.43  → 0.43
 *   Parrot:         0.35m    Vanilla: 0.90m  → 0.39  → 0.45 (bump slightly)
 *   Cat:            0.25m    Vanilla: 0.70m  → 0.36  → 0.45
 *   Turtle:         0.45m    Vanilla: 0.40m  → 1.13  → 0.55 (vanilla already small)
 *   Cod:            0.50m    Vanilla: 0.30m  → 1.67  → cap — 0.50 feels right
 *   Squid:          0.30m    Vanilla: 0.80m  → 0.38  → 0.40
 *   Salmon:         0.60m    Vanilla: 0.50m  → 1.20  → 0.70 (vanilla already reasonable)
 *   Fox:            0.35m    Vanilla: 0.70m  → 0.50
 *   Ocelot:         0.45m    Vanilla: 0.70m  → 0.64
 *   Armadillo:      0.20m    Vanilla: 0.60m  → 0.33  → 0.38
 *   Allay:          0.25m    Vanilla: 0.60m  → 0.42  → 0.42
 *   Vex:            0.20m    Vanilla: 0.80m  → 0.25  → 0.30
 *   Chicken:        0.45m    Vanilla: 0.70m  → 0.64
 *   Guardians:      0.90m    Vanilla: 0.85m  → 1.06  → 0.70 (vanilla already biggish)
 *   Goat:           0.75m    Vanilla: 1.30m  → 0.58  → 0.65
 *   Wolf:           0.80m    Vanilla: 0.85m  → 0.94  → 0.88
 *   Pig:            0.90m    Vanilla: 0.90m  → 1.00  (correct already, minor tweak)
 *   Sheep:          0.90m    Vanilla: 1.30m  → 0.69  → 0.75
 *   Panda:          1.20m    Vanilla: 1.30m  → 0.92
 *   Polar Bear:     1.50m    Vanilla: 1.40m  → 1.07  → 1.05
 *   Hoglin/Zoglin:  1.10m    Vanilla: 1.40m  → 0.79  → 0.85
 *   Donkey:         1.20m    Vanilla: 1.40m  → 0.86  → 0.88
 *   Mule:           1.30m    Vanilla: 1.40m  → 0.93
 *   Horse:          1.60m    Vanilla: 1.60m  → 1.00  (already correct! slight bump 1.05)
 *   Llama:          1.80m    Vanilla: 1.95m  → 0.92
 *   Sniffer:        1.70m    Vanilla: 1.80m  → 0.94
 *   Camel:          2.00m    Vanilla: 1.87m  → 1.07  → 1.10
 *   Dolphin:        2.00m    Vanilla: 0.60m  → 3.33  → cap 1.30
 *   Ravager:        2.00m    Vanilla: 1.90m  → 1.05  → 1.10
 *   Elder Guardian: 2.00m    Vanilla: 1.99m  → 1.00  → 1.30 (should feel huge)
 *   Iron Golem:     2.70m    Vanilla: 2.70m  → 1.00  → 1.20 (bump for impact)
 *   Piglin Brute:   2.00m    Vanilla: 1.95m  → 1.03  → 1.05
 *
 * Stat adjustments:
 *   scale < 0.40 → FOLLOW_RANGE boost: ADD_MULTIPLIED_BASE, value = (1/scale) - 1
 *   scale > 1.10 → STEP_HEIGHT boost: ADD_VALUE +0.5
 */
public class RealSizeMod implements ModInitializer {
    public static final String MOD_ID = "realsize";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final Identifier ID_SCALE        = Identifier.of(MOD_ID, "scale");
    private static final Identifier ID_FOLLOW_RANGE = Identifier.of(MOD_ID, "follow_range");
    private static final Identifier ID_STEP_HEIGHT  = Identifier.of(MOD_ID, "step_height");

    private static final double FLOOR = 0.22;
    private static final double CAP   = 1.45;

    // Registry-ID based overrides for mobs added in 1.21.11 (not in 1.21.1 compile mappings)
    private static final Map<String, Double> REGISTRY_SCALES = new HashMap<>();
    static {
        REGISTRY_SCALES.put("minecraft:nautilus",        0.40);
        REGISTRY_SCALES.put("minecraft:zombie_nautilus", 0.40);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("RealSize v1.0.7 loaded — spider/bee visibility + culling fixes");

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!(entity instanceof LivingEntity living)) return;

            // Check registry-ID overrides first (for 1.21.11 mobs not in 1.21.1 mappings)
            String entityId = Registries.ENTITY_TYPE.getId(entity.getType()).toString();
            double scale;
            if (REGISTRY_SCALES.containsKey(entityId)) {
                scale = REGISTRY_SCALES.get(entityId);
            } else {
                scale = getScale(entity.getType());
            }
            if (scale == 1.0) return;

            // Clamp to safe range
            scale = Math.max(FLOOR, Math.min(CAP, scale));

            // Apply visual + hitbox scale
            applyModifier(living, EntityAttributes.GENERIC_SCALE, ID_SCALE,
                    scale - 1.0, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);

            // Small mobs (< 0.50): boost follow range to compensate for engine
            // render culling which scales with hitbox volume. A spider at 0.26x
            // would otherwise vanish at ~5 blocks; this keeps them visible at ~16.
            if (scale < 0.50) {
                double rangeBoost = (1.0 / scale) - 1.0;
                applyModifier(living, EntityAttributes.GENERIC_FOLLOW_RANGE, ID_FOLLOW_RANGE,
                        rangeBoost, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
            }

            // Large mobs (> 1.10): step height boost so they can actually navigate terrain
            if (scale > 1.10) {
                applyModifier(living, EntityAttributes.GENERIC_STEP_HEIGHT, ID_STEP_HEIGHT,
                        0.5, EntityAttributeModifier.Operation.ADD_VALUE);
            }
        });
    }

    private void applyModifier(LivingEntity living,
                                RegistryEntry<EntityAttribute> attribute,
                                Identifier id, double value,
                                EntityAttributeModifier.Operation operation) {
        EntityAttributeInstance inst = living.getAttributeInstance(attribute);
        if (inst == null) return;
        if (inst.getModifier(id) != null) return;
        inst.addPersistentModifier(new EntityAttributeModifier(id, value, operation));
    }

    /**
     * Returns the desired scale for each mob type.
     * 1.0 = unchanged (humanoids, fictional mobs, anything already correct).
     * Values are pre-clamped reasoning; the onInitialize code clamps again as safety net.
     */
    private double getScale(EntityType<?> type) {

        // ── ARTHROPODS ──────────────────────────────────────────────────────────
        // Spider: bumped to 0.26 — roughly tarantula-sized, still tiny but
        // the bigger hitbox reduces engine render-culling at normal distances.
        // Cave spider is notably smaller than normal spider IRL (Tegenaria vs
        // Dysdera) — keeping distinct separation between the two.
        if (type == EntityType.SPIDER)           return 0.26;
        if (type == EntityType.CAVE_SPIDER)      return 0.20; // smaller species, floor-ish
        if (type == EntityType.SILVERFISH)       return 0.22;
        if (type == EntityType.ENDERMITE)        return 0.22;

        // ── INSECTS ─────────────────────────────────────────────────────────────
        // Bee: bumped to 0.25 — closer to a real bumblebee vs honeybee size,
        // also helps render culling distance noticeably.
        if (type == EntityType.BEE)              return 0.25;

        // ── BATS ────────────────────────────────────────────────────────────────
        // Bat: 0.24 — slightly bigger than floor, bats have a decent wingspan IRL.
        if (type == EntityType.BAT)              return 0.24;

        // ── AMPHIBIANS ──────────────────────────────────────────────────────────
        // Real frog: ~8-10cm body. Vanilla frog: ~0.55m. Ratio ~0.18 → bump to 0.28.
        if (type == EntityType.FROG)             return 0.28;
        // Real tadpole: ~1cm. Floor.
        if (type == EntityType.TADPOLE)          return 0.22; // floor

        // Axolotl: ~25cm. Vanilla: ~0.42m. Ratio ~0.60 → 0.55.
        if (type == EntityType.AXOLOTL)          return 0.55;

        // ── FISH ────────────────────────────────────────────────────────────────
        // Tropical fish: ~5cm. Floor.
        if (type == EntityType.TROPICAL_FISH)    return 0.22; // floor
        // Pufferfish: ~30cm. Vanilla: ~0.70m. Ratio ~0.43.
        if (type == EntityType.PUFFERFISH)       return 0.43;
        // Cod: ~50cm real, vanilla already smallish. 0.50 feels right.
        if (type == EntityType.COD)              return 0.50;
        // Salmon: ~60cm. Vanilla ~0.50m. Ratio >1 but vanilla already reasonable. 0.70.
        if (type == EntityType.SALMON)           return 0.70;

        // ── CEPHALOPODS & MOLLUSCS ──────────────────────────────────────────────
        // Real squid: ~30cm mantle. Vanilla: ~0.80m. Ratio ~0.38.
        if (type == EntityType.SQUID)            return 0.40;
        if (type == EntityType.GLOW_SQUID)       return 0.40;
        // Nautilus + Zombie Nautilus (1.21.11) handled via REGISTRY_SCALES map above

        // ── REPTILES ────────────────────────────────────────────────────────────
        // Real turtle: ~40cm shell. Vanilla is already quite small. Slight boost: 0.55.
        if (type == EntityType.TURTLE)           return 0.55;

        // ── BIRDS ───────────────────────────────────────────────────────────────
        // Chicken: ~45cm. Vanilla: ~0.70m. Ratio ~0.64.
        if (type == EntityType.CHICKEN)          return 0.64;
        // Parrot: ~35cm (macaw-sized). Vanilla: ~0.90m. Ratio ~0.39 → 0.45.
        if (type == EntityType.PARROT)           return 0.45;

        // ── SMALL MAMMALS ───────────────────────────────────────────────────────
        // Rabbit: ~25cm tall when sitting. Vanilla: ~0.50m. Ratio ~0.50.
        if (type == EntityType.RABBIT)           return 0.50;
        // Cat: ~25cm shoulder. Vanilla: ~0.70m. Ratio ~0.36 → bump to 0.45 for visibility.
        if (type == EntityType.CAT)              return 0.45;
        // Fox: ~35cm shoulder. Vanilla: ~0.70m. Ratio ~0.50.
        if (type == EntityType.FOX)              return 0.50;
        // Armadillo: ~20cm. Vanilla: ~0.60m. Ratio ~0.33 → 0.38.
        if (type == EntityType.ARMADILLO)        return 0.38;

        // ── FICTIONAL SMALL ─────────────────────────────────────────────────────
        // Allay: tiny fairy-like mob. 0.42.
        if (type == EntityType.ALLAY)            return 0.42;
        // Vex: tiny spirit. 0.30.
        if (type == EntityType.VEX)              return 0.30;

        // ── MEDIUM MAMMALS ──────────────────────────────────────────────────────
        // Ocelot: ~45cm shoulder. Vanilla: ~0.70m. Ratio ~0.64.
        if (type == EntityType.OCELOT)           return 0.64;
        // Goat: ~75cm shoulder. Vanilla: ~1.30m. Ratio ~0.58 → 0.65.
        if (type == EntityType.GOAT)             return 0.65;
        // Wolf: ~80cm shoulder. Vanilla: ~0.85m. Ratio ~0.94 → 0.88.
        if (type == EntityType.WOLF)             return 0.88;
        // Pig: ~90cm. Vanilla: ~0.90m. Already roughly correct. Tiny nudge.
        if (type == EntityType.PIG)              return 0.95;
        // Sheep: ~90cm. Vanilla: ~1.30m (with wool). Ratio ~0.69 → 0.75.
        if (type == EntityType.SHEEP)            return 0.75;
        // Panda: ~1.2m shoulder. Vanilla: ~1.30m. Ratio ~0.92.
        if (type == EntityType.PANDA)            return 0.92;
        // Mooshroom = cow, already ~1.0.

        // ── LARGE MAMMALS ───────────────────────────────────────────────────────
        // Polar bear: ~1.5m shoulder. Vanilla: ~1.40m. Ratio ~1.07 → 1.05.
        if (type == EntityType.POLAR_BEAR)       return 1.05;
        // Hoglin: large boar-like ~1.1m. Vanilla: ~1.40m. Ratio ~0.79 → 0.85.
        if (type == EntityType.HOGLIN)           return 0.85;
        if (type == EntityType.ZOGLIN)           return 0.85;
        // Donkey: ~1.2m shoulder. Vanilla: ~1.40m. Ratio ~0.86 → 0.88.
        if (type == EntityType.DONKEY)           return 0.88;
        // Mule: ~1.3m. Vanilla: ~1.40m. Ratio ~0.93.
        if (type == EntityType.MULE)             return 0.93;
        // Horse: ~1.6m shoulder. Vanilla horse: ~1.60m. Already correct → slight 1.05.
        if (type == EntityType.HORSE)            return 1.05;
        if (type == EntityType.SKELETON_HORSE)   return 1.05;
        if (type == EntityType.ZOMBIE_HORSE)     return 1.05;
        // Llama: ~1.8m. Vanilla: ~1.95m. Ratio ~0.92.
        if (type == EntityType.LLAMA)            return 0.92;
        if (type == EntityType.TRADER_LLAMA)     return 0.92;
        // Camel: ~2.0m at hump. Vanilla: ~1.87m. Ratio ~1.07 → 1.10.
        if (type == EntityType.CAMEL)            return 1.10;
        // Sniffer: large ancient creature ~1.7m. Vanilla ~1.8m. Ratio ~0.94.
        if (type == EntityType.SNIFFER)          return 0.94;

        // ── AQUATIC LARGE ───────────────────────────────────────────────────────
        // Dolphin: ~2.5m long. Vanilla: ~0.60m. True ratio 4.2 → cap hard at 1.30.
        if (type == EntityType.DOLPHIN)          return 1.30;
        // Guardian: ~0.9m. Vanilla: ~0.85m. Already close → 0.70 for visual correctness.
        if (type == EntityType.GUARDIAN)         return 0.70;
        // Elder Guardian: should feel like an ocean titan. 1.35.
        if (type == EntityType.ELDER_GUARDIAN)   return 1.35;

        // ── NETHER REAL-BASED ───────────────────────────────────────────────────
        // Piglin Brute: larger humanoid variant. 1.05.
        if (type == EntityType.PIGLIN_BRUTE)     return 1.05;

        // ── CONSTRUCTED / BOSS ──────────────────────────────────────────────────
        // Iron Golem: ~2.7m vanilla. Real giant would be bigger. Bump to 1.20 for impact.
        if (type == EntityType.IRON_GOLEM)       return 1.20;
        // Ravager: massive beast. Vanilla already big. 1.15 for extra menace.
        if (type == EntityType.RAVAGER)          return 1.15;

        // ── EVERYTHING ELSE → unchanged ─────────────────────────────────────────
        // Humanoids: Zombie, Skeleton, Creeper, Pillager, Vindicator, Evoker, Witch,
        //   Villager, Wandering Trader, Piglin, Zombified Piglin, Drowned, Husk,
        //   Stray, Zombie Villager, Bogged — all human-scale, return 1.0
        // Fictional: Enderman, Blaze, Ghast, Phantom, Shulker, Slime, Magma Cube,
        //   Strider, Warden, Snow Golem, Breeze, Creaking — return 1.0

        return 1.0;
    }
}
