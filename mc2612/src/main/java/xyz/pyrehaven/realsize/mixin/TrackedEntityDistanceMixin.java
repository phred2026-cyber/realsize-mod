package xyz.pyrehaven.realsize.mixin;

import org.spongepowered.asm.mixin.Mixin;

/** Structural hook shell. Tiny-mob tracking behavior is intentionally absent. */
@Mixin(targets = "net.minecraft.server.level.ChunkMap$TrackedEntity")
public abstract class TrackedEntityDistanceMixin {
}
