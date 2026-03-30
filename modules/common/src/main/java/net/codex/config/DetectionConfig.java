// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.config;

public class DetectionConfig {
    public double minImpactVelocity = 0.9;    // MIN_IMPACT_VELOCITY
    public long spawnCooldownMs = 100L;       // SPAWN_COOLDOWN_MS
    public long pendingTtlMs = 300L;          // PENDING_TTL_MS
    public int sampleGrid = 3;                // SAMPLE_GRID
    public double spawnYEps = 0.02D;           // SPAWN_Y_EPS
    public int complexExtraBlocks = 2;        // COMPLEX_EXTRA_BLOCKS
}