// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.particle.custom;

import net.codex.particle.custom.pipeline.CuboidRenderable;
import net.codex.particle.custom.pipeline.CuboidParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public final class GroundSplashParticle extends CuboidParticle {
    public static final ParticleTextureSheet SHEET = new ParticleTextureSheet("ground_splash");

    private static final float BASE_SCALE = 1.25f;
    private static final int SIDE_FRAMES = 10;
    private static final int BOTTOM_FRAMES = 10;

    private static final float OUTER_RADIUS_MULT = 1.6f;
    private static final float OUTER_HEIGHT_SCALE = 0.5f;

    private static final int OUTER_RISE_DELAY_TICKS = 2;
    private static final int OUTER_RISE_DURATION_TICKS = 4;
    private static final int OUTER_ANIM_DELAY_TICKS = OUTER_RISE_DELAY_TICKS;
    private static final float OUTER_FINAL_BOTTOM_OFFSET = 0.002f;

    private final SpriteProvider spriteProvider;
    private final float sizeMultiplier;
    private final float heightMultiplier;

    private float red = 1.0f;
    private float green = 1.0f;
    private float blue = 1.0f;
    private float alpha = 1.0f;

    private float scale = BASE_SCALE;

    public GroundSplashParticle(ClientWorld world, double x, double y, double z,
                                GroundSplashParticleEffect effect,
                                SpriteProvider spriteProvider) {
        super(world, x, y, z);
        this.spriteProvider = spriteProvider;
        this.sizeMultiplier = effect.sizeMultiplier;
        this.heightMultiplier = effect.heightMultiplier;

        this.maxAge = 18 + this.random.nextInt(8);
        this.gravityStrength = 0.0F;
        this.collidesWithWorld = false;
        this.velocityMultiplier = 0.0F;

        this.red = effect.red;
        this.green = effect.green;
        this.blue = effect.blue;

        this.scale = BASE_SCALE * this.sizeMultiplier;
    }

    @Override
    public void tick() {
        this.lastX = this.x;
        this.lastY = this.y;
        this.lastZ = this.z;

        if (this.age++ >= this.maxAge) {
            this.markDead();
            return;
        }
    }

    @Override
    public CuboidRenderable captureRenderState(Camera camera, float tickDelta) {
        Vec3d cam = camera.getCameraPos();

        int totalFrames = SIDE_FRAMES + BOTTOM_FRAMES;
        int maxA = Math.max(1, this.maxAge);

        int sideIndex = MathHelper.clamp((this.age * SIDE_FRAMES) / maxA, 0, SIDE_FRAMES - 1);
        int syntheticAgeForSide = (sideIndex * this.maxAge) / Math.max(1, totalFrames - 1);
        Sprite sideSprite = this.spriteProvider.getSprite(syntheticAgeForSide, this.maxAge);

        int bottomIndex = MathHelper.clamp((this.age * BOTTOM_FRAMES) / maxA, 0, BOTTOM_FRAMES - 1);
        int syntheticAgeForBottom = ((SIDE_FRAMES + bottomIndex) * this.maxAge) / Math.max(1, totalFrames - 1);
        Sprite bottomSprite = this.spriteProvider.getSprite(syntheticAgeForBottom, this.maxAge);

        int outerAnimAge = Math.max(0, this.age - OUTER_ANIM_DELAY_TICKS);

        int outerSideIndex = MathHelper.clamp((outerAnimAge * SIDE_FRAMES) / maxA, 0, SIDE_FRAMES - 1);
        int syntheticAgeForOuterSide = (outerSideIndex * this.maxAge) / Math.max(1, totalFrames - 1);
        Sprite outerSideSprite = this.spriteProvider.getSprite(syntheticAgeForOuterSide, this.maxAge);

        int outerBottomIndex = MathHelper.clamp((outerAnimAge * BOTTOM_FRAMES) / maxA, 0, BOTTOM_FRAMES - 1);
        int syntheticAgeForOuterBottom = ((SIDE_FRAMES + outerBottomIndex) * this.maxAge) / Math.max(1, totalFrames - 1);
        Sprite outerBottomSprite = this.spriteProvider.getSprite(syntheticAgeForOuterBottom, this.maxAge);

        float px = (float) (MathHelper.lerp(tickDelta, this.lastX, this.x) - cam.x);
        float py = (float) (MathHelper.lerp(tickDelta, this.lastY, this.y) - cam.y);
        float pz = (float) (MathHelper.lerp(tickDelta, this.lastZ, this.z) - cam.z);

        return new RenderState(
                px, py, pz,
                this.age, this.maxAge, this.age + tickDelta,
                this.getSize(tickDelta), this.heightMultiplier,
                this.red, this.green, this.blue, this.alpha,
                this.getBrightness(tickDelta),
                sideSprite, bottomSprite, outerSideSprite, outerBottomSprite
        );
    }

    public float getSize(float tickDelta) {
        return this.scale;
    }

    public record RenderState(
            float px, float py, float pz,
            int age, int maxAge, float ageWithDelta,
            float size, float heightMultiplier,
            float red, float green, float blue, float alpha,
            int light, Sprite sideSprite, Sprite bottomSprite,
            Sprite outerSideSprite, Sprite outerBottomSprite
    ) implements CuboidRenderable {

        @Override
        public void emit(VertexConsumer vc) {
            final int argb = ColorHelper.fromFloats(alpha, red, green, blue);

            final float half = size * 0.5f;
            final float height = (half * 2.2f) * heightMultiplier;

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

            emitDoubleSidedFace(vc,
                    x1, yBottom, z1, su1, sv2,
                    x2, yBottom, z1, su2, sv2,
                    x2, yTop,    z1, su2, sv1,
                    x1, yTop,    z1, su1, sv1,
                    argb, light,
                    0.0f, 0.0f, -1.0f);

            emitDoubleSidedFace(vc,
                    x2, yBottom, z2, su1, sv2,
                    x1, yBottom, z2, su2, sv2,
                    x1, yTop,    z2, su2, sv1,
                    x2, yTop,    z2, su1, sv1,
                    argb, light,
                    0.0f, 0.0f, 1.0f);

            emitDoubleSidedFace(vc,
                    x1, yBottom, z2, su1, sv2,
                    x1, yBottom, z1, su2, sv2,
                    x1, yTop,    z1, su2, sv1,
                    x1, yTop,    z2, su1, sv1,
                    argb, light,
                    -1.0f, 0.0f, 0.0f);

            emitDoubleSidedFace(vc,
                    x2, yBottom, z1, su1, sv2,
                    x2, yBottom, z2, su2, sv2,
                    x2, yTop,    z2, su2, sv1,
                    x2, yTop,    z1, su1, sv1,
                    argb, light,
                    1.0f, 0.0f, 0.0f);

            if (bottomSprite != null) {
                final float bu1 = bottomSprite.getMinU();
                final float bu2 = bottomSprite.getMaxU();
                final float bv1 = bottomSprite.getMinV();
                final float bv2 = bottomSprite.getMaxV();

                emitDoubleSidedFace(vc,
                        x1, yBottom, z1, bu1, bv1,
                        x2, yBottom, z1, bu2, bv1,
                        x2, yBottom, z2, bu2, bv2,
                        x1, yBottom, z2, bu1, bv2,
                        argb, light,
                        0.0f, -1.0f, 0.0f);
            }

            final float ageWithDeltaLocal = ageWithDelta;
            final int outerAnimAge = Math.max(0, age - OUTER_ANIM_DELAY_TICKS);

            final float outerHalf = half * OUTER_RADIUS_MULT;
            final float outerHeight = (half * 2.2f) * heightMultiplier * OUTER_HEIGHT_SCALE;
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
            final float riseProgress = MathHelper.clamp((ageWithDeltaLocal - riseStart) / riseDur, 0.0f, 1.0f);
            final float smooth = riseProgress * riseProgress * (3.0f - 2.0f * riseProgress);
            final float outerAlpha = alpha * smooth;
            final int outerArgb = ColorHelper.fromFloats(outerAlpha, red, green, blue);

            final float yOuterBottom = MathHelper.lerp(riseProgress, startOuterBottom, finalOuterBottom);
            final float yOuterTop = MathHelper.lerp(riseProgress, startOuterTop, finalOuterTop);

            final float osu1 = outerSideSprite.getMinU();
            final float osu2 = outerSideSprite.getMaxU();
            final float osv1 = outerSideSprite.getMinV();
            final float osv2 = outerSideSprite.getMaxV();

            emitDoubleSidedFace(vc,
                    ox1, yOuterBottom, oz1, osu1, osv2,
                    ox2, yOuterBottom, oz1, osu2, osv2,
                    ox2, yOuterTop,    oz1, osu2, osv1,
                    ox1, yOuterTop,    oz1, osu1, osv1,
                    outerArgb, light,
                    0.0f, 0.0f, -1.0f);

            emitDoubleSidedFace(vc,
                    ox2, yOuterBottom, oz2, osu1, osv2,
                    ox1, yOuterBottom, oz2, osu2, osv2,
                    ox1, yOuterTop,    oz2, osu2, osv1,
                    ox2, yOuterTop,    oz2, osu1, osv1,
                    outerArgb, light,
                    0.0f, 0.0f, 1.0f);

            emitDoubleSidedFace(vc,
                    ox1, yOuterBottom, oz2, osu1, osv2,
                    ox1, yOuterBottom, oz1, osu2, osv2,
                    ox1, yOuterTop,    oz1, osu2, osv1,
                    ox1, yOuterTop,    oz2, osu1, osv1,
                    outerArgb, light,
                    -1.0f, 0.0f, 0.0f);

            emitDoubleSidedFace(vc,
                    ox2, yOuterBottom, oz1, osu1, osv2,
                    ox2, yOuterBottom, oz2, osu2, osv2,
                    ox2, yOuterTop,    oz2, osu2, osv1,
                    ox2, yOuterTop,    oz1, osu1, osv1,
                    outerArgb, light,
                    1.0f, 0.0f, 0.0f);

            if (outerBottomSprite != null) {
                final float obu1 = outerBottomSprite.getMinU();
                final float obu2 = outerBottomSprite.getMaxU();
                final float obv1 = outerBottomSprite.getMinV();
                final float obv2 = outerBottomSprite.getMaxV();

                emitDoubleSidedFace(vc,
                        ox1, yOuterBottom, oz1, obu1, obv1,
                        ox2, yOuterBottom, oz1, obu2, obv1,
                        ox2, yOuterBottom, oz2, obu2, obv2,
                        ox1, yOuterBottom, oz2, obu1, obv2,
                        outerArgb, light,
                        0.0f, -1.0f, 0.0f);
            }
        }
    }

    private static void emitDoubleSidedFace(VertexConsumer vc,
                                            float x1, float y1, float z1, float u1, float v1,
                                            float x2, float y2, float z2, float u2, float v2,
                                            float x3, float y3, float z3, float u3, float v3,
                                            float x4, float y4, float z4, float u4, float v4,
                                            int argb, int light,
                                            float nx, float ny, float nz) {
        emit(vc, x1, y1, z1, u1, v1, argb, light, nx, ny, nz);
        emit(vc, x2, y2, z2, u2, v2, argb, light, nx, ny, nz);
        emit(vc, x3, y3, z3, u3, v3, argb, light, nx, ny, nz);
        emit(vc, x4, y4, z4, u4, v4, argb, light, nx, ny, nz);

        emit(vc, x2, y2, z2, u2, v2, argb, light, -nx, -ny, -nz);
        emit(vc, x1, y1, z1, u1, v1, argb, light, -nx, -ny, -nz);
        emit(vc, x4, y4, z4, u4, v4, argb, light, -nx, -ny, -nz);
        emit(vc, x3, y3, z3, u3, v3, argb, light, -nx, -ny, -nz);
    }

    private static void emit(VertexConsumer vc,
                             float x, float y, float z,
                             float u, float v,
                             int argb, int light,
                             float nx, float ny, float nz) {
        vc.vertex(x, y, z, argb, u, v, 0, light, nx, ny, nz);
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
                                       double vx, double vy, double vz,
                                       Random random) {
            return new GroundSplashParticle(world, x, y, z, effect, spriteProvider);
        }
    }
}