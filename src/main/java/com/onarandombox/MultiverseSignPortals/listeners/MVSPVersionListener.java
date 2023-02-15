/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project.
 */

package com.onarandombox.MultiverseSignPortals.listeners;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.destination.ParsedDestination;
import com.onarandombox.MultiverseCore.event.MVDebugModeEvent;
import com.onarandombox.MultiverseCore.event.MVPlayerTouchedPortalEvent;
import com.onarandombox.MultiverseCore.event.MVVersionEvent;
import com.onarandombox.MultiverseSignPortals.MultiverseSignPortals;
import com.onarandombox.MultiverseSignPortals.exceptions.MoreThanOneSignFoundException;
import com.onarandombox.MultiverseSignPortals.exceptions.NoMultiverseSignFoundException;
import com.onarandombox.MultiverseSignPortals.utils.PortalDetector;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MVSPVersionListener implements Listener {
    private MultiverseSignPortals plugin;

    public MVSPVersionListener(MultiverseSignPortals plugin) {
        this.plugin = plugin;
    }

    /**
     * This method is called when Multiverse-Core wants to know what version we are.
     * @param event The Version event.
     */
    @EventHandler
    public void versionEvent(MVVersionEvent event) {
        event.appendVersionInfo(this.plugin.getVersionInfo());
    }

    /**
     * This method is called when a player touches a portal.
     * It's used to handle the intriquite messiness of priority between MV plugins.
     * @param event The PTP event.
     */
    @EventHandler
    public void portalTouchEvent(MVPlayerTouchedPortalEvent event) {
        Logging.finer("Found The TouchedPortal event.");
        Player p = event.getPlayer();
        Location l = event.getBlockTouched();

        PortalDetector detector = new PortalDetector(this.plugin);
        try {
            String destString = detector.getNotchPortalDestination(p, l);

            if (destString != null) {
                ParsedDestination<?> d = this.plugin.getCore().getDestinationsProvider().parseDestination(destString);
                Logging.fine(destString + " ::: " + d);
                if (detector.playerCanGoToDestination(p, d)) {
                    // If the player can go to the destination on the sign...
                    // We're overriding NetherPortals.
                    Logging.fine("Player could go to destination!");
                    event.setCancelled(true);
                } else {
                    Logging.fine("Player could NOT go to destination!");
                }
            }

        } catch (NoMultiverseSignFoundException e) {
            // This will simply act as a notch portal.
            Logging.finer("Did NOT find a Multiverse Sign");
        } catch (MoreThanOneSignFoundException e) {
            p.sendMessage(String.format("%sSorry %sbut more than 1 sign was found where the second line was [mv] or [multiverse]. Please remove one of the signs.",
                            ChatColor.RED, ChatColor.WHITE));
        }
    }

    @EventHandler
    public void debugModeChange(MVDebugModeEvent event) {
        Logging.setDebugLevel(event.getLevel());
    }
}
