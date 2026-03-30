// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.config;

import net.codex.sound.ModSounds;
import net.minecraft.sound.SoundEvent;

public class SoundConfig {
    // Use string identifiers for mod compatibility; resolve to SoundEvent at runtime
    public String slamSoundId = "groundslammer:slam";
    public String snowSlamSoundId = "groundslammer:snow_slam";
    public String iceSlamSoundId = "groundslammer:ice_slam";
    public String leafSlamSoundId = "groundslammer:leaf_slam";
    public String cherryLeafSlamSoundId = "groundslammer:leaf_slam"; // or a dedicated cherry sound

    public transient SoundEvent slamSound = ModSounds.SLAM;
    public transient SoundEvent snowSlamSound = ModSounds.SNOW_SLAM;
    public transient SoundEvent iceSlamSound = ModSounds.ICE_SLAM;
    public transient SoundEvent leafSlamSound = ModSounds.LEAF_SLAM;
    public transient SoundEvent cherryLeafSlamSound = ModSounds.LEAF_SLAM;

    // Call this after loading from file to resolve SoundEvents
    public void resolveSounds() {
        // In practice you'd map IDs to actual SoundEvent instances
        // For now we keep the defaults; you can use registry lookup if needed
    }
}