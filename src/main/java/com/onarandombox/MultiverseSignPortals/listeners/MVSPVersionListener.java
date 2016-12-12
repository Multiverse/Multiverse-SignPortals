/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project.
 */

package com.onarandombox.multiversesignportals.listeners;

import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiverseCore.event.MVPlayerTouchedPortalEvent;
import com.onarandombox.MultiverseCore.event.MVVersionEvent;
import com.onarandombox.multiversesignportals.MultiverseSignPortals;
import com.onarandombox.multiversesignportals.exceptions.MoreThanOneSignFoundException;
import com.onarandombox.multiversesignportals.exceptions.NoMultiverseSignFoundException;
import com.onarandombox.multiversesignportals.utils.PortalDetector;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.logging.Level;

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
        this.plugin.log(Level.FINER, "Found The TouchedPortal event.");
        Player p = event.getPlayer();
        Location l = event.getBlockTouched();

        PortalDetector detector = new PortalDetector(this.plugin);
        try {
            String destString = detector.getNotchPortalDestination(p, l);

            if (destString != null) {
                MVDestination d = this.plugin.getCore().getDestFactory().getDestination(destString);
                this.plugin.log(Level.FINE, destString + " ::: " + d);
                if (detector.playerCanGoToDestination(p, d)) {
                    // If the player can go to the destination on the sign...
                    // We're overriding NetherPortals.
                    this.plugin.log(Level.FINE, "Player could go to destination!");
                    event.setCancelled(true);
                } else {
                    this.plugin.log(Level.FINE, "Player could NOT go to destination!");
                }
            }

        } catch (NoMultiverseSignFoundException e) {
            // This will simply act as a notch portal.
            this.plugin.log(Level.FINER, "Did NOT find a Multiverse Sign");
        } catch (MoreThanOneSignFoundException e) {
            this.plugin.getCore().getMessaging().sendMessage(p,
                    String.format("%sSorry %sbut more than 1 sign was found where the second line was [mv] or [multiverse]. Please remove one of the signs.",
                            ChatColor.RED, ChatColor.WHITE), false);
        }
    }
}
