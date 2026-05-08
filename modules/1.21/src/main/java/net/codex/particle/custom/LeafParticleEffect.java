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
import net.minecraft.registry.Registries;
import net.minecraft.util.math.MathHelper;

public final class LeafParticleEffect implements ParticleEffect {
    private final ParticleType<LeafParticleEffect> type;
    public final float amountMultiplier;
    public final float randomness;
    public final float red;
    public final float green;
    public final float blue;

    public LeafParticleEffect(ParticleType<LeafParticleEffect> type,
                              float amountMultiplier,
                              float randomness,
                              float red,
                              float green,
                              float blue) {
        this.type = type;
        this.amountMultiplier = amountMultiplier;
        this.randomness = randomness;
        this.red = MathHelper.clamp(red, 0.0f, 1.0f);
        this.green = MathHelper.clamp(green, 0.0f, 1.0f);
        this.blue = MathHelper.clamp(blue, 0.0f, 1.0f);
    }

    @Override
    public ParticleType<?> getType() {
        return this.type;
    }

    public static MapCodec<LeafParticleEffect> codec(ParticleType<LeafParticleEffect> type) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.FLOAT.fieldOf("amountMultiplier").forGetter(e -> e.amountMultiplier),
                Codec.FLOAT.fieldOf("randomness").forGetter(e -> e.randomness),
                Codec.FLOAT.fieldOf("red").forGetter(e -> e.red),
                Codec.FLOAT.fieldOf("green").forGetter(e -> e.green),
                Codec.FLOAT.fieldOf("blue").forGetter(e -> e.blue)
        ).apply(instance, (amountMultiplier, randomness, red, green, blue) ->
                new LeafParticleEffect(type, amountMultiplier, randomness, red, green, blue)));
    }

    public static PacketCodec<RegistryByteBuf, LeafParticleEffect> packetCodec(ParticleType<LeafParticleEffect> type) {
        return PacketCodec.tuple(
                PacketCodecs.FLOAT.cast(), e -> e.amountMultiplier,
                PacketCodecs.FLOAT.cast(), e -> e.randomness,
                PacketCodecs.FLOAT.cast(), e -> e.red,
                PacketCodecs.FLOAT.cast(), e -> e.green,
                PacketCodecs.FLOAT.cast(), e -> e.blue,
                (amountMultiplier, randomness, red, green, blue) ->
                        new LeafParticleEffect(type, amountMultiplier, randomness, red, green, blue)
        );
    }
}