// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.particle.custom;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

public class SplashDropletParticle extends SpriteBillboardParticle {

    protected SplashDropletParticle(ClientWorld world, double x, double y, double z,
                                    double vx, double vy, double vz,
                                    SpriteProvider spriteProvider) {
        super(world, x, y, z, vx, vy, vz);
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

        // Die when hitting the ground
        if (this.onGround) {
            this.markDead();
        }

        // Optional: also die if we fall into the void (below world height)
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

            SplashDropletParticle particle = new SplashDropletParticle(
                    world,
                    x, y, z,
                    vx,
                    upward,
                    vz,
                    spriteProvider
            );

            // Apply color from the effect
            particle.setColor(effect.red, effect.green, effect.blue);

            // Apply scale based on amountMultiplier (optional: can also factor in entity size if you want)
            particle.scale *= effect.amountMultiplier;

            // Optional: scale lifetime by amountMultiplier to make bigger bursts last slightly longer
            particle.maxAge = (int) (particle.maxAge * effect.amountMultiplier);

            return particle;
        }
    }
}