// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.particle;

import com.mojang.serialization.Codec;
import net.codex.particle.custom.*;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import javax.swing.*;


public class ModParticles {
    public static final String MODID = "groundslammer";

    public static final ParticleType<GroundSplashParticleEffect> OUTTER_SLAM =
            register("outter_slam", new ParticleType<>(false, GroundSplashParticleEffect.FACTORY) {
                @Override
                public Codec<GroundSplashParticleEffect> getCodec() {
                    return null;
                }
            });

    public static final ParticleType<GroundSplashParticleEffect> SIMPLE_SLAM =
            register("simple_slam", new ParticleType<>(false, GroundSplashParticleEffect.FACTORY) {
                @Override
                public Codec<GroundSplashParticleEffect> getCodec() {
                    return null;
                }
            });

    public static final ParticleType<GroundSplashParticleEffect> IMPACT =
            register("impact", new ParticleType<>(false, GroundSplashParticleEffect.FACTORY) {
                @Override
                public Codec<GroundSplashParticleEffect> getCodec() {
                    return null;
                }
            });

    public static final ParticleType<GroundSplashParticleEffect> IMPACTINY =
            register("impactiny", new ParticleType<>(false, GroundSplashParticleEffect.FACTORY) {
                @Override
                public Codec<GroundSplashParticleEffect> getCodec() {
                    return null;
                }
            });

    public static final ParticleType<DirectedImpactParticleEffect> DIRECTED_IMPACT =
            register("directed_impact", new ParticleType<>(false, DirectedImpactParticleEffect.FACTORY) {
                @Override
                public Codec<DirectedImpactParticleEffect> getCodec() {
                    return null;
                }
            });

    // simple particle type (no extra data)
    public static final ParticleType<SplashDropletParticleEffect> SPLASH_PIXEL =
            register("splash_pixel", new ParticleType<>(false, SplashDropletParticleEffect.FACTORY) {
                @Override
                public Codec<SplashDropletParticleEffect> getCodec() {
                    return null;
                }
            });

    public static final ParticleType<SplashDropletParticleEffect> SPLASH_PIXEL_RAIN =
            register("splash_pixel_rain", new ParticleType<>(false, SplashDropletParticleEffect.FACTORY) {
                @Override
                public Codec<SplashDropletParticleEffect> getCodec() { return null; }
            });

    public static final ParticleType<DefaultParticleType> SPLASH_LANDING =
            register("splash_landing", FabricParticleTypes.simple());

    // Leaf particle with configurable count
    public static final ParticleType<LeafParticleEffect> LEAF_PARTICLE =
            register("leaf", new ParticleType<>(false, LeafParticleEffect.FACTORY) {
                @Override
                public Codec<LeafParticleEffect> getCodec() {
                    return null;
                }
            });

    public static final ParticleType<LeafParticleEffect> LEAF_JUNGLE_PARTICLE =
            register("leaf_jungle", new ParticleType<>(false, LeafParticleEffect.FACTORY) {
                @Override
                public Codec<LeafParticleEffect> getCodec() {
                    return null;
                }
            });

    public static final ParticleType<LeafParticleEffect> LEAF_AZALEA_PARTICLE =
            register("leaf_azalea", new ParticleType<>(false, LeafParticleEffect.FACTORY) {
                @Override
                public Codec<LeafParticleEffect> getCodec() {
                    return null;
                }
            });

    public static final ParticleType<LeafParticleEffect> LEAF_MANGROVE_PARTICLE =
            register("leaf_mangrove", new ParticleType<>(false, LeafParticleEffect.FACTORY) {
                @Override
                public Codec<LeafParticleEffect> getCodec() {
                    return null;
                }
            });

    public static final ParticleType<CherryBlossomParticleEffect> LEAF_CHERRY_PARTICLE =
            register("leaf_cherry", new ParticleType<>(false, CherryBlossomParticleEffect.FACTORY) {
                @Override
                public Codec<CherryBlossomParticleEffect> getCodec() {
                    return null;
                }
            });



    private static <T extends ParticleEffect> ParticleType<T> register(String name, ParticleType<T> type) {
        return Registry.register(Registries.PARTICLE_TYPE, new Identifier(MODID, name), type);
    }
}
