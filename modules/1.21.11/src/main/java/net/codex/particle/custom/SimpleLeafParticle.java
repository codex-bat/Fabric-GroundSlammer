// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.particle.custom;

import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.random.Random;

/**
 * Smooth "leaf" particle for 1.21.11 — tuned to be slightly farther out and much less spiny.
 */
public class SimpleLeafParticle extends BillboardParticle {

    private final SpriteProvider spriteProvider;

    private final double spawnX;
    private final double spawnY;
    private final double spawnZ;

    private final double baseAngle;
    private final double orbitRadius;
    private final double verticalRadius;
    private final double spinMultiplier;
    private final float initialRotation;
    private final float rotationAmount;

    protected SimpleLeafParticle(ClientWorld world, double x, double y, double z,
                                 double dummyVx, double dummyVy, double dummyVz,
                                 SpriteProvider spriteProvider) {
        super(world, x, y, z, dummyVx, dummyVy, dummyVz, spriteProvider.getFirst());
        this.spriteProvider = spriteProvider;

        this.spawnX = x;
        this.spawnY = y;
        this.spawnZ = z;

        this.scale = 0.12f + this.random.nextFloat() * 0.14f;
        this.maxAge = 40 + this.random.nextInt(30);

        this.setBoundingBoxSpacing(0.05f, 0.05f);

        this.baseAngle = this.random.nextDouble() * Math.PI * 2.0;
        this.orbitRadius = 0.18 + this.random.nextDouble() * 0.95;
        this.verticalRadius = 0.30 + this.random.nextDouble() * 0.55;
        this.spinMultiplier = 0.00015 + this.random.nextDouble() * 0.00005;
        this.initialRotation = this.random.nextFloat() * 0.35f;
        this.rotationAmount = (this.random.nextFloat() - 0.0005f) * 0.5f;

        this.alpha = 1.0f;
        this.gravityStrength = 0.0f;

        this.updateSprite(this.spriteProvider);
    }

    private static double easeInOutSine(double t) {
        return -(Math.cos(Math.PI * t) - 1.0) / 2.0;
    }

    private static double easeOutQuad(double t) {
        return 1.0 - (1.0 - t) * (1.0 - t);
    }

    @Override
    public void tick() {
        this.lastX = this.x;
        this.lastY = this.y;
        this.lastZ = this.z;
        this.lastZRotation = this.zRotation;

        if (this.age++ >= this.maxAge) {
            this.markDead();
            return;
        }

        final double t = (double) this.age / (double) this.maxAge;
        final double eased = easeInOutSine(t);
        final double radialEase = easeOutQuad(t);

        final double orbitalAngle = this.baseAngle + this.spinMultiplier * 2.0 * Math.PI * eased;
        final double quatreMod = 1.0 + 0.18 * Math.sin(4.0 * Math.PI * eased + this.baseAngle);
        final double currentRadius = this.orbitRadius * (0.25 + 0.75 * radialEase) * quatreMod;

        final double nx = this.spawnX + Math.cos(orbitalAngle) * currentRadius;
        final double nz = this.spawnZ + Math.sin(orbitalAngle) * currentRadius;

        double ny = this.spawnY + Math.sin(t * Math.PI) * this.verticalRadius * 0.95;
        ny += 0.03 * easeOutQuad(t);

        this.setPos(nx, ny, nz);

        this.zRotation = this.initialRotation + (float) (this.rotationAmount * eased);

        final double fadeStart = 0.65;
        if (t < fadeStart) {
            this.alpha = 1.0f;
        } else {
            final double fadeT = (t - fadeStart) / (1.0 - fadeStart);
            this.alpha = (float) (1.0 - easeInOutSine(fadeT));
        }
    }

    @Override
    public float getSize(float tickProgress) {
        return this.scale;
    }

    @Override
    protected BillboardParticle.RenderType getRenderType() {
        return BillboardParticle.RenderType.PARTICLE_ATLAS_TRANSLUCENT;
    }

    public static final class Factory implements ParticleFactory<LeafParticleEffect> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(LeafParticleEffect effect,
                                       ClientWorld world,
                                       double x, double y, double z,
                                       double vx, double vy, double vz,
                                       Random random) {
            return new SimpleLeafParticle(world, x, y, z, vx, vy, vz, this.spriteProvider);
        }
    }
}