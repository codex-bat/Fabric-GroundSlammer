// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.particle.custom;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;

/**
 * Rain variant of splash droplet that spawns a landing animation on impact,
 * and passes its colour to that landing particle.
 */
public class SplashDropletRainParticle extends SpriteBillboardParticle {

    // store provider so I can create the landing animation with the same atlas
    private final SpriteProvider spriteProvider;

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

        // When hit the ground, spawn the landing animation particle and die
        if (this.onGround) {
            double landY = this.y + 0.01; // or ps.spawnY + 0.01

            SplashDropletLandingParticle landing = new SplashDropletLandingParticle(
                    (ClientWorld) this.world,
                    this.x, landY, this.z,
                    this.spriteProvider,
                    this.rCol, this.gCol, this.bCol
            );

            // Add concrete Particle instance via particle manager
            MinecraftClient.getInstance().particleManager.addParticle(landing);

            this.markDead();
            return;
        }

        // Optional: also die if fall into the void (below world height)
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
            // Apply height multiplier to the vertical velocity
            double upward = vy * effect.heightMultiplier;

            SplashDropletRainParticle particle = new SplashDropletRainParticle(
                    world,
                    x, y, z,
                    vx,
                    upward,
                    vz,
                    spriteProvider
            );

            // Apply color from the effect (also store on the instance)
            particle.setColor(effect.red, effect.green, effect.blue);
            particle.rCol = effect.red;
            particle.gCol = effect.green;
            particle.bCol = effect.blue;

            // Apply scale based on amountMultiplier (optional: can also factor in entity size if you want)
            particle.scale *= effect.amountMultiplier;

            // Optional: scale lifetime by amountMultiplier to make bigger bursts last slightly longer
            particle.maxAge = (int) (particle.maxAge * effect.amountMultiplier);

            return particle;
        }
    }
}