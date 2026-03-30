// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;

public class GroundslammerClientConfig {
    public ImpactConfig impact = new ImpactConfig();
    public DetectionConfig detection = new DetectionConfig();
    public ParticleConfig particle = new ParticleConfig();
    public SoundConfig sound = new SoundConfig();
    public LeafConfig leaf = new LeafConfig();
    public VoidFogConfig voidFog = new VoidFogConfig();

    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("groundslammer.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // Load configuration from file, create default if missing
    public static GroundslammerClientConfig load() {
        GroundslammerClientConfig config = new GroundslammerClientConfig();
        if (CONFIG_PATH.toFile().exists()) {
            try (Reader reader = new FileReader(CONFIG_PATH.toFile())) {
                config = GSON.fromJson(reader, GroundslammerClientConfig.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            config.save(); // save defaults
        }
        return config;
    }

    // Save current configuration to file
    public void save() {
        try (Writer writer = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}