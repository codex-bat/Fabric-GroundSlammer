package net.codex.mixin.client;

import net.codex.particle.custom.CuboidParticlesSheet;
import net.codex.particle.custom.pipeline.CuboidParticleRenderer;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleRenderer;
import net.minecraft.client.particle.ParticleTextureSheet;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.ArrayList;
import java.util.List;

@Mixin(ParticleManager.class)
abstract class ParticleManagerMixin {

    @Shadow @Final @Mutable
    private static List<ParticleTextureSheet> PARTICLE_TEXTURE_SHEETS;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void codex$addCustomCuboidSheet(CallbackInfo ci) {
        PARTICLE_TEXTURE_SHEETS = new ArrayList<>(PARTICLE_TEXTURE_SHEETS);
        PARTICLE_TEXTURE_SHEETS.add(CuboidParticlesSheet.SHEET);
    }

    @Inject(method = "createParticleRenderer", at = @At("HEAD"), cancellable = true)
    private void codex$createCuboidRenderer(ParticleTextureSheet sheet,
                                            CallbackInfoReturnable<ParticleRenderer<?>> cir) {
        if (CuboidParticlesSheet.SHEET.equals(sheet)) {
            cir.setReturnValue(new CuboidParticleRenderer((ParticleManager)(Object)this));
        }
    }
}