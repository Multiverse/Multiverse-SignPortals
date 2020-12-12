package com.onarandombox.MultiverseSignPortals.listeners;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiverseCore.utils.BukkitTravelAgent;
import com.onarandombox.MultiverseCore.utils.MVTravelAgent;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerPortalEvent;

import java.util.logging.Level;

class MVSPTravelAgent extends MVTravelAgent {

    MVSPTravelAgent(MultiverseCore multiverseCore, MVDestination d, Player p) {
        super(multiverseCore, d, p);
    }

    void setPortalEventTravelAgent(PlayerPortalEvent event) {
        try {
            Class.forName("org.bukkit.TravelAgent");
            event.useTravelAgent(true);
            new BukkitTravelAgent(this).setPortalEventTravelAgent(event);
        } catch (ClassNotFoundException ignore) {
            Logging.fine("TravelAgent not available for PlayerPortalEvent for " + player.getName());
        }
    }
}

