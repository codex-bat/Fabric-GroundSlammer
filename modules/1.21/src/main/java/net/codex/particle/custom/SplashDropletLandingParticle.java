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
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.util.math.MathHelper;

public class SplashDropletLandingParticle extends SpriteBillboardParticle {
    private final SpriteProvider spriteProvider;
    private Sprite currentSprite;

    private final int ticksPerFrame = 2;
    private final int maxFrames = 6;
    private final int minFramesIfDies = 3;
    private final int targetFrames;

    protected SplashDropletLandingParticle(
            ClientWorld world,
            double x, double y, double z,
            SpriteProvider spriteProvider,
            SplashDropletParticleEffect effect
    ) {
        super(world, x, y, z, 0, 0, 0);
        this.spriteProvider = spriteProvider;

        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;

        this.collidesWithWorld = false;
        this.gravityStrength = 0.0f;
        this.velocityMultiplier = 1.0f;

        this.scale = 0.75f * effect.amountMultiplier;
        this.setColor(effect.red, effect.green, effect.blue);

        this.maxAge = 12;

        if (this.random.nextFloat() < 0.80f) {
            this.targetFrames = this.maxFrames;
        } else {
            this.targetFrames = this.minFramesIfDies + this.random.nextInt(this.maxFrames - this.minFramesIfDies);
        }

        Sprite init = this.spriteProvider.getSprite(0, this.maxAge);
        if (init != null) {
            this.setSprite(init);
            this.currentSprite = init;
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

        Sprite s = this.spriteProvider.getSprite(this.age, this.maxAge);
        if (s != null) {
            this.setSprite(s);
            this.currentSprite = s;
        }
    }

    @Override
    public void buildGeometry(VertexConsumer vc, Camera camera, float tickDelta) {
        Sprite sprite = this.currentSprite;
        if (sprite == null) {
            sprite = this.spriteProvider.getSprite(0, this.maxAge);
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
        final int argb = ((int) (this.alpha * 255.0f) << 24)
                | ((int) (this.red * 255.0f) << 16)
                | ((int) (this.green * 255.0f) << 8)
                | ((int) (this.blue * 255.0f));

        vc.vertex(x1, yBottom, z1, argb, u1, v1, OverlayTexture.DEFAULT_UV, light, 0, 1, 0);
        vc.vertex(x2, yBottom, z1, argb, u2, v1, OverlayTexture.DEFAULT_UV, light, 0, 1, 0);
        vc.vertex(x2, yBottom, z2, argb, u2, v2, OverlayTexture.DEFAULT_UV, light, 0, 1, 0);
        vc.vertex(x1, yBottom, z2, argb, u1, v2, OverlayTexture.DEFAULT_UV, light, 0, 1, 0);

        vc.vertex(x2, yBottom, z1, argb, u2, v1, OverlayTexture.DEFAULT_UV, light, 0, 1, 0);
        vc.vertex(x1, yBottom, z1, argb, u1, v1, OverlayTexture.DEFAULT_UV, light, 0, 1, 0);
        vc.vertex(x1, yBottom, z2, argb, u1, v2, OverlayTexture.DEFAULT_UV, light, 0, 1, 0);
        vc.vertex(x2, yBottom, z2, argb, u2, v2, OverlayTexture.DEFAULT_UV, light, 0, 1, 0);
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static final class Factory implements ParticleFactory<SplashDropletParticleEffect> {
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
            return new SplashDropletLandingParticle(world, x, y, z, this.spriteProvider, effect);
        }
    }
}