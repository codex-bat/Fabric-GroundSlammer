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
 *
 * Register with: FallWindListener.register();
 */
public final class FallWindListener {
    // the single tracked moving sound instance
    private static PlayerWindSound windInstance = null;

    // short guard to avoid playing on tiny hops: must be falling for this many ticks
    private static final int FALL_TICKS_THRESHOLD = 5;

    // vertical velocity thresholds (tweak as needed)
    private static final double START_VEL_Y = -0.5; // start when falling faster than this
    private static final double STOP_VEL_Y  = -0.05; // stop when vertical speed recovers above this

    private static int fallTicks = 0;
    private static MinecraftClient lastClientForWorldCheck = null;

    private FallWindListener() {} // no instances

    /** Call once on your client init (keeps listener code separate). */
    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client == null) return;

            // If world or player missing -> stop & cleanup
            if (client.world == null || client.player == null) {
                stopWind(client);
                return;
            }

            // If client changed (resource reload / world reload), stop existing sound
            if (lastClientForWorldCheck != client) {
                stopWind(client);
                lastClientForWorldCheck = client;
            }

            PlayerEntity player = client.player;

            boolean inGui = client.currentScreen != null;
            // screenPausesGame is true if the screen normally pauses the game OR it's the command-block screen
            boolean screenPausesGame = false;
            if (inGui) {
                Screen s = client.currentScreen;
                screenPausesGame = s.shouldPause() || (s instanceof CommandBlockScreen);
            }
            boolean singleplayer = client.isIntegratedServerRunning(); // integrated server = singleplayer (or LAN host)

            boolean isFlying = player.getAbilities().flying || player.isFallFlying();
            boolean onGround = player.isOnGround();
            double vy = player.getVelocity().y;

            // START condition: not in a GUI that should block (allow non-pausing GUIs),
            // not flying, not on ground, and vertical speed low enough
            boolean guiBlocksStart = inGui && screenPausesGame && singleplayer;
            if (!guiBlocksStart && !isFlying && !onGround && vy < START_VEL_Y) {
                fallTicks = Math.min(fallTicks + 1, FALL_TICKS_THRESHOLD);
                if (fallTicks >= FALL_TICKS_THRESHOLD && windInstance == null) {
                    startWind(client, player);
                }
            } else {
                // reset fall counter if not in a falling state
                fallTicks = 0;

                // STOP the sound if no longer falling/conditions removed
                if (windInstance != null) {
                    if (isFlying || onGround || vy >= STOP_VEL_Y) {
                        // fully stop and cleanup
                        stopWind(client);
                    }
                }
            }

            // If the sound is playing, update its paused/unpaused state based on whether
            // the open screen actually pauses the game in singleplayer.
            if (windInstance != null) {
                boolean shouldPauseSound = (inGui && screenPausesGame && singleplayer);
                windInstance.setMuted(shouldPauseSound);
                // keep following the player's current position so attenuation works
                windInstance.updatePosition(player.getPos());
                // if the player somehow got removed, stop
                if (player.isRemoved()) stopWind(client);
            }
        });
    }

    private static void startWind(MinecraftClient client, PlayerEntity player) {
        if (client == null || client.getSoundManager() == null) return;
        if (windInstance != null) return; // already playing

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
            super(ModSounds.WIND, SoundCategory.AMBIENT, player.world.random);
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
            return this.muted ? 0.0f : this.baseVolume;
        }
    }
}