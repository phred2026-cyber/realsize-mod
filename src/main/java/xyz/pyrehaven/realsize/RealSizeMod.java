package xyz.pyrehaven.realsize;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RealSize v1.0.4 — Full 1.21.1 mob audit
 *
 * Scale factor = real_world_height_meters / vanilla_height_in_blocks
 * Clamped to [0.18, 1.8] for render/gameplay sanity.
 *
 * Stat adjustments:
 *   scale < 0.5  → GENERIC_FOLLOW_RANGE boost (ADD_MULTIPLIED_BASE, value = 1/scale - 1)
 *                  so engine culling doesn't eat tiny mobs at normal viewing distance
 *   scale > 1.2  → GENERIC_STEP_HEIGHT boost (+0.6, ADD_VALUE)
 *                  so large mobs can step up blocks naturally
 *   NO speed modifier — v1.0.1 proved this breaks animations
 */
public class RealSizeMod implements ModInitializer {
    public static final String MOD_ID = "realsize";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final Identifier ID_SCALE        = Identifier.of(MOD_ID, "scale");
    private static final Identifier ID_FOLLOW_RANGE = Identifier.of(MOD_ID, "follow_range");
    private static final Identifier ID_STEP_HEIGHT  = Identifier.of(MOD_ID, "step_height");

    @Override
    public void onInitialize() {
        LOGGER.info("RealSize v1.0.4 loaded — all 1.21.1 mobs rescaled");

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!(entity instanceof LivingEntity living)) return;

            double scale = getScale(entity.getType());
            if (scale == 1.0) return;

            // Apply visual + hitbox scale
            applyModifier(living, EntityAttributes.GENERIC_SCALE, ID_SCALE,
                    scale - 1.0, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);

