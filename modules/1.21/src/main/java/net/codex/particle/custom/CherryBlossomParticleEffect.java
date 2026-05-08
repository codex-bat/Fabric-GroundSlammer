// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.particle.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public final class CherryBlossomParticleEffect implements ParticleEffect {
    private final ParticleType<CherryBlossomParticleEffect> type;
    public final float red;
    public final float green;
    public final float blue;

    public CherryBlossomParticleEffect(ParticleType<CherryBlossomParticleEffect> type, float red, float green, float blue) {
        this.type = type;
        this.red = MathHelper.clamp(red, 0.0f, 1.0f);
        this.green = MathHelper.clamp(green, 0.0f, 1.0f);
        this.blue = MathHelper.clamp(blue, 0.0f, 1.0f);
    }

    @Override
    public ParticleType<?> getType() {
        return this.type;
    }

    public static MapCodec<CherryBlossomParticleEffect> codec(ParticleType<CherryBlossomParticleEffect> type) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.FLOAT.fieldOf("red").forGetter(e -> e.red),
                Codec.FLOAT.fieldOf("green").forGetter(e -> e.green),
                Codec.FLOAT.fieldOf("blue").forGetter(e -> e.blue)
        ).apply(instance, (red, green, blue) -> new CherryBlossomParticleEffect(type, red, green, blue)));
    }

    public static PacketCodec<RegistryByteBuf, CherryBlossomParticleEffect> packetCodec(ParticleType<CherryBlossomParticleEffect> type) {
        return PacketCodec.tuple(
                PacketCodecs.FLOAT.cast(), e -> e.red,
                PacketCodecs.FLOAT.cast(), e -> e.green,
                PacketCodecs.FLOAT.cast(), e -> e.blue,
                (red, green, blue) -> new CherryBlossomParticleEffect(type, red, green, blue)
        );
    }
}