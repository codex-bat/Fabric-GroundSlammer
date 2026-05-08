// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.particle.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.math.MathHelper;

public final class GroundSplashParticleEffect implements ParticleEffect {
    private final ParticleType<GroundSplashParticleEffect> type;

    public final float sizeMultiplier;
    public final float heightMultiplier;
    public final float red;
    public final float green;
    public final float blue;

    public GroundSplashParticleEffect(
            ParticleType<GroundSplashParticleEffect> type,
            float sizeMultiplier,
            float heightMultiplier,
            float red,
            float green,
            float blue
    ) {
        this.type = type;
        this.sizeMultiplier = sizeMultiplier;
        this.heightMultiplier = heightMultiplier;
        this.red = MathHelper.clamp(red, 0.0f, 1.0f);
        this.green = MathHelper.clamp(green, 0.0f, 1.0f);
        this.blue = MathHelper.clamp(blue, 0.0f, 1.0f);
    }

    @Override
    public ParticleType<?> getType() {
        return this.type;
    }

    public static MapCodec<GroundSplashParticleEffect> codec(ParticleType<GroundSplashParticleEffect> type) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.FLOAT.fieldOf("sizeMultiplier").forGetter(e -> e.sizeMultiplier),
                Codec.FLOAT.fieldOf("heightMultiplier").forGetter(e -> e.heightMultiplier),
                Codec.FLOAT.fieldOf("red").forGetter(e -> e.red),
                Codec.FLOAT.fieldOf("green").forGetter(e -> e.green),
                Codec.FLOAT.fieldOf("blue").forGetter(e -> e.blue)
        ).apply(instance, (sizeMultiplier, heightMultiplier, red, green, blue) ->
                new GroundSplashParticleEffect(type, sizeMultiplier, heightMultiplier, red, green, blue)));
    }

    public static PacketCodec<RegistryByteBuf, GroundSplashParticleEffect> packetCodec(
            ParticleType<GroundSplashParticleEffect> type
    ) {
        return PacketCodec.tuple(
                PacketCodecs.FLOAT.cast(), e -> e.sizeMultiplier,
                PacketCodecs.FLOAT.cast(), e -> e.heightMultiplier,
                PacketCodecs.FLOAT.cast(), e -> e.red,
                PacketCodecs.FLOAT.cast(), e -> e.green,
                PacketCodecs.FLOAT.cast(), e -> e.blue,
                (sizeMultiplier, heightMultiplier, red, green, blue) ->
                        new GroundSplashParticleEffect(type, sizeMultiplier, heightMultiplier, red, green, blue)
        );
    }
}