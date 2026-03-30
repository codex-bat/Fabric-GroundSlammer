// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.elytra;

import net.codex.particle.custom.DirectedImpactParticleEffect;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayDeque;
import java.util.Deque;

public class ElytraTrailManager {
    private static final int MAX_HISTORY = 200;           // about 10 seconds at 20 ticks/sec
    private static final float PARTICLE_SPACING = 0.5F;   // spawn a particle every 0.5 blocks
    private static final float TRAIL_AGE_SECONDS = 2.0F;  // trail length in seconds

    private final Deque<Entry> positions = new ArrayDeque<>();
    private int lastTick;

    private record Entry(Vec3d pos, Vec3d direction, long time) {}

    public void update(PlayerEntity player) {
        // Only store positions when flying fast enough (same condition as your listener)
        if (!player.isFallFlying() || player.getVelocity().horizontalLength() < 1.5) return;

        long now = player.getWorld().getTime();
        // Avoid duplicate entries in the same tick (can happen if you call this multiple times)
        if (now == lastTick) return;
        lastTick = (int) now;

        // Use velocity direction for orientation (more natural for a trail)
        Vec3d dir = player.getVelocity().normalize();
        positions.addFirst(new Entry(player.getPos(), dir, now));

        // Remove entries older than TRAIL_AGE_SECONDS
        long cutoff = now - (long)(TRAIL_AGE_SECONDS * 20);
        while (!positions.isEmpty() && positions.getLast().time() < cutoff) {
            positions.removeLast();
        }
    }

    // Called each tick to spawn particles along the stored path
    public void spawnTrailParticles(ClientWorld world) {
        if (positions.size() < 2) return;

        Entry prev = null;
        for (Entry curr : positions) {
            if (prev != null) {
                // Spawn particles along the segment from prev to curr
                Vec3d from = prev.pos();
                Vec3d to = curr.pos();
                double distance = from.distanceTo(to);
                if (distance > PARTICLE_SPACING) {
                    int steps = (int) Math.ceil(distance / PARTICLE_SPACING);
                    for (int i = 1; i <= steps; i++) {
                        double t = (double) i / steps;
                        Vec3d pos = from.lerp(to, t);
                        // Interpolate direction as well (spherical linear interpolation could be better, but linear works)
                        Vec3d dir = prev.direction().lerp(curr.direction(), t).normalize();
                        // Convert direction to yaw/pitch for your particle
                        float yaw = (float) Math.atan2(dir.z, dir.x);
                        float pitch = (float) Math.asin(dir.y);
                        // Scale particle by distance from player (further = smaller)
                        float ageFactor = 1.0f - (float) (world.getTime() - curr.time()) / (20 * TRAIL_AGE_SECONDS);
                        float size = 0.5f * Math.max(0.2f, ageFactor); // adjust as needed
                        // Color based on speed or fade out
                        float r = 1.0f, g = 1.0f, b = 1.0f;
                        DirectedImpactParticleEffect effect = new DirectedImpactParticleEffect(r, g, b, size, yaw, pitch);
                        world.addParticle(effect, pos.x, pos.y, pos.z, 0, 0, 0);
                    }
                } else {
                    // If the segment is shorter than spacing, just spawn a particle at the start
                    spawnParticleAt(world, prev.pos(), prev.direction(), ageFactor(world.getTime(), curr.time()));
                }
            }
            prev = curr;
        }
    }

    private float ageFactor(long now, long birth) {
        return 1.0f - (float)(now - birth) / (20 * TRAIL_AGE_SECONDS);
    }

    private void spawnParticleAt(ClientWorld world, Vec3d pos, Vec3d dir, float ageFactor) {
        float yaw = (float) Math.atan2(dir.z, dir.x);
        float pitch = (float) Math.asin(dir.y);
        float size = 0.5f * Math.max(0.2f, ageFactor);
        DirectedImpactParticleEffect effect = new DirectedImpactParticleEffect(1.0f, 1.0f, 1.0f, size, yaw, pitch);
        world.addParticle(effect, pos.x, pos.y, pos.z, 0, 0, 0);
    }
}
