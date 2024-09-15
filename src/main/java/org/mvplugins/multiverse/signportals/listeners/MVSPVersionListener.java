/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project.
 */

package org.mvplugins.multiverse.signportals.listeners;

import com.dumptruckman.minecraft.util.Logging;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.event.MVDebugModeEvent;
import org.mvplugins.multiverse.core.event.MVPlayerTouchedPortalEvent;
import org.mvplugins.multiverse.core.event.MVVersionEvent;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.signportals.MultiverseSignPortals;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

@Service
public class MVSPVersionListener implements SignPortalsListener {
    private final MultiverseSignPortals plugin;

    @Inject
    MVSPVersionListener(@NotNull MultiverseSignPortals plugin) {
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

        // TODO
//        try {
//            String destString = detector.getNotchPortalDestination(p, l);
//
//            if (destString != null) {
//                MVDestination d = this.plugin.getCore().getDestFactory().getDestination(destString);
//                Logging.fine(destString + " ::: " + d);
//                if (detector.playerCanGoToDestination(p, d)) {
//                    // If the player can go to the destination on the sign...
//                    // We're overriding NetherPortals.
//                    Logging.fine("Player could go to destination!");
//                    event.setCancelled(true);
//                } else {
//                    Logging.fine("Player could NOT go to destination!");
//                }
//            }
//
//        } catch (NoMultiverseSignFoundException e) {
//            // This will simply act as a notch portal.
//            Logging.finer("Did NOT find a Multiverse Sign");
//        } catch (MoreThanOneSignFoundException e) {
//            p.sendMessage(String.format("%sSorry %sbut more than 1 sign was found where the second line was [mv] or [multiverse]. Please remove one of the signs.",
//                            ChatColor.RED, ChatColor.WHITE));
//        }
    }

    @EventHandler
    public void debugModeChange(MVDebugModeEvent event) {
        Logging.setDebugLevel(event.getLevel());
    }
}
