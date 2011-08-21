package com.onarandombox.MultiverseSignPortals.listeners;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerPortalEvent;

import com.onarandombox.MultiverseSignPortals.MultiverseSignPortals;
import com.onarandombox.MultiverseSignPortals.exceptions.MoreThanOneSignFoundException;
import com.onarandombox.MultiverseSignPortals.utils.NoMultiverseSignFoundException;
import com.onarandombox.MultiverseSignPortals.utils.PortalDetector;

public class MVSPPlayerListener extends PlayerListener {

    private final MultiverseSignPortals plugin;

    public MVSPPlayerListener(MultiverseSignPortals plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event.isCancelled()) {
            return;
        }
        PortalDetector detector = new PortalDetector(this.plugin);
        try {
            detector.getNotchPortalDestination(event.getPlayer().getLocation());
            this.plugin.log(Level.FINER, "Found a Multiverse Sign");
        } catch (NoMultiverseSignFoundException e) {
            // This will simply act as a notch portal.
            this.plugin.log(Level.FINER, "Did NOT find a Multiverse Sign");
        } catch (MoreThanOneSignFoundException e) {
            event.getPlayer().sendMessage(ChatColor.RED + "Sorry " + ChatColor.WHITE + "but more than 1 sign was found where the first line was [mv] or [multiverse]. Please remove one of them.");
        }

        event.setCancelled(true);
    }
}
