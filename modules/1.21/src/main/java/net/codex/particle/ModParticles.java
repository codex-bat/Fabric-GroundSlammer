// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.particle;

import net.codex.GroundSlammer;
import net.codex.particle.custom.*;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModParticles {
    public static final String MOD_ID = GroundSlammer.MOD_ID;

    public static final ParticleType<CherryBlossomParticleEffect> CHERRY_BLOSSOM =
            registerCherry("leaf_cherry");

    public static final ParticleType<LeafParticleEffect> LEAF =
            registerLeaf("leaf");

    public static final ParticleType<LeafParticleEffect> LEAF_JUNGLE =
            registerLeaf("leaf_jungle");

    public static final ParticleType<LeafParticleEffect> LEAF_AZALEA =
            registerLeaf("leaf_azalea");

    public static final ParticleType<LeafParticleEffect> LEAF_MANGROVE =
            registerLeaf("leaf_mangrove");

    public static final ParticleType<SplashDropletParticleEffect> SPLASH_DROPLET =
            registerSplash("splash_pixel");

    public static final ParticleType<SplashDropletParticleEffect> SPLASH_DROPLET_RAIN =
            registerSplash("splash_pixel_rain");

    public static final ParticleType<SplashDropletParticleEffect> SPLASH_LANDING =
            registerSplash("splash_landing");

    public static final ParticleType<GroundSplashParticleEffect> IMPACT =
            registerGround("impact");

    public static final ParticleType<GroundSplashParticleEffect> IMPACTINY =
            registerGround("impactiny");

    public static final ParticleType<GroundSplashParticleEffect> OUTTER_SLAM =
            registerGround("outter_slam");

    public static final ParticleType<GroundSplashParticleEffect> SIMPLE_SLAM =
            registerGround("simple_slam");

    public static final ParticleType<DirectedImpactParticleEffect> DIRECTED_IMPACT =
            registerDirected("directed_impact");

    public static void registerClient() {
        ParticleFactoryRegistry.getInstance().register(CHERRY_BLOSSOM, CherryBlossomParticle.Factory::new);

        ParticleFactoryRegistry.getInstance().register(LEAF, LeafParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(LEAF_JUNGLE, LeafParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(LEAF_AZALEA, LeafParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(LEAF_MANGROVE, LeafParticle.Factory::new);

        ParticleFactoryRegistry.getInstance().register(SPLASH_DROPLET, SplashDropletParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(SPLASH_DROPLET_RAIN, SplashDropletRainParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(SPLASH_LANDING, SplashDropletLandingParticle.Factory::new);

        ParticleFactoryRegistry.getInstance().register(IMPACT, ImpactParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(IMPACTINY, ImpactinyParticle.Factory::new);

        ParticleFactoryRegistry.getInstance().register(OUTTER_SLAM, GroundSplashParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(SIMPLE_SLAM, GroundSplashSimpleParticle.Factory::new);

        ParticleFactoryRegistry.getInstance().register(DIRECTED_IMPACT, DirectedImpactParticle.Factory::new);
    }

    private static ParticleType<CherryBlossomParticleEffect> registerCherry(String path) {
        return Registry.register(
                Registries.PARTICLE_TYPE,
                Identifier.of(MOD_ID, path),
                FabricParticleTypes.complex(
                        type -> CherryBlossomParticleEffect.codec(type),
                        type -> CherryBlossomParticleEffect.packetCodec(type)
                )
        );
    }

    private static ParticleType<LeafParticleEffect> registerLeaf(String path) {
        return Registry.register(
                Registries.PARTICLE_TYPE,
                Identifier.of(MOD_ID, path),
                FabricParticleTypes.complex(
                        type -> LeafParticleEffect.codec(type),
                        type -> LeafParticleEffect.packetCodec(type)
                )
        );
    }

    private static ParticleType<SplashDropletParticleEffect> registerSplash(String path) {
        return Registry.register(
                Registries.PARTICLE_TYPE,
                Identifier.of(MOD_ID, path),
                FabricParticleTypes.complex(
                        type -> SplashDropletParticleEffect.codec(type),
                        type -> SplashDropletParticleEffect.packetCodec(type)
                )
        );
    }

    private static ParticleType<GroundSplashParticleEffect> registerGround(String path) {
        return Registry.register(
                Registries.PARTICLE_TYPE,
                Identifier.of(MOD_ID, path),
                FabricParticleTypes.complex(
                        type -> GroundSplashParticleEffect.codec(type),
                        type -> GroundSplashParticleEffect.packetCodec(type)
                )
        );
    }

    private static ParticleType<DirectedImpactParticleEffect> registerDirected(String path) {
        return Registry.register(
                Registries.PARTICLE_TYPE,
                Identifier.of(MOD_ID, path),
                FabricParticleTypes.complex(
                        type -> DirectedImpactParticleEffect.codec(type),
                        type -> DirectedImpactParticleEffect.packetCodec(type)
                )
        );
    }

    private ModParticles() {}
}