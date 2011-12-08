/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project.
 */

package com.onarandombox.MultiverseSignPortals.listeners;

import com.onarandombox.MultiverseCore.event.MVVersionRequestEvent;
import com.onarandombox.MultiverseSignPortals.MultiverseSignPortals;
import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Event;

public class MVSPVersionListener extends CustomEventListener {
    private MultiverseSignPortals plugin;

    public MVSPVersionListener(MultiverseSignPortals plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCustomEvent(Event event) {
        if (event.getEventName().equals("MVVersion") && event instanceof MVVersionRequestEvent) {
            ((MVVersionRequestEvent) event).setPasteBinBuffer(this.plugin.dumpVersionInfo(((MVVersionRequestEvent) event).getPasteBinBuffer()));
        }
    }
}
