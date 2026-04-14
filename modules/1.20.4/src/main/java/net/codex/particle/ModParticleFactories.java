// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.particle;

import net.codex.particle.custom.*;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;

public class ModParticleFactories {
    public static void registerFactories() {
        ParticleFactoryRegistry registry = ParticleFactoryRegistry.getInstance();

        registry.register(ModParticles.OUTTER_SLAM, GroundSplashParticle.Factory::new);
        registry.register(ModParticles.SIMPLE_SLAM, GroundSplashSimpleParticle.Factory::new);
        registry.register(ModParticles.IMPACT, ImpactParticle.Factory::new);
        registry.register(ModParticles.IMPACTINY, ImpactinyParticle.Factory::new);
        registry.register(ModParticles.DIRECTED_IMPACT, DirectedImpactParticle.Factory::new);
        registry.register(ModParticles.SPLASH_PIXEL, SplashDropletParticle.Factory::new);
        registry.register(ModParticles.SPLASH_PIXEL_RAIN, SplashDropletRainParticle.Factory::new);
        registry.register(ModParticles.SPLASH_LANDING, SplashDropletLandingParticle.Factory::new);
        registry.register(ModParticles.LEAF_PARTICLE, LeafParticle.Factory::new);
        registry.register(ModParticles.LEAF_MANGROVE_PARTICLE, SimpleLeafParticle.Factory::new);
        registry.register(ModParticles.LEAF_JUNGLE_PARTICLE, SimpleLeafParticle.Factory::new);
        registry.register(ModParticles.LEAF_AZALEA_PARTICLE, SimpleLeafParticle.Factory::new);
        registry.register(ModParticles.LEAF_CHERRY_PARTICLE, CherryBlossomParticle.Factory::new);
    }
}