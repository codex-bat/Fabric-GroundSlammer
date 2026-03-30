// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.config;

public class ParticleConfig {
    public int dropletBurstBase = 8;          // base count for droplet burst (multiplied by amountMultiplier)
    public float dropletUpwardMin = 0.25f;    // upwardMin in spawnDropletBurst
    public float dropletUpwardMax = 0.45f;    // upwardMax
    public float leafRadialOffsetMin = 0.06f; // leaf radial offset min
    public float leafRadialOffsetMax = 0.22f; // leaf radial offset max
    public float leafVelocityXZ = 0.03f;      // vx/vz for leaf particles
    public float leafVelocityYMin = 0.02f;    // vy min
    public float leafVelocityYMax = 0.04f;    // vy max
}