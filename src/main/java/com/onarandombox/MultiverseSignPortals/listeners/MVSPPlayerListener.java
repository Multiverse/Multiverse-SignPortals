/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project.
 */

package com.onarandombox.multiversesignportals.listeners;

import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiverseCore.destination.DestinationFactory;
import com.onarandombox.MultiverseCore.enums.TeleportResult;
import com.onarandombox.MultiverseCore.utils.MVPermissions;
import com.onarandombox.MultiverseCore.utils.MVTravelAgent;
import com.onarandombox.MultiverseCore.api.SafeTTeleporter;
import com.onarandombox.multiversesignportals.MultiverseSignPortals;
import com.onarandombox.multiversesignportals.exceptions.MoreThanOneSignFoundException;
import com.onarandombox.multiversesignportals.exceptions.NoMultiverseSignFoundException;
import com.onarandombox.multiversesignportals.utils.PortalDetector;
import com.onarandombox.multiversesignportals.utils.SignStatus;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.permissions.PermissionDefault;

import java.util.logging.Level;

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
                this.plugin.log(Level.FINER, "Found a Multiverse Sign");
                DestinationFactory df = this.plugin.getCore().getDestFactory();
                event.useTravelAgent(true);
                MVDestination dest = df.getDestination(destString);
                event.setPortalTravelAgent(new MVTravelAgent(this.plugin.getCore(), dest, event.getPlayer()));
                event.setTo(dest.getLocation(event.getPlayer()));
            }

        } catch (NoMultiverseSignFoundException e) {
            // This will simply act as a notch portal.
            this.plugin.log(Level.FINER, "Did NOT find a Multiverse Sign");
        } catch (MoreThanOneSignFoundException e) {
            this.plugin.getCore().getMessaging().sendMessage(event.getPlayer(),
                    String.format("%sSorry %sbut more than 1 sign was found where the second line was [mv] or [multiverse]. Please remove one of the signs.",
                            ChatColor.RED, ChatColor.WHITE), false);
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
                this.plugin.log(Level.FINER, "Found a Sign!");
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
            this.plugin.log(Level.FINER, "Found a SignPortal! (" + destString + ")");
            SafeTTeleporter teleporter = this.plugin.getCore().getSafeTTeleporter();
            DestinationFactory df = this.plugin.getCore().getDestFactory();

            MVDestination d = df.getDestination(destString);
            this.plugin.log(Level.FINER, "Found a Destination! (" + d + ")");
            if (this.pd.playerCanGoToDestination(player, d)) {
                TeleportResult result = teleporter.safelyTeleport(player, player, d);
                if (result == TeleportResult.FAIL_UNSAFE) {
                    player.sendMessage("The Destination was not safe! (" + ChatColor.RED + d + ChatColor.WHITE + ")");
                }
            } else {
                this.plugin.log(Level.FINER, "Denied permission to go to destination!");
            }
        } else {
            player.sendMessage("The Destination was not set on the sign!");
        }
    }


}
