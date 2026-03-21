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

import java.lang.reflect.Method;
import java.util.Random;

/**
 * Robust landing animation particle that supports multiple SpriteProvider APIs via reflection.
 */
public class SplashDropletLandingParticle extends SpriteBillboardParticle {

    private final SpriteProvider spriteProvider;
    private Sprite currentSprite; // store active sprite so render can read it safely

    private final int ticksPerFrame;
    private final int targetFrames; // how many frames it will play (<= maxFrames)
    private final int maxFrames = 6;
    private final int minFramesIfDies = 3;

    // Reflection helpers (cached per-class, reused across instances)
    private static Method provider_getSprite_age_max = null; // getSprite(int syntheticAge, int maxAge)
    private static Method provider_getSprite_index = null;   // getSprite(int index)
    private static Method provider_getSprite_random = null;  // getSprite(Random rnd)
    private static boolean providerMethodsResolved = false;

    protected SplashDropletLandingParticle(ClientWorld world, double x, double y, double z,
                                           SpriteProvider spriteProvider,
                                           float r, float g, float b) {
        super(world, x, y, z, 0, 0, 0);
        this.spriteProvider = spriteProvider;

        // keep particle pinned — never move it
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;

        // visuals
        this.collidesWithWorld = false;
        this.gravityStrength = 0.0f;
        this.velocityMultiplier = 1.0f;
        this.scale = 0.12f; // twerk TWERK IT TWERK IT TWERK IT
        this.setColor(r, g, b);

        // animation timing
        this.ticksPerFrame = 2; // adjust speed (try 1 if you want it faster)

        // 80% chance to play full 6 frames, else 3..5
        if (this.random.nextFloat() < 0.80f) {
            this.targetFrames = this.maxFrames;
        } else {
            // inclusive 3..5
            this.targetFrames = this.minFramesIfDies + this.random.nextInt(this.maxFrames - this.minFramesIfDies);
        }

        // set a maxAge equal to frames * ticksPerFrame (plus small padding)
        this.maxAge = this.targetFrames * this.ticksPerFrame + 1;

        // resolve provider methods once per JVM
        resolveProviderMethods();

        // initialize sprite to frame 0 (safe synthetic age clamp)
        int initSynthetic = MathHelper.clamp(0, 0, Math.max(0, this.maxAge - 1));
        Sprite init = getSpriteForIndex(initSynthetic, this.maxAge, 0);
        if (init != null) {
            this.setSprite(init);
            this.currentSprite = init;
        } else {
            // fallback: use provider.getSprite(random) — provider might only support random selection
            try {
                Sprite fallback = this.spriteProvider.getSprite(this.random);
                if (fallback != null) {
                    this.setSprite(fallback);
                    this.currentSprite = fallback;
                }
            } catch (Throwable ignored) {
                // if everything fails, don't call stSprites — renderer will handle // I meant to write "setSprite"
            }
        }

        // Debug: print a short line to confirm which provider method is used (remove later)
        String methodName = provider_getSprite_age_max != null ? "getSprite(age,max)"
                : provider_getSprite_index != null ? "getSprite(index)"
                : provider_getSprite_random != null ? "getSprite(random)"
                : "none";
        // System.err.println("[SplashLanding] created targetFrames=" + this.targetFrames + " usingProvider=" + methodName);
    }

    @Override
    public void tick() {
        // age and die
        if (this.age++ >= this.maxAge) {
            this.markDead();
            return;
        }
        // keep pinned in world
        this.x = this.prevPosX;
        this.y = this.prevPosY;
        this.z = this.prevPosZ;

        // update sprite according to current frame
        int frameIndex = Math.min(this.age / this.ticksPerFrame, this.targetFrames - 1); // 0-based

        // Map frameIndex -> syntheticAge used by spriteProvider (like GroundSplash)
        int syntheticAgeForFrame = (frameIndex * this.maxAge) / Math.max(1, this.targetFrames - 1);
        syntheticAgeForFrame = MathHelper.clamp(syntheticAgeForFrame, 0, Math.max(0, this.maxAge - 1));

        Sprite s = getSpriteForIndex(syntheticAgeForFrame, this.maxAge, frameIndex);
        if (s != null) {
            this.setSprite(s);
            this.currentSprite = s;
        }
        // else keep previous sprite (defensive)
    }

