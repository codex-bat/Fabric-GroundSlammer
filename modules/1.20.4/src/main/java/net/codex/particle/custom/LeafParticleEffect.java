// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.particle.custom;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.MathHelper;

public class LeafParticleEffect implements ParticleEffect {
    public static final Factory FACTORY = new Factory();

    private final ParticleType<LeafParticleEffect> type;
    public final float amountMultiplier;
    public final float randomness;
    public final float red;
    public final float green;
    public final float blue;

    public LeafParticleEffect(ParticleType<LeafParticleEffect> type,
                              float amountMultiplier, float randomness,
                              float red, float green, float blue) {
        this.type = type;
        this.amountMultiplier = amountMultiplier;
        this.randomness = randomness;
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
        buf.writeFloat(amountMultiplier);
        buf.writeFloat(randomness);
        buf.writeFloat(red);
        buf.writeFloat(green);
        buf.writeFloat(blue);
    }

    @Override
    public String asString() {
        return String.format("%s %.2f %.2f %.2f %.2f %.2f",
                Registries.PARTICLE_TYPE.getId(type),
                amountMultiplier, randomness, red, green, blue);
    }

    public static class Factory implements ParticleEffect.Factory<LeafParticleEffect> {
        @Override
        public LeafParticleEffect read(ParticleType<LeafParticleEffect> type,
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
            return new LeafParticleEffect(type, size, height, r, g, b);
        }

        @Override
        public LeafParticleEffect read(ParticleType<LeafParticleEffect> type,
                                       PacketByteBuf buf) {
            return new LeafParticleEffect(type,
                    buf.readFloat(), buf.readFloat(),
                    buf.readFloat(), buf.readFloat(), buf.readFloat());
        }
    }
}