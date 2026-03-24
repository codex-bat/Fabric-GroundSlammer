// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.particle.custom;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

import java.lang.reflect.Method;

/**
 * Robust landing animation particle that supports multiple SpriteProvider APIs via reflection.
 */
public class SplashDropletLandingParticle extends SpriteBillboardParticle {

    private final SpriteProvider spriteProvider;
    private Sprite currentSprite;

    private final int ticksPerFrame;
    private final int targetFrames;
    private final int maxFrames = 6;
    private final int minFramesIfDies = 3;

    private static Method provider_getSprite_age_max = null;
    private static Method provider_getSprite_index = null;
    private static Method provider_getSprite_random = null;
    private static boolean providerMethodsResolved = false;

    protected SplashDropletLandingParticle(ClientWorld world, double x, double y, double z,
                                           SpriteProvider spriteProvider,
                                           float r, float g, float b) {
        super(world, x, y, z, 0, 0, 0);
        this.spriteProvider = spriteProvider;

        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;

        this.collidesWithWorld = false;
        this.gravityStrength = 0.0f;
        this.velocityMultiplier = 1.0f;
        this.scale = 0.12f;
        this.setColor(r, g, b);

        this.ticksPerFrame = 2;

        if (this.random.nextFloat() < 0.80f) {
            this.targetFrames = this.maxFrames;
        } else {
            this.targetFrames = this.minFramesIfDies + this.random.nextInt(this.maxFrames - this.minFramesIfDies);
        }

        this.maxAge = this.targetFrames * this.ticksPerFrame + 1;

        resolveProviderMethods();

        int initSynthetic = MathHelper.clamp(0, 0, Math.max(0, this.maxAge - 1));
        Sprite init = getSpriteForIndex(initSynthetic, this.maxAge, 0);
        if (init != null) {
            this.setSprite(init);
            this.currentSprite = init;
        } else {
            try {
                Sprite fallback = this.spriteProvider.getSprite(this.random);
                if (fallback != null) {
                    this.setSprite(fallback);
                    this.currentSprite = fallback;
                }
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    public void tick() {
        if (this.age++ >= this.maxAge) {
            this.markDead();
            return;
        }

        this.x = this.prevPosX;
        this.y = this.prevPosY;
        this.z = this.prevPosZ;

        int frameIndex = Math.min(this.age / this.ticksPerFrame, this.targetFrames - 1);
        int syntheticAgeForFrame = (frameIndex * this.maxAge) / Math.max(1, this.targetFrames - 1);
        syntheticAgeForFrame = MathHelper.clamp(syntheticAgeForFrame, 0, Math.max(0, this.maxAge - 1));

        Sprite s = getSpriteForIndex(syntheticAgeForFrame, this.maxAge, frameIndex);
        if (s != null) {
            this.setSprite(s);
            this.currentSprite = s;
        }
    }

    @Override
    public void buildGeometry(VertexConsumer vc, Camera camera, float tickDelta) {
        Sprite sprite = this.currentSprite;
        if (sprite == null) {
            int fallbackSynthetic = MathHelper.clamp(0, 0, Math.max(0, this.maxAge - 1));
            sprite = getSpriteForIndex(fallbackSynthetic, this.maxAge, 0);
            if (sprite == null) return;
        }

        final float camX = (float) camera.getPos().x;
        final float camY = (float) camera.getPos().y;
        final float camZ = (float) camera.getPos().z;

        final float px = (float) (MathHelper.lerp(tickDelta, this.prevPosX, this.x) - camX);
        final float py = (float) (MathHelper.lerp(tickDelta, this.prevPosY, this.y) - camY);
        final float pz = (float) (MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - camZ);

        final float fullSize = this.getSize(tickDelta);
        final float half = fullSize * 0.5f;

        final float yBottom = py + 0.01f;
        final float x1 = px - half;
        final float x2 = px + half;
        final float z1 = pz - half;
        final float z2 = pz + half;

        final float u1 = sprite.getMinU();
        final float u2 = sprite.getMaxU();
        final float v1 = sprite.getMinV();
        final float v2 = sprite.getMaxV();

        final int light = 0xF000F0;
        final float r = this.red;
        final float g = this.green;
        final float b = this.blue;
        final float a = this.alpha;

        vc.vertex(x1, yBottom, z1).texture(u1, v1).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yBottom, z1).texture(u2, v1).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yBottom, z2).texture(u2, v2).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yBottom, z2).texture(u1, v2).color(r, g, b, a).light(light).next();

        vc.vertex(x2, yBottom, z1).texture(u2, v1).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yBottom, z1).texture(u1, v1).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yBottom, z2).texture(u1, v2).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yBottom, z2).texture(u2, v2).color(r, g, b, a).light(light).next();
    }

    @Override
    public net.minecraft.client.particle.ParticleTextureSheet getType() {
        return net.minecraft.client.particle.ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    private static synchronized void resolveProviderMethods() {
        if (providerMethodsResolved) return;
        providerMethodsResolved = true;

        try {
            provider_getSprite_age_max = SpriteProvider.class.getMethod("getSprite", int.class, int.class);
        } catch (NoSuchMethodException ignored) {
            provider_getSprite_age_max = null;
        }

        try {
            provider_getSprite_index = SpriteProvider.class.getMethod("getSprite", int.class);
        } catch (NoSuchMethodException ignored) {
            provider_getSprite_index = null;
        }

        try {
            provider_getSprite_random = SpriteProvider.class.getMethod("getSprite", Random.class);
        } catch (NoSuchMethodException ignored) {
            provider_getSprite_random = null;
        }
    }

    private Sprite getSpriteForIndex(int syntheticAge, int maxAge, int frameIndex) {
        try {
            if (provider_getSprite_age_max != null) {
                Object o = provider_getSprite_age_max.invoke(this.spriteProvider, syntheticAge, maxAge);
                if (o instanceof Sprite) return (Sprite) o;
            }
        } catch (Throwable ignored) {
        }

        try {
            if (provider_getSprite_index != null) {
                int providerIndex;
                if (this.targetFrames <= 1) {
                    providerIndex = 0;
                } else {
                    double ratio = (double) frameIndex / (double) Math.max(1, (this.targetFrames - 1));
                    providerIndex = (int) Math.round(ratio * (this.maxFrames - 1));
                }
                providerIndex = MathHelper.clamp(providerIndex, 0, this.maxFrames - 1);

                Object o = provider_getSprite_index.invoke(this.spriteProvider, providerIndex);
                if (o instanceof Sprite) return (Sprite) o;
            }
        } catch (Throwable ignored) {
        }

        try {
            if (provider_getSprite_random != null) {
                Random seeded = Random.create(syntheticAge * 31L + maxAge);
                Object o = provider_getSprite_random.invoke(this.spriteProvider, seeded);
                if (o instanceof Sprite) return (Sprite) o;
            }
        } catch (Throwable ignored) {
        }

        try {
            return this.spriteProvider.getSprite(this.random);
        } catch (Throwable ignored) {
        }

        return null;
    }

    public static final class Factory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(DefaultParticleType data, ClientWorld world,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new SplashDropletLandingParticle(world, x, y, z, this.spriteProvider, 1f, 1f, 1f);
        }
    }
}