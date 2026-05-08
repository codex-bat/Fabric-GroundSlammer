// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.particle.custom;

import net.codex.particle.ModParticles;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;

/**
 * Rain variant of splash droplet that spawns a landing animation on impact,
 * and passes its colour to that landing particle.
 */
public class SplashDropletRainParticle extends SpriteBillboardParticle {
    // store provider so I can create the landing animation with the same atlas
    private final SpriteProvider spriteProvider;

    private float amountMultiplier = 1.0f;

    // store the current colour so landing particle can inherit it
    private float rCol = 1.0f;
    private float gCol = 1.0f;
    private float bCol = 1.0f;

    protected SplashDropletRainParticle(ClientWorld world, double x, double y, double z,
                                        double vx, double vy, double vz,
                                        SpriteProvider spriteProvider) {
        super(world, x, y, z, vx, vy, vz);
        this.spriteProvider = spriteProvider;
        this.setSprite(spriteProvider.getSprite(this.random));

        // Enable block collision
        this.collidesWithWorld = true;

        // Physics
        this.gravityStrength = 0.35F;
        this.velocityMultiplier = 1F;

        // Size (tiny pixel)
        this.scale = 0.05F;
        this.setColor(1.0F, 1.0F, 1.0F);

        // Set a long lifetime as safety net (e.g., 10 seconds = 200 ticks)
        this.maxAge = 200;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.onGround) {
            double landY = this.y + 0.01;

            this.world.addParticle(
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
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
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
                double vx, double vy, double vz
        ) {
            double upward = vy * effect.heightMultiplier;

            SplashDropletRainParticle particle = new SplashDropletRainParticle(
                    world, x, y, z, vx, upward, vz, spriteProvider
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