// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.particle.custom;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;

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
                           SpriteProvider spriteProvider,
                           float red, float green, float blue) {
        super(world, x, y, z);
        this.spriteProvider = spriteProvider;

        this.spawnX = x;
        this.spawnY = y;
        this.spawnZ = z;

        // Apply colour immediately
        this.red   = red;
        this.green = green;
        this.blue  = blue;

        // scale
        this.scale = 0.12f + random.nextFloat() * 0.14f;

        // life
        this.maxAge = 40 + random.nextInt(30);

        this.setBoundingBoxSpacing(0.05f, 0.05f);

        // ====== TUNABLES ======
        this.baseAngle = random.nextDouble() * Math.PI * 2.0;
        this.orbitRadius = 0.18 + random.nextDouble() * 0.95;
        this.verticalRadius = 0.30 + random.nextDouble() * 0.55;
        this.spinMultiplier = 0.00015 + random.nextDouble() * 0.00005;
        this.initialRotation = random.nextFloat() * 0.35f;
        this.rotationAmount = (random.nextFloat() - 0.0005f) * 0.5f;
        // =======================

        this.alpha = 1.0f;
        this.gravityStrength = 0.0f;

        // Pick a random sprite from the provider
        this.setSprite(spriteProvider.getSprite(world.random));
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

        // update previous angle so interpolation doesn't spin wildly
        this.prevAngle = this.angle;

        // orbital angle — slower, gentler progression
        double orbitalAngle = baseAngle + spinMultiplier * 2.0 * Math.PI * eased;

        // quatre-circle modulation — smaller amplitude for subtle lobing
        double quatreMod = 1.0 + 0.18 * Math.sin(4.0 * Math.PI * eased + baseAngle);

        // radial growth from spawn -> final orbitRadius
        double currentRadius = orbitRadius * (0.25 + 0.75 * radialEase) * quatreMod;

        double nx = spawnX + Math.cos(orbitalAngle) * currentRadius;
        double nz = spawnZ + Math.sin(orbitalAngle) * currentRadius;

        // hemisphere-like vertical movement with more lift
        double ny = spawnY + Math.sin(t * Math.PI) * verticalRadius * 0.95;
        ny += 0.03 * easeOutQuad(t);

        this.setPos(nx, ny, nz);

        // subtle sprite rotation (ease-in/ease-out)
        this.angle = initialRotation + (float) (rotationAmount * eased);

        // fade near end of life
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
            return new LeafParticle(world, x, y, z, vx, vy, vz,
                    spriteProvider,
                    effect.red, effect.green, effect.blue);
        }
    }
}