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

    private static final int SIDE_FRAMES = 10;  // adjust to match your combined json
    private static final int BOTTOM_FRAMES = 10;

    // Outer skirt configuration (tweak these)
    private static final float OUTER_RADIUS_MULT = 1.6f;
    private static final float OUTER_HEIGHT_SCALE = 0.5f;

    // Rise timing (in ticks). 1 tick = 50 ms.
    // Outer skirt stays below ground until OUTER_RISE_DELAY_TICKS, then rises over OUTER_RISE_DURATION_TICKS.
    private static final int OUTER_RISE_DELAY_TICKS = 2;    // ~100 ms
    private static final int OUTER_RISE_DURATION_TICKS = 4; // ~200 ms

    // Delay for starting outer animation frames (in ticks). Keep equal or larger than OUTER_RISE_DELAY_TICKS if you want animations to start strictly after lift.
    private static final int OUTER_ANIM_DELAY_TICKS = OUTER_RISE_DELAY_TICKS;

    // Slight final offset for outer bottom to reduce z-fighting (in world units).
    private static final float OUTER_FINAL_BOTTOM_OFFSET = 0.002f;

    public GroundSplashParticle(ClientWorld world,
                                double x, double y, double z,
                                GroundSplashParticleEffect effect,
                                SpriteProvider spriteProvider) {

        super(world, x, y, z, 0, 0, 0);
        this.spriteProvider = spriteProvider;
        this.sizeMultiplier = effect.sizeMultiplier;
        this.heightMultiplier = effect.heightMultiplier;

        // set maxAge *before* any synthetic-age calculations
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
        // cache constants & avoid allocations:
        final int totalFrames = SIDE_FRAMES + BOTTOM_FRAMES;
        final int maxA = Math.max(1, this.maxAge);

        // --- compute per-face sprite indices for the INNER faces (no allocations) ---
        int sideIndex = (this.age * SIDE_FRAMES) / maxA;
        if (sideIndex < 0) sideIndex = 0;
        else if (sideIndex >= SIDE_FRAMES) sideIndex = SIDE_FRAMES - 1;
        int desiredSideIndex = sideIndex; // 0..SIDE_FRAMES-1
        int syntheticAgeForSide = (desiredSideIndex * this.maxAge) / Math.max(1, totalFrames - 1);
        Sprite sideSprite = this.spriteProvider.getSprite(syntheticAgeForSide, this.maxAge);

        int bottomIndex = (this.age * BOTTOM_FRAMES) / maxA;
        if (bottomIndex < 0) bottomIndex = 0;
        else if (bottomIndex >= BOTTOM_FRAMES) bottomIndex = BOTTOM_FRAMES - 1;
        int desiredBottomIndex = SIDE_FRAMES + bottomIndex; // index inside combined sprite list
        int syntheticAgeForBottom = (desiredBottomIndex * this.maxAge) / Math.max(1, totalFrames - 1);
        Sprite bottomSprite = this.spriteProvider.getSprite(syntheticAgeForBottom, this.maxAge);

        // --- compute delayed sprite indices for the OUTER skirt (animations start after OUTER_ANIM_DELAY_TICKS) ---
        final float ageWithDelta = this.age + tickDelta;
        final float outerAnimAgeFloat = Math.max(0.0f, ageWithDelta - (float) OUTER_ANIM_DELAY_TICKS);
        // convert to integer ticks for indexing
        final int outerAnimAge = Math.max(0, this.age - OUTER_ANIM_DELAY_TICKS);

        int outerSideIndex = (outerAnimAge * SIDE_FRAMES) / maxA;
        if (outerSideIndex < 0) outerSideIndex = 0;
        else if (outerSideIndex >= SIDE_FRAMES) outerSideIndex = SIDE_FRAMES - 1;
        int desiredOuterSideIndex = outerSideIndex;
        int syntheticAgeForOuterSide = (desiredOuterSideIndex * this.maxAge) / Math.max(1, totalFrames - 1);
        Sprite outerSideSprite = this.spriteProvider.getSprite(syntheticAgeForOuterSide, this.maxAge);

        int outerBottomIndex = (outerAnimAge * BOTTOM_FRAMES) / maxA;
        if (outerBottomIndex < 0) outerBottomIndex = 0;
        else if (outerBottomIndex >= BOTTOM_FRAMES) outerBottomIndex = BOTTOM_FRAMES - 1;
        int desiredOuterBottomIndex = SIDE_FRAMES + outerBottomIndex;
        int syntheticAgeForOuterBottom = (desiredOuterBottomIndex * this.maxAge) / Math.max(1, totalFrames - 1);
        Sprite outerBottomSprite = this.spriteProvider.getSprite(syntheticAgeForOuterBottom, this.maxAge);

        // --- world-space positions (cached) ---
        final float camX = (float) camera.getPos().x;
        final float camY = (float) camera.getPos().y;
        final float camZ = (float) camera.getPos().z;

        final float px = (float) (MathHelper.lerp(tickDelta, this.prevPosX, this.x) - camX);
        final float py = (float) (MathHelper.lerp(tickDelta, this.prevPosY, this.y) - camY);
        final float pz = (float) (MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - camZ);

        final float fullSize = this.getSize(tickDelta);
        final float half = fullSize * 0.5f;
        final float height = (half * 2.2f) * this.heightMultiplier;

        // Lift bottom slightly so it doesn't z-fight with the block beneath.
        final float yBottom = py + 0.01f;
        final float yTop = py + height;
        final float x1 = px - half;
        final float x2 = px + half;
        final float z1 = pz - half;
        final float z2 = pz + half;

        // --- UVs for INNER side faces ---
        final float su1 = sideSprite.getMinU();
        final float su2 = sideSprite.getMaxU();
        final float sv1 = sideSprite.getMinV();
        final float sv2 = sideSprite.getMaxV();

        // --- UVs for OUTER side faces (delayed) ---
        final float osu1 = outerSideSprite.getMinU();
        final float osu2 = outerSideSprite.getMaxU();
        final float osv1 = outerSideSprite.getMinV();
        final float osv2 = outerSideSprite.getMaxV();

        final int light = 0xF000F0;
        final float r = this.red;
        final float g = this.green;
        final float b = this.blue;
        final float a = this.alpha;

        // ---- Emit INNER vertices (same as before) ----
        // Side 1: (x1,z1) -> (x2,z1)
        vc.vertex(x1, yBottom, z1).texture(su1, sv2).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yBottom, z1).texture(su2, sv2).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yTop,    z1).texture(su2, sv1).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yTop,    z1).texture(su1, sv1).color(r, g, b, a).light(light).next();

        vc.vertex(x2, yBottom, z1).texture(su2, sv2).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yBottom, z1).texture(su1, sv2).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yTop,    z1).texture(su1, sv1).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yTop,    z1).texture(su2, sv1).color(r, g, b, a).light(light).next();

        // Side 2: (x2,z2) -> (x1,z2)
        vc.vertex(x2, yBottom, z2).texture(su1, sv2).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yBottom, z2).texture(su2, sv2).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yTop,    z2).texture(su2, sv1).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yTop,    z2).texture(su1, sv1).color(r, g, b, a).light(light).next();

        vc.vertex(x1, yBottom, z2).texture(su1, sv2).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yBottom, z2).texture(su2, sv2).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yTop,    z2).texture(su2, sv1).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yTop,    z2).texture(su1, sv1).color(r, g, b, a).light(light).next();

        // Side 3: (x1,z2) -> (x1,z1)
        vc.vertex(x1, yBottom, z2).texture(su1, sv2).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yBottom, z1).texture(su2, sv2).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yTop,    z1).texture(su2, sv1).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yTop,    z2).texture(su1, sv1).color(r, g, b, a).light(light).next();

        vc.vertex(x1, yBottom, z1).texture(su1, sv2).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yBottom, z2).texture(su2, sv2).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yTop,    z2).texture(su2, sv1).color(r, g, b, a).light(light).next();
        vc.vertex(x1, yTop,    z1).texture(su1, sv1).color(r, g, b, a).light(light).next();

        // Side 4: (x2,z1) -> (x2,z2)
        vc.vertex(x2, yBottom, z1).texture(su1, sv2).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yBottom, z2).texture(su2, sv2).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yTop,    z2).texture(su2, sv1).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yTop,    z1).texture(su1, sv1).color(r, g, b, a).light(light).next();

        vc.vertex(x2, yBottom, z2).texture(su2, sv2).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yBottom, z1).texture(su1, sv2).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yTop,    z1).texture(su1, sv1).color(r, g, b, a).light(light).next();
        vc.vertex(x2, yTop,    z2).texture(su2, sv1).color(r, g, b, a).light(light).next();

        // --- OUTER skirt: larger radius, but shorter height ---
        // compute outer extents (final sizes)
        final float outerHalf = half * OUTER_RADIUS_MULT;
        final float outerHeight = (half * 2.2f) * this.heightMultiplier * OUTER_HEIGHT_SCALE;
        final float ox1 = px - outerHalf;
        final float ox2 = px + outerHalf;
        final float oz1 = pz - outerHalf;
        final float oz2 = pz + outerHalf;

        // final positions (where the outer skirt should end up)
        final float finalOuterBottom = py + 0.01f + OUTER_FINAL_BOTTOM_OFFSET; // slightly above inner bottom to reduce z-fight
        final float finalOuterTop = py + outerHeight;

        // starting positions (fully below ground by outerHeight)
        final float startOuterBottom = finalOuterBottom - outerHeight;
        final float startOuterTop = finalOuterTop - outerHeight;

        // compute rise progress: 0.0 -> fully below ground, 1.0 -> fully up
        final float riseStart = (float) OUTER_RISE_DELAY_TICKS;
        final float riseDur = Math.max(1.0f, (float) OUTER_RISE_DURATION_TICKS);
        final float riseProgress = MathHelper.clamp((ageWithDelta - riseStart) / riseDur, 0.0f, 1.0f);

        // smoothstep for nicer fade (t*t*(3-2t))
        final float t = riseProgress;
        final float smooth = t * t * (3.0f - 2.0f * t);
        final float outerAlpha = a * smooth;

        // lerp bottom & top between start and final positions
        final float yOuterBottom = MathHelper.lerp(riseProgress, startOuterBottom, finalOuterBottom);
        final float yOuterTop = MathHelper.lerp(riseProgress, startOuterTop, finalOuterTop);

        // --- OUTER side faces (use delayed sprites and fade alpha) ---
        // Note: use outer sprite UVs (osu1 .. osv2) and outerAlpha for color alpha
        // Outer Side 1
        vc.vertex(ox1, yOuterBottom, oz1).texture(osu1, osv2).color(r, g, b, outerAlpha).light(light).next();
        vc.vertex(ox2, yOuterBottom, oz1).texture(osu2, osv2).color(r, g, b, outerAlpha).light(light).next();
        vc.vertex(ox2, yOuterTop,    oz1).texture(osu2, osv1).color(r, g, b, outerAlpha).light(light).next();
        vc.vertex(ox1, yOuterTop,    oz1).texture(osu1, osv1).color(r, g, b, outerAlpha).light(light).next();

        vc.vertex(ox2, yOuterBottom, oz1).texture(osu2, osv2).color(r, g, b, outerAlpha).light(light).next();
        vc.vertex(ox1, yOuterBottom, oz1).texture(osu1, osv2).color(r, g, b, outerAlpha).light(light).next();
        vc.vertex(ox1, yOuterTop,    oz1).texture(osu1, osv1).color(r, g, b, outerAlpha).light(light).next();
        vc.vertex(ox2, yOuterTop,    oz1).texture(osu2, osv1).color(r, g, b, outerAlpha).light(light).next();

        // Outer Side 2
        vc.vertex(ox2, yOuterBottom, oz2).texture(osu1, osv2).color(r, g, b, outerAlpha).light(light).next();
        vc.vertex(ox1, yOuterBottom, oz2).texture(osu2, osv2).color(r, g, b, outerAlpha).light(light).next();
        vc.vertex(ox1, yOuterTop,    oz2).texture(osu2, osv1).color(r, g, b, outerAlpha).light(light).next();
        vc.vertex(ox2, yOuterTop,    oz2).texture(osu1, osv1).color(r, g, b, outerAlpha).light(light).next();

        vc.vertex(ox1, yOuterBottom, oz2).texture(osu1, osv2).color(r, g, b, outerAlpha).light(light).next();
        vc.vertex(ox2, yOuterBottom, oz2).texture(osu2, osv2).color(r, g, b, outerAlpha).light(light).next();
        vc.vertex(ox2, yOuterTop,    oz2).texture(osu2, osv1).color(r, g, b, outerAlpha).light(light).next();
        vc.vertex(ox1, yOuterTop,    oz2).texture(osu1, osv1).color(r, g, b, outerAlpha).light(light).next();

        // Outer Side 3
        vc.vertex(ox1, yOuterBottom, oz2).texture(osu1, osv2).color(r, g, b, outerAlpha).light(light).next();
        vc.vertex(ox1, yOuterBottom, oz1).texture(osu2, osv2).color(r, g, b, outerAlpha).light(light).next();
        vc.vertex(ox1, yOuterTop,    oz1).texture(osu2, osv1).color(r, g, b, outerAlpha).light(light).next();
        vc.vertex(ox1, yOuterTop,    oz2).texture(osu1, osv1).color(r, g, b, outerAlpha).light(light).next();

        vc.vertex(ox1, yOuterBottom, oz1).texture(osu1, osv2).color(r, g, b, outerAlpha).light(light).next();
        vc.vertex(ox1, yOuterBottom, oz2).texture(osu2, osv2).color(r, g, b, outerAlpha).light(light).next();
        vc.vertex(ox1, yOuterTop,    oz2).texture(osu2, osv1).color(r, g, b, outerAlpha).light(light).next();
        vc.vertex(ox1, yOuterTop,    oz1).texture(osu1, osv1).color(r, g, b, outerAlpha).light(light).next();

        // Outer Side 4
        vc.vertex(ox2, yOuterBottom, oz1).texture(osu1, osv2).color(r, g, b, outerAlpha).light(light).next();
        vc.vertex(ox2, yOuterBottom, oz2).texture(osu2, osv2).color(r, g, b, outerAlpha).light(light).next();
        vc.vertex(ox2, yOuterTop,    oz2).texture(osu2, osv1).color(r, g, b, outerAlpha).light(light).next();
        vc.vertex(ox2, yOuterTop,    oz1).texture(osu1, osv1).color(r, g, b, outerAlpha).light(light).next();

        vc.vertex(ox2, yOuterBottom, oz2).texture(osu2, osv2).color(r, g, b, outerAlpha).light(light).next();
        vc.vertex(ox2, yOuterBottom, oz1).texture(osu1, osv2).color(r, g, b, outerAlpha).light(light).next();
        vc.vertex(ox2, yOuterTop,    oz1).texture(osu1, osv1).color(r, g, b, outerAlpha).light(light).next();
        vc.vertex(ox2, yOuterTop,    oz2).texture(osu2, osv1).color(r, g, b, outerAlpha).light(light).next();

        // --- draw outer bottom face (duplicate of bottom, same radius as outer skirt) using delayed bottom sprite and outerAlpha ---
        if (outerBottomSprite != null) {
            final float obu1 = outerBottomSprite.getMinU();
            final float obu2 = outerBottomSprite.getMaxU();
            final float obv1 = outerBottomSprite.getMinV();
            final float obv2 = outerBottomSprite.getMaxV();

            // outer bottom uses ox1/ox2/oz1/oz2 at yOuterBottom
            vc.vertex(ox1, yOuterBottom, oz1).texture(obu1, obv1).color(r, g, b, outerAlpha).light(light).next();
            vc.vertex(ox2, yOuterBottom, oz1).texture(obu2, obv1).color(r, g, b, outerAlpha).light(light).next();
            vc.vertex(ox2, yOuterBottom, oz2).texture(obu2, obv2).color(r, g, b, outerAlpha).light(light).next();
            vc.vertex(ox1, yOuterBottom, oz2).texture(obu1, obv2).color(r, g, b, outerAlpha).light(light).next();

            // second winding
            vc.vertex(ox2, yOuterBottom, oz1).texture(obu2, obv1).color(r, g, b, outerAlpha).light(light).next();
            vc.vertex(ox1, yOuterBottom, oz1).texture(obu1, obv1).color(r, g, b, outerAlpha).light(light).next();
            vc.vertex(ox1, yOuterBottom, oz2).texture(obu1, obv2).color(r, g, b, outerAlpha).light(light).next();
            vc.vertex(ox2, yOuterBottom, oz2).texture(obu2, obv2).color(r, g, b, outerAlpha).light(light).next();
        }

        // --- draw inner bottom face with bottomSprite (flat quad on floor) ---
        if (bottomSprite != null) {
            final float bu1 = bottomSprite.getMinU();
            final float bu2 = bottomSprite.getMaxU();
            final float bv1 = bottomSprite.getMinV();
            final float bv2 = bottomSprite.getMaxV();

            // first winding (e.g., counter-clockwise)
            vc.vertex(x1, yBottom, z1).texture(bu1, bv1).color(r, g, b, a).light(light).next();
            vc.vertex(x2, yBottom, z1).texture(bu2, bv1).color(r, g, b, a).light(light).next();
            vc.vertex(x2, yBottom, z2).texture(bu2, bv2).color(r, g, b, a).light(light).next();
            vc.vertex(x1, yBottom, z2).texture(bu1, bv2).color(r, g, b, a).light(light).next();

            // second winding (clockwise)
            vc.vertex(x2, yBottom, z1).texture(bu2, bv1).color(r, g, b, a).light(light).next();
            vc.vertex(x1, yBottom, z1).texture(bu1, bv1).color(r, g, b, a).light(light).next();
            vc.vertex(x1, yBottom, z2).texture(bu1, bv2).color(r, g, b, a).light(light).next();
            vc.vertex(x2, yBottom, z2).texture(bu2, bv2).color(r, g, b, a).light(light).next();
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
}