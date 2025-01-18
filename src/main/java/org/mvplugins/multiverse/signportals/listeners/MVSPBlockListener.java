/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project.
 */

package org.mvplugins.multiverse.signportals.listeners;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.mvplugins.multiverse.core.api.destination.DestinationInstance;
import org.mvplugins.multiverse.core.api.destination.DestinationsProvider;
import org.mvplugins.multiverse.core.api.teleportation.SafetyTeleporter;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.signportals.MultiverseSignPortals;
import org.mvplugins.multiverse.signportals.utils.PortalDetector;
import org.mvplugins.multiverse.signportals.utils.SignStatus;
import org.mvplugins.multiverse.signportals.utils.SignTools;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.material.RedstoneTorch;

import static org.mvplugins.multiverse.core.permissions.PermissionUtils.hasPermission;

@Service
public class MVSPBlockListener implements SignPortalsListener {
    private final String CREATE_PERM = "multiverse.signportal.create";
    private final MultiverseSignPortals plugin;
    private final PortalDetector pd;
    private final PortalDetector portalDetector;
    private final DestinationsProvider destinationsProvider;
    private final SafetyTeleporter safetyTeleporter;

    @Inject
    public MVSPBlockListener(@NotNull MultiverseSignPortals plugin,
                             @NotNull PortalDetector pd,
                             @NotNull PluginManager pluginManager,
                             @NotNull PortalDetector portalDetector,
                             @NotNull DestinationsProvider destinationsProvider,
                             @NotNull SafetyTeleporter safetyTeleporter) {
        this.plugin = plugin;
        this.pd = pd;
        this.portalDetector = portalDetector;
        this.destinationsProvider = destinationsProvider;
        this.safetyTeleporter = safetyTeleporter;
        pluginManager.addPermission(new Permission(CREATE_PERM, PermissionDefault.OP));
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Logging.finer("Sign changed");
        if (event.getLine(1).equalsIgnoreCase("[mv]") || event.getLine(1).equalsIgnoreCase("[multiverse]")) {
            createMultiverseSignPortal(event);
        } else {
            checkForHack(event);
        }
    }

    @EventHandler
    public void redstonePower(BlockRedstoneEvent event) {
        if (event.getNewCurrent() <= 0) {
            return;
        }
        boolean torch = false;
        if (event.getBlock().getState().getData() instanceof RedstoneTorch) {
            torch = true;
        }
        Block block = getNearbySign(event.getBlock(), torch);
        if (block == null) {
            return;
        }
        Sign sign = (Sign) block.getState();
        SignStatus status = pd.getSignStatus(sign);
        if (status == SignStatus.SignPortal) {
            String destString = pd.processSign(sign);
            for (Entity entity : pd.getRedstoneTeleportEntities(sign)) {
                this.takeEntityToDestination(entity, destString);
            }
        }
    }

    private void takeEntityToDestination(Entity entity, String destString) {
        if (destString == null) {
            Logging.finer("The destination was not set on the sign!");
        }
        DestinationInstance<?, ?> d = destinationsProvider.parseDestination(destString).getOrNull();
        if (d == null) {
            Logging.warning("Could not find destination: " + destString);
            return;
        }
        Logging.finer("Found a Destination! (" + d + ")");
        safetyTeleporter.to(d)
                .by(Bukkit.getConsoleSender())
                .teleport(entity)
                .onSuccess(() -> Logging.finer("Teleported " + entity + " to: " + ChatColor.GREEN + d))
                .onFailure(error -> Logging.warning("Failed to teleport " + entity + " to: " + d + " (" + error + ")"));
    }

    private Block getNearbySign(Block block, boolean torch) {
        Block nearBlock = block.getRelative(BlockFace.EAST);
        if (nearBlock.getState() instanceof Sign) {
            return nearBlock;
        }
        nearBlock = block.getRelative(BlockFace.NORTH);
        if (nearBlock.getState() instanceof Sign) {
            return nearBlock;
        }
        nearBlock = block.getRelative(BlockFace.SOUTH);
        if (nearBlock.getState() instanceof Sign) {
            return nearBlock;
        }
        nearBlock = block.getRelative(BlockFace.WEST);
        if (nearBlock.getState() instanceof Sign) {
            return nearBlock;
        }
        nearBlock = block.getRelative(BlockFace.UP);
        if (nearBlock.getState() instanceof Sign) {
            return nearBlock;
        }
        if (torch) {
            nearBlock = nearBlock.getRelative(BlockFace.UP);
            if (nearBlock.getState() instanceof Sign) {
                return nearBlock;
            }
        }
        return null;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }
        BlockState state = event.getBlock().getState();
        if (state instanceof Sign) {
            Sign s = (Sign) state;
            if (pd.getSignStatus(s) == SignStatus.NetherPortalSign || pd.getSignStatus(s) == SignStatus.SignPortal) {
                if (!hasPermission(event.getPlayer(), CREATE_PERM)) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage("You don't have permission to destroy a SignPortal!");
                    event.getPlayer().sendMessage(ChatColor.GREEN + CREATE_PERM);
                }
            }
        }
    }

    private void checkForHack(SignChangeEvent event) {
        if (SignTools.isMVSign(event.getLine(1), ChatColor.DARK_GREEN) || SignTools.isMVSign(event.getLine(1), ChatColor.DARK_BLUE)) {
            Logging.warning("WOAH! Player: [" + event.getPlayer().getName() + "] tried to HACK a Multiverse SignPortal into existance!");
            this.warnOps("WOAH! Player: [" + event.getPlayer().getName() + "] tried to " + ChatColor.RED + "HACK" + ChatColor.WHITE + " a"
                    + ChatColor.AQUA + " Multiverse SignPortal" + ChatColor.WHITE + " into existance!");
            event.setCancelled(true);
        }
    }

    private void createMultiverseSignPortal(SignChangeEvent event) {
        if (hasPermission(event.getPlayer(), "multiverse.signportal.create")) {
            Logging.finer("MV SignPortal Created");
            event.setLine(1, ChatColor.DARK_GREEN + event.getLine(1));
            checkRedstoneTeleportTargets(event);
        } else {
            Logging.finer("No Perms to create");
            event.setLine(1, ChatColor.DARK_RED + event.getLine(1));
            event.getPlayer().sendMessage("You don't have permission to create a SignPortal!");
            event.getPlayer().sendMessage(ChatColor.GREEN + CREATE_PERM);
        }
    }

    private void checkRedstoneTeleportTargets(SignChangeEvent event) {
        if (PortalDetector.REDSTONE_TELEPORT_PATTERN.matcher(event.getLine(0)).matches()) {
            event.setLine(0, ChatColor.DARK_GREEN + event.getLine(0));
        }
    }

    private void warnOps(String string) {
        for (Player p : this.plugin.getServer().getOnlinePlayers()) {
            if (p.isOp()) {
                p.sendMessage(string);
            }
        }
    }
}
