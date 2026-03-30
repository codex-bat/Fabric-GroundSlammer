// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.util;

import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class VoidFogUtil {
    public static float computeEnclosure(ClientWorld world, Vec3d origin, float maxDist) {
        Vec3d[] directions = new Vec3d[] {
                new Vec3d( 1, 0, 0), new Vec3d(-1, 0, 0),
                new Vec3d( 0, 0, 1), new Vec3d( 0, 0,-1),
                new Vec3d( 1, 0, 1).normalize(), new Vec3d(-1, 0, 1).normalize(),
                new Vec3d( 1, 0,-1).normalize(), new Vec3d(-1, 0,-1).normalize(),
                new Vec3d( 0, 1, 0), new Vec3d( 0,-1, 0)
        };

        float total = 0.0f;
        for (Vec3d dir : directions) {
            total += rayDistanceToSolid(world, origin, dir, maxDist);
        }

        float avg = total / directions.length;
        return 1.0f - MathHelper.clamp(avg / maxDist, 0.0f, 1.0f);
    }


    public static float rayDistanceToSolid(ClientWorld world, Vec3d origin, Vec3d dir, float maxDist) {
        final float step = 0.5f;

        for (float dist = step; dist <= maxDist; dist += step) {
            Vec3d p = origin.add(dir.multiply(dist));
            BlockPos pos = new BlockPos(
                    MathHelper.floor(p.x),
                    MathHelper.floor(p.y),
                    MathHelper.floor(p.z)
            );
            BlockState state = world.getBlockState(pos);

            // Collision shape is a much better "room wall" signal than light.
            if (!state.getCollisionShape(world, pos).isEmpty()) {
                return dist;
            }
        }

        return maxDist;
    }
}