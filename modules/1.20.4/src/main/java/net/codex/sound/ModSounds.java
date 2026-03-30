// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.sound;

import net.codex.GroundSlammer;
import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class ModSounds {
    public static final SoundEvent LEAF_SLAM = registerSoundEvent("leaf_slam");
    public static final SoundEvent SLAM = registerSoundEvent("slam");
    public static final SoundEvent SNOW_SLAM = registerSoundEvent("snow_slam");
    public static final SoundEvent ICE_SLAM = registerSoundEvent("ice_slam");
    public static final SoundEvent WIND = registerSoundEvent("wind");
    public static final SoundEvent CAVE = registerSoundEvent("cave");

    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = new Identifier(GroundSlammer.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void registerSounds() {
        GroundSlammer.LOGGER.info("Registering Sounds for: " + GroundSlammer.MOD_ID);
    }
}