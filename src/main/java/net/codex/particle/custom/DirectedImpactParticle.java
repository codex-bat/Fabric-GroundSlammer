// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.particle.custom;

import net.minecraft.client.particle.*;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class DirectedImpactParticle extends SpriteBillboardParticle {
    private final SpriteProvider spriteProvider;
    private final float yaw, pitch;          // full rotation
    private final float scale;
    private final int maxAge;

    private static final float LIFT = 0.01f;

    public DirectedImpactParticle(ClientWorld world,
                                  double x, double y, double z,
                                  DirectedImpactParticleEffect effect,
                                  SpriteProvider spriteProvider) {
        super(world, x, y, z);
        this.spriteProvider = spriteProvider;
        this.yaw = effect.yaw;
        this.pitch = effect.pitch;
        this.scale = effect.sizeMultiplier;
        this.maxAge = 5 + this.random.nextInt(4); // shorter: 5-8 ticks

        this.setColor(effect.red, effect.green, effect.blue);
        this.alpha = 1.0F;

        this.velocityX = 0;
        this.velocityY = 0;
        this.velocityZ = 0;
        this.gravityStrength = 0.0F;
        this.collidesWithWorld = false;
        this.velocityMultiplier = 0.0F;

        this.setSprite(spriteProvider.getSprite(this.random));
    }

    @Override
    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;

        if (this.age++ >= this.maxAge) {
            this.markDead();
        }

        this.setSprite(spriteProvider.getSprite(Math.min(this.age, this.maxAge), this.maxAge));
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        float ageFloat = Math.min((float) this.age + tickDelta, (float) this.maxAge);
        Sprite sprite = this.spriteProvider.getSprite((int) Math.floor(ageFloat), this.maxAge);
        if (sprite == null) return;

        Vec3d camPos = camera.getPos();
        float px = (float) (MathHelper.lerp(tickDelta, this.prevPosX, this.x) - camPos.x);
        float py = (float) (MathHelper.lerp(tickDelta, this.prevPosY, this.y) - camPos.y);
        float pz = (float) (MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - camPos.z);

        float lifeNorm = MathHelper.clamp(ageFloat / this.maxAge, 0.0f, 1.0f);
        float expand = 1.0f + (2.0f * lifeNorm);   // expands up to 3x
        float size = this.scale * expand;
        float half = size * 0.5f;

        float cosYaw = MathHelper.cos(yaw);
        float sinYaw = MathHelper.sin(yaw);
        float cosPitch = MathHelper.cos(pitch);
        float sinPitch = MathHelper.sin(pitch);

        Vec3d forward = new Vec3d(cosYaw * cosPitch, sinPitch, sinYaw * cosPitch).normalize();

        Vec3d worldUp = new Vec3d(0, 1, 0);
        Vec3d right = worldUp.crossProduct(forward).normalize();
        if (right.lengthSquared() < 0.1) { // looking straight up/down, use a different reference
            right = new Vec3d(1, 0, 0);
        }
        Vec3d up = forward.crossProduct(right).normalize();

        Vec3d[] corners = new Vec3d[4];
        corners[0] = right.multiply(-half).add(up.multiply(-half)); // bottom-left
        corners[1] = right.multiply( half).add(up.multiply(-half)); // bottom-right
        corners[2] = right.multiply( half).add(up.multiply( half)); // top-right
        corners[3] = right.multiply(-half).add(up.multiply( half)); // top-left

        float[] xCoords = new float[4];
        float[] yCoords = new float[4];
        float[] zCoords = new float[4];
        for (int i = 0; i < 4; i++) {
            xCoords[i] = px + (float) corners[i].x;
            yCoords[i] = py + LIFT + (float) corners[i].y;
            zCoords[i] = pz + (float) corners[i].z;
        }

        float u1 = sprite.getMinU();
        float u2 = sprite.getMaxU();
        float v1 = sprite.getMinV();
        float v2 = sprite.getMaxV();

        float fade = MathHelper.clamp(1.0f - lifeNorm, 0.0f, 1.0f);
        float a = this.alpha * fade;
        int light = 0xF000F0;

        float r = this.red;
        float g = this.green;
        float b = this.blue;

        // Front (counter-clockwise)
        vertexConsumer.vertex(xCoords[0], yCoords[0], zCoords[0]).texture(u1, v1).color(r, g, b, a).light(light).next();
        vertexConsumer.vertex(xCoords[1], yCoords[1], zCoords[1]).texture(u2, v1).color(r, g, b, a).light(light).next();
        vertexConsumer.vertex(xCoords[2], yCoords[2], zCoords[2]).texture(u2, v2).color(r, g, b, a).light(light).next();

        vertexConsumer.vertex(xCoords[0], yCoords[0], zCoords[0]).texture(u1, v1).color(r, g, b, a).light(light).next();
        vertexConsumer.vertex(xCoords[2], yCoords[2], zCoords[2]).texture(u2, v2).color(r, g, b, a).light(light).next();
        vertexConsumer.vertex(xCoords[3], yCoords[3], zCoords[3]).texture(u1, v2).color(r, g, b, a).light(light).next();

        // Back side (reverse winding)
        vertexConsumer.vertex(xCoords[0], yCoords[0], zCoords[0]).texture(u1, v1).color(r, g, b, a).light(light).next();
        vertexConsumer.vertex(xCoords[3], yCoords[3], zCoords[3]).texture(u1, v2).color(r, g, b, a).light(light).next();
        vertexConsumer.vertex(xCoords[2], yCoords[2], zCoords[2]).texture(u2, v2).color(r, g, b, a).light(light).next();

        vertexConsumer.vertex(xCoords[0], yCoords[0], zCoords[0]).texture(u1, v1).color(r, g, b, a).light(light).next();
        vertexConsumer.vertex(xCoords[2], yCoords[2], zCoords[2]).texture(u2, v2).color(r, g, b, a).light(light).next();
        vertexConsumer.vertex(xCoords[1], yCoords[1], zCoords[1]).texture(u2, v1).color(r, g, b, a).light(light).next();
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleFactory<DirectedImpactParticleEffect> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(DirectedImpactParticleEffect effect,
                                       ClientWorld world,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new DirectedImpactParticle(world, x, y, z, effect, spriteProvider);
        }
    }
}