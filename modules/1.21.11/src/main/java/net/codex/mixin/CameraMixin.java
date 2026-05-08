// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.mixin;

import net.codex.camera.CameraShakeManager;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow public abstract Vec3d getCameraPos();
    @Shadow protected abstract void setPos(Vec3d pos);

    @Shadow public abstract float getYaw();
    @Shadow public abstract float getPitch();
    @Shadow protected abstract void setRotation(float yaw, float pitch);

    @Inject(method = "update", at = @At("TAIL"))
    private void groundslammer$applyShake(
            World area,
            Entity focusedEntity,
            boolean thirdPerson,
            boolean inverseView,
            float tickDelta,
            CallbackInfo ci
    ) {
        Vec3d offset = CameraShakeManager.sampleOffset(tickDelta);
        float intensity = CameraShakeManager.getCurrentStrength();

        Vec3d pos = getCameraPos();

        // Position shake
        pos = pos.add(offset.multiply(0.3 + intensity * 0.5));
        pos = pos.add(0.0, -Math.abs(offset.y) * (0.4 + intensity), 0.0);
        setPos(pos);

        // Rotation shake
        float yawOffset = (float) (offset.x * (6.0 + 14.0 * intensity));
        float pitchOffset = (float) (offset.y * (6.0 + 14.0 * intensity));

        setRotation(getYaw() + yawOffset, getPitch() + pitchOffset);
    }
}