// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.particle.custom.pipeline;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;

import java.util.ArrayList;
import java.util.List;

public final class CuboidParticleRenderer extends ParticleRenderer<Particle> {
    public CuboidParticleRenderer(ParticleManager manager) {
        super(manager);
    }

    @Override
    public Submittable render(Frustum frustum, Camera camera, float tickDelta) {
        List<CuboidRenderable> drawList = new ArrayList<>();

        for (Particle particle : this.getParticles()) {
            if (!particle.isAlive()) continue;

            // Each of the ONCE SPRITEBILLBOARDPARTICLE's must have this method to capture its state
            CuboidRenderable state = ((CuboidParticle) particle).captureRenderState(camera, tickDelta);
            if (state != null) {
                drawList.add(state);
            }
        }

        return new CuboidRenderResult(drawList);
    }

    private record CuboidRenderResult(List<CuboidRenderable> drawList) implements Submittable {
        private static final RenderLayer LAYER = RenderLayer.of(
                "custom_cuboid_particle",
                RenderSetup.builder(RenderPipelines.TRANSLUCENT_PARTICLE)
                        .texture("Sampler0", SpriteAtlasTexture.PARTICLE_ATLAS_TEXTURE)
                        .translucent()
                        .build()
        );

        @Override
        public void submit(OrderedRenderCommandQueue queue, CameraRenderState cameraRenderState) {
            queue.submitCustom(new MatrixStack(), LAYER, (entry, vc) -> {
                for (CuboidRenderable renderable : drawList) {
                    renderable.emit(vc);
                }
            });
        }
    }
}