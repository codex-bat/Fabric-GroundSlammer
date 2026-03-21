// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.particle.custom;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.codex.particle.ModParticles;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;

public class DirectedImpactParticleEffect implements ParticleEffect {
    public static final Codec<DirectedImpactParticleEffect> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("color").forGetter(effect ->
                            ((int)(effect.red * 255) << 16) |
                                    ((int)(effect.green * 255) << 8) |
                                    (int)(effect.blue * 255)
                    ),
                    Codec.FLOAT.fieldOf("scale").forGetter(effect -> effect.sizeMultiplier),
                    Codec.FLOAT.fieldOf("yaw").forGetter(effect -> effect.yaw),
                    Codec.FLOAT.fieldOf("pitch").forGetter(effect -> effect.pitch)   // <--- added
            ).apply(instance, (Integer color, Float scale, Float yaw, Float pitch) -> {
                float r = ((color >> 16) & 0xFF) / 255f;
                float g = ((color >> 8) & 0xFF) / 255f;
                float b = (color & 0xFF) / 255f;
                return new DirectedImpactParticleEffect(r, g, b, scale, yaw, pitch);
            }));

    public static final ParticleEffect.Factory<DirectedImpactParticleEffect> FACTORY = new ParticleEffect.Factory<>() {
        @Override
        public DirectedImpactParticleEffect read(ParticleType<DirectedImpactParticleEffect> type, PacketByteBuf buf) {
            return new DirectedImpactParticleEffect(
                    buf.readFloat(), buf.readFloat(), buf.readFloat(), // r, g, b
                    buf.readFloat(), buf.readFloat(), buf.readFloat()  // scale, yaw, pitch
            );
        }

        @Override
        public DirectedImpactParticleEffect read(ParticleType<DirectedImpactParticleEffect> type, StringReader reader) throws CommandSyntaxException {
            float r = reader.readFloat();
            reader.expect(' ');
            float g = reader.readFloat();
            reader.expect(' ');
            float b = reader.readFloat();
            reader.expect(' ');
            float scale = reader.readFloat();
            reader.expect(' ');
            float yaw = reader.readFloat();
            reader.expect(' ');
            float pitch = reader.readFloat();
            return new DirectedImpactParticleEffect(r, g, b, scale, yaw, pitch);
        }
    };

    public final float red, green, blue;
    public final float sizeMultiplier;
    public final float yaw;
    public final float pitch;

    public DirectedImpactParticleEffect(float red, float green, float blue, float sizeMultiplier, float yaw, float pitch) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.sizeMultiplier = sizeMultiplier;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public ParticleType<?> getType() {
        return ModParticles.DIRECTED_IMPACT;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeFloat(red);
        buf.writeFloat(green);
        buf.writeFloat(blue);
        buf.writeFloat(sizeMultiplier);
        buf.writeFloat(yaw);
        buf.writeFloat(pitch);
    }

    @Override
    public String asString() {
        return String.format("directed_impact: r=%f,g=%f,b=%f,scale=%f,yaw=%f", red, green, blue, sizeMultiplier, yaw, pitch);
    }
}