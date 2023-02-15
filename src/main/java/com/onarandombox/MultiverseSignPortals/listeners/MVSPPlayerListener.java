/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project.
 */

package com.onarandombox.MultiverseSignPortals.listeners;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.api.SafeTTeleporter;
import com.onarandombox.MultiverseCore.destination.DestinationsProvider;
import com.onarandombox.MultiverseCore.destination.ParsedDestination;
import com.onarandombox.MultiverseCore.teleportation.TeleportResult;
import com.onarandombox.MultiverseCore.utils.MVPermissions;
import com.onarandombox.MultiverseSignPortals.MultiverseSignPortals;
import com.onarandombox.MultiverseSignPortals.exceptions.MoreThanOneSignFoundException;
import com.onarandombox.MultiverseSignPortals.exceptions.NoMultiverseSignFoundException;
import com.onarandombox.MultiverseSignPortals.utils.PortalDetector;
import com.onarandombox.MultiverseSignPortals.utils.SignStatus;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.permissions.PermissionDefault;

public class MVSPPlayerListener implements Listener {

    private MultiverseSignPortals plugin;
    private MVPermissions permissions;
    private PortalDetector pd;

    public MVSPPlayerListener(MultiverseSignPortals plugin) {
        this.plugin = plugin;
        this.permissions = this.plugin.getCore().getMVPerms();
        this.permissions.addPermission("multiverse.signportal.validate", PermissionDefault.OP);
        this.pd = new PortalDetector(this.plugin);
    }

    /**
     * Called when the portal is ready to take the player to the destination.
     * @param event The Portal event.
     */
    @EventHandler
    public void playerPortal(PlayerPortalEvent event) {
        if (event.isCancelled()) {
            return;
        }
        PortalDetector detector = new PortalDetector(this.plugin);
        try {
            String destString = detector.getNotchPortalDestination(event.getPlayer(), event.getFrom());
            if (destString != null) {
                Logging.finer("Found a Multiverse Sign");
                DestinationsProvider destinationsProvider = this.plugin.getCore().getDestinationsProvider();
                destString = ChatColor.stripColor(destString);
                ParsedDestination<?> dest = destinationsProvider.parseDestination(destString);
                if (dest == null) {
                    Logging.finer("Destination was null!");
                    return;
                }

                // This is a valid sign portal, so we need to cancel the event.
                event.setCancelled(true);
                SafeTTeleporter teleporter = this.plugin.getCore().getSafeTTeleporter();
                TeleportResult result = teleporter.safelyTeleport(
                        this.plugin.getCore().getMVCommandManager().getCommandIssuer(event.getPlayer()), event.getPlayer(), dest);
                if (result == TeleportResult.FAIL_UNSAFE) {
                    event.getPlayer().sendMessage("The Destination was not safe! (" + ChatColor.RED + dest + ChatColor.WHITE + ")");
                }
            }

        } catch (NoMultiverseSignFoundException e) {
            // This will simply act as a notch portal.
            Logging.finer("Did NOT find a Multiverse Sign");
        } catch (MoreThanOneSignFoundException e) {
            event.getPlayer().sendMessage(String.format("%sSorry %sbut more than 1 sign was found where the second line was [mv] or [multiverse]. Please remove one of the signs.",
                            ChatColor.RED, ChatColor.WHITE));
            event.setCancelled(true);
        }
    }

    /**
     * Called when a player clicks on anything.
     * @param event The Interact event.
     */
    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getClickedBlock().getState() instanceof Sign) {
                Logging.finer("Found a Sign!");
                Sign s = (Sign) event.getClickedBlock().getState();
                SignStatus status = this.pd.getSignStatus(s);
                if (status == SignStatus.SignPortal) {
                    String destString = this.pd.processSign(s);
                    this.takePlayerToDestination(event.getPlayer(), destString);
                    event.setCancelled(true);
                } else if (status == SignStatus.Disabled) {
                    this.pd.activateSignPortal(event.getPlayer(), ChatColor.RED + "Disabled", s);
                    event.setCancelled(true);
                } else if (status == SignStatus.Legacy) {
                    this.pd.activateSignPortal(event.getPlayer(), ChatColor.AQUA + "Legacy", s);
                    event.setCancelled(true);
                } else if (status == SignStatus.NetherPortalSign) {
                    event.setCancelled(true);
                }
            }
        }
    }

    private void takePlayerToDestination(Player player, String destString) {
        if (destString != null) {
            Logging.finer("Found a SignPortal! (" + destString + ")");
            SafeTTeleporter teleporter = this.plugin.getCore().getSafeTTeleporter();
            DestinationsProvider destinationsProvider = this.plugin.getCore().getDestinationsProvider();

            ParsedDestination<?> destination = destinationsProvider.parseDestination(destString);
            Logging.finer("Found a Destination! (" + destination + ")");
            if (this.pd.playerCanGoToDestination(player, destination)) {
                TeleportResult result = teleporter.safelyTeleport(
                        this.plugin.getCore().getMVCommandManager().getCommandIssuer(player), player, destination);
                if (result == TeleportResult.FAIL_UNSAFE) {
                    player.sendMessage("The Destination was not safe! (" + ChatColor.RED + destination + ChatColor.WHITE + ")");
                }
            } else {
                Logging.finer("Denied permission to go to destination!");
            }
        } else {
            player.sendMessage("The Destination was not set on the sign!");
        }
    }
}