    /**
     * Render a floor-facing quad (flat on ground). Uses same style as your GroundSplash bottom face.
     */
    @Override
    public void buildGeometry(VertexConsumer vc, Camera camera, float tickDelta) {
        // Use the particle's current sprite (I update it in tick()). Fall back to provider if null.
        Sprite sprite = this.currentSprite;
        if (sprite == null) {
            int fallbackSynthetic = MathHelper.clamp(0, 0, Math.max(0, this.maxAge - 1));
            sprite = getSpriteForIndex(fallbackSynthetic, this.maxAge, 0);
            if (sprite == null) return; // nothing to render
        }

        // camera positions
        final float camX = (float) camera.getPos().x;
        final float camY = (float) camera.getPos().y;
        final float camZ = (float) camera.getPos().z;

        // lerped particle position
        final float px = (float) (MathHelper.lerp(tickDelta, this.prevPosX, this.x) - camX);
        final float py = (float) (MathHelper.lerp(tickDelta, this.prevPosY, this.y) - camY);
        final float pz = (float) (MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - camZ);

        final float fullSize = this.getSize(tickDelta);
        final float half = fullSize * 0.5f;

        // render a flat quad sitting just above the ground to avoid z-fighting:
        final float yBottom = py + 0.01f; // tiny offset above block
        final float x1 = px - half;
        final float x2 = px + half;
        final float z1 = pz - half;
        final float z2 = pz + half;

        // UVs from sprite
        final float u1 = sprite.getMinU();
        final float u2 = sprite.getMaxU();
        final float v1 = sprite.getMinV();
        final float v2 = sprite.getMaxV();

        final int light = 0xF000F0; // full brightness
        final float r = this.red;
        final float g = this.green;
        final float b = this.blue;
        final float a = this.alpha;

        // first winding
        vc.vertex(x1, yBottom, z1).texture(u1, v1).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yBottom, z1).texture(u2, v1).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yBottom, z2).texture(u2, v2).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yBottom, z2).texture(u1, v2).color(r, g, b, a).light(light).next();

        // second winding (to ensure correct facing)
        vc.vertex(x2, yBottom, z1).texture(u2, v1).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yBottom, z1).texture(u1, v1).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yBottom, z2).texture(u1, v2).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yBottom, z2).texture(u2, v2).color(r, g, b, a).light(light).next();
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    // ------------------- reflection helpers -------------------

    private static synchronized void resolveProviderMethods() {
        if (providerMethodsResolved) return;
        providerMethodsResolved = true;

        try {
            provider_getSprite_age_max = SpriteProvider.class.getMethod("getSprite", int.class, int.class);
        } catch (NoSuchMethodException ignored) { provider_getSprite_age_max = null; }

        try {
            provider_getSprite_index = SpriteProvider.class.getMethod("getSprite", int.class);
        } catch (NoSuchMethodException ignored) { provider_getSprite_index = null; }

        try {
            provider_getSprite_random = SpriteProvider.class.getMethod("getSprite", Random.class);
        } catch (NoSuchMethodException ignored) { provider_getSprite_random = null; }
    }

    /**
     * Robustly get a sprite for a given synthetic age and frameIndex. Tries multiple provider APIs.
     *
     * @param syntheticAge synthetic age scaled into [0..maxAge-1] (for age-based providers)
     * @param maxAge       maxAge value used with age-based providers
     * @param frameIndex   0-based animation frame index (0..targetFrames-1)
     */
    private Sprite getSpriteForIndex(int syntheticAge, int maxAge, int frameIndex) {
        try {
            // Preferred API: getSprite(age, maxAge)
            if (provider_getSprite_age_max != null) {
                Object o = provider_getSprite_age_max.invoke(this.spriteProvider, syntheticAge, maxAge);
                if (o instanceof Sprite) return (Sprite) o;
            }
        } catch (Throwable t) {
            // swallow and continue to try others (TRY OTHERS??!?! TRY SWALLOING OTHERS TOO!?!?!? LMAOO --- FAILED GRAMMAR, I SEE)
        }

        try {
            // Provider that exposes getSprite(int index) — map our frameIndex into provider's index space (0..maxFrames-1)
            if (provider_getSprite_index != null) {
                // Map animation frameIndex -> provider index range [0 .. maxFrames-1]
                int providerIndex = 0;
                if (this.targetFrames <= 1) {
                    providerIndex = 0;
                } else {
                    double ratio = (double) frameIndex / (double) Math.max(1, (this.targetFrames - 1));
                    providerIndex = (int) Math.round(ratio * (this.maxFrames - 1));
                }
                providerIndex = MathHelper.clamp(providerIndex, 0, this.maxFrames - 1);

                Object o = provider_getSprite_index.invoke(this.spriteProvider, providerIndex);
                if (o instanceof Sprite) return (Sprite) o;
            }
        } catch (Throwable t) { /* ignore */ }

        try {
            if (provider_getSprite_random != null) {
                // seed a deterministic Random for the frame so results are stable across ticks
                Random seeded = new Random(syntheticAge * 31L + maxAge);
                Object o = provider_getSprite_random.invoke(this.spriteProvider, seeded);
                if (o instanceof Sprite) return (Sprite) o;
            }
        } catch (Throwable t) { /* ignore */ }

        // As a last resort, try provider.getSprite(this.random) (non-indexed) to avoid null — not deterministic.
        try {
            Sprite s = this.spriteProvider.getSprite(this.random);
            return s;
        } catch (Throwable ignored) {}

        return null;
    }

    // factory: when created via particle manager by type, default white; direct constructor used by droplets passes colors.
    public static final class Factory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(DefaultParticleType data, ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
            return new SplashDropletLandingParticle(world, x, y, z, this.spriteProvider, 1f, 1f, 1f);
        }
    }
}