// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.config;

public final class VoidFogConfig {
    public boolean enabled = true;

    // Start fog below this Y.
    public float voidFogStartY = -50.0f;

    // Bigger = smoother vertical fade.
    // Try 8, 12, or 16. 20 is okay too, but 8-12 feels more “gradual”.
    public float voidFogFadeDepth = 20.0f;

    public float maxScanDistance = 14.0f;

    public float minFogStart = 0.10f;
    public float maxFogStart = 2.25f;
    public float minFogEnd = 1.75f;
    public float maxFogEnd = 10.5f;

    public float intensityExponent = 1.6f;

    public float fogRed = 0.58f;
    public float fogGreen = 0.57f;
    public float fogBlue = 0.55f;

    public float lightInfluence = 0.05f;

    // Ash
    public float ashSpawnThreshold = 0.12f;
    public int ashParticleCount = 18;
    public int ashCooldownTicks = 20;
}