            // Tiny mobs: boost follow range so culling doesn't eat them
            if (scale < 0.5) {
                double rangeBoost = (1.0 / scale) - 1.0;
                applyModifier(living, EntityAttributes.GENERIC_FOLLOW_RANGE, ID_FOLLOW_RANGE,
                        rangeBoost, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
            }

            // Large mobs: step height boost so they can climb blocks
            if (scale > 1.2) {
                applyModifier(living, EntityAttributes.GENERIC_STEP_HEIGHT, ID_STEP_HEIGHT,
                        0.6, EntityAttributeModifier.Operation.ADD_VALUE);
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

    private double getScale(EntityType<?> type) {
        // ── ARTHROPODS & TINY CREATURES ──────────────────────────────────────────
        // Real spiders are 1-3cm. Vanilla spider is ~0.9m tall. True scale ~0.01→ clamp.
        if (type == EntityType.SPIDER)           return 0.22; // visible minimum, still scary
        if (type == EntityType.CAVE_SPIDER)      return 0.18; // slightly smaller than normal
        if (type == EntityType.BEE)              return 0.18; // ~1.5cm real, clamp floor
        if (type == EntityType.SILVERFISH)       return 0.18; // ~1.5cm real, clamp floor
        if (type == EntityType.ENDERMITE)        return 0.18; // ~2cm real, clamp floor

        // ── BATS & FLYING SMALL ──────────────────────────────────────────────────
        // Real bats: ~5cm body. Vanilla bat is oversized.
        if (type == EntityType.BAT)              return 0.18; // clamp — tiny flying mammal

        // ── AQUATIC — FISH ────────────────────────────────────────────────────────
        if (type == EntityType.TROPICAL_FISH)    return 0.18; // ~5cm real, clamp floor
        if (type == EntityType.TADPOLE)          return 0.18; // ~1cm real, clamp floor
        if (type == EntityType.PUFFERFISH)       return 0.35; // ~30cm real / vanilla ~0.35 → 1.0 raw, tune to 0.35
        if (type == EntityType.COD)              return 0.45; // ~50cm real
        if (type == EntityType.SALMON)           return 0.55; // ~60cm real
        if (type == EntityType.SQUID)            return 0.35; // ~30cm mantle
        if (type == EntityType.GLOW_SQUID)       return 0.35; // same as squid

        // ── AMPHIBIANS ────────────────────────────────────────────────────────────
        if (type == EntityType.FROG)             return 0.22; // ~8cm real
        if (type == EntityType.AXOLOTL)          return 0.35; // ~25cm real

        // ── REPTILES ─────────────────────────────────────────────────────────────
        if (type == EntityType.TURTLE)           return 0.45; // ~40cm shell length

        // ── BIRDS ────────────────────────────────────────────────────────────────
        if (type == EntityType.CHICKEN)          return 0.64; // ~45cm / vanilla ~0.7m
        if (type == EntityType.PARROT)           return 0.35; // ~25cm / vanilla is comically large

        // ── SMALL MAMMALS ─────────────────────────────────────────────────────────
        if (type == EntityType.RABBIT)           return 0.45; // ~30cm
        if (type == EntityType.CAT)              return 0.55; // ~25cm at shoulder
        if (type == EntityType.ALLAY)            return 0.55; // tiny fairy-like mob

        // ── MEDIUM MAMMALS ────────────────────────────────────────────────────────
        if (type == EntityType.FOX)              return 0.65; // ~35cm at shoulder
        if (type == EntityType.ARMADILLO)        return 0.65; // ~75cm / vanilla ~1.15m → ~0.65
        if (type == EntityType.OCELOT)           return 0.65; // ~45cm / vanilla ~0.7m
        if (type == EntityType.GUARDIAN)         return 0.65; // large fish ~1m / vanilla ~1.5m
        if (type == EntityType.VEX)              return 0.55; // tiny spirit/fairy creature
        if (type == EntityType.GOAT)             return 0.80; // ~75cm at shoulder
        if (type == EntityType.WOLF)             return 0.85; // ~80cm at shoulder
        if (type == EntityType.PIG)              return 0.88; // ~90cm / vanilla ~1.0m
        if (type == EntityType.SHEEP)            return 0.88; // ~90cm / vanilla ~1.0m
        if (type == EntityType.PANDA)            return 0.92; // ~1.2m / vanilla ~1.3m

        // ── LARGE MAMMALS ─────────────────────────────────────────────────────────
        if (type == EntityType.POLAR_BEAR)       return 1.10; // ~1.5m shoulder / vanilla ~1.4m
        if (type == EntityType.HOGLIN)           return 1.10; // large boar-like beast
        if (type == EntityType.ZOGLIN)           return 1.10; // same base as hoglin
        if (type == EntityType.DONKEY)           return 1.10; // ~1.1m shoulder
        if (type == EntityType.MULE)             return 1.15; // ~1.15m shoulder
        if (type == EntityType.DOLPHIN)          return 1.20; // ~2.5m long / vanilla ~0.6m — capped for playability
        if (type == EntityType.HORSE)            return 1.20; // ~1.6m shoulder
        if (type == EntityType.SKELETON_HORSE)   return 1.20; // same frame as horse
        if (type == EntityType.ZOMBIE_HORSE)     return 1.20; // same frame as horse
        if (type == EntityType.LLAMA)            return 1.20; // ~1.8m / vanilla ~1.5m
        if (type == EntityType.TRADER_LLAMA)     return 1.20; // same as llama
        if (type == EntityType.SNIFFER)          return 1.20; // large ancient creature
        if (type == EntityType.PIGLIN_BRUTE)     return 1.05; // larger humanoid variant
        if (type == EntityType.RAVAGER)          return 1.30; // massive beast — should feel huge
        if (type == EntityType.IRON_GOLEM)       return 1.35; // constructed giant — imposing
        if (type == EntityType.ELDER_GUARDIAN)   return 1.40; // deep ocean titan
        if (type == EntityType.CAMEL)            return 1.40; // ~2.0m at hump / vanilla ~1.0m

        // ── HUMANOIDS (human-sized — keep 1.0) ───────────────────────────────────
        // Zombie, Skeleton, Creeper, Pillager, Vindicator, Evoker, Witch,
        // Villager, Wandering Trader, Piglin, Zombified Piglin, Drowned,
        // Husk, Stray, Zombie Villager, Bogged — all ~1.8-2.0m → scale 1.0

        // ── FICTIONAL / SUPERNATURAL (keep vanilla scale) ─────────────────────────
        // Enderman, Blaze, Ghast, Phantom, Shulker, Slime, Magma Cube,
        // Strider, Warden, Snow Golem, Breeze, Creaking — all 1.0

        // ── MOOSHROOM (same as cow → 1.0) ────────────────────────────────────────

        return 1.0;
    }
}
