// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class GroundslammerConfigScreen {

    public static Screen create(Screen parent) {
        GroundslammerClientConfig config = GroundslammerClientConfig.load();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("groundslammer.config.title"));

        ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();

        // --- Impact Category ---
        ConfigCategory impactCat = builder.getOrCreateCategory(Text.translatable("category.groundslammer.impact"));
        impactCat.addEntry(entryBuilder.startDoubleField(
                        Text.translatable("option.groundslammer.impact.impactScale"),
                        config.impact.impactScale)
                .setDefaultValue(1.5)
                .setMin(0.1)
                .setMax(5.0)
                .setSaveConsumer(newValue -> config.impact.impactScale = newValue)
                .build());
        impactCat.addEntry(entryBuilder.startFloatField(
                        Text.translatable("option.groundslammer.impact.minHeightMult"),
                        config.impact.minHeightMult)
                .setDefaultValue(0.9f)
                .setMin(0.1f)
                .setMax(10.0f)
                .setSaveConsumer(newValue -> config.impact.minHeightMult = newValue)
                .build());
        impactCat.addEntry(entryBuilder.startFloatField(
                        Text.translatable("option.groundslammer.impact.maxHeightMult"),
                        config.impact.maxHeightMult)
                .setDefaultValue(6.0f)
                .setMin(0.1f)
                .setMax(20.0f)
                .setSaveConsumer(newValue -> config.impact.maxHeightMult = newValue)
                .build());
        impactCat.addEntry(entryBuilder.startFloatField(
                        Text.translatable("option.groundslammer.impact.minSizeMult"),
                        config.impact.minSizeMult)
                .setDefaultValue(0.6f)
                .setMin(0.1f)
                .setMax(5.0f)
                .setSaveConsumer(newValue -> config.impact.minSizeMult = newValue)
                .build());
        impactCat.addEntry(entryBuilder.startFloatField(
                        Text.translatable("option.groundslammer.impact.maxSizeMult"),
                        config.impact.maxSizeMult)
                .setDefaultValue(3.0f)
                .setMin(0.1f)
                .setMax(10.0f)
                .setSaveConsumer(newValue -> config.impact.maxSizeMult = newValue)
                .build());
        impactCat.addEntry(entryBuilder.startFloatField(
                        Text.translatable("option.groundslammer.impact.heightReductionFactor"),
                        config.impact.heightReductionFactor)
                .setDefaultValue(2.0f)
                .setMin(0.1f)
                .setMax(10.0f)
                .setSaveConsumer(newValue -> config.impact.heightReductionFactor = newValue)
                .build());

        // --- Detection Category ---
        ConfigCategory detectionCat = builder.getOrCreateCategory(Text.translatable("category.groundslammer.detection"));
        detectionCat.addEntry(entryBuilder.startDoubleField(
                        Text.translatable("option.groundslammer.detection.minImpactVelocity"),
                        config.detection.minImpactVelocity)
                .setDefaultValue(0.9)
                .setMin(0.0)
                .setMax(5.0)
                .setSaveConsumer(newValue -> config.detection.minImpactVelocity = newValue)
                .build());
        detectionCat.addEntry(entryBuilder.startLongField(
                        Text.translatable("option.groundslammer.detection.spawnCooldownMs"),
                        config.detection.spawnCooldownMs)
                .setDefaultValue(100L)
                .setMin(0L)
                .setMax(10000L)
                .setSaveConsumer(newValue -> config.detection.spawnCooldownMs = newValue)
                .build());
        detectionCat.addEntry(entryBuilder.startLongField(
                        Text.translatable("option.groundslammer.detection.pendingTtlMs"),
                        config.detection.pendingTtlMs)
                .setDefaultValue(300L)
                .setMin(0L)
                .setMax(10000L)
                .setSaveConsumer(newValue -> config.detection.pendingTtlMs = newValue)
                .build());
        detectionCat.addEntry(entryBuilder.startIntField(
                        Text.translatable("option.groundslammer.detection.sampleGrid"),
                        config.detection.sampleGrid)
                .setDefaultValue(3)
                .setMin(1)
                .setMax(10)
                .setSaveConsumer(newValue -> config.detection.sampleGrid = newValue)
                .build());
        detectionCat.addEntry(entryBuilder.startDoubleField(
                        Text.translatable("option.groundslammer.detection.spawnYEps"),
                        config.detection.spawnYEps)
                .setDefaultValue(0.02)
                .setMin(0.0)
                .setMax(1.0)
                .setSaveConsumer(newValue -> config.detection.spawnYEps = newValue)
                .build());
        detectionCat.addEntry(entryBuilder.startIntField(
                        Text.translatable("option.groundslammer.detection.complexExtraBlocks"),
                        config.detection.complexExtraBlocks)
                .setDefaultValue(2)
                .setMin(0)
                .setMax(10)
                .setSaveConsumer(newValue -> config.detection.complexExtraBlocks = newValue)
                .build());

        // --- Particle Category ---
        ConfigCategory particleCat = builder.getOrCreateCategory(Text.translatable("category.groundslammer.particle"));
        particleCat.addEntry(entryBuilder.startIntField(
                        Text.translatable("option.groundslammer.particle.dropletBurstBase"),
                        config.particle.dropletBurstBase)
                .setDefaultValue(8)
                .setMin(1)
                .setMax(100)
                .setSaveConsumer(newValue -> config.particle.dropletBurstBase = newValue)
                .build());
        particleCat.addEntry(entryBuilder.startFloatField(
                        Text.translatable("option.groundslammer.particle.dropletUpwardMin"),
                        config.particle.dropletUpwardMin)
                .setDefaultValue(0.25f)
                .setMin(0.0f)
                .setMax(1.0f)
                .setSaveConsumer(newValue -> config.particle.dropletUpwardMin = newValue)
                .build());
        particleCat.addEntry(entryBuilder.startFloatField(
                        Text.translatable("option.groundslammer.particle.dropletUpwardMax"),
                        config.particle.dropletUpwardMax)
                .setDefaultValue(0.45f)
                .setMin(0.0f)
                .setMax(1.0f)
                .setSaveConsumer(newValue -> config.particle.dropletUpwardMax = newValue)
                .build());
        particleCat.addEntry(entryBuilder.startFloatField(
                        Text.translatable("option.groundslammer.particle.leafRadialOffsetMin"),
                        config.particle.leafRadialOffsetMin)
                .setDefaultValue(0.06f)
                .setMin(0.0f)
                .setMax(1.0f)
                .setSaveConsumer(newValue -> config.particle.leafRadialOffsetMin = newValue)
                .build());
        particleCat.addEntry(entryBuilder.startFloatField(
                        Text.translatable("option.groundslammer.particle.leafRadialOffsetMax"),
                        config.particle.leafRadialOffsetMax)
                .setDefaultValue(0.22f)
                .setMin(0.0f)
                .setMax(1.0f)
                .setSaveConsumer(newValue -> config.particle.leafRadialOffsetMax = newValue)
                .build());
        particleCat.addEntry(entryBuilder.startFloatField(
                        Text.translatable("option.groundslammer.particle.leafVelocityXZ"),
                        config.particle.leafVelocityXZ)
                .setDefaultValue(0.03f)
                .setMin(0.0f)
                .setMax(1.0f)
                .setSaveConsumer(newValue -> config.particle.leafVelocityXZ = newValue)
                .build());
        particleCat.addEntry(entryBuilder.startFloatField(
                        Text.translatable("option.groundslammer.particle.leafVelocityYMin"),
                        config.particle.leafVelocityYMin)
                .setDefaultValue(0.02f)
                .setMin(0.0f)
                .setMax(1.0f)
                .setSaveConsumer(newValue -> config.particle.leafVelocityYMin = newValue)
                .build());
        particleCat.addEntry(entryBuilder.startFloatField(
                        Text.translatable("option.groundslammer.particle.leafVelocityYMax"),
                        config.particle.leafVelocityYMax)
                .setDefaultValue(0.04f)
                .setMin(0.0f)
                .setMax(1.0f)
                .setSaveConsumer(newValue -> config.particle.leafVelocityYMax = newValue)
                .build());

        // --- Sound Category ---
        ConfigCategory soundCat = builder.getOrCreateCategory(Text.translatable("category.groundslammer.sound"));
        soundCat.addEntry(entryBuilder.startTextField(
                        Text.translatable("option.groundslammer.sound.slamSoundId"),
                        config.sound.slamSoundId)
                .setDefaultValue("codex:slam")
                .setSaveConsumer(newValue -> config.sound.slamSoundId = newValue)
                .build());
        soundCat.addEntry(entryBuilder.startTextField(
                        Text.translatable("option.groundslammer.sound.snowSlamSoundId"),
                        config.sound.snowSlamSoundId)
                .setDefaultValue("codex:snow_slam")
                .setSaveConsumer(newValue -> config.sound.snowSlamSoundId = newValue)
                .build());
        soundCat.addEntry(entryBuilder.startTextField(
                        Text.translatable("option.groundslammer.sound.iceSlamSoundId"),
                        config.sound.iceSlamSoundId)
                .setDefaultValue("codex:ice_slam")
                .setSaveConsumer(newValue -> config.sound.iceSlamSoundId = newValue)
                .build());
        soundCat.addEntry(entryBuilder.startTextField(
                        Text.translatable("option.groundslammer.sound.leafSlamSoundId"),
                        config.sound.leafSlamSoundId)
                .setDefaultValue("codex:leaf_slam")
                .setSaveConsumer(newValue -> config.sound.leafSlamSoundId = newValue)
                .build());
        soundCat.addEntry(entryBuilder.startTextField(
                        Text.translatable("option.groundslammer.sound.cherryLeafSlamSoundId"),
                        config.sound.cherryLeafSlamSoundId)
                .setDefaultValue("codex:leaf_slam")
                .setSaveConsumer(newValue -> config.sound.cherryLeafSlamSoundId = newValue)
                .build());

        // --- Leaf Category ---
        ConfigCategory leafCat = builder.getOrCreateCategory(Text.translatable("category.groundslammer.leaf"));
        leafCat.addEntry(entryBuilder.startFloatField(
                        Text.translatable("option.groundslammer.leaf.leafParticleSizeMin"),
                        config.leaf.leafParticleSizeMin)
                .setDefaultValue(0.05f)
                .setMin(0.01f)
                .setMax(1.0f)
                .setSaveConsumer(newValue -> config.leaf.leafParticleSizeMin = newValue)
                .build());
        leafCat.addEntry(entryBuilder.startFloatField(
                        Text.translatable("option.groundslammer.leaf.leafParticleSizeMax"),
                        config.leaf.leafParticleSizeMax)
                .setDefaultValue(0.1f)
                .setMin(0.01f)
                .setMax(1.0f)
                .setSaveConsumer(newValue -> config.leaf.leafParticleSizeMax = newValue)
                .build());
        leafCat.addEntry(entryBuilder.startIntField(
                        Text.translatable("option.groundslammer.leaf.leafCountBase"),
                        config.leaf.leafCountBase)
                .setDefaultValue(8)
                .setMin(0)
                .setMax(100)
                .setSaveConsumer(newValue -> config.leaf.leafCountBase = newValue)
                .build());

        // --- Void Fog Category ---
        ConfigCategory voidFogCat = builder.getOrCreateCategory(Text.translatable("category.groundslammer.voidFog"));
        voidFogCat.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("option.groundslammer.voidFog.enabled"),
                        config.voidFog.enabled)
                .setDefaultValue(true)
                .setSaveConsumer(newValue -> config.voidFog.enabled = newValue)
                .build());
        voidFogCat.addEntry(entryBuilder.startFloatField(
                        Text.translatable("option.groundslammer.voidFog.maxScanDistance"),
                        config.voidFog.maxScanDistance)
                .setDefaultValue(14.0f)
                .setMin(1.0f)
                .setMax(50.0f)
                .setSaveConsumer(newValue -> config.voidFog.maxScanDistance = newValue)
                .build());
        voidFogCat.addEntry(entryBuilder.startFloatField(
                        Text.translatable("option.groundslammer.voidFog.minFogStart"),
                        config.voidFog.minFogStart)
                .setDefaultValue(0.10f)
                .setMin(0.0f)
                .setMax(10.0f)
                .setSaveConsumer(newValue -> config.voidFog.minFogStart = newValue)
                .build());
        voidFogCat.addEntry(entryBuilder.startFloatField(
                        Text.translatable("option.groundslammer.voidFog.maxFogStart"),
                        config.voidFog.maxFogStart)
                .setDefaultValue(2.25f)
                .setMin(0.0f)
                .setMax(20.0f)
                .setSaveConsumer(newValue -> config.voidFog.maxFogStart = newValue)
                .build());
        voidFogCat.addEntry(entryBuilder.startFloatField(
                        Text.translatable("option.groundslammer.voidFog.minFogEnd"),
                        config.voidFog.minFogEnd)
                .setDefaultValue(1.75f)
                .setMin(0.0f)
                .setMax(30.0f)
                .setSaveConsumer(newValue -> config.voidFog.minFogEnd = newValue)
                .build());
        voidFogCat.addEntry(entryBuilder.startFloatField(
                        Text.translatable("option.groundslammer.voidFog.maxFogEnd"),
                        config.voidFog.maxFogEnd)
                .setDefaultValue(10.5f)
                .setMin(0.0f)
                .setMax(50.0f)
                .setSaveConsumer(newValue -> config.voidFog.maxFogEnd = newValue)
                .build());
        voidFogCat.addEntry(entryBuilder.startFloatField(
                        Text.translatable("option.groundslammer.voidFog.intensityExponent"),
                        config.voidFog.intensityExponent)
                .setDefaultValue(2.2f)
                .setMin(0.1f)
                .setMax(5.0f)
                .setSaveConsumer(newValue -> config.voidFog.intensityExponent = newValue)
                .build());
        voidFogCat.addEntry(entryBuilder.startFloatField(
                        Text.translatable("option.groundslammer.voidFog.fogRed"),
                        config.voidFog.fogRed)
                .setDefaultValue(0.58f)
                .setMin(0.0f)
                .setMax(1.0f)
                .setSaveConsumer(newValue -> config.voidFog.fogRed = newValue)
                .build());
        voidFogCat.addEntry(entryBuilder.startFloatField(
                        Text.translatable("option.groundslammer.voidFog.fogGreen"),
                        config.voidFog.fogGreen)
                .setDefaultValue(0.57f)
                .setMin(0.0f)
                .setMax(1.0f)
                .setSaveConsumer(newValue -> config.voidFog.fogGreen = newValue)
                .build());
        voidFogCat.addEntry(entryBuilder.startFloatField(
                        Text.translatable("option.groundslammer.voidFog.fogBlue"),
                        config.voidFog.fogBlue)
                .setDefaultValue(0.55f)
                .setMin(0.0f)
                .setMax(1.0f)
                .setSaveConsumer(newValue -> config.voidFog.fogBlue = newValue)
                .build());
        voidFogCat.addEntry(entryBuilder.startFloatField(
                        Text.translatable("option.groundslammer.voidFog.lightInfluence"),
                        config.voidFog.lightInfluence)
                .setDefaultValue(0.05f)
                .setMin(0.0f)
                .setMax(1.0f)
                .setSaveConsumer(newValue -> config.voidFog.lightInfluence = newValue)
                .build());

        // Save the config when the screen is closed
        builder.setSavingRunnable(config::save);

        return builder.build();
    }
}