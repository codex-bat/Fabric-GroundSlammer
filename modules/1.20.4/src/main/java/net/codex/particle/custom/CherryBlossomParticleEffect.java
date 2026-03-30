// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.particle.custom;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class CherryBlossomParticleEffect implements ParticleEffect {
    private final ParticleType<CherryBlossomParticleEffect> type;
    private final float r, g, b; // optional color data

    public CherryBlossomParticleEffect(ParticleType<CherryBlossomParticleEffect> type, float r, float g, float b) {
        this.type = type;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    @Override
    public ParticleType<?> getType() {
        return type;
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeFloat(r);
        buf.writeFloat(g);
        buf.writeFloat(b);
    }

    @Override
    public String asString() {
        return Registries.PARTICLE_TYPE.getId(type).toString();
    }

    // Factory for reading from packets and commands
    public static final Factory<CherryBlossomParticleEffect> FACTORY = new Factory<>() {
        @Override
        public CherryBlossomParticleEffect read(ParticleType<CherryBlossomParticleEffect> type, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            float r = reader.readFloat();
            reader.expect(' ');
            float g = reader.readFloat();
            reader.expect(' ');
            float b = reader.readFloat();
            return new CherryBlossomParticleEffect(type, r, g, b);
        }

        @Override
        public CherryBlossomParticleEffect read(ParticleType<CherryBlossomParticleEffect> type, PacketByteBuf buf) {
            return new CherryBlossomParticleEffect(type, buf.readFloat(), buf.readFloat(), buf.readFloat());
        }
    };
}