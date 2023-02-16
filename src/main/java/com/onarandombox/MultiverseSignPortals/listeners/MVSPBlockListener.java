/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project.
 */

package com.onarandombox.MultiverseSignPortals.listeners;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.api.SafeTTeleporter;
import com.onarandombox.MultiverseCore.destination.ParsedDestination;
import com.onarandombox.MultiverseCore.teleportation.TeleportResult;
import com.onarandombox.MultiverseCore.utils.MVPermissions;
import com.onarandombox.MultiverseSignPortals.MultiverseSignPortals;
import com.onarandombox.MultiverseSignPortals.utils.PortalDetector;
import com.onarandombox.MultiverseSignPortals.utils.SignStatus;
import com.onarandombox.MultiverseSignPortals.utils.SignTools;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.material.RedstoneTorch;
import org.bukkit.permissions.PermissionDefault;

public class MVSPBlockListener implements Listener {
    private final String CREATE_PERM = "multiverse.signportal.create";
    private MultiverseSignPortals plugin;
    private MVPermissions permissions;

    public MVSPBlockListener(MultiverseSignPortals plugin) {
        this.plugin = plugin;
        this.permissions = this.plugin.getCore().getMVPerms();
        this.permissions.addPermission(CREATE_PERM, PermissionDefault.OP);
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
        SignStatus status = plugin.getPortalDetector().getSignStatus(sign);
        if (status == SignStatus.SignPortal) {
            String destString = plugin.getPortalDetector().processSign(sign);
            for (Entity entity : plugin.getPortalDetector().getRedstoneTeleportEntities(sign)) {
                this.takeEntityToDestination(entity, destString);
            }
        }
    }

    private void takeEntityToDestination(Entity entity, String destString) {
        if (destString != null) {
            SafeTTeleporter teleporter = plugin.getCore().getSafeTTeleporter();
            ParsedDestination<?> d = plugin.getCore().getDestinationsProvider().parseDestination(destString);
            Logging.finer("Found a Destination! (" + d + ")");
            if (entity instanceof Player) {
                Player player = (Player) entity;
                if (plugin.getPortalDetector().playerCanGoToDestination(player, d)) {
                    TeleportResult result = teleporter.safelyTeleport(
                            this.plugin.getCore().getMVCommandManager().getCommandIssuer(Bukkit.getConsoleSender()), player, d);
                    if (result == TeleportResult.FAIL_UNSAFE) {
                        Logging.finer("The Destination was not safe! (" + ChatColor.RED + d + ChatColor.WHITE + ")");
                    } else {
                        Logging.finer("Teleported " + entity + " to: " + ChatColor.GREEN + d);
                    }
                } else {
                    Logging.finer("Denied permission to go to destination!");
                }
            } else {
                TeleportResult result = teleporter.safelyTeleport(Bukkit.getConsoleSender(), entity, d.getLocation(entity), true);
                if (result == TeleportResult.FAIL_UNSAFE) {
                    Logging.finer("The Destination was not safe! (" + ChatColor.RED + d + ChatColor.WHITE + ")");
                } else {
                    Logging.finer("Teleported " + entity + " to: " + ChatColor.GREEN + d);
                }
            }
        } else {
            Logging.finer("The destination was not set on the sign!");
        }
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
            PortalDetector pd = this.plugin.getPortalDetector();
            if (pd.getSignStatus(s) == SignStatus.NetherPortalSign || pd.getSignStatus(s) == SignStatus.SignPortal) {
                if (!this.permissions.hasPermission(event.getPlayer(), CREATE_PERM, true)) {
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
        if (this.plugin.getCore().getMVPerms().hasPermission(event.getPlayer(), "multiverse.signportal.create", true)) {
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
