/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project.
 */

package com.onarandombox.MultiverseSignPortals.listeners;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiverseCore.destination.DestinationFactory;
import com.onarandombox.MultiverseCore.enums.TeleportResult;
import com.onarandombox.MultiverseCore.utils.MVPermissions;
import com.onarandombox.MultiverseCore.api.SafeTTeleporter;
import com.onarandombox.MultiverseSignPortals.MultiverseSignPortals;
import com.onarandombox.MultiverseSignPortals.exceptions.MoreThanOneSignFoundException;
import com.onarandombox.MultiverseSignPortals.exceptions.NoMultiverseSignFoundException;
import com.onarandombox.MultiverseSignPortals.utils.PortalDetector;
import com.onarandombox.MultiverseSignPortals.utils.SignStatus;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.permissions.PermissionDefault;

public class MVSPPlayerListener implements Listener {

    private static final String USE_PERMISSION = "multiverse.signportal.use";
    private static final String VALIDATE_PERMISSION = "multiverse.signportal.validate";
    private final MultiverseSignPortals plugin;
    private final MVPermissions permissions;
    private final PortalDetector pd;

    public MVSPPlayerListener(MultiverseSignPortals plugin) {
        this.plugin = plugin;
        this.permissions = this.plugin.getCore().getMVPerms();
        this.permissions.addPermission(VALIDATE_PERMISSION, PermissionDefault.OP);
        this.permissions.addPermission(USE_PERMISSION, PermissionDefault.TRUE);
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
                DestinationFactory df = this.plugin.getCore().getDestFactory();
                destString = ChatColor.stripColor(destString);
                MVDestination dest = df.getDestination(destString);
                MVSPTravelAgent travelAgent = new MVSPTravelAgent(this.plugin.getCore(), dest, event.getPlayer());
                travelAgent.setPortalEventTravelAgent(event);
                event.setTo(dest.getLocation(event.getPlayer()));
            }

        } catch (NoMultiverseSignFoundException e) {
            // This will simply act as a notch portal.
            Logging.finer("Did NOT find a Multiverse Sign");
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
        // The event must not be canceled...
        if (event.isCancelled()) {
            return;
        }

        // We must be right-clicking...
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // And it must be a sign
        if (!(event.getClickedBlock().getState() instanceof Sign)) {
            return;
        }

        Logging.finer("Found a Sign!");
        Sign s = (Sign) event.getClickedBlock().getState();
        SignStatus status = this.pd.getSignStatus(s);

        Player player = event.getPlayer();
        switch (status) {
            case SignPortal:
                if (permissions.hasPermission(player, USE_PERMISSION, false)) {
                    String destString = this.pd.processSign(s);
                    this.takePlayerToDestination(player, destString);
                } else {
                    player.sendMessage(ChatColor.RED + "You do not have the required permission to use SignPortals (" + USE_PERMISSION + ")");
                }
                event.setCancelled(true);
                break;
            case Legacy:
                this.pd.activateSignPortal(player, ChatColor.AQUA + "Legacy", s);
                event.setCancelled(true);
                break;
            case Disabled:
                this.pd.activateSignPortal(player, ChatColor.RED + "Disabled", s);
                event.setCancelled(true);
                break;
            case NetherPortalSign:
                event.setCancelled(true);
        }
    }

    private void takePlayerToDestination(Player player, String destString) {
        if (destString != null) {
            Logging.finer("Found a SignPortal! (" + destString + ")");
            SafeTTeleporter teleporter = this.plugin.getCore().getSafeTTeleporter();
            DestinationFactory df = this.plugin.getCore().getDestFactory();

            MVDestination d = df.getDestination(destString);
            Logging.finer("Found a Destination! (" + d + ")");
            if (this.pd.playerCanGoToDestination(player, d)) {
                TeleportResult result = teleporter.safelyTeleport(player, player, d);
                if (result == TeleportResult.FAIL_UNSAFE) {
                    player.sendMessage("The Destination was not safe! (" + ChatColor.RED + d + ChatColor.WHITE + ")");
                }
            } else {
                Logging.finer("Denied permission to go to destination!");
            }
        } else {
            player.sendMessage("The Destination was not set on the sign!");
        }
    }


}
