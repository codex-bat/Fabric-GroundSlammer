// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.mixin;

import net.codex.GroundSlammerClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {

    @Inject(
            method = "addParticleClient(Lnet/minecraft/particle/ParticleEffect;ZZDDDDDD)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void groundslammer$filterBlockParticles(
            ParticleEffect parameters,
            boolean force,
            boolean canSpawnOnMinimal,
            double x,
            double y,
            double z,
            double velocityX,
            double velocityY,
            double velocityZ,
            CallbackInfo ci
    ) {
        if (!(parameters instanceof BlockStateParticleEffect effect)) {
            return;
        }

        if (effect.getType() != ParticleTypes.BLOCK && effect.getType() != ParticleTypes.FALLING_DUST) {
            return;
        }

        for (Entity entity : GroundSlammerClient.SUPPRESSED_ENTITIES) {
            double radius = entity.getWidth() * 2.5;

            if (entity.squaredDistanceTo(x, y, z) < radius * radius) {
                ci.cancel();
                return;
            }
        }
    }
}