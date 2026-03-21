// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.listener;

import net.codex.particle.ModParticles;
import net.codex.particle.custom.DirectedImpactParticleEffect;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

public final class ElytraImpactListener {
    private static final int COOLDOWN_TICKS = 20;
    private static final double SPEED_THRESHOLD = 1.5;
    private static final float PARTICLE_SIZE = 1.0f;

    private static int cooldown = 0;

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client == null || client.world == null || client.player == null) return;

            if (cooldown > 0) {
                cooldown--;
                return;
            }

            PlayerEntity player = client.player;
            if (!player.isFallFlying()) return;

            double speed = player.getVelocity().horizontalLength();
            if (speed >= SPEED_THRESHOLD) {
                spawnImpactParticle(client, player);
                cooldown = COOLDOWN_TICKS;
            }
        });
    }

    private static void spawnImpactParticle(MinecraftClient client, PlayerEntity player) {
        // Use player's actual look direction (yaw and pitch in radians)
        float yaw = (float) Math.toRadians(player.getYaw());
        float pitch = (float) Math.toRadians(player.getPitch());

        // Color based on speed? Keep white for now.
        float r = 1.0f, g = 1.0f, b = 1.0f;

        DirectedImpactParticleEffect effect = new DirectedImpactParticleEffect(r, g, b, PARTICLE_SIZE, yaw, pitch);

        if (client.world != null) {
            // Spawn slightly behind the player, at chest height
            Vec3d back = player.getRotationVec(1.0f).multiply(-0.5);
            client.world.addParticle(effect,
                    player.getX() + back.x,
                    player.getY() + player.getHeight() * 0.5,
                    player.getZ() + back.z,
                    0, 0, 0);
        }
    }
}