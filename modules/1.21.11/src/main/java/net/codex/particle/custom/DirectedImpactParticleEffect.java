// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.particle.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.codex.particle.ModParticles;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.math.MathHelper;

public final class DirectedImpactParticleEffect implements ParticleEffect {
    private final ParticleType<DirectedImpactParticleEffect> type;

    public final float red;
    public final float green;
    public final float blue;
    public final float sizeMultiplier;
    public final float yaw;
    public final float pitch;

    public DirectedImpactParticleEffect(
            ParticleType<DirectedImpactParticleEffect> type,
            float red,
            float green,
            float blue,
            float sizeMultiplier,
            float yaw,
            float pitch
    ) {
        this.type = type;
        this.red = MathHelper.clamp(red, 0.0f, 1.0f);
        this.green = MathHelper.clamp(green, 0.0f, 1.0f);
        this.blue = MathHelper.clamp(blue, 0.0f, 1.0f);
        this.sizeMultiplier = sizeMultiplier;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public ParticleType<?> getType() {
        return this.type;
    }

    public static MapCodec<DirectedImpactParticleEffect> codec(ParticleType<DirectedImpactParticleEffect> type) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.FLOAT.fieldOf("red").forGetter(e -> e.red),
                Codec.FLOAT.fieldOf("green").forGetter(e -> e.green),
                Codec.FLOAT.fieldOf("blue").forGetter(e -> e.blue),
                Codec.FLOAT.fieldOf("sizeMultiplier").forGetter(e -> e.sizeMultiplier),
                Codec.FLOAT.fieldOf("yaw").forGetter(e -> e.yaw),
                Codec.FLOAT.fieldOf("pitch").forGetter(e -> e.pitch)
        ).apply(instance, (red, green, blue, sizeMultiplier, yaw, pitch) ->
                new DirectedImpactParticleEffect(type, red, green, blue, sizeMultiplier, yaw, pitch)));
    }

    public static PacketCodec<RegistryByteBuf, DirectedImpactParticleEffect> packetCodec(
            ParticleType<DirectedImpactParticleEffect> type
    ) {
        return PacketCodec.tuple(
                PacketCodecs.FLOAT.cast(), e -> e.red,
                PacketCodecs.FLOAT.cast(), e -> e.green,
                PacketCodecs.FLOAT.cast(), e -> e.blue,
                PacketCodecs.FLOAT.cast(), e -> e.sizeMultiplier,
                PacketCodecs.FLOAT.cast(), e -> e.yaw,
                PacketCodecs.FLOAT.cast(), e -> e.pitch,
                (red, green, blue, sizeMultiplier, yaw, pitch) ->
                        new DirectedImpactParticleEffect(type, red, green, blue, sizeMultiplier, yaw, pitch)
        );
    }

    public static DirectedImpactParticleEffect of(
            float red, float green, float blue,
            float sizeMultiplier, float yaw, float pitch
    ) {
        return new DirectedImpactParticleEffect(ModParticles.DIRECTED_IMPACT, red, green, blue, sizeMultiplier, yaw, pitch);
    }
}