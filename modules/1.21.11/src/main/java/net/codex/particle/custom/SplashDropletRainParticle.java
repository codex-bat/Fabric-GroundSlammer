// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.particle.custom;

import net.codex.particle.ModParticles;
import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.random.Random;

public class SplashDropletRainParticle extends BillboardParticle {
    private final SpriteProvider spriteProvider;

    private float amountMultiplier = 1.0f;

    private float rCol = 1.0f;
    private float gCol = 1.0f;
    private float bCol = 1.0f;

    protected SplashDropletRainParticle(ClientWorld world, double x, double y, double z,
                                        double vx, double vy, double vz,
                                        SpriteProvider spriteProvider) {
        super(world, x, y, z, vx, vy, vz, spriteProvider.getFirst());
        this.spriteProvider = spriteProvider;

        this.collidesWithWorld = true;
        this.gravityStrength = 0.35F;
        this.velocityMultiplier = 1F;

        this.scale = 0.05F;
        this.setColor(1.0F, 1.0F, 1.0F);

        this.maxAge = 200;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.onGround) {
            double landY = this.y + 0.01;

            this.world.addParticleClient(
                    new SplashDropletParticleEffect(
                            ModParticles.SPLASH_LANDING,
                            this.amountMultiplier,
                            1.0f,
                            this.rCol,
                            this.gCol,
                            this.bCol
                    ),
                    this.x, landY, this.z,
                    0.0, 0.0, 0.0
            );

            this.markDead();
            return;
        }

        if (this.y < this.world.getBottomY()) {
            this.markDead();
        }
    }

    @Override
    protected BillboardParticle.RenderType getRenderType() {
        return BillboardParticle.RenderType.PARTICLE_ATLAS_TRANSLUCENT;
    }

    public static class Factory implements ParticleFactory<SplashDropletParticleEffect> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(
                SplashDropletParticleEffect effect,
                ClientWorld world,
                double x, double y, double z,
                double vx, double vy, double vz,
                Random random
        ) {
            double upward = vy * effect.heightMultiplier;

            SplashDropletRainParticle particle = new SplashDropletRainParticle(
                    world, x, y, z, vx, upward, vz, this.spriteProvider
            );

            particle.amountMultiplier = effect.amountMultiplier;
            particle.rCol = effect.red;
            particle.gCol = effect.green;
            particle.bCol = effect.blue;
            particle.setColor(effect.red, effect.green, effect.blue);
            particle.scale *= effect.amountMultiplier;
            particle.maxAge = (int) (particle.maxAge * effect.amountMultiplier);

            return particle;
        }
    }
}