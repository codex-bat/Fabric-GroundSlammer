// SPDX-License-Identifier: GPL-3.0-or-later
// Copyright (C) 2026 Codex.bat

package net.codex.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;

public interface GroundSlamEvent {
    Event<GroundSlamEvent> EVENT = EventFactory.createArrayBacked(GroundSlamEvent.class,
            listeners -> (entity, velocity) -> {
                for (GroundSlamEvent listener : listeners) {
                    listener.onGroundSlam(entity, velocity);
                }
            });

    void onGroundSlam(Entity entity, double downwardVelocity);
}