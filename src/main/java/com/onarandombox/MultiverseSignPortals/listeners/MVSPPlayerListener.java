package com.onarandombox.MultiverseSignPortals.listeners;

import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerPortalEvent;

import com.onarandombox.MultiverseSignPortals.MultiverseSignPortals;
import com.onarandombox.MultiverseSignPortals.utils.PortalDetector;

public class MVSPPlayerListener extends PlayerListener {

    private final MultiverseSignPortals plugin;

    public MVSPPlayerListener(MultiverseSignPortals plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPlayerPortal(PlayerPortalEvent event) {
        if(event.isCancelled()) {
            return;
        }
        PortalDetector detector = new PortalDetector(this.plugin);
        detector.isNotchStylePortal(event.getPlayer().getLocation());
        event.setCancelled(true);
    }
}
