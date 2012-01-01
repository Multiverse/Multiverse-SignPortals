/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project.
 */

package com.onarandombox.MultiverseSignPortals.listeners;

import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiverseCore.destination.DestinationFactory;
import com.onarandombox.MultiverseCore.enums.TeleportResult;
import com.onarandombox.MultiverseCore.utils.MVPermissions;
import com.onarandombox.MultiverseCore.utils.MVTravelAgent;
import com.onarandombox.MultiverseCore.utils.SafeTTeleporter;
import com.onarandombox.MultiverseSignPortals.MultiverseSignPortals;
import com.onarandombox.MultiverseSignPortals.exceptions.MoreThanOneSignFoundException;
import com.onarandombox.MultiverseSignPortals.exceptions.NoMultiverseSignFoundException;
import com.onarandombox.MultiverseSignPortals.utils.PortalDetector;
import com.onarandombox.MultiverseSignPortals.utils.SignStatus;
import com.onarandombox.MultiverseSignPortals.utils.SignTools;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.permissions.PermissionDefault;

import java.awt.*;
import java.util.logging.Level;

public class MVSPPlayerListener extends PlayerListener {

    private MultiverseSignPortals plugin;
    private MVPermissions permissions;

    public MVSPPlayerListener(MultiverseSignPortals plugin) {
        this.plugin = plugin;
        this.permissions = this.plugin.getCore().getMVPerms();
        this.permissions.addPermission("multiverse.signportal.validate", PermissionDefault.OP);
    }

    @Override
    public void onPlayerPortal(PlayerPortalEvent event) {
        this.plugin.log(Level.FINE, "CALLING SP!!!");
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
            event.getPlayer().sendMessage(ChatColor.RED + "Sorry " + ChatColor.WHITE + "but more than 1 sign was found where the second line was [mv] or [multiverse]. Please remove one of the signs.");
            event.setCancelled(true);
        }
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getClickedBlock().getState() instanceof Sign) {
                this.plugin.log(Level.FINER, "Found a Sign!");
                Sign s = (Sign) event.getClickedBlock().getState();
                PortalDetector pd = new PortalDetector(this.plugin);
                SignStatus status = pd.getSignStatus(s);
                if (status == SignStatus.SignPortal) {
                    String destString = pd.processSign(s);
                    this.takePlayerToDestination(event.getPlayer(), destString);
                    event.setCancelled(true);
                } else if (status == SignStatus.Disabled) {
                    pd.activateSignPortal(event.getPlayer(), ChatColor.RED + "Disabled", s);
                    event.setCancelled(true);
                } else if (status == SignStatus.Legacy) {
                    pd.activateSignPortal(event.getPlayer(), ChatColor.AQUA + "Legacy", s);
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
            SafeTTeleporter teleporter = new SafeTTeleporter(this.plugin.getCore());
            DestinationFactory df = this.plugin.getCore().getDestFactory();
            MVDestination d = df.getDestination(destString);
            this.plugin.log(Level.FINER, "Found a Destination! (" + d + ")");
            if(this.playerCanGoToDestination(player, d)) {
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

    private boolean playerCanGoToDestination(Player player, MVDestination d) {
        return this.permissions.hasPermission(player, d.getRequiredPermission(), true) &&
               this.permissions.hasPermission(player, "multiverse.teleport.self." + d.getIdentifier(), true);
    }
}
