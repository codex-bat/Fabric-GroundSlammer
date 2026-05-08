// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.particle.custom;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;

public class SplashDropletParticle extends SpriteBillboardParticle {

    protected SplashDropletParticle(ClientWorld world, double x, double y, double z,
                                    double vx, double vy, double vz,
                                    SpriteProvider spriteProvider) {
        super(world, x, y, z, vx, vy, vz);
        this.setSprite(spriteProvider.getSprite(this.random));

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
            this.markDead();
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

            SplashDropletParticle particle = new SplashDropletParticle(
                    world,
                    x, y, z,
                    vx,
                    upward,
                    vz,
                    spriteProvider
            );

            particle.setColor(effect.red, effect.green, effect.blue);

            particle.scale *= effect.amountMultiplier;
            particle.maxAge = (int) (particle.maxAge * effect.amountMultiplier);

            return particle;
        }
    }
}