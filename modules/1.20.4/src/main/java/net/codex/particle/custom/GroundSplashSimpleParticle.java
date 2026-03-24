// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.particle.custom;

import net.minecraft.client.particle.*;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;

public class GroundSplashSimpleParticle extends SpriteBillboardParticle {
    private final SpriteProvider spriteProvider;
    private static final float BASE_SCALE = 1.25f;
    private final float sizeMultiplier;
    private final float heightMultiplier;

    private static final int SIDE_FRAMES = 10;
    private static final int BOTTOM_FRAMES = 10;

    public GroundSplashSimpleParticle(ClientWorld world,
                                      double x, double y, double z,
                                      GroundSplashParticleEffect effect,
                                      SpriteProvider spriteProvider) {
        super(world, x, y, z, 0.0, 0.0, 0.0);
        this.spriteProvider = spriteProvider;
        this.sizeMultiplier = effect.sizeMultiplier;
        this.heightMultiplier = effect.heightMultiplier;

        this.maxAge = 18 + this.random.nextInt(8);

        this.setSprite(spriteProvider.getSprite(this.random));
        this.gravityStrength = 0.0F;
        this.collidesWithWorld = false;
        this.velocityMultiplier = 0.0F;

        this.scale = BASE_SCALE * this.sizeMultiplier;
        this.alpha = 1.0F;

        this.setColor(effect.red, effect.green, effect.blue);
    }

    @Override
    public void tick() {
        super.tick();

        this.x = this.prevPosX;
        this.y = this.prevPosY;
        this.z = this.prevPosZ;

        this.setSpriteForAge(this.spriteProvider);

        if (this.age >= this.maxAge) {
            this.markDead();
        }
    }

    @Override
    public void buildGeometry(VertexConsumer vc, Camera camera, float tickDelta) {
        final int totalFrames = SIDE_FRAMES + BOTTOM_FRAMES;
        final int maxA = Math.max(1, this.maxAge);

        int sideIndex = (this.age * SIDE_FRAMES) / maxA;
        if (sideIndex < 0) sideIndex = 0;
        else if (sideIndex >= SIDE_FRAMES) sideIndex = SIDE_FRAMES - 1;
        int syntheticAgeForSide = (sideIndex * this.maxAge) / Math.max(1, totalFrames - 1);
        Sprite sideSprite = this.spriteProvider.getSprite(syntheticAgeForSide, this.maxAge);

        int bottomIndex = (this.age * BOTTOM_FRAMES) / maxA;
        if (bottomIndex < 0) bottomIndex = 0;
        else if (bottomIndex >= BOTTOM_FRAMES) bottomIndex = BOTTOM_FRAMES - 1;
        int syntheticAgeForBottom = ((SIDE_FRAMES + bottomIndex) * this.maxAge) / Math.max(1, totalFrames - 1);
        Sprite bottomSprite = this.spriteProvider.getSprite(syntheticAgeForBottom, this.maxAge);

        final float camX = (float) camera.getPos().x;
        final float camY = (float) camera.getPos().y;
        final float camZ = (float) camera.getPos().z;

        final float px = (float) (MathHelper.lerp(tickDelta, this.prevPosX, this.x) - camX);
        final float py = (float) (MathHelper.lerp(tickDelta, this.prevPosY, this.y) - camY);
        final float pz = (float) (MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - camZ);

        final float fullSize = this.getSize(tickDelta);
        final float half = fullSize * 0.5f;
        final float height = (half * 2.2f) * this.heightMultiplier;

        final float yBottom = py + 0.01f;
        final float yTop = py + height;
        final float x1 = px - half;
        final float x2 = px + half;
        final float z1 = pz - half;
        final float z2 = pz + half;

        final float su1 = sideSprite.getMinU();
        final float su2 = sideSprite.getMaxU();
        final float sv1 = sideSprite.getMinV();
        final float sv2 = sideSprite.getMaxV();

        final int light = 0xF000F0;
        final float r = this.red;
        final float g = this.green;
        final float b = this.blue;
        final float a = this.alpha;

        vc.vertex(x1, yBottom, z1).texture(su1, sv2).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yBottom, z1).texture(su2, sv2).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yTop, z1).texture(su2, sv1).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yTop, z1).texture(su1, sv1).color(r, g, b, a).light(light).next();

        vc.vertex(x2, yBottom, z1).texture(su2, sv2).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yBottom, z1).texture(su1, sv2).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yTop, z1).texture(su1, sv1).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yTop, z1).texture(su2, sv1).color(r, g, b, a).light(light).next();

        vc.vertex(x2, yBottom, z2).texture(su1, sv2).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yBottom, z2).texture(su2, sv2).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yTop, z2).texture(su2, sv1).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yTop, z2).texture(su1, sv1).color(r, g, b, a).light(light).next();

        vc.vertex(x1, yBottom, z2).texture(su1, sv2).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yBottom, z2).texture(su2, sv2).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yTop, z2).texture(su2, sv1).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yTop, z2).texture(su1, sv1).color(r, g, b, a).light(light).next();

        vc.vertex(x1, yBottom, z2).texture(su1, sv2).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yBottom, z1).texture(su2, sv2).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yTop, z1).texture(su2, sv1).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yTop, z2).texture(su1, sv1).color(r, g, b, a).light(light).next();

        vc.vertex(x1, yBottom, z1).texture(su1, sv2).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yBottom, z2).texture(su2, sv2).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yTop, z2).texture(su2, sv1).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yTop, z1).texture(su1, sv1).color(r, g, b, a).light(light).next();

        vc.vertex(x2, yBottom, z1).texture(su1, sv2).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yBottom, z2).texture(su2, sv2).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yTop, z2).texture(su2, sv1).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yTop, z1).texture(su1, sv1).color(r, g, b, a).light(light).next();

        vc.vertex(x2, yBottom, z2).texture(su2, sv2).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yBottom, z1).texture(su1, sv2).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yTop, z1).texture(su1, sv1).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yTop, z2).texture(su2, sv1).color(r, g, b, a).light(light).next();

        if (bottomSprite != null) {
            final float bu1 = bottomSprite.getMinU();
            final float bu2 = bottomSprite.getMaxU();
            final float bv1 = bottomSprite.getMinV();
            final float bv2 = bottomSprite.getMaxV();

            vc.vertex(x1, yBottom, z1).texture(bu1, bv1).color(r, g, b, a).light(light).next();
            vc.vertex(x2, yBottom, z1).texture(bu2, bv1).color(r, g, b, a).light(light).next();
            vc.vertex(x2, yBottom, z2).texture(bu2, bv2).color(r, g, b, a).light(light).next();
            vc.vertex(x1, yBottom, z2).texture(bu1, bv2).color(r, g, b, a).light(light).next();

            vc.vertex(x2, yBottom, z1).texture(bu2, bv1).color(r, g, b, a).light(light).next();
            vc.vertex(x1, yBottom, z1).texture(bu1, bv1).color(r, g, b, a).light(light).next();
            vc.vertex(x1, yBottom, z2).texture(bu1, bv2).color(r, g, b, a).light(light).next();
            vc.vertex(x2, yBottom, z2).texture(bu2, bv2).color(r, g, b, a).light(light).next();
        }
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static final class Factory implements ParticleFactory<GroundSplashParticleEffect> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(GroundSplashParticleEffect effect,
                                       ClientWorld world,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new GroundSplashSimpleParticle(world, x, y, z, effect, spriteProvider);
        }
    }
}