package xyz.pyrehaven.realsize;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
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

            EntityAttributeInstance attr = living.getAttributeInstance(EntityAttributes.GENERIC_SCALE);
            if (attr == null) return;

            Identifier modId = Identifier.of(MOD_ID, "scale");
            if (attr.getModifier(modId) != null) return; // already applied

            // ADD_MULTIPLIED_BASE: final = base * (1 + value)
            // To get scale X: value = X - 1.0
            attr.addPersistentModifier(new EntityAttributeModifier(
                modId,
                scale - 1.0,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
            ));
        });
    }

    private double getScale(EntityType<?> type) {
        // Arthropods / insects
        if (type == EntityType.SPIDER)       return 0.07;
        if (type == EntityType.CAVE_SPIDER)  return 0.05;
        if (type == EntityType.BEE)          return 0.10;
        if (type == EntityType.SILVERFISH)   return 0.04;
        if (type == EntityType.ENDERMITE)    return 0.05;

        // Small animals
        if (type == EntityType.BAT)          return 0.15;
        if (type == EntityType.RABBIT)       return 0.40;
        if (type == EntityType.CAT)          return 0.55;
        if (type == EntityType.AXOLOTL)      return 0.15;
        if (type == EntityType.FROG)         return 0.10;
        if (type == EntityType.TADPOLE)      return 0.05;
        if (type == EntityType.TROPICAL_FISH) return 0.08;
        if (type == EntityType.SALMON)       return 0.40;
        if (type == EntityType.COD)          return 0.30;
        if (type == EntityType.SQUID)        return 0.35;
        if (type == EntityType.GLOW_SQUID)   return 0.35;

        // Medium animals
        if (type == EntityType.CHICKEN)      return 0.60;
        if (type == EntityType.FOX)          return 0.65;
        if (type == EntityType.WOLF)         return 0.75;
        if (type == EntityType.OCELOT)       return 0.55;
        if (type == EntityType.PIG)          return 0.85;
        if (type == EntityType.SHEEP)        return 0.90;
        if (type == EntityType.GOAT)         return 0.85;
        if (type == EntityType.PANDA)        return 0.90;
        if (type == EntityType.POLAR_BEAR)   return 1.10;
        if (type == EntityType.TURTLE)       return 0.30;
        if (type == EntityType.DOLPHIN)      return 0.90;
        if (type == EntityType.GUARDIAN)     return 0.50;

        return 1.0; // no change
    }
}
