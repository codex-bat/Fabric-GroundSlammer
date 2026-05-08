// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.particle.custom.pipeline;

import net.codex.particle.custom.CuboidParticlesSheet;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.world.ClientWorld;

public abstract class CuboidParticle extends Particle {
    protected CuboidParticle(ClientWorld world, double x, double y, double z) {
        super(world, x, y, z);
    }

    @Override
    public ParticleTextureSheet textureSheet() {
        return CuboidParticlesSheet.SHEET;
    }

    public abstract CuboidRenderable captureRenderState(net.minecraft.client.render.Camera camera, float tickDelta);
}