// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.particle.custom.pipeline;

import net.minecraft.client.render.VertexConsumer;

public interface CuboidRenderable {
    void emit(VertexConsumer vc);
}