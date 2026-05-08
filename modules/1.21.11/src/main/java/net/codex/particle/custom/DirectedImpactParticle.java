// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.particle.custom;

import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import org.joml.Quaternionf;

public class DirectedImpactParticle extends BillboardParticle {
    private final SpriteProvider spriteProvider;
    private final float yaw;
    private final float pitch;
    private final float particleScale;
    private final int lifeMaxAge;

    private static final float LIFT = 0.01f;

    public DirectedImpactParticle(
            ClientWorld world,
            double x, double y, double z,
            DirectedImpactParticleEffect effect,
            SpriteProvider spriteProvider,
            Sprite sprite
    ) {
        super(world, x, y, z, 0.0, 0.0, 0.0, sprite);
        this.spriteProvider = spriteProvider;
        this.yaw = effect.yaw;
        this.pitch = effect.pitch;
        this.particleScale = effect.sizeMultiplier;
        this.lifeMaxAge = 5 + this.random.nextInt(4);

        this.setColor(effect.red, effect.green, effect.blue);
        this.alpha = 1.0F;

        this.velocityX = 0.0;
        this.velocityY = 0.0;
        this.velocityZ = 0.0;
        this.gravityStrength = 0.0F;
        this.collidesWithWorld = false;
        this.velocityMultiplier = 0.0F;
    }

    @Override
    public void tick() {
        this.lastX = this.x;
        this.lastY = this.y;
        this.lastZ = this.z;
        this.lastZRotation = this.zRotation;

        if (this.age++ >= this.lifeMaxAge) {
            this.markDead();
            return;
        }

        this.updateSprite(this.spriteProvider);

        final float ageFloat = Math.min((float) this.age, (float) this.lifeMaxAge);
        final float lifeNorm = MathHelper.clamp(ageFloat / Math.max(1.0f, (float) this.lifeMaxAge), 0.0f, 1.0f);

        this.alpha = 1.0f - lifeNorm;

        double t = (double) this.age / (double) this.lifeMaxAge;
        double eased = easeInOutSine(t);
        double radialEase = easeOutQuad(t);

        this.zRotation = (float) (this.particleScale * eased);

        double orbitalAngle = this.yaw + this.pitch * 0.15f + (0.25 * Math.PI * eased);
        double currentRadius = 0.01 + (0.02 * radialEase);

        double nx = this.x + Math.cos(orbitalAngle) * currentRadius;
        double nz = this.z + Math.sin(orbitalAngle) * currentRadius;

        double ny = this.y + LIFT + (0.02 * easeOutQuad(t));
        this.setPos(nx, ny, nz);
    }

    @Override
    public float getSize(float tickProgress) {
        final float ageFloat = Math.min((float) this.age + tickProgress, (float) this.lifeMaxAge);
        final float lifeNorm = MathHelper.clamp(ageFloat / Math.max(1.0f, (float) this.lifeMaxAge), 0.0f, 1.0f);
        return this.particleScale * (1.0f + (2.0f * lifeNorm));
    }

    @Override
    protected BillboardParticle.RenderType getRenderType() {
        return BillboardParticle.RenderType.PARTICLE_ATLAS_TRANSLUCENT;
    }

    @Override
    public BillboardParticle.Rotator getRotator() {
        return (quaternion, camera, tickDelta) -> quaternion.rotateY(this.yaw).rotateX(this.pitch);
    }

    private static double easeInOutSine(double t) {
        return -(Math.cos(Math.PI * t) - 1.0) / 2.0;
    }

    private static double easeOutQuad(double t) {
        return 1.0 - (1.0 - t) * (1.0 - t);
    }

    public static final class Factory implements ParticleFactory<DirectedImpactParticleEffect> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(
                DirectedImpactParticleEffect effect,
                ClientWorld world,
                double x, double y, double z,
                double vx, double vy, double vz,
                Random random
        ) {
            return new DirectedImpactParticle(
                    world,
                    x, y, z,
                    effect,
                    this.spriteProvider,
                    this.spriteProvider.getSprite(random)
            );
        }
    }
}