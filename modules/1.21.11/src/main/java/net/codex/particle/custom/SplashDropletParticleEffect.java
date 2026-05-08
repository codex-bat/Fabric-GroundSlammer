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

public final class SplashDropletParticleEffect implements ParticleEffect {
    private final ParticleType<SplashDropletParticleEffect> type;

    public final float amountMultiplier;
    public final float heightMultiplier;
    public final float red;
    public final float green;
    public final float blue;

    public SplashDropletParticleEffect(
            ParticleType<SplashDropletParticleEffect> type,
            float amountMultiplier,
            float heightMultiplier,
            float red,
            float green,
            float blue
    ) {
        this.type = type;
        this.amountMultiplier = amountMultiplier;
        this.heightMultiplier = heightMultiplier;
        this.red = MathHelper.clamp(red, 0.0f, 1.0f);
        this.green = MathHelper.clamp(green, 0.0f, 1.0f);
        this.blue = MathHelper.clamp(blue, 0.0f, 1.0f);
    }

    @Override
    public ParticleType<?> getType() {
        return this.type;
    }

    public static MapCodec<SplashDropletParticleEffect> codec(ParticleType<SplashDropletParticleEffect> type) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.FLOAT.fieldOf("amountMultiplier").forGetter(e -> e.amountMultiplier),
                Codec.FLOAT.fieldOf("heightMultiplier").forGetter(e -> e.heightMultiplier),
                Codec.FLOAT.fieldOf("red").forGetter(e -> e.red),
                Codec.FLOAT.fieldOf("green").forGetter(e -> e.green),
                Codec.FLOAT.fieldOf("blue").forGetter(e -> e.blue)
        ).apply(instance, (amount, height, red, green, blue) ->
                new SplashDropletParticleEffect(type, amount, height, red, green, blue)));
    }

    public static PacketCodec<RegistryByteBuf, SplashDropletParticleEffect> packetCodec(
            ParticleType<SplashDropletParticleEffect> type
    ) {
        return PacketCodec.tuple(
                PacketCodecs.FLOAT, e -> e.amountMultiplier,
                PacketCodecs.FLOAT, e -> e.heightMultiplier,
                PacketCodecs.FLOAT, e -> e.red,
                PacketCodecs.FLOAT, e -> e.green,
                PacketCodecs.FLOAT, e -> e.blue,
                (amount, height, red, green, blue) ->
                        new SplashDropletParticleEffect(type, amount, height, red, green, blue)
        );
    }
}