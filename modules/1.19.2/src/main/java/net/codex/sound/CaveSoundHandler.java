// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.sound;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.random.Random;

public class CaveSoundHandler {

    /*
    minDelay = 200;
    maxDelay = 600;
    minPitch = 0.8f;
    maxPitch = 1.2f;
    minYLevel = 40;
     */

    private static void tick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;

        if (player == null || client.world == null) return;

        // Check if player is underground
        if (!isUnderground(player)) return;

        timer++;

        if (timer >= nextDelay) {
            playCaveSound(player);

            timer = 0;
            nextDelay = getRandomDelay(); // reset delay
        }
    }

    private static boolean isUnderground(ClientPlayerEntity player) {
        // Simple but effective:
        return player.getY() < 40 && !player.getWorld().isSkyVisible(player.getBlockPos());
    }

    private static void playCaveSound(ClientPlayerEntity player) {
        float pitch = 0.8f + random.nextFloat() * 0.4f; // 0.8 → 1.2
        float volume = 0.6f + random.nextFloat() * 0.4f;

        player.getWorld().playSound(
                player.getX() + random.nextInt(16) - 8,
                player.getY(),
                player.getZ() + random.nextInt(16) - 8,
                ModSounds.CAVE,
                SoundCategory.AMBIENT,
                volume,
                pitch,
                false
        );
    }

    private static int getRandomDelay() {
        // ticks (20 ticks = 1 second)
        return 200 + random.nextInt(400); // 10s → 30s
    }

    private static int timer = 0;
    private static int nextDelay;

    private static final Random random = Random.create();

    public static void register() {
        nextDelay = getRandomDelay();
        ClientTickEvents.END_CLIENT_TICK.register(CaveSoundHandler::tick);
    }
}