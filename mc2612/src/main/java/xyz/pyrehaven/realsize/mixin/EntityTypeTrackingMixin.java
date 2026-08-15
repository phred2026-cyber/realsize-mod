package xyz.pyrehaven.realsize.mixin;

import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;

/** Structural hook shell. Tiny-mob tracking behavior is intentionally absent. */
@Mixin(EntityType.class)
public abstract class EntityTypeTrackingMixin {
}
