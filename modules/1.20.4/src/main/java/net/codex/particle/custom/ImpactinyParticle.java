// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.particle.custom;

import net.minecraft.client.particle.*;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;

public class ImpactinyParticle extends SpriteBillboardParticle {
    private final SpriteProvider spriteProvider;

    private static final float BASE_SCALE = 0.85f;
    private static final float EXPAND_MULTIPLIER = 2.0f;
    private static final float LIFT = 0.01f;

    public ImpactinyParticle(ClientWorld world,
                             double x, double y, double z,
                             GroundSplashParticleEffect effect,
                             SpriteProvider spriteProvider) {
        super(world, x, y, z, 0.0, 0.0, 0.0);
        this.spriteProvider = spriteProvider;

        this.setColor(effect.red, effect.green, effect.blue);
        this.scale = BASE_SCALE * effect.sizeMultiplier;
        this.alpha = 1.0F;

        this.maxAge = 10 + this.random.nextInt(6);

        this.gravityStrength = 0.0F;
        this.collidesWithWorld = false;
        this.velocityMultiplier = 0.0F;

        this.setSprite(spriteProvider.getSprite(this.random));
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
        final float ageFloat = Math.min((float) this.age + tickDelta, (float) this.maxAge);
        final Sprite sprite = this.spriteProvider.getSprite((int) Math.floor(ageFloat), this.maxAge);
        if (sprite == null) return;

        final float camX = (float) camera.getPos().x;
        final float camY = (float) camera.getPos().y;
        final float camZ = (float) camera.getPos().z;

        final float px = (float) (MathHelper.lerp(tickDelta, this.prevPosX, this.x) - camX);
        final float py = (float) (MathHelper.lerp(tickDelta, this.prevPosY, this.y) - camY);
        final float pz = (float) (MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - camZ);

        final float lifeNorm = MathHelper.clamp(ageFloat / Math.max(1.0f, (float) this.maxAge), 0.0f, 1.0f);
        final float expand = 1.0f + (EXPAND_MULTIPLIER * lifeNorm);
        final float fullSize = BASE_SCALE * this.scale * expand;
        final float half = fullSize * 0.5f;

        final float yBottom = py + LIFT;

        final float x1 = px - half;
        final float x2 = px + half;
        final float z1 = pz - half;
        final float z2 = pz + half;

        final float u1 = sprite.getMinU();
        final float u2 = sprite.getMaxU();
        final float v1 = sprite.getMinV();
        final float v2 = sprite.getMaxV();

        final float fade = MathHelper.clamp(1.0f - lifeNorm, 0.0f, 1.0f);

        final int light = 0xF000F0;
        final float r = 1.0f;
        final float g = 1.0f;
        final float b = 1.0f;
        final float a = this.alpha * fade;

        vc.vertex(x1, yBottom, z1).texture(u1, v1).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yBottom, z1).texture(u2, v1).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yBottom, z2).texture(u2, v2).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yBottom, z2).texture(u1, v2).color(r, g, b, a).light(light).next();

        vc.vertex(x2, yBottom, z1).texture(u2, v1).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yBottom, z1).texture(u1, v1).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yBottom, z2).texture(u1, v2).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yBottom, z2).texture(u2, v2).color(r, g, b, a).light(light).next();
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
            return new ImpactinyParticle(world, x, y, z, effect, spriteProvider);
        }
    }
}