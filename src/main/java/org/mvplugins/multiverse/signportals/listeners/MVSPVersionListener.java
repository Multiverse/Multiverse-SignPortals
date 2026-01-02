/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project.
 */

package org.mvplugins.multiverse.signportals.listeners;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.ChatColor;
import org.mvplugins.multiverse.core.destination.DestinationInstance;
import org.mvplugins.multiverse.core.destination.DestinationsProvider;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.EventMethod;
import org.mvplugins.multiverse.core.event.MVDebugModeEvent;
import org.mvplugins.multiverse.core.event.MVDumpsDebugInfoEvent;
import org.mvplugins.multiverse.core.event.MVPlayerTouchedPortalEvent;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.signportals.MultiverseSignPortals;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.mvplugins.multiverse.signportals.exceptions.MoreThanOneSignFoundException;
import org.mvplugins.multiverse.signportals.exceptions.NoMultiverseSignFoundException;
import org.mvplugins.multiverse.signportals.utils.PortalDetector;

@Service
final class MVSPVersionListener implements SignPortalsListener {
    private final MultiverseSignPortals plugin;
    private final PortalDetector detector;
    private final DestinationsProvider destinationsProvider;

    @Inject
    MVSPVersionListener(@NotNull MultiverseSignPortals plugin,
                        @NotNull PortalDetector detector,
                        @NotNull DestinationsProvider destinationsProvider) {
        this.plugin = plugin;
        this.detector = detector;
        this.destinationsProvider = destinationsProvider;
    }

    /**
     * This method is called when Multiverse-Core wants to know what version we are.
     * @param event The Version event.
     */
    @EventMethod
    public void versionEvent(MVDumpsDebugInfoEvent event) {
        event.appendDebugInfo(this.plugin.getVersionInfo());
    }

    /**
     * This method is called when a player touches a portal.
     * It's used to handle the intriquite messiness of priority between MV plugins.
     * @param event The PTP event.
     */
    @EventMethod
    public void portalTouchEvent(MVPlayerTouchedPortalEvent event) {
        Logging.finer("Found The TouchedPortal event.");
        Player p = event.getPlayer();
        Location l = event.getBlockTouched();

        try {
            String destString = detector.getNotchPortalDestination(p, l);

            if (destString != null) {
                DestinationInstance<?, ?> d = this.destinationsProvider.parseDestination(destString).getOrNull();
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

    @EventMethod
    public void debugModeChange(MVDebugModeEvent event) {
        Logging.setDebugLevel(event.getLevel());
    }
}
