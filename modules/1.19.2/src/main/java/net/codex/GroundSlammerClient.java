// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex;

import net.codex.listener.ElytraImpactListener;
import net.codex.listener.FallWindListener;
import net.codex.particle.ModParticleFactories;
import net.codex.particle.ModParticles;
import net.codex.particle.custom.GroundSplashParticleEffect;
import net.codex.particle.custom.LeafParticleEffect;
import net.codex.particle.custom.SplashDropletParticleEffect;
import net.codex.sound.ModSounds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;

import java.util.*;

public class GroundSlammerClient implements ClientModInitializer {
    public static final Set<Entity> SUPPRESSED_ENTITIES = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<Entity, Integer> suppressionTicks = new IdentityHashMap<>();
    public static boolean SCREENSHOT_MODE = false;

    private final Map<Entity, Double> prevVelYMap = new IdentityHashMap<>();
    private final Map<Entity, Boolean> prevOnGroundMap = new IdentityHashMap<>();
    private final Map<Entity, Long> lastSpawnMs = new IdentityHashMap<>();
    private final Map<Entity, PendingSpawn> pendingSpawns = new IdentityHashMap<>();

    private int cleanupTicker = 0;

    private static final double IMPACT_SCALE = 1.5;
    private static final float MIN_HEIGHT_MULT = 0.9f;
    private static final float MAX_HEIGHT_MULT = 6f;
    private static final float MIN_SIZE_MULT = 0.6f;
    private static final float MAX_SIZE_MULT = 3.0f;
    private static final float HEIGHT_REDUCTION_FACTOR = 2.0f;
    private static final int COMPLEX_EXTRA_BLOCKS = 2;
    private static final double MIN_IMPACT_VELOCITY = 0.9;
    private static final long SPAWN_COOLDOWN_MS = 100L;
    private static final long PENDING_TTL_MS = 300L;
    private static final int SAMPLE_GRID = 3;
    private static final double SPAWN_Y_EPS = 0.02D;

    @Override
    public void onInitializeClient() {
        ModParticleFactories.registerFactories();
        FallWindListener.register();
        //ElytraImpactListener.register();

        ClientTickEvents.START_CLIENT_TICK.register(this::onClientTickStart);
        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
    }

