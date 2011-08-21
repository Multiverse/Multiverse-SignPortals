package com.onarandombox.MultiverseSignPortals.utils;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import com.onarandombox.MultiverseSignPortals.MultiverseSignPortals;

enum Axis {
    X, Z
}

public class PortalDetector {
    MultiverseSignPortals plugin;

    public PortalDetector(MultiverseSignPortals plugin) {
        this.plugin = plugin;
    }

    public boolean isNotchStylePortal(Location l) {
        // Determine corner, should be 1 of 4
        Block block = l.getBlock();
        Location portalStart;
        Location portalEnd;
        boolean foundnotch = false;
        if (block.getType() == Material.PORTAL) {
            // We found the bottom 2: ##
            if (block.getRelative(1, 0, 0).getType() == Material.PORTAL) {
                portalEnd = block.getRelative(1, 0, 0).getLocation();
                portalStart = block.getRelative(0, 2, 0).getLocation();
                foundnotch = this.checkBlocksOutside(l.getWorld().getBlockAt(portalStart), l.getWorld().getBlockAt(portalEnd), Axis.X);
                
            } else if (block.getRelative(-1, 0, 0).getType() == Material.PORTAL) {
                portalEnd = block.getLocation();
                portalStart = block.getRelative(-1, 2, 0).getLocation();
                foundnotch = this.checkBlocksOutside(l.getWorld().getBlockAt(portalStart), l.getWorld().getBlockAt(portalEnd), Axis.X);
            }
        }
        if(foundnotch) {
            this.plugin.log(Level.FINE, "Woo! Notch Portal!");
        }
        return foundnotch;
    }

    private boolean checkBlocksOutside(Block top, Block bottom, Axis a) {
        int xM = (a == Axis.X) ? 1 : 0;
        int zM = (a == Axis.Z) ? 1 : 0;
        // Check the top 2
        if (top.getRelative(0, 1, 0).getType() != Material.OBSIDIAN) {
            return false;
        }
        if (top.getRelative(1 * xM, 1, 1 * zM).getType() != Material.OBSIDIAN) {
            return false;
        }
        // Check the bottom 2
        if (top.getRelative(0, -1, 0).getType() != Material.OBSIDIAN) {
            return false;
        }
        if (top.getRelative(-1 * xM, -1, -1 * zM).getType() != Material.OBSIDIAN) {
            return false;
        }
        // Check the Left 3
        if (top.getRelative(-1 * xM, 0, -1 * zM).getType() != Material.OBSIDIAN) {
            return false;
        }
        if (top.getRelative(-1 * xM, -1, -1 * zM).getType() != Material.OBSIDIAN) {
            return false;
        }
        if (top.getRelative(-1 * xM, -2, -1 * zM).getType() != Material.OBSIDIAN) {
            return false;
        }
        // Check the Right 3
        if (top.getRelative(1 * xM, 0, 1 * zM).getType() != Material.OBSIDIAN) {
            return false;
        }
        if (top.getRelative(1 * xM, 1, 1 * zM).getType() != Material.OBSIDIAN) {
            return false;
        }
        if (top.getRelative(1 * xM, 2, 1 * zM).getType() != Material.OBSIDIAN) {
            return false;
        }
        return true;
    }
}
