/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project.
 */

package com.onarandombox.MultiverseSignPortals.listeners;

import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiverseCore.event.MVPlayerTouchedPortalEvent;
import com.onarandombox.MultiverseCore.event.MVVersionRequestEvent;
import com.onarandombox.MultiverseSignPortals.MultiverseSignPortals;
import com.onarandombox.MultiverseSignPortals.exceptions.MoreThanOneSignFoundException;
import com.onarandombox.MultiverseSignPortals.exceptions.NoMultiverseSignFoundException;
import com.onarandombox.MultiverseSignPortals.utils.PortalDetector;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Event;

import java.util.logging.Level;

public class MVSPVersionListener extends CustomEventListener {
    private MultiverseSignPortals plugin;

    public MVSPVersionListener(MultiverseSignPortals plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCustomEvent(Event event) {
        this.plugin.log(Level.FINEST, "Found event: " + event.getEventName());
        if (event.getEventName().equals("MVVersion") && event instanceof MVVersionRequestEvent) {
            ((MVVersionRequestEvent) event).setPasteBinBuffer(this.plugin.dumpVersionInfo(((MVVersionRequestEvent) event).getPasteBinBuffer()));
        } else if (event.getEventName().equals("MVPlayerTouchedPortalEvent") && event instanceof MVPlayerTouchedPortalEvent) {
            this.plugin.log(Level.FINER, "Found The TouchedPortal event.");
            MVPlayerTouchedPortalEvent pte = ((MVPlayerTouchedPortalEvent) event);
            Player p = pte.getPlayer();
            Location l = pte.getBlockTouched();

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
                        pte.setCancelled(true);
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
}
