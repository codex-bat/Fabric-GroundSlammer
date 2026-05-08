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

public class SplashDropletLandingParticle extends CuboidParticle {
    private final SpriteProvider spriteProvider;
    private final double spawnX, spawnY, spawnZ;

    private static final float BASE_SIZE = 0.75f;
    private static final float LIFT = 0.01f;

    private float red, green, blue;
    private float alpha;
    private final float baseSize;

    protected SplashDropletLandingParticle(
            ClientWorld world,
            double x, double y, double z,
            SpriteProvider spriteProvider,
            SplashDropletParticleEffect effect
    ) {
        super(world, x, y, z);
        this.spriteProvider = spriteProvider;
        this.spawnX = x;
        this.spawnY = y;
        this.spawnZ = z;

        this.collidesWithWorld = false;
        this.gravityStrength = 0.0f;
        this.velocityMultiplier = 1.0f;

        this.baseSize = BASE_SIZE * effect.amountMultiplier;
        this.red   = effect.red;
        this.green = effect.green;
        this.blue  = effect.blue;
        this.alpha = 1.0f;

        this.maxAge = 12;
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

        // stay exactly at spawn point - ok :3 lemme just go back into my mom's womb (WTF WAS I SAYING HERE, WHY IS MY HUMOUR SO EDGY
        this.setPos(this.spawnX, this.spawnY, this.spawnZ);
    }

    @Override
    public CuboidRenderable captureRenderState(Camera camera, float tickDelta) {
        Vec3d cam = camera.getCameraPos();

        float px = (float) (MathHelper.lerp(tickDelta, this.lastX, this.x) - cam.x);
        float py = (float) (MathHelper.lerp(tickDelta, this.lastY, this.y) - cam.y);
        float pz = (float) (MathHelper.lerp(tickDelta, this.lastZ, this.z) - cam.z);

        Sprite sprite = this.spriteProvider.getSprite(this.age, this.maxAge);
        if (sprite == null) {
            sprite = this.spriteProvider.getSprite(0, this.maxAge);
            if (sprite == null) return null;
        }

        int light = this.getBrightness(tickDelta);
        float size = this.baseSize;

        return new RenderState(px, py, pz, size, red, green, blue, alpha, light, sprite);
    }

    public record RenderState(
            float px, float py, float pz,
            float size,
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

            int argb = ColorHelper.fromFloats(alpha, red, green, blue);

            // Double‑sided ground quad (normal up = 0,1,0)
            // Front face
            vc.vertex(x1, y, z1, argb, u1, v1, 0, light, 0, 1, 0);
            vc.vertex(x2, y, z1, argb, u2, v1, 0, light, 0, 1, 0);
            vc.vertex(x2, y, z2, argb, u2, v2, 0, light, 0, 1, 0);
            vc.vertex(x1, y, z2, argb, u1, v2, 0, light, 0, 1, 0);

            // Back face
            vc.vertex(x2, y, z1, argb, u2, v1, 0, light, 0, -1, 0);
            vc.vertex(x1, y, z1, argb, u1, v1, 0, light, 0, -1, 0);
            vc.vertex(x1, y, z2, argb, u1, v2, 0, light, 0, -1, 0);
            vc.vertex(x2, y, z2, argb, u2, v2, 0, light, 0, -1, 0);
        }
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
                double vx, double vy, double vz,
                Random random
        ) {
            // Sprite is fetched during rendering, no need to pass one here just like the other one
            return new SplashDropletLandingParticle(world, x, y, z, this.spriteProvider, effect);
        }
    }
}