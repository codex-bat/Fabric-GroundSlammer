// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.particle.custom;

import net.codex.particle.custom.pipeline.CuboidRenderable;
import net.codex.particle.custom.pipeline.CuboidParticle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.client.particle.Particle;

public class GroundSplashSimpleParticle extends CuboidParticle {
    private final SpriteProvider spriteProvider;
    private static final float BASE_SCALE = 1.25f;
    private final float sizeMultiplier;
    private final float heightMultiplier;

    private static final int SIDE_FRAMES = 10;
    private static final int BOTTOM_FRAMES = 10;

    private final double spawnX, spawnY, spawnZ;
    private float red, green, blue;

    // custom fields because of new 1.21.11 API changes (you can see it in other similar particles)
    private final float baseScale;
    private float alpha;

    public GroundSplashSimpleParticle(ClientWorld world,
                                      double x, double y, double z,
                                      GroundSplashParticleEffect effect,
                                      SpriteProvider spriteProvider) {
        super(world, x, y, z);
        this.spriteProvider = spriteProvider;
        this.sizeMultiplier = effect.sizeMultiplier;
        this.heightMultiplier = effect.heightMultiplier;

        this.spawnX = x;
        this.spawnY = y;
        this.spawnZ = z;

        this.maxAge = 18 + this.random.nextInt(8);
        this.gravityStrength = 0.0F;
        this.collidesWithWorld = false;
        this.velocityMultiplier = 0.0F;

        this.baseScale = BASE_SCALE * this.sizeMultiplier;
        this.red = effect.red;
        this.green = effect.green;
        this.blue = effect.blue;
        this.alpha = 1.0F;
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

        this.setPos(this.spawnX, this.spawnY, this.spawnZ);
    }

    @Override
    public CuboidRenderable captureRenderState(Camera camera, float tickDelta) {
        int totalFrames = SIDE_FRAMES + BOTTOM_FRAMES;
        int maxA = Math.max(1, this.maxAge);

        int sideIndex = MathHelper.clamp((this.age * SIDE_FRAMES) / maxA, 0, SIDE_FRAMES - 1);
        int syntheticAgeForSide = (sideIndex * this.maxAge) / Math.max(1, totalFrames - 1);
        Sprite sideSprite = this.spriteProvider.getSprite(syntheticAgeForSide, this.maxAge);

        int bottomIndex = MathHelper.clamp((this.age * BOTTOM_FRAMES) / maxA, 0, BOTTOM_FRAMES - 1);
        int syntheticAgeForBottom = ((SIDE_FRAMES + bottomIndex) * this.maxAge) / Math.max(1, totalFrames - 1);
        Sprite bottomSprite = this.spriteProvider.getSprite(syntheticAgeForBottom, this.maxAge);

        Vec3d cam = camera.getCameraPos();
        float px = (float) (MathHelper.lerp(tickDelta, this.lastX, this.x) - cam.x);
        float py = (float) (MathHelper.lerp(tickDelta, this.lastY, this.y) - cam.y);
        float pz = (float) (MathHelper.lerp(tickDelta, this.lastZ, this.z) - cam.z);

        int light = this.getBrightness(tickDelta);

        return new RenderState(
                px, py, pz,
                baseScale, heightMultiplier,
                red, green, blue, alpha,
                light, sideSprite, bottomSprite
        );
    }

    public record RenderState(
            float px, float py, float pz,
            float size, float heightMultiplier,
            float red, float green, float blue, float alpha,
            int light, Sprite sideSprite, Sprite bottomSprite
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

            // Four side walls, double‑sided
            emitDoubleSidedFace(vc,
                    x1, yBottom, z1, su1, sv2,
                    x2, yBottom, z1, su2, sv2,
                    x2, yTop,    z1, su2, sv1,
                    x1, yTop,    z1, su1, sv1,
                    argb, light,
                    0, 0, -1);

            emitDoubleSidedFace(vc,
                    x2, yBottom, z2, su1, sv2,
                    x1, yBottom, z2, su2, sv2,
                    x1, yTop,    z2, su2, sv1,
                    x2, yTop,    z2, su1, sv1,
                    argb, light,
                    0, 0, 1);

            emitDoubleSidedFace(vc,
                    x1, yBottom, z2, su1, sv2,
                    x1, yBottom, z1, su2, sv2,
                    x1, yTop,    z1, su2, sv1,
                    x1, yTop,    z2, su1, sv1,
                    argb, light,
                    -1, 0, 0);

            emitDoubleSidedFace(vc,
                    x2, yBottom, z1, su1, sv2,
                    x2, yBottom, z2, su2, sv2,
                    x2, yTop,    z2, su2, sv1,
                    x2, yTop,    z1, su1, sv1,
                    argb, light,
                    1, 0, 0);

            // Bottom face, double‑sided
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
                        0, -1, 0);
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
        emit(vc, x1, y1, z1, u1, v1, argb, light,  nx,  ny,  nz);
        emit(vc, x2, y2, z2, u2, v2, argb, light,  nx,  ny,  nz);
        emit(vc, x3, y3, z3, u3, v3, argb, light,  nx,  ny,  nz);
        emit(vc, x4, y4, z4, u4, v4, argb, light,  nx,  ny,  nz);

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
            // No sprite needed at construction – it's fetched during rendering
            return new GroundSplashSimpleParticle(world, x, y, z, effect, this.spriteProvider);
        }
    }
}