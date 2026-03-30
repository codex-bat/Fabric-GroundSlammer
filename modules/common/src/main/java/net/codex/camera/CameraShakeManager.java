// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.camera;

import net.minecraft.util.math.Vec3d;

import java.util.concurrent.ThreadLocalRandom;

public final class CameraShakeManager {
    private static float strength;
    private static int durationLeft;
    private static int totalDuration;

    private static float seedX;
    private static float seedY;
    private static float seedZ;

    private static int age;

    private CameraShakeManager() {}

    public static void addImpact(float downwardVelocity) {
        float t = clamp(downwardVelocity / 6.0f, 0.0f, 1.0f);

        // exponential curve
        t = (float) Math.pow(t, 1.8);

        if (t <= 0.0f) return;

        float addedStrength = 0.05f + (0.6f * t);
        int addedDuration = 6 + Math.round(18.0f * t);

        strength = Math.max(strength, addedStrength);
        durationLeft = Math.max(durationLeft, addedDuration);
        totalDuration = Math.max(totalDuration, addedDuration);

        if (age == 0 || durationLeft == addedDuration) {
            reseed();
        }
    }

    public static void tick() {
        if (durationLeft > 0) {
            durationLeft--;
            age++;
        } else {
            strength = 0.0f;
            totalDuration = 0;
            age = 0;
        }
    }

    public static Vec3d sampleOffset(float tickDelta) {
        if (durationLeft <= 0 || totalDuration <= 0 || strength <= 0.0f) {
            return Vec3d.ZERO;
        }

        float life = (durationLeft - tickDelta) / (float) totalDuration;
        life = clamp(life, 0.0f, 1.0f);

        // Smooth fade-out that feels punchy at the start and soft at the end.
        float impact = (float)Math.pow(1.0f - life, 2.5); // strong initial hit
        float envelope = strength * easeOutCubic(life) * (1.0f + impact * 1.5f);

        float t = age + tickDelta;

        float x = layeredNoise(t, seedX, 13.0f, 29.0f, 0.65f);
        float y = layeredNoise(t, seedY, 20.0f, 45.0f, 1.2f);
        float z = layeredNoise(t, seedZ, 11.0f, 31.0f, 0.65f);

        // Keep it subtle. Position shake feels best when it is tiny.
        double scale = 0.5 + (0.5 * envelope);  // much larger

        return new Vec3d(
                x * envelope * scale,
                y * envelope * scale,
                z * envelope * scale
        );
    }

    private static float layeredNoise(float t, float seed, float a, float b, float c) {
        float n1 = (float) Math.sin(t * a + seed);
        float n2 = (float) Math.sin(t * b + seed * 1.73f);
        float n3 = (float) Math.sin(t * c + seed * 2.31f);
        return (n1 * 0.55f) + (n2 * 0.30f) + (n3 * 0.15f);
    }

    private static void reseed() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        seedX = r.nextFloat() * 1000.0f;
        seedY = r.nextFloat() * 1000.0f;
        seedZ = r.nextFloat() * 1000.0f;
    }

    private static float easeOutCubic(float x) {
        float inv = 1.0f - x;
        return 1.0f - inv * inv * inv;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static float getCurrentStrength() {
        return strength;
    }
}