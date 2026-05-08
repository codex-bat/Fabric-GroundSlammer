// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.particle.custom;

import net.minecraft.client.particle.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.texture.Sprite;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.MathHelper;

/**
 * Landing animation particle that plays a flat ground splash.
 * Now uses the same reliable SpriteProvider.getSprite(age, maxAge) as GroundSplash.
 */
public class SplashDropletLandingParticle extends SpriteBillboardParticle {
    private final SpriteProvider spriteProvider;
    private Sprite currentSprite;

    private static final ThreadLocal<Float> PENDING_AMOUNT =
            ThreadLocal.withInitial(() -> 1.0f);

    public static void setPendingAmount(float amount) {
        PENDING_AMOUNT.set(amount);
    }

    private static float consumePendingAmount() {
        float amount = PENDING_AMOUNT.get();
        PENDING_AMOUNT.remove();
        return amount;
    }

    private final int ticksPerFrame;
    private final int targetFrames;
    private final int maxFrames = 6;
    private final int minFramesIfDies = 3;

    protected SplashDropletLandingParticle(ClientWorld world, double x, double y, double z,
                                           SpriteProvider spriteProvider,
                                           float r, float g, float b) {
        super(world, x, y, z, 0, 0, 0);
        this.spriteProvider = spriteProvider;

        // Pin particle to its spawn position
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;

        this.collidesWithWorld = false;
        this.gravityStrength = 0.0f;
        this.velocityMultiplier = 1.0f;
        float baseScale = 0.75f;
        this.scale = baseScale * consumePendingAmount();

        this.setColor(r, g, b);

        this.ticksPerFrame = 2; // each frame lasts 2 ticks

        // Choose number of frames to play
        if (this.random.nextFloat() < 0.80f) {
            this.targetFrames = this.maxFrames;
        } else {
            this.targetFrames = this.minFramesIfDies + this.random.nextInt(this.maxFrames - this.minFramesIfDies);
        }

        // maxAge = frames * ticksPerFrame   (no +1, so age ranges 0..maxAge-1)
        this.maxAge = this.targetFrames * this.ticksPerFrame;

        // Get initial sprite using the same method that works elsewhere
        Sprite init = this.spriteProvider.getSprite(0, this.maxAge);
        if (init != null) {
            this.setSprite(init);
            this.currentSprite = init;
        }

        // Debug (remove after verifying)
        // System.err.println("[Landing] created targetFrames=" + this.targetFrames + " maxAge=" + this.maxAge);
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    @Override
    public void tick() {
        if (this.age++ >= this.maxAge) {
            this.markDead();
            return;
        }

        // Keep position fixed
        this.x = this.prevPosX;
        this.y = this.prevPosY;
        this.z = this.prevPosZ;

        // Update sprite using the age‑based provider method (exactly like GroundSplash)
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
            // Fallback – should never happen
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

        // Slightly above ground to avoid z‑fighting
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

        // First winding (counter‑clockwise)
        vc.vertex(x1, yBottom, z1).texture(u1, v1).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yBottom, z1).texture(u2, v1).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yBottom, z2).texture(u2, v2).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yBottom, z2).texture(u1, v2).color(r, g, b, a).light(light).next();

        // Second winding (clockwise) for double‑sided rendering
        vc.vertex(x2, yBottom, z1).texture(u2, v1).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yBottom, z1).texture(u1, v1).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yBottom, z2).texture(u1, v2).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yBottom, z2).texture(u2, v2).color(r, g, b, a).light(light).next();
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    // Factory for when the particle is registered with a DefaultParticleType
    public static final class Factory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(DefaultParticleType data, ClientWorld world,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {

            SplashDropletLandingParticle p =
                    new SplashDropletLandingParticle(world, x, y, z, this.spriteProvider, 1f, 1f, 1f);

            p.setColor((float) vx, (float) vy, (float) vz);

            return p;
        }
    }
}