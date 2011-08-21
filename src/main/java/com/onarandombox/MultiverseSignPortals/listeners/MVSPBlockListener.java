package com.onarandombox.MultiverseSignPortals.listeners;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.permissions.PermissionDefault;

import com.onarandombox.MultiverseCore.MVPermissions;
import com.onarandombox.MultiverseSignPortals.MultiverseSignPortals;
import com.onarandombox.MultiverseSignPortals.utils.SignTools;

public class MVSPBlockListener extends BlockListener {
    private MultiverseSignPortals plugin;
    private MVPermissions permissions;

    public MVSPBlockListener(MultiverseSignPortals plugin) {
        this.plugin = plugin;
        this.permissions = this.plugin.getCore().getPermissions();
        this.permissions.addPermission("multiverse.signportal.create", PermissionDefault.OP);
    }

    @Override
    public void onSignChange(SignChangeEvent event) {
        if (event.isCancelled()) {
            return;
        }
        this.plugin.log(Level.FINER, "Sign changed");
        if (event.getLine(0).equalsIgnoreCase("[mv]") || event.getLine(0).equalsIgnoreCase("[multiverse]")) {
            createMultiverseSignPortal(event);
        } else {
            checkForHack(event);
        }
    }

    private void checkForHack(SignChangeEvent event) {
        if (SignTools.isMVSign(event.getLine(0), ChatColor.DARK_GREEN) || SignTools.isMVSign(event.getLine(0), ChatColor.DARK_BLUE)) {
            this.plugin.log(Level.WARNING, "WOAH! Player: [" + event.getPlayer().getName() + "] tried to HACK a Multiverse SignPortal into existance!");
            this.warnOps("WOAH! Player: [" + event.getPlayer().getName() + "] tried to " + ChatColor.RED + "HACK" + ChatColor.WHITE + " a"
                    + ChatColor.AQUA + " Multiverse SignPortal" + ChatColor.WHITE + " into existance!");
            event.setCancelled(true);
        }
    }

    private void createMultiverseSignPortal(SignChangeEvent event) {
        if (this.plugin.getCore().getPermissions().hasPermission(event.getPlayer(), "multiverse.signportal.create", true)) {
            this.plugin.log(Level.FINER, "MV SignPortal Created");
            event.setLine(0, ChatColor.DARK_GREEN + event.getLine(0));
        } else {
            this.plugin.log(Level.FINER, "No Perms to create");
            event.setLine(0, ChatColor.DARK_RED + event.getLine(0));
            event.getPlayer().sendMessage("You don't have permission to create a SignPortal!");
            event.getPlayer().sendMessage(ChatColor.GREEN + "multiverse.signportal.create");
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
