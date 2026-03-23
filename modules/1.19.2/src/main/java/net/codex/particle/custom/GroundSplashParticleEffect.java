// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.particle.custom;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;

public class GroundSplashParticleEffect implements ParticleEffect {
    public static final Factory FACTORY = new Factory();

    private final ParticleType<GroundSplashParticleEffect> type;
    public final float sizeMultiplier;
    public final float heightMultiplier;
    public final float red;
    public final float green;
    public final float blue;

    public GroundSplashParticleEffect(ParticleType<GroundSplashParticleEffect> type,
                                      float sizeMultiplier, float heightMultiplier,
                                      float red, float green, float blue) {
        this.type = type;
        this.sizeMultiplier = sizeMultiplier;
        this.heightMultiplier = heightMultiplier;
        this.red = MathHelper.clamp(red, 0.0f, 1.0f);
        this.green = MathHelper.clamp(green, 0.0f, 1.0f);
        this.blue = MathHelper.clamp(blue, 0.0f, 1.0f);
    }

    @Override
    public ParticleType<?> getType() {
        return type;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeFloat(sizeMultiplier);
        buf.writeFloat(heightMultiplier);
        buf.writeFloat(red);
        buf.writeFloat(green);
        buf.writeFloat(blue);
    }

    @Override
    public String asString() {
        return String.format("%s %.2f %.2f %.2f %.2f %.2f",
                Registry.PARTICLE_TYPE.getId(type),
                sizeMultiplier, heightMultiplier, red, green, blue);
    }

    public static class Factory implements ParticleEffect.Factory<GroundSplashParticleEffect> {
        @Override
        public GroundSplashParticleEffect read(ParticleType<GroundSplashParticleEffect> type,
                                               StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            float size = reader.readFloat();
            reader.expect(' ');
            float height = reader.readFloat();
            reader.expect(' ');
            float r = reader.readFloat();
            reader.expect(' ');
            float g = reader.readFloat();
            reader.expect(' ');
            float b = reader.readFloat();
            return new GroundSplashParticleEffect(type, size, height, r, g, b);
        }

        @Override
        public GroundSplashParticleEffect read(ParticleType<GroundSplashParticleEffect> type,
                                               PacketByteBuf buf) {
            return new GroundSplashParticleEffect(type,
                    buf.readFloat(), buf.readFloat(),
                    buf.readFloat(), buf.readFloat(), buf.readFloat());
        }
    }
}