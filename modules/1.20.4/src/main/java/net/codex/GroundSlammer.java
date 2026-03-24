// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex;

import net.codex.sound.ModSounds;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GroundSlammer implements ModInitializer {
    public static final String MOD_ID = "groundslammer";

       // This logger is used to write text to the console and the log file.
      // It is considered best practice to use your mod id as the logger's name.
     // That way, it's clear which mod wrote info, warnings, and errors.
    // again... Ik, templater... but ty for telling me anyway ^^
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModSounds.registerSounds();

        LOGGER.info("Hello 1.20.4 Fabric world!");
    }
}