    private void onClientTick(MinecraftClient client) {
        ClientWorld world = client.world;
        if (world == null || client.isPaused()) {
            return;
        }

        long nowMs = System.currentTimeMillis();
        PlayerEntity player = client.player;

        processPendingSpawns(world, player, nowMs);

        Entity camera = client.getCameraEntity();
        if (camera == null) {
            return;
        }

        Box range = camera.getBoundingBox().expand(32.0D);
        for (Entity entity : world.getEntitiesByClass(Entity.class, range, e -> true)) {
            if (entity == null || entity.isRemoved()) {
                continue;
            }

            boolean wasOnGround = prevOnGroundMap.getOrDefault(entity, false);
            double prevVelY = prevVelYMap.getOrDefault(entity, 0.0D);
            double currVelY = entity.getVelocity().y;
            boolean isOnGround = entity.isOnGround();

            if (isOnGround && wasOnGround) {
                prevOnGroundMap.put(entity, true);
                prevVelYMap.put(entity, currVelY);
                continue;
            }

            if (!wasOnGround && isOnGround && prevVelY < -MIN_IMPACT_VELOCITY) {
                SUPPRESSED_ENTITIES.add(entity);
                suppressionTicks.put(entity, 2); // 2 ticks is perfect, I think!?

                long last = lastSpawnMs.getOrDefault(entity, 0L);
                if (nowMs - last >= SPAWN_COOLDOWN_MS) {
                    handleLanding(world, player, entity, nowMs, prevVelY);
                }
            }

            prevOnGroundMap.put(entity, isOnGround);
            prevVelYMap.put(entity, currVelY);
        }

        if ((cleanupTicker++ & 7) == 0) {
            cleanupRemovedEntities();
        }

        Iterator<Map.Entry<Entity, Integer>> it = suppressionTicks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Entity, Integer> entry = it.next();
            int ticks = entry.getValue() - 1;

            if (ticks <= 0) {
                SUPPRESSED_ENTITIES.remove(entry.getKey());
                it.remove();
            } else {
                entry.setValue(ticks);
            }
        }
    }



    private void onClientTickStart(MinecraftClient client) {
        ClientWorld world = client.world;
        if (world == null || client.isPaused()) {
            SCREENSHOT_MODE = false;
            return;
        }

        Entity camera = client.getCameraEntity();
        if (camera == null) return;

        Box range = camera.getBoundingBox().expand(32.0D);

        for (Entity entity : world.getEntitiesByClass(Entity.class, range, e -> true)) {
            boolean wasOnGround = prevOnGroundMap.getOrDefault(entity, false);
            double currVelY = entity.getVelocity().y;
            boolean isOnGround = entity.isOnGround();

            // PRE-LANDING DETECTION --- ABSOLUTE HOT GARBAGE 🔥
            if (!isOnGround && currVelY < -MIN_IMPACT_VELOCITY) {
                double nextY = entity.getBoundingBox().minY + currVelY;

                // If we're about to cross a block boundary downward → landing soon
                if (Math.floor(nextY) < Math.floor(entity.getBoundingBox().minY)) {
                    SUPPRESSED_ENTITIES.add(entity);
                    suppressionTicks.put(entity, 3); // slightly longer buffer
                }
            }
        }
    }

    private void processPendingSpawns(ClientWorld world, PlayerEntity player, long nowMs) {
        if (pendingSpawns.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<Entity, PendingSpawn>> it = pendingSpawns.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Entity, PendingSpawn> entry = it.next();
            Entity entity = entry.getKey();
            PendingSpawn ps = entry.getValue();

            if (entity == null || entity.isRemoved()) {
                it.remove();
                continue;
            }

            if (nowMs - ps.startMs > PENDING_TTL_MS) {
                it.remove();
                continue;
            }

            double feetY = entity.getBoundingBox().minY;
            if (feetY <= ps.spawnY + SPAWN_Y_EPS || entity.isOnGround()) {
                spawnGroundSlam(world, player, ps.spawnX, ps.spawnY, ps.spawnZ,
                        ps.sizeMultiplier, ps.heightMultiplier,
                        ps.red, ps.green, ps.blue,
                        ps.useSimple, ps.slamSound,
                        entity, ps.amountMultiplier);

                lastSpawnMs.put(entity, nowMs);
                it.remove();
            }
        }
    }

    private void handleLanding(ClientWorld world, PlayerEntity player, Entity entity, long nowMs, double prevVelY) {
        float entityWidth = entity.getWidth();
        float rawSize = entityWidth * 1.1f;
        float sizeMultiplier = MathHelper.clamp(rawSize, MIN_SIZE_MULT, MAX_SIZE_MULT);

        double impact = Math.abs(prevVelY);
        float rawHeight = 1.0f + (float) (impact / IMPACT_SCALE);

        float sizeFactor = 1.0f / (1.0f + (entityWidth - 0.6f) * HEIGHT_REDUCTION_FACTOR);
        sizeFactor = MathHelper.clamp(sizeFactor, 0.2f, 1.0f);

        float heightMultiplier = MathHelper.clamp(rawHeight * sizeFactor, MIN_HEIGHT_MULT, MAX_HEIGHT_MULT);

        Box bbox = entity.getBoundingBox();
        double minX = bbox.minX;
        double maxX = bbox.maxX;
        double minZ = bbox.minZ;
        double maxZ = bbox.maxZ;

        double entityX = entity.getX();
        double entityZ = entity.getZ();
        double entityY = entity.getY();
        double feetY = bbox.minY;

        double bestX = entityX;
        double bestZ = entityZ;
        double bestTopY = Double.NEGATIVE_INFINITY;
        boolean found = false;
        BlockPos bestBlockPos = null;

        for (int ix = 0; ix < SAMPLE_GRID; ix++) {
            double sx = MathHelper.lerp((double) ix / (SAMPLE_GRID - 1), minX, maxX);
            for (int iz = 0; iz < SAMPLE_GRID; iz++) {
                double sz = MathHelper.lerp((double) iz / (SAMPLE_GRID - 1), minZ, maxZ);

                BlockPos.Mutable scanPos = new BlockPos.Mutable(
                        MathHelper.floor(sx),
                        MathHelper.floor(entityY - 0.1D),
                        MathHelper.floor(sz)
                );

                BlockState state;
                VoxelShape shape;
                do {
                    state = world.getBlockState(scanPos);
                    if (state.isAir()) {
                        shape = null;
                    } else {
                        shape = state.getCollisionShape(world, scanPos);
                    }

                    if (!state.isAir() && !shape.isEmpty()) {
                        break;
                    }
                    scanPos.move(0, -1, 0);
                } while (scanPos.getY() > world.getBottomY());

                if (!state.isAir() && !shape.isEmpty()) {
                    double topY = scanPos.getY() + shape.getMax(Direction.Axis.Y);
                    if (!found || topY > bestTopY) {
                        found = true;
                        bestTopY = topY;
                        bestX = sx;
                        bestZ = sz;
                        bestBlockPos = scanPos.toImmutable();
                    } else if (Math.abs(topY - bestTopY) < 1.0E-6D) {
                        double prevDx = bestX - entityX;
                        double prevDz = bestZ - entityZ;
                        double thisDx = sx - entityX;
                        double thisDz = sz - entityZ;
                        double prevDistSq = prevDx * prevDx + prevDz * prevDz;
                        double thisDistSq = thisDx * thisDx + thisDz * thisDz;

                        if (thisDistSq < prevDistSq) {
                            bestX = sx;
                            bestZ = sz;
                            bestBlockPos = scanPos.toImmutable();
                        }
                    }
                }
            }
        }

        double spawnX;
        double spawnY;
        double spawnZ;
        float red = 1.0f;
        float green = 1.0f;
        float blue = 1.0f;

        if (found) {
            spawnX = bestX;
            spawnZ = bestZ;
            spawnY = bestTopY;

            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc != null) {
                float[] rgb = computeLandingColor(world, bestBlockPos, world.getBlockState(bestBlockPos), mc);
                red = rgb[0];
                green = rgb[1];
                blue = rgb[2];
            }
        } else {
            spawnX = entityX;
            spawnZ = entityZ;
            spawnY = feetY;
        }

        BlockPos landingBlockPos = new BlockPos(spawnX, spawnY - 0.1D, spawnZ);
        BlockState blockUnder = world.getBlockState(landingBlockPos);

        boolean onLeaves = blockUnder.isIn(BlockTags.LEAVES);
        boolean isSnow = blockUnder.isOf(Blocks.SNOW) || blockUnder.isOf(Blocks.SNOW_BLOCK);
        boolean isIce = blockUnder.isOf(Blocks.ICE)
                || blockUnder.isOf(Blocks.PACKED_ICE)
                || blockUnder.isOf(Blocks.BLUE_ICE)
                || blockUnder.isOf(Blocks.FROSTED_ICE);
        boolean isGlassBlock = blockUnder.getMaterial() == Material.GLASS;

        SoundEvent slamToPlay = ModSounds.SLAM;
        if (isSnow) {
            slamToPlay = ModSounds.SNOW_SLAM;
        } else if (isIce || isGlassBlock) {
            slamToPlay = ModSounds.ICE_SLAM;
        }

        float amountMultiplier = 0;
        if (feetY <= spawnY + SPAWN_Y_EPS || entity.isOnGround()) {
            amountMultiplier = MathHelper.clamp(entityWidth, 0.6f, 3.0f);

            if (onLeaves) {
                slamToPlay = ModSounds.LEAF_SLAM;

                int color = blockUnder.getMapColor(world, landingBlockPos).color;
                float r = channel(color, 16);
                float g = channel(color, 8);
                float b = channel(color, 0);

                int leafCount = (int) (8 * sizeMultiplier);
                ParticleType<LeafParticleEffect> leafParticle = getLeafParticle(blockUnder);
                spawnLeafParticles(world, spawnX, spawnY, spawnZ, leafCount, r, g, b, leafParticle, player);
            } else {
                boolean useSimple = isAreaOpen(world, entity, spawnX, spawnY, spawnZ);
                spawnGroundSlam(world, player, spawnX, spawnY, spawnZ,
                        sizeMultiplier, heightMultiplier,
                        red, green, blue,
                        useSimple, slamToPlay,
                        entity, amountMultiplier);
            }

            lastSpawnMs.put(entity, nowMs);
        } else {
            boolean useSimple = isAreaOpen(world, entity, spawnX, spawnY, spawnZ);
            pendingSpawns.put(entity, new PendingSpawn(
                    spawnX, spawnY, spawnZ,
                    sizeMultiplier, heightMultiplier,
                    red, green, blue,
                    useSimple, amountMultiplier, nowMs, slamToPlay
            ));
        }
    }

    private float[] computeLandingColor(ClientWorld world, BlockPos pos, BlockState landingState, MinecraftClient mc) {
        try {
            if (landingState.isOf(Blocks.GLASS)) {
                return new float[]{1.0f, 1.0f, 1.0f};
            }

            int mapColorInt = landingState.getMapColor(world, pos).color;
            float mapR = channel(mapColorInt, 16);
            float mapG = channel(mapColorInt, 8);
            float mapB = channel(mapColorInt, 0);

            int tintInt = mc.getBlockColors().getColor(landingState, world, pos, 0);
            float tintR = channel(tintInt, 16);
            float tintG = channel(tintInt, 8);
            float tintB = channel(tintInt, 0);

            return new float[]{mapR * tintR, mapG * tintG, mapB * tintB};
        } catch (Exception ignored) {
            return new float[]{1.0f, 1.0f, 1.0f};
        }
    }

    private void spawnGroundSlam(ClientWorld world, PlayerEntity player,
                                 double x, double y, double z,
                                 float sizeMultiplier, float heightMultiplier,
                                 float red, float green, float blue,
                                 boolean useSimple, SoundEvent slamSound,
                                 Entity source, float amountMultiplier) {
        ParticleType<GroundSplashParticleEffect> slamType = useSimple ? ModParticles.SIMPLE_SLAM : ModParticles.OUTTER_SLAM;
        ParticleType<GroundSplashParticleEffect> impactType = useSimple ? ModParticles.IMPACTINY : ModParticles.IMPACT;

        world.addParticle(new GroundSplashParticleEffect(
                slamType,
                sizeMultiplier,
                heightMultiplier,
                red, green, blue
        ), x, y, z, 0, 0, 0);

        world.addParticle(new GroundSplashParticleEffect(
                impactType,
                sizeMultiplier,
                heightMultiplier,
                1.0f, 1.0f, 1.0f
        ), x, y, z, 0, 0, 0);

        world.playSound(
                player,
                x, y, z,
                slamSound,
                SoundCategory.BLOCKS,
                1.0f,
                1.0f
        );

        spawnDropletBurst(world, x, y, z, heightMultiplier, red, green, blue, amountMultiplier);
    }

    private ParticleType<LeafParticleEffect> getLeafParticle(BlockState state) {
        if (state.isOf(net.minecraft.block.Blocks.MANGROVE_LEAVES)) {
            return ModParticles.LEAF_MANGROVE_PARTICLE;
        } else if (state.isOf(net.minecraft.block.Blocks.JUNGLE_LEAVES)) {
            return ModParticles.LEAF_JUNGLE_PARTICLE;
        } else if (state.isOf(net.minecraft.block.Blocks.AZALEA_LEAVES) || state.isOf(net.minecraft.block.Blocks.FLOWERING_AZALEA_LEAVES)) {
            return ModParticles.LEAF_AZALEA_PARTICLE;
        } else {
            return ModParticles.LEAF_PARTICLE;
        }
    }

    private boolean isAreaOpen(ClientWorld world, Entity entity, double spawnX, double spawnY, double spawnZ) {
        float width = entity.getWidth();
        float playerWidth = 0.6f;

        int radius = Math.max(1, (int) Math.floor(width) + 1);
        int threshold = (int) Math.ceil(3.0D * (width / playerWidth));
        int solidCount = 0;
        int baseX = MathHelper.floor(spawnX);
        int baseZ = MathHelper.floor(spawnZ);
        int startY = MathHelper.floor(spawnY + 2);
        int minY = MathHelper.floor(spawnY - 3);

        for (int dx = -radius; dx <= radius; dx++) {
            int bx = baseX + dx;
            for (int dz = -radius; dz <= radius; dz++) {
                int bz = baseZ + dz;

                BlockPos.Mutable scanPos = new BlockPos.Mutable(bx, startY, bz);
                while (scanPos.getY() > minY) {
                    BlockState state = world.getBlockState(scanPos);
                    if (state.isAir()) {
                        scanPos.move(0, -1, 0);
                        continue;
                    }

                    VoxelShape shape = state.getCollisionShape(world, scanPos);
                    if (!shape.isEmpty()) {
                        double topY = scanPos.getY() + 1.0D;
                        if (Math.abs(topY - spawnY) <= 0.5D) {
                            solidCount++;
                        }
                        break;
                    }

                    scanPos.move(0, -1, 0);
                }
            }
        }

        return solidCount < (threshold + COMPLEX_EXTRA_BLOCKS);
    }

    private void spawnDropletBurst(ClientWorld world, double cx, double cy, double cz,
                                   float heightMultiplier,
                                   float red, float green, float blue, float amountMultiplier) {
        int count = (int) (8 * amountMultiplier);
        double upwardMin = 0.25;
        double upwardMax = 0.45;
        boolean rainingHere = world.isRaining() || world.hasRain(new BlockPos(cx, cy, cz));
        ParticleType<SplashDropletParticleEffect> chosenType = rainingHere
                ? ModParticles.SPLASH_PIXEL_RAIN
                : ModParticles.SPLASH_PIXEL;

        for (int i = 0; i < count; i++) {
            double angle = world.random.nextDouble() * Math.PI * 2.0D;
            double baseOutward = 0.2D * amountMultiplier;
            double randomFactor = 0.8D + 0.4D * world.random.nextDouble();
            double speed = baseOutward * randomFactor;

            double vx = Math.cos(angle) * speed;
            double vz = Math.sin(angle) * speed;
            double vy = upwardMin + world.random.nextDouble() * (upwardMax - upwardMin);

            double ox = cx + (world.random.nextDouble() - 0.5D) * 0.08D;
            double oz = cz + (world.random.nextDouble() - 0.5D) * 0.08D;
            double oy = cy + 0.02D;

            world.addParticle(new SplashDropletParticleEffect(
                    chosenType,
                    amountMultiplier,
                    heightMultiplier,
                    red, green, blue
            ), ox, oy, oz, vx, vy, vz);
        }
    }

    public static void spawnLeafParticles(
            ClientWorld world,
            double x, double y, double z,
            int leafCount,
            float r, float g, float b,
            ParticleType<LeafParticleEffect> particleType,
            PlayerEntity player) {
        world.playSound(
                player,
                x, y, z,
                ModSounds.LEAF_SLAM,
                SoundCategory.BLOCKS,
                1.0f, 1.0f
        );

        for (int i = 0; i < leafCount; i++) {
            double angle = world.random.nextDouble() * Math.PI * 2.0D;
            double radialOffset = 0.06D + world.random.nextDouble() * 0.22D;
            double spawnX = x + Math.cos(angle) * radialOffset;
            double spawnZ = z + Math.sin(angle) * radialOffset;
            double spawnY = y + (world.random.nextDouble() * 0.08D);

            double vx = Math.cos(angle) * 0.03D;
            double vz = Math.sin(angle) * 0.03D;
            double vy = 0.02D + world.random.nextDouble() * 0.04D;

            world.addParticle(new LeafParticleEffect(
                    particleType,
                    0.05f,
                    0.1f,
                    r, g, b
            ), spawnX, spawnY, spawnZ, vx, vy, vz);
        }
    }

    private void cleanupRemovedEntities() {
        removeRemovedEntities(prevVelYMap);
        removeRemovedEntities(prevOnGroundMap);
        removeRemovedEntities(lastSpawnMs);
        removeRemovedEntities(pendingSpawns);
    }

    private <T> void removeRemovedEntities(Map<Entity, T> map) {
        map.keySet().removeIf(Entity::isRemoved);
    }

    private static float channel(int color, int shift) {
        return ((color >> shift) & 0xFF) / 255.0f;
    }

    private static final class PendingSpawn {
        final double spawnX, spawnY, spawnZ;
        final float sizeMultiplier, heightMultiplier;
        final float red, green, blue;
        final boolean useSimple;
        final long startMs;
        final SoundEvent slamSound;
        final float amountMultiplier;

        PendingSpawn(double spawnX, double spawnY, double spawnZ,
                     float sizeMultiplier, float heightMultiplier,
                     float red, float green, float blue,
                     boolean useSimple, float amountMultiplier, long startMs, SoundEvent slamSound) {
            this.spawnX = spawnX;
            this.spawnY = spawnY;
            this.spawnZ = spawnZ;
            this.sizeMultiplier = sizeMultiplier;
            this.heightMultiplier = heightMultiplier;
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.useSimple = useSimple;
            this.amountMultiplier = amountMultiplier;
            this.startMs = startMs;
            this.slamSound = slamSound;
        }
    }
}
