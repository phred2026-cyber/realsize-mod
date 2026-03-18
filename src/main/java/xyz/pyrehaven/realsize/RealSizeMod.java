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

public class RealSizeMod implements ModInitializer {
    public static final String MOD_ID = "realsize";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("RealSize mod loaded - scaling mobs to real-world proportions");

        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (!(entity instanceof LivingEntity living)) return;

            double scale = getScale(entity.getType());
            if (scale == 1.0) return;

            // Scale the model + hitbox
            applyModifier(living, EntityAttributes.GENERIC_SCALE,
                Identifier.of(MOD_ID, "scale"), scale - 1.0);

            // For mobs smaller than ~0.5x vanilla, boost follow range so the engine
            // doesn't cull them too aggressively at normal viewing distances.
            // The culling distance scales with hitbox size, so we invert-compensate.
            if (scale < 0.5) {
                double rangeBoost = (1.0 / scale) - 1.0; // e.g. 0.25x -> +3x range
                applyModifier(living, EntityAttributes.GENERIC_FOLLOW_RANGE,
                    Identifier.of(MOD_ID, "follow_range"), rangeBoost);
            }
        });
    }

    private void applyModifier(LivingEntity living,
                                RegistryEntry<EntityAttribute> attribute,
                                Identifier id, double value) {
        EntityAttributeInstance inst = living.getAttributeInstance(attribute);
        if (inst == null) return;
        if (inst.getModifier(id) != null) return;
        inst.addPersistentModifier(new EntityAttributeModifier(
            id, value, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
        ));
    }

    private double getScale(EntityType<?> type) {
        // Scale values are a balance between realism and gameplay visibility.
        // True biological scale (e.g. 0.04x for silverfish) breaks rendering and
        // animations, so these are tuned to feel realistic while staying functional.

        // Arthropods — tiny but still visible/functional
        if (type == EntityType.SPIDER)        return 0.22;
        if (type == EntityType.CAVE_SPIDER)   return 0.17;
        if (type == EntityType.BEE)           return 0.18;
        if (type == EntityType.SILVERFISH)    return 0.16;
        if (type == EntityType.ENDERMITE)     return 0.15;

        // Small animals
        if (type == EntityType.BAT)           return 0.28;
        if (type == EntityType.RABBIT)        return 0.50;
        if (type == EntityType.CAT)           return 0.65;
        if (type == EntityType.AXOLOTL)       return 0.35;
        if (type == EntityType.FROG)          return 0.28;
        if (type == EntityType.TADPOLE)       return 0.18;
        if (type == EntityType.TROPICAL_FISH) return 0.18;
        if (type == EntityType.SALMON)        return 0.55;
        if (type == EntityType.COD)           return 0.45;
        if (type == EntityType.SQUID)         return 0.50;
        if (type == EntityType.GLOW_SQUID)    return 0.50;
        if (type == EntityType.PARROT)        return 0.35; // vanilla parrot is comically large

        // Medium animals — closer to vanilla, slight corrections
        if (type == EntityType.CHICKEN)       return 0.70;
        if (type == EntityType.FOX)           return 0.70;
        if (type == EntityType.WOLF)          return 0.80;
        if (type == EntityType.OCELOT)        return 0.65;
        if (type == EntityType.PIG)           return 0.88;
        if (type == EntityType.SHEEP)         return 0.90;
        if (type == EntityType.GOAT)          return 0.85;
        if (type == EntityType.PANDA)         return 0.92;
        if (type == EntityType.POLAR_BEAR)    return 1.10;
        if (type == EntityType.TURTLE)        return 0.45;
        if (type == EntityType.DOLPHIN)       return 0.90;
        if (type == EntityType.GUARDIAN)      return 0.60;

        // Mobs that are too SMALL in vanilla — scaled up
        if (type == EntityType.HORSE)         return 1.20; // horses should tower over players
        if (type == EntityType.DONKEY)        return 1.10;
        if (type == EntityType.MULE)          return 1.15;
        if (type == EntityType.CAMEL)         return 1.40; // camels are huge
        if (type == EntityType.IRON_GOLEM)    return 1.35; // should be genuinely imposing
        if (type == EntityType.RAVAGER)       return 1.30; // beast, should feel massive
        if (type == EntityType.ELDER_GUARDIAN) return 1.40; // deep ocean titan

        return 1.0;
    }
}
