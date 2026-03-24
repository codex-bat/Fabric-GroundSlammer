// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.particle.custom;

import net.minecraft.client.particle.*;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class DirectedImpactParticle extends SpriteBillboardParticle {
    private final SpriteProvider spriteProvider;
    private final float yaw;
    private final float pitch;
    private final float particleScale;
    private final int lifeMaxAge;

    private static final float LIFT = 0.01f;

    public DirectedImpactParticle(ClientWorld world,
                                  double x, double y, double z,
                                  DirectedImpactParticleEffect effect,
                                  SpriteProvider spriteProvider) {
        super(world, x, y, z);
        this.spriteProvider = spriteProvider;
        this.yaw = effect.yaw;
        this.pitch = effect.pitch;
        this.particleScale = effect.sizeMultiplier;
        this.lifeMaxAge = 5 + this.random.nextInt(4);

        this.setColor(effect.red, effect.green, effect.blue);
        this.alpha = 1.0F;

        this.velocityX = 0.0;
        this.velocityY = 0.0;
        this.velocityZ = 0.0;
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

        if (this.age++ >= this.lifeMaxAge) {
            this.markDead();
        }

        this.setSpriteForAge(this.spriteProvider);
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        final float ageFloat = Math.min((float) this.age + tickDelta, (float) this.lifeMaxAge);
        final Sprite sprite = this.spriteProvider.getSprite((int) Math.floor(ageFloat), this.lifeMaxAge);
        if (sprite == null) return;

        final Vec3d camPos = camera.getPos();
        final float px = (float) (MathHelper.lerp(tickDelta, this.prevPosX, this.x) - camPos.x);
        final float py = (float) (MathHelper.lerp(tickDelta, this.prevPosY, this.y) - camPos.y);
        final float pz = (float) (MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - camPos.z);

        final float lifeNorm = MathHelper.clamp(ageFloat / Math.max(1.0f, (float) this.lifeMaxAge), 0.0f, 1.0f);
        final float expand = 1.0f + (2.0f * lifeNorm);
        final float size = this.particleScale * expand;
        final float half = size * 0.5f;

        final float cosYaw = MathHelper.cos(this.yaw);
        final float sinYaw = MathHelper.sin(this.yaw);
        final float cosPitch = MathHelper.cos(this.pitch);
        final float sinPitch = MathHelper.sin(this.pitch);

        final Vec3d forward = new Vec3d(cosYaw * cosPitch, sinPitch, sinYaw * cosPitch).normalize();

        Vec3d right = new Vec3d(0.0, 1.0, 0.0).crossProduct(forward);
        if (right.lengthSquared() < 1.0E-4) {
            right = new Vec3d(1.0, 0.0, 0.0);
        } else {
            right = right.normalize();
        }
        final Vec3d up = forward.crossProduct(right).normalize();

        final Vec3d c0 = right.multiply(-half).add(up.multiply(-half));
        final Vec3d c1 = right.multiply(half).add(up.multiply(-half));
        final Vec3d c2 = right.multiply(half).add(up.multiply(half));
        final Vec3d c3 = right.multiply(-half).add(up.multiply(half));

        final float x0 = px + (float) c0.x;
        final float y0 = py + LIFT + (float) c0.y;
        final float z0 = pz + (float) c0.z;

        final float x1 = px + (float) c1.x;
        final float y1 = py + LIFT + (float) c1.y;
        final float z1 = pz + (float) c1.z;

        final float x2 = px + (float) c2.x;
        final float y2 = py + LIFT + (float) c2.y;
        final float z2 = pz + (float) c2.z;

        final float x3 = px + (float) c3.x;
        final float y3 = py + LIFT + (float) c3.y;
        final float z3 = pz + (float) c3.z;

        final float u1 = sprite.getMinU();
        final float u2 = sprite.getMaxU();
        final float v1 = sprite.getMinV();
        final float v2 = sprite.getMaxV();

        final float fade = MathHelper.clamp(1.0f - lifeNorm, 0.0f, 1.0f);
        final float a = this.alpha * fade;
        final int light = 0xF000F0;

        final float r = this.red;
        final float g = this.green;
        final float b = this.blue;

        vertexConsumer.vertex(x0, y0, z0).texture(u1, v1).color(r, g, b, a).light(light).next();
        vertexConsumer.vertex(x1, y1, z1).texture(u2, v1).color(r, g, b, a).light(light).next();
        vertexConsumer.vertex(x2, y2, z2).texture(u2, v2).color(r, g, b, a).light(light).next();

        vertexConsumer.vertex(x0, y0, z0).texture(u1, v1).color(r, g, b, a).light(light).next();
        vertexConsumer.vertex(x2, y2, z2).texture(u2, v2).color(r, g, b, a).light(light).next();
        vertexConsumer.vertex(x3, y3, z3).texture(u1, v2).color(r, g, b, a).light(light).next();

        vertexConsumer.vertex(x0, y0, z0).texture(u1, v1).color(r, g, b, a).light(light).next();
        vertexConsumer.vertex(x3, y3, z3).texture(u1, v2).color(r, g, b, a).light(light).next();
        vertexConsumer.vertex(x2, y2, z2).texture(u2, v2).color(r, g, b, a).light(light).next();

        vertexConsumer.vertex(x0, y0, z0).texture(u1, v1).color(r, g, b, a).light(light).next();
        vertexConsumer.vertex(x2, y2, z2).texture(u2, v2).color(r, g, b, a).light(light).next();
        vertexConsumer.vertex(x1, y1, z1).texture(u2, v1).color(r, g, b, a).light(light).next();
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