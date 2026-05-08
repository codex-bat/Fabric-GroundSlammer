// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.mixin;

import net.codex.listener.FallWindListener;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(
            method = "getFov(Lnet/minecraft/client/render/Camera;FZ)F",
            at = @At("RETURN"),
            cancellable = true
    )
    private void codex$fallFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
        float intensity = FallWindListener.getFallVisualIntensity(tickDelta);
        if (intensity <= 0.0F) return;

        float maxBoost = 6.0F;
        float eased = 1.0F - (float) Math.pow(1.0F - intensity, 2.0);

        cir.setReturnValue(cir.getReturnValueF() + (maxBoost * eased));
    }
}