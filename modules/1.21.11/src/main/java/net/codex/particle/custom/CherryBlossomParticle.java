// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.particle.custom;

import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.random.Random;

public class CherryBlossomParticle extends BillboardParticle {
    private final double spawnX, spawnY, spawnZ;
    private final double baseAngle, orbitRadius, verticalRadius, spinMultiplier;
    private final float initialRotation, rotationAmount;

    protected CherryBlossomParticle(ClientWorld world, double x, double y, double z,
                                    double vx, double vy, double vz,
                                    Sprite sprite,
                                    CherryBlossomParticleEffect effect) {
        super(world, x, y, z, vx, vy, vz, sprite);

        this.spawnX = x;
        this.spawnY = y;
        this.spawnZ = z;

        this.scale = 0.08f + this.random.nextFloat() * 0.12f;
        this.maxAge = 30 + this.random.nextInt(25);

        this.setBoundingBoxSpacing(0.04f, 0.04f);

        this.baseAngle = this.random.nextDouble() * Math.PI * 2.0;
        this.orbitRadius = 0.12 + this.random.nextDouble() * 0.8;
        this.verticalRadius = 0.25 + this.random.nextDouble() * 0.5;
        this.spinMultiplier = 0.0002 + this.random.nextDouble() * 0.00006;
        this.initialRotation = this.random.nextFloat() * 0.35f;
        this.rotationAmount = (this.random.nextFloat() - 0.5f) * 0.6f;

        this.alpha = 1.0f;
        this.gravityStrength = 0.0f;

        this.setColor(effect.red, effect.green, effect.blue);
    }

    private static double easeInOutSine(double t) {
        return -(Math.cos(Math.PI * t) - 1.0) / 2.0;
    }

    private static double easeOutQuad(double t) {
        return 1.0 - (1.0 - t) * (1.0 - t);
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

        this.lastZRotation = this.zRotation;
        this.zRotation = this.initialRotation + (float) (this.rotationAmount * eased);

        double orbitalAngle = this.baseAngle + this.spinMultiplier * 2.0 * Math.PI * eased;
        double quatreMod = 1.0 + 0.18 * Math.sin(4.0 * Math.PI * eased + this.baseAngle);
        double currentRadius = this.orbitRadius * (0.25 + 0.75 * radialEase) * quatreMod;

        double nx = this.spawnX + Math.cos(orbitalAngle) * currentRadius;
        double nz = this.spawnZ + Math.sin(orbitalAngle) * currentRadius;

        double ny = this.spawnY + Math.sin(t * Math.PI) * this.verticalRadius * 0.95;
        ny += 0.03 * easeOutQuad(t);

        this.setPos(nx, ny, nz);

        double fadeStart = 0.65;
        if (t < fadeStart) {
            this.alpha = 1.0f;
        } else {
            double fadeT = (t - fadeStart) / (1.0 - fadeStart);
            this.alpha = (float) (1.0 - easeInOutSine(fadeT));
        }
    }

    @Override
    protected BillboardParticle.RenderType getRenderType() {
        return BillboardParticle.RenderType.PARTICLE_ATLAS_TRANSLUCENT;
    }

    public static final class Factory implements ParticleFactory<CherryBlossomParticleEffect> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(CherryBlossomParticleEffect effect,
                                       ClientWorld world,
                                       double x, double y, double z,
                                       double vx, double vy, double vz,
                                       Random random) {
            Sprite sprite = this.spriteProvider.getSprite(random);
            return new CherryBlossomParticle(world, x, y, z, vx, vy, vz, sprite, effect);
        }
    }
}