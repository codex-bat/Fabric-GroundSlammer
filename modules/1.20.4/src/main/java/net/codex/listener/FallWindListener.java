// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.listener;

import net.codex.sound.ModSounds;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CommandBlockScreen;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;

/**
 * Plays a looping falling-wind sound while the player is falling.
 * - Pauses (mutes) the sound when a pause-screen is open in singleplayer (Screen.shouldPause()).
 * - Also treats CommandBlockScreen (singleplayer) as pausing so command blocks can't keep running the sound.
 * - Does NOT pause for ordinary inventories that don't stop the game.
 * - CURRENTLY WORKS FOR BOTH 1.19.2 AND 1.20.4. ONCE NEWER VERSIONS ARE FORKED, THIS WILL BE MOVED AND COPIED TO ITS...
 * - RESPECTIVE MODULES IN VARIOUS FORMS.
 * <p>
 * Register with: FallWindListener.register();
 */
public final class FallWindListener {
    private static PlayerWindSound windInstance = null;

    private static final int FALL_TICKS_THRESHOLD = 5;
    private static final double START_VEL_Y = -0.5;
    private static final double STOP_VEL_Y  = -0.05;

    private static int fallTicks = 0;
    private static float fallVisualIntensity = 0.0f;
    private static float prevFallVisualIntensity = 0.0f;
    private static MinecraftClient lastClientForWorldCheck = null;

    private FallWindListener() {}

    public static float getFallVisualIntensity() {
        return fallVisualIntensity;
    }

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client == null) return;

            if (client.world == null || client.player == null) {
                stopWind(client);
                fallVisualIntensity = 0.0f;
                return;
            }

            if (lastClientForWorldCheck != client) {
                stopWind(client);
                lastClientForWorldCheck = client;
            }

            PlayerEntity player = client.player;

            boolean inGui = client.currentScreen != null;
            boolean screenPausesGame = false;
            if (inGui) {
                Screen s = client.currentScreen;
                screenPausesGame = s.shouldPause() || (s instanceof CommandBlockScreen);
            }
            boolean singleplayer = client.isIntegratedServerRunning();

            boolean isFlying = player.getAbilities().flying || player.isFallFlying();
            boolean onGround = player.isOnGround();
            double vy = player.getVelocity().y;

            boolean guiBlocksStart = inGui && screenPausesGame && singleplayer;

            float targetIntensity = 0.0f;

            if (!guiBlocksStart && !isFlying && !onGround && vy < START_VEL_Y) {
                fallTicks = Math.min(fallTicks + 1, FALL_TICKS_THRESHOLD);

                double speedFactor = Math.min(1.0, Math.max(0.0, (-vy - 0.5) / 2.5));
                float timeFactor = fallTicks / (float) FALL_TICKS_THRESHOLD;
                targetIntensity = (float) Math.max(speedFactor, timeFactor);

                if (fallTicks >= FALL_TICKS_THRESHOLD && windInstance == null) {
                    startWind(client, player);
                }
            } else {
                fallTicks = 0;

                if (windInstance != null) {
                    if (isFlying || onGround || vy >= STOP_VEL_Y) {
                        stopWind(client);
                    }
                }
            }

            prevFallVisualIntensity = fallVisualIntensity;

            float lerp = targetIntensity > fallVisualIntensity ? 0.18f : 0.10f;
            fallVisualIntensity += (targetIntensity - fallVisualIntensity) * lerp;

            if (windInstance != null) {
                boolean shouldPauseSound = (inGui && screenPausesGame && singleplayer);
                windInstance.setMuted(shouldPauseSound);
                windInstance.updatePosition(player.getPos());
                if (player.isRemoved()) stopWind(client);
            }
        });
    }

    private static void startWind(MinecraftClient client, PlayerEntity player) {
        if (client == null || client.getSoundManager() == null) return;
        if (windInstance != null) return;

        try {
            windInstance = new PlayerWindSound(player);
            client.getSoundManager().play(windInstance);
        } catch (Throwable t) {
            windInstance = null;
            t.printStackTrace();
        }
    }

    private static void stopWind(MinecraftClient client) {
        if (client == null || client.getSoundManager() == null) {
            windInstance = null;
            return;
        }
        if (windInstance != null) {
            try {
                client.getSoundManager().stop(windInstance);
            } catch (Throwable ignored) {}
            windInstance = null;
        }
    }

    public static float getFallVisualIntensity(float tickDelta) {
        return prevFallVisualIntensity + (fallVisualIntensity - prevFallVisualIntensity) * tickDelta;
    }

    /**
     * Moving sound instance which follows the player, repeats, and supports muting
     * (we don't stop the stream; we just return 0 volume while "muted" so playback position is preserved).
     */
    private static final class PlayerWindSound extends MovingSoundInstance {
        private final PlayerEntity player;
        private volatile boolean muted = false;
        private final float baseVolume = 1.0f; // tweak if lower default volume

        public PlayerWindSound(PlayerEntity player) {
            // Use the world's existing random (correct type) instead of creating/casting java.util.Random.
            super(ModSounds.WIND, SoundCategory.AMBIENT, player.getWorld().random);
            this.player = player;
            this.repeat = true;
            this.repeatDelay = 0;
            this.volume = baseVolume;
            // initialize position to player; engine will read these coordinates for attenuation
            Vec3d p = player.getPos();
            this.x = p.x;
            this.y = p.y;
            this.z = p.z;
            this.relative = false; // use world position (so attenuation works)
        }

        /** Called from the outer tick to update whether the sound should be muted. */
        public void setMuted(boolean muted) {
            this.muted = muted;
        }

        /** Update the sound's world position so attenuation follows the player. */
        public void updatePosition(Vec3d pos) {
            this.x = pos.x;
            this.y = pos.y;
            this.z = pos.z;
        }

        @Override
        public void tick() {
            // If the player disappears (teleport out / removed), mark done
            if (this.player == null || this.player.isRemoved()) {
                this.setDone();
                return;
            }
            // normally the listener (SoundManager) will fetch getVolume() each tick,
            // so I don't need to modify the engine here; I just keep my coords updated.
            Vec3d p = this.player.getPos();
            this.x = p.x;
            this.y = p.y;
            this.z = p.z;
        }

        @Override
        public float getVolume() {
            // While "muted" (paused GUI in singleplayer), return zero so sound is inaudible,
            // but the sound engine still progresses the stream. When unmuted I return baseVolume.
            float target = this.muted ? 0.0f : 1.0f;
            this.volume += (target - this.volume) * 0.15f;
            return this.volume * baseVolume;
        }
    }
}