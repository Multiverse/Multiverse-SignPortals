/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project.
 */

package org.mvplugins.multiverse.signportals.listeners;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.Location;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.mvplugins.multiverse.core.destination.DestinationInstance;
import org.mvplugins.multiverse.core.destination.DestinationsProvider;
import org.mvplugins.multiverse.core.teleportation.AsyncSafetyTeleporter;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.signportals.exceptions.MoreThanOneSignFoundException;
import org.mvplugins.multiverse.signportals.exceptions.NoMultiverseSignFoundException;
import org.mvplugins.multiverse.signportals.utils.PortalDetector;
import org.mvplugins.multiverse.signportals.utils.SignStatus;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;

import static org.mvplugins.multiverse.core.permissions.PermissionUtils.hasPermission;

@Service
public class MVSPPlayerListener implements SignPortalsListener {

    private static final String USE_PERMISSION = "multiverse.signportal.use";
    private static final String VALIDATE_PERMISSION = "multiverse.signportal.validate";
    private final DestinationsProvider destinationsProvider;
    private final AsyncSafetyTeleporter safetyTeleporter;
    private final PortalDetector pd;

    @Inject
    MVSPPlayerListener(@NotNull PortalDetector pd,
                       @NotNull PluginManager pluginManager,
                       @NotNull DestinationsProvider destinationsProvider,
                       @NotNull AsyncSafetyTeleporter safetyTeleporter) {
        this.destinationsProvider = destinationsProvider;
        this.safetyTeleporter = safetyTeleporter;
        pluginManager.addPermission(new Permission(VALIDATE_PERMISSION, PermissionDefault.OP));
        pluginManager.addPermission(new Permission(USE_PERMISSION, PermissionDefault.TRUE));
        this.pd = pd;
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
        try {
            String destString = pd.getNotchPortalDestination(event.getPlayer(), event.getFrom());
            if (destString != null) {
                Logging.finer("Found a Multiverse Sign");
                destString = ChatColor.stripColor(destString);
                DestinationInstance<?, ?> dest = destinationsProvider.parseDestination(destString).getOrNull();
                if (dest == null) {
                    Logging.warning("Could not find destination: " + destString);
                    return;
                }
                Location destLocation = dest.getLocation(event.getPlayer()).getOrNull();
                if (destLocation == null) {
                    Logging.warning("Could not find Location for destination: " + destString);
                    return;
                }
                event.setTo(destLocation);
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
                if (hasPermission(player, USE_PERMISSION)) {
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
            DestinationInstance<?, ?> d = destinationsProvider.parseDestination(destString).getOrNull();
            if (d == null) {
                Logging.warning("Could not find destination: " + destString);
                return;
            }
            Logging.finer("Found a Destination! (" + d + ")");
            if (this.pd.playerCanGoToDestination(player, d)) {
                safetyTeleporter.to(d).teleport(player)
                        .onSuccess(() -> player.sendMessage("Teleported " + player.getName() + " to: " + ChatColor.GREEN + d))
                        .onFailure(result -> player.sendMessage("Could not teleport " + player.getName() + " to: " + ChatColor.RED + d));
            } else {
                Logging.finer("Denied permission to go to destination!");
            }
        } else {
            player.sendMessage("The Destination was not set on the sign!");
        }
    }
}
