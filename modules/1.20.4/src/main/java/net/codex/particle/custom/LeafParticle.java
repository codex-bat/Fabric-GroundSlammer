// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.particle.custom;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

/**
 * Smooth "leaf" particle for 1.20.4 — tuned to be slightly farther out and much less spiny.
 */
public class LeafParticle extends SpriteBillboardParticle {

    private final SpriteProvider spriteProvider;

    // spawn origin (so the particle orbits relative to spawn)
    private final double spawnX;
    private final double spawnY;
    private final double spawnZ;

    // orbital parameters (per particle)
    private final double baseAngle;
    private final double orbitRadius;
    private final double verticalRadius;
    private final double spinMultiplier;
    private final float initialRotation;
    private final float rotationAmount;

    protected LeafParticle(ClientWorld world, double x, double y, double z,
                           double dummyVx, double dummyVy, double dummyVz,
                           SpriteProvider spriteProvider) {
        super(world, x, y, z, dummyVx, dummyVy, dummyVz);
        this.spriteProvider = spriteProvider;

        this.spawnX = x;
        this.spawnY = y;
        this.spawnZ = z;

        this.scale = 0.12f + random.nextFloat() * 0.14f;
        this.maxAge = 40 + random.nextInt(30);

        this.setBoundingBoxSpacing(0.05f, 0.05f);

        this.baseAngle = random.nextDouble() * Math.PI * 2.0;
        this.orbitRadius = 0.18 + random.nextDouble() * 0.95;
        this.verticalRadius = 0.30 + random.nextDouble() * 0.55;
        this.spinMultiplier = 0.00015 + random.nextDouble() * 0.00005;
        this.initialRotation = random.nextFloat() * 0.35f;
        this.rotationAmount = (random.nextFloat() - 0.0005f) * 0.5f;

        this.alpha = 1.0f;
        this.gravityStrength = 0.0f;

        this.setSprite(spriteProvider);
    }

    private static double easeInOutSine(double t) {
        return -(Math.cos(Math.PI * t) - 1.0) / 2.0;
    }

    private static double easeOutQuad(double t) {
        return 1 - (1 - t) * (1 - t);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.age >= this.maxAge) {
            this.markDead();
            return;
        }

        double t = (double) this.age / (double) this.maxAge;
        double eased = easeInOutSine(t);
        double radialEase = easeOutQuad(t);

        this.prevAngle = this.angle;
        this.angle = initialRotation + (float) (rotationAmount * eased);

        double orbitalAngle = baseAngle + spinMultiplier * 2.0 * Math.PI * eased;
        double quatreMod = 1.0 + 0.18 * Math.sin(4.0 * Math.PI * eased + baseAngle);
        double currentRadius = orbitRadius * (0.25 + 0.75 * radialEase) * quatreMod;

        double nx = spawnX + Math.cos(orbitalAngle) * currentRadius;
        double nz = spawnZ + Math.sin(orbitalAngle) * currentRadius;

        double ny = spawnY + Math.sin(t * Math.PI) * verticalRadius * 0.95;
        ny += 0.03 * easeOutQuad(t);

        this.setPos(nx, ny, nz);

        this.angle = initialRotation + (float) (rotationAmount * eased);

        double fadeStart = 0.65;
        if (t < fadeStart) {
            this.alpha = 1.0f;
        } else {
            double fadeT = (t - fadeStart) / (1.0 - fadeStart);
            this.alpha = (float) (1.0 - easeInOutSine(fadeT));
        }

        if (this.age >= this.maxAge) {
            this.markDead();
        }
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
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
                                       double vx, double vy, double vz) {
            return new LeafParticle(world, x, y, z, vx, vy, vz, spriteProvider);
        }
    }
}