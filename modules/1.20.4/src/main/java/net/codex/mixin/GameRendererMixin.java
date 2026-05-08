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
            method = "getFov(Lnet/minecraft/client/render/Camera;FZ)D",
            at = @At("RETURN"),
            cancellable = true
    )
    private void codex$fallFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Double> cir) {
        float i = FallWindListener.getFallVisualIntensity(tickDelta);
        if (i <= 0.0f) return;

        double maxBoost = 6.0D;
        double eased = 1.0 - Math.pow(1.0 - i, 2.0);
        cir.setReturnValue(cir.getReturnValueD() + (maxBoost * eased));
    }
}