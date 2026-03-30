// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.mixin;

import net.codex.camera.CameraShakeManager;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    private float yaw;
    private float pitch;

    @Shadow public abstract Vec3d getPos();

    @Shadow public abstract void setPos(Vec3d pos);

    @Inject(method = "update", at = @At("TAIL"))
    private void groundslammer$applyShake(
            BlockView area,
            Entity focusedEntity,
            boolean thirdPerson,
            boolean inverseView,
            float tickDelta,
            CallbackInfo ci
    ) {
        var offset = CameraShakeManager.sampleOffset(tickDelta);
        float intensity = CameraShakeManager.getCurrentStrength(); // add this getter

        // Position
        setPos(getPos().add(offset.multiply(0.3 + intensity * 0.5)));
        setPos(getPos().add(0, -Math.abs(offset.y) * (0.4 + intensity), 0));

        // Rotation scales with impact
        float yawOffset = (float)(offset.x * (6.0 + 14.0 * intensity));
        float pitchOffset = (float)(offset.y * (6.0 + 14.0 * intensity));

        this.yaw += yawOffset;
        this.pitch += pitchOffset;
    }
}