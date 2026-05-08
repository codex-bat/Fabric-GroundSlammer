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

public class ImpactinyParticle extends CuboidParticle {
    private final SpriteProvider spriteProvider;

    private static final float BASE_SCALE = 0.85f;
    private static final float EXPAND_MULTIPLIER = 2.0f;
    private static final float LIFT = 0.01f;

    private float red = 1.0f;
    private float green = 1.0f;
    private float blue = 1.0f;
    private float alpha = 1.0f;

    private final float baseScale;

    public ImpactinyParticle(
            ClientWorld world,
            double x, double y, double z,
            GroundSplashParticleEffect effect,
            SpriteProvider spriteProvider,
            Sprite sprite // kept for factory compatibility, unused otherwise
    ) {
        super(world, x, y, z);
        this.spriteProvider = spriteProvider;

        this.red = effect.red;
        this.green = effect.green;
        this.blue = effect.blue;
        this.alpha = 1.0F;

        this.maxAge = 10 + this.random.nextInt(6);
        this.baseScale = BASE_SCALE * effect.sizeMultiplier;

        this.gravityStrength = 0.0F;
        this.collidesWithWorld = false;
        this.velocityMultiplier = 0.0F;
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

        float lifeNorm = (float) this.age / Math.max(1.0f, (float) this.maxAge);
        this.alpha = 1.0f - lifeNorm;
    }

    @Override
    public CuboidRenderable captureRenderState(Camera camera, float tickDelta) {
        Vec3d cam = camera.getCameraPos();

        float ageFloat = Math.min(this.age + tickDelta, this.maxAge);
        int ageFloor = MathHelper.floor(ageFloat);
        Sprite sprite = this.spriteProvider.getSprite(ageFloor, this.maxAge);
        if (sprite == null) return null;

        float lifeNorm = MathHelper.clamp(ageFloat / Math.max(1.0f, (float) this.maxAge), 0.0f, 1.0f);
        float expand = 1.0f + (EXPAND_MULTIPLIER * lifeNorm);
        float fullSize = this.baseScale * expand;

        float px = (float) (MathHelper.lerp(tickDelta, this.lastX, this.x) - cam.x);
        float py = (float) (MathHelper.lerp(tickDelta, this.lastY, this.y) - cam.y);
        float pz = (float) (MathHelper.lerp(tickDelta, this.lastZ, this.z) - cam.z);

        int light = this.getBrightness(tickDelta);

        return new RenderState(px, py, pz, fullSize, lifeNorm,
                this.red, this.green, this.blue, this.alpha,
                light, sprite);
    }

    public record RenderState(
            float px, float py, float pz,
            float size, float lifeNorm,
            float red, float green, float blue, float alpha,
            int light, Sprite sprite
    ) implements CuboidRenderable {

        @Override
        public void emit(VertexConsumer vc) {
            float half = size * 0.5f;
            float x1 = px - half;
            float x2 = px + half;
            float z1 = pz - half;
            float z2 = pz + half;
            float y = py + LIFT;

            float u1 = sprite.getMinU();
            float u2 = sprite.getMaxU();
            float v1 = sprite.getMinV();
            float v2 = sprite.getMaxV();

            float fade = MathHelper.clamp(1.0f - lifeNorm, 0.0f, 1.0f);
            int argb = ColorHelper.fromFloats(alpha * fade, red, green, blue);

            // Front face (upward normal 0,1,0)
            vc.vertex(x1, y, z1, argb, u1, v1, 0, light, 0, 1, 0);
            vc.vertex(x2, y, z1, argb, u2, v1, 0, light, 0, 1, 0);
            vc.vertex(x2, y, z2, argb, u2, v2, 0, light, 0, 1, 0);
            vc.vertex(x1, y, z2, argb, u1, v2, 0, light, 0, 1, 0);

            // Back face (downward normal 0,-1,0)
            vc.vertex(x2, y, z1, argb, u2, v1, 0, light, 0, -1, 0);
            vc.vertex(x1, y, z1, argb, u1, v1, 0, light, 0, -1, 0);
            vc.vertex(x1, y, z2, argb, u1, v2, 0, light, 0, -1, 0);
            vc.vertex(x2, y, z2, argb, u2, v2, 0, light, 0, -1, 0);
        }
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
            Sprite sprite = this.spriteProvider.getSprite(random);
            return new ImpactinyParticle(world, x, y, z, effect, this.spriteProvider, sprite);
        }
    }
}