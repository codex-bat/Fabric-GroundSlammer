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
            method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onAddParticle7(
            ParticleEffect parameters,
            double x, double y, double z,
            double velocityX, double velocityY, double velocityZ,
            CallbackInfo ci
    ) {
        // Only act when screenshot mode is enabled
        if (parameters instanceof BlockStateParticleEffect effect) {
            var type = effect.getType();

            if (type == ParticleTypes.BLOCK || type == ParticleTypes.FALLING_DUST) {
                for (Entity entity : GroundSlammerClient.SUPPRESSED_ENTITIES) {
                    double radius = entity.getWidth() * 2.5;
                    if (entity.squaredDistanceTo(x, y, z) < radius * radius) {
                        ci.cancel();
                        return;
                    }
                }
            }
        }
    }

    @Inject(
            method = "addParticle(Lnet/minecraft/particle/ParticleEffect;ZDDDDDD)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onAddParticle8(
            ParticleEffect parameters,
            boolean alwaysSpawn,
            double x, double y, double z,
            double velocityX, double velocityY, double velocityZ,
            CallbackInfo ci
    ) {
        // Only act when screenshot mode is enabled
        if (parameters instanceof BlockStateParticleEffect effect) {
            var type = effect.getType();

            if (type == ParticleTypes.BLOCK || type == ParticleTypes.FALLING_DUST) {
                for (Entity entity : GroundSlammerClient.SUPPRESSED_ENTITIES) {
                    double radius = entity.getWidth() * 2.5;
                    if (entity.squaredDistanceTo(x, y, z) < radius * radius) {
                        ci.cancel();
                        return;
                    }
                }
            }
        }
    }
}