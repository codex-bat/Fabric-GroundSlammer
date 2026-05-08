// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.particle.custom;

import net.minecraft.client.particle.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.MathHelper;

public class GroundSplashParticle extends SpriteBillboardParticle {
    private final SpriteProvider spriteProvider;
    private static final float BASE_SCALE = 1.25f;
    private final float sizeMultiplier;
    private final float heightMultiplier;

    private static final int SIDE_FRAMES = 10;
    private static final int BOTTOM_FRAMES = 10;

    private static final float OUTER_RADIUS_MULT = 1.6f;
    private static final float OUTER_HEIGHT_SCALE = 0.5f;

    private static final int OUTER_RISE_DELAY_TICKS = 2;
    private static final int OUTER_RISE_DURATION_TICKS = 4;
    private static final int OUTER_ANIM_DELAY_TICKS = OUTER_RISE_DELAY_TICKS;
    private static final float OUTER_FINAL_BOTTOM_OFFSET = 0.002f;

    public GroundSplashParticle(ClientWorld world,
                                double x, double y, double z,
                                GroundSplashParticleEffect effect,
                                SpriteProvider spriteProvider) {

        super(world, x, y, z, 0, 0, 0);
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

        this.setSprite(spriteProvider.getSprite(this.age, this.maxAge));

        if (this.age >= this.maxAge) this.markDead();
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

        final float ageWithDelta = this.age + tickDelta;
        final int outerAnimAge = Math.max(0, this.age - OUTER_ANIM_DELAY_TICKS);

        int outerSideIndex = (outerAnimAge * SIDE_FRAMES) / maxA;
        if (outerSideIndex < 0) outerSideIndex = 0;
        else if (outerSideIndex >= SIDE_FRAMES) outerSideIndex = SIDE_FRAMES - 1;
        int syntheticAgeForOuterSide = (outerSideIndex * this.maxAge) / Math.max(1, totalFrames - 1);
        Sprite outerSideSprite = this.spriteProvider.getSprite(syntheticAgeForOuterSide, this.maxAge);

        int outerBottomIndex = (outerAnimAge * BOTTOM_FRAMES) / maxA;
        if (outerBottomIndex < 0) outerBottomIndex = 0;
        else if (outerBottomIndex >= BOTTOM_FRAMES) outerBottomIndex = BOTTOM_FRAMES - 1;
        int syntheticAgeForOuterBottom = ((SIDE_FRAMES + outerBottomIndex) * this.maxAge) / Math.max(1, totalFrames - 1);
        Sprite outerBottomSprite = this.spriteProvider.getSprite(syntheticAgeForOuterBottom, this.maxAge);

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

        final float osu1 = outerSideSprite.getMinU();
        final float osu2 = outerSideSprite.getMaxU();
        final float osv1 = outerSideSprite.getMinV();
        final float osv2 = outerSideSprite.getMaxV();

        final int light = 0xF000F0;
        final int argb = toArgb(this.red, this.green, this.blue, this.alpha);

        emit(vc, x1, yBottom, z1, su1, sv2, argb, light);
        emit(vc, x2, yBottom, z1, su2, sv2, argb, light);
        emit(vc, x2, yTop,    z1, su2, sv1, argb, light);
        emit(vc, x1, yTop,    z1, su1, sv1, argb, light);

        emit(vc, x2, yBottom, z1, su2, sv2, argb, light);
        emit(vc, x1, yBottom, z1, su1, sv2, argb, light);
        emit(vc, x1, yTop,    z1, su1, sv1, argb, light);
        emit(vc, x2, yTop,    z1, su2, sv1, argb, light);

        emit(vc, x2, yBottom, z2, su1, sv2, argb, light);
        emit(vc, x1, yBottom, z2, su2, sv2, argb, light);
        emit(vc, x1, yTop,    z2, su2, sv1, argb, light);
        emit(vc, x2, yTop,    z2, su1, sv1, argb, light);

        emit(vc, x1, yBottom, z2, su1, sv2, argb, light);
        emit(vc, x2, yBottom, z2, su2, sv2, argb, light);
        emit(vc, x2, yTop,    z2, su2, sv1, argb, light);
        emit(vc, x1, yTop,    z2, su1, sv1, argb, light);

        emit(vc, x1, yBottom, z2, su1, sv2, argb, light);
        emit(vc, x1, yBottom, z1, su2, sv2, argb, light);
        emit(vc, x1, yTop,    z1, su2, sv1, argb, light);
        emit(vc, x1, yTop,    z2, su1, sv1, argb, light);

        emit(vc, x1, yBottom, z1, su1, sv2, argb, light);
        emit(vc, x1, yBottom, z2, su2, sv2, argb, light);
        emit(vc, x1, yTop,    z2, su2, sv1, argb, light);
        emit(vc, x1, yTop,    z1, su1, sv1, argb, light);

        emit(vc, x2, yBottom, z1, su1, sv2, argb, light);
        emit(vc, x2, yBottom, z2, su2, sv2, argb, light);
        emit(vc, x2, yTop,    z2, su2, sv1, argb, light);
        emit(vc, x2, yTop,    z1, su1, sv1, argb, light);

        emit(vc, x2, yBottom, z2, su2, sv2, argb, light);
        emit(vc, x2, yBottom, z1, su1, sv2, argb, light);
        emit(vc, x2, yTop,    z1, su1, sv1, argb, light);
        emit(vc, x2, yTop,    z2, su2, sv1, argb, light);

        final float outerHalf = half * OUTER_RADIUS_MULT;
        final float outerHeight = (half * 2.2f) * this.heightMultiplier * OUTER_HEIGHT_SCALE;
        final float ox1 = px - outerHalf;
        final float ox2 = px + outerHalf;
        final float oz1 = pz - outerHalf;
        final float oz2 = pz + outerHalf;

        final float finalOuterBottom = py + 0.01f + OUTER_FINAL_BOTTOM_OFFSET;
        final float finalOuterTop = py + outerHeight;

        final float startOuterBottom = finalOuterBottom - outerHeight;
        final float startOuterTop = finalOuterTop - outerHeight;

        final float riseStart = (float) OUTER_RISE_DELAY_TICKS;
        final float riseDur = Math.max(1.0f, (float) OUTER_RISE_DURATION_TICKS);
        final float riseProgress = MathHelper.clamp((ageWithDelta - riseStart) / riseDur, 0.0f, 1.0f);

        final float smooth = riseProgress * riseProgress * (3.0f - 2.0f * riseProgress);
        final float outerAlpha = this.alpha * smooth;

        final float yOuterBottom = MathHelper.lerp(riseProgress, startOuterBottom, finalOuterBottom);
        final float yOuterTop = MathHelper.lerp(riseProgress, startOuterTop, finalOuterTop);

        emit(vc, ox1, yOuterBottom, oz1, osu1, osv2, toArgb(this.red, this.green, this.blue, outerAlpha), light);
        emit(vc, ox2, yOuterBottom, oz1, osu2, osv2, toArgb(this.red, this.green, this.blue, outerAlpha), light);
        emit(vc, ox2, yOuterTop,    oz1, osu2, osv1, toArgb(this.red, this.green, this.blue, outerAlpha), light);
        emit(vc, ox1, yOuterTop,    oz1, osu1, osv1, toArgb(this.red, this.green, this.blue, outerAlpha), light);

        emit(vc, ox2, yOuterBottom, oz1, osu2, osv2, toArgb(this.red, this.green, this.blue, outerAlpha), light);
        emit(vc, ox1, yOuterBottom, oz1, osu1, osv2, toArgb(this.red, this.green, this.blue, outerAlpha), light);
        emit(vc, ox1, yOuterTop,    oz1, osu1, osv1, toArgb(this.red, this.green, this.blue, outerAlpha), light);
        emit(vc, ox2, yOuterTop,    oz1, osu2, osv1, toArgb(this.red, this.green, this.blue, outerAlpha), light);

        emit(vc, ox2, yOuterBottom, oz2, osu1, osv2, toArgb(this.red, this.green, this.blue, outerAlpha), light);
        emit(vc, ox1, yOuterBottom, oz2, osu2, osv2, toArgb(this.red, this.green, this.blue, outerAlpha), light);
        emit(vc, ox1, yOuterTop,    oz2, osu2, osv1, toArgb(this.red, this.green, this.blue, outerAlpha), light);
        emit(vc, ox2, yOuterTop,    oz2, osu1, osv1, toArgb(this.red, this.green, this.blue, outerAlpha), light);

        emit(vc, ox1, yOuterBottom, oz2, osu1, osv2, toArgb(this.red, this.green, this.blue, outerAlpha), light);
        emit(vc, ox2, yOuterBottom, oz2, osu2, osv2, toArgb(this.red, this.green, this.blue, outerAlpha), light);
        emit(vc, ox2, yOuterTop,    oz2, osu2, osv1, toArgb(this.red, this.green, this.blue, outerAlpha), light);
        emit(vc, ox1, yOuterTop,    oz2, osu1, osv1, toArgb(this.red, this.green, this.blue, outerAlpha), light);

        emit(vc, ox1, yOuterBottom, oz2, osu1, osv2, toArgb(this.red, this.green, this.blue, outerAlpha), light);
        emit(vc, ox1, yOuterBottom, oz1, osu2, osv2, toArgb(this.red, this.green, this.blue, outerAlpha), light);
        emit(vc, ox1, yOuterTop,    oz1, osu2, osv1, toArgb(this.red, this.green, this.blue, outerAlpha), light);
        emit(vc, ox1, yOuterTop,    oz2, osu1, osv1, toArgb(this.red, this.green, this.blue, outerAlpha), light);

        emit(vc, ox1, yOuterBottom, oz1, osu1, osv2, toArgb(this.red, this.green, this.blue, outerAlpha), light);
        emit(vc, ox1, yOuterBottom, oz2, osu2, osv2, toArgb(this.red, this.green, this.blue, outerAlpha), light);
        emit(vc, ox1, yOuterTop,    oz2, osu2, osv1, toArgb(this.red, this.green, this.blue, outerAlpha), light);
        emit(vc, ox1, yOuterTop,    oz1, osu1, osv1, toArgb(this.red, this.green, this.blue, outerAlpha), light);

        emit(vc, ox2, yOuterBottom, oz1, osu1, osv2, toArgb(this.red, this.green, this.blue, outerAlpha), light);
        emit(vc, ox2, yOuterBottom, oz2, osu2, osv2, toArgb(this.red, this.green, this.blue, outerAlpha), light);
        emit(vc, ox2, yOuterTop,    oz2, osu2, osv1, toArgb(this.red, this.green, this.blue, outerAlpha), light);
        emit(vc, ox2, yOuterTop,    oz1, osu1, osv1, toArgb(this.red, this.green, this.blue, outerAlpha), light);

        emit(vc, ox2, yOuterBottom, oz2, osu2, osv2, toArgb(this.red, this.green, this.blue, outerAlpha), light);
        emit(vc, ox2, yOuterBottom, oz1, osu1, osv2, toArgb(this.red, this.green, this.blue, outerAlpha), light);
        emit(vc, ox2, yOuterTop,    oz1, osu1, osv1, toArgb(this.red, this.green, this.blue, outerAlpha), light);
        emit(vc, ox2, yOuterTop,    oz2, osu2, osv1, toArgb(this.red, this.green, this.blue, outerAlpha), light);

        if (outerBottomSprite != null) {
            final float obu1 = outerBottomSprite.getMinU();
            final float obu2 = outerBottomSprite.getMaxU();
            final float obv1 = outerBottomSprite.getMinV();
            final float obv2 = outerBottomSprite.getMaxV();
            final int outerArgb = toArgb(this.red, this.green, this.blue, outerAlpha);

            emit(vc, ox1, yOuterBottom, oz1, obu1, obv1, outerArgb, light);
            emit(vc, ox2, yOuterBottom, oz1, obu2, obv1, outerArgb, light);
            emit(vc, ox2, yOuterBottom, oz2, obu2, obv2, outerArgb, light);
            emit(vc, ox1, yOuterBottom, oz2, obu1, obv2, outerArgb, light);

            emit(vc, ox2, yOuterBottom, oz1, obu2, obv1, outerArgb, light);
            emit(vc, ox1, yOuterBottom, oz1, obu1, obv1, outerArgb, light);
            emit(vc, ox1, yOuterBottom, oz2, obu1, obv2, outerArgb, light);
            emit(vc, ox2, yOuterBottom, oz2, obu2, obv2, outerArgb, light);
        }

        if (bottomSprite != null) {
            final float bu1 = bottomSprite.getMinU();
            final float bu2 = bottomSprite.getMaxU();
            final float bv1 = bottomSprite.getMinV();
            final float bv2 = bottomSprite.getMaxV();

            emit(vc, x1, yBottom, z1, bu1, bv1, argb, light);
            emit(vc, x2, yBottom, z1, bu2, bv1, argb, light);
            emit(vc, x2, yBottom, z2, bu2, bv2, argb, light);
            emit(vc, x1, yBottom, z2, bu1, bv2, argb, light);

            emit(vc, x2, yBottom, z1, bu2, bv1, argb, light);
            emit(vc, x1, yBottom, z1, bu1, bv1, argb, light);
            emit(vc, x1, yBottom, z2, bu1, bv2, argb, light);
            emit(vc, x2, yBottom, z2, bu2, bv2, argb, light);
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
            return new GroundSplashParticle(world, x, y, z, effect, spriteProvider);
        }
    }

    private static void emit(VertexConsumer vc,
                             float x, float y, float z,
                             float u, float v,
                             int argb, int light) {
        vc.vertex(x, y, z, argb, u, v, 0, light, 0.0f, 1.0f, 0.0f);
    }

    private static int toArgb(float r, float g, float b, float a) {
        int ia = MathHelper.clamp((int) (a * 255.0f), 0, 255);
        int ir = MathHelper.clamp((int) (r * 255.0f), 0, 255);
        int ig = MathHelper.clamp((int) (g * 255.0f), 0, 255);
        int ib = MathHelper.clamp((int) (b * 255.0f), 0, 255);
        return (ia << 24) | (ir << 16) | (ig << 8) | ib;
    }
}