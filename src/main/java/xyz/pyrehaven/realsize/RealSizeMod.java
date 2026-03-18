package xyz.pyrehaven.realsize;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.entity.attribute.EntityAttribute;
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

            // Scale size (hitbox + model)
            applyModifier(living, EntityAttributes.GENERIC_SCALE,
                Identifier.of(MOD_ID, "scale"), scale - 1.0);

            // Scale movement speed proportionally so movement feels correct for the size.
            // We use a square root curve: a mob at 0.07x size would be absurdly fast at
            // full speed, but sqrt(0.07) ≈ 0.26 keeps it feeling natural rather than
            // completely stationary. Pure linear (scale directly) makes very small mobs
            // nearly unable to move; sqrt is a good middle ground.
            double speedFactor = Math.sqrt(scale);
            applyModifier(living, EntityAttributes.GENERIC_MOVEMENT_SPEED,
                Identifier.of(MOD_ID, "speed"), speedFactor - 1.0);
        });
    }

    private void applyModifier(LivingEntity living,
                                RegistryEntry<EntityAttribute> attribute,
                                Identifier id, double value) {
        EntityAttributeInstance inst = living.getAttributeInstance(attribute);
        if (inst == null) return;
        if (inst.getModifier(id) != null) return; // already applied
        inst.addPersistentModifier(new EntityAttributeModifier(
            id, value, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
        ));
    }

    private double getScale(EntityType<?> type) {
        // Arthropods / insects
        if (type == EntityType.SPIDER)        return 0.07;
        if (type == EntityType.CAVE_SPIDER)   return 0.05;
        if (type == EntityType.BEE)           return 0.10;
        if (type == EntityType.SILVERFISH)    return 0.04;
        if (type == EntityType.ENDERMITE)     return 0.05;

        // Small animals
        if (type == EntityType.BAT)           return 0.15;
        if (type == EntityType.RABBIT)        return 0.40;
        if (type == EntityType.CAT)           return 0.55;
        if (type == EntityType.AXOLOTL)       return 0.15;
        if (type == EntityType.FROG)          return 0.10;
        if (type == EntityType.TADPOLE)       return 0.05;
        if (type == EntityType.TROPICAL_FISH) return 0.08;
        if (type == EntityType.SALMON)        return 0.40;
        if (type == EntityType.COD)           return 0.30;
        if (type == EntityType.SQUID)         return 0.35;
        if (type == EntityType.GLOW_SQUID)    return 0.35;

        // Medium animals
        if (type == EntityType.CHICKEN)       return 0.60;
        if (type == EntityType.FOX)           return 0.65;
        if (type == EntityType.WOLF)          return 0.75;
        if (type == EntityType.OCELOT)        return 0.55;
        if (type == EntityType.PIG)           return 0.85;
        if (type == EntityType.SHEEP)         return 0.90;
        if (type == EntityType.GOAT)          return 0.85;
        if (type == EntityType.PANDA)         return 0.90;
        if (type == EntityType.POLAR_BEAR)    return 1.10;
        if (type == EntityType.TURTLE)        return 0.30;
        if (type == EntityType.DOLPHIN)       return 0.90;
        if (type == EntityType.GUARDIAN)      return 0.50;

        return 1.0;
    }
}
