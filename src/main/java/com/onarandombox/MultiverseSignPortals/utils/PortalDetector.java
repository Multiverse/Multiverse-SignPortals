package com.onarandombox.MultiverseSignPortals.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;

import com.onarandombox.MultiverseSignPortals.MultiverseSignPortals;
import com.onarandombox.MultiverseSignPortals.exceptions.MoreThanOneSignFoundException;
import com.onarandombox.utils.LocationManipulation;

enum Axis {
    X, Z
}

public class PortalDetector {
    private MultiverseSignPortals plugin;
    private static BlockFace[] faces = { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST,
            BlockFace.NORTH_EAST, BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST };

    public PortalDetector(MultiverseSignPortals plugin) {
        this.plugin = plugin;
    }

    public String getNotchPortalDestination(Location l) throws MoreThanOneSignFoundException, NoMultiverseSignFoundException {
        // Determine corner, should be 1 of 4
        Block block = l.getBlock();
        Location portalStart;
        Location portalEnd;
        List<Sign> foundSigns = null;
        if (block.getType() == Material.PORTAL) {
            // We found the bottom 2: ##
            if (block.getRelative(1, 0, 0).getType() == Material.PORTAL) {
                portalEnd = block.getRelative(1, 0, 0).getLocation();
                portalStart = block.getRelative(0, 2, 0).getLocation();
                foundSigns = this.checkBlocksOutside(l.getWorld().getBlockAt(portalStart), l.getWorld().getBlockAt(portalEnd), Axis.X);
                this.plugin.log(Level.FINER, "Found normal X");

            } else if (block.getRelative(-1, 0, 0).getType() == Material.PORTAL) {
                portalEnd = block.getLocation();
                portalStart = block.getRelative(-1, 2, 0).getLocation();
                foundSigns = this.checkBlocksOutside(l.getWorld().getBlockAt(portalStart), l.getWorld().getBlockAt(portalEnd), Axis.X);
                this.plugin.log(Level.FINER, "Found inverse X");
            }
            else if (block.getRelative(0, 0, 1).getType() == Material.PORTAL) {
                portalEnd = block.getRelative(0, 0, 1).getLocation();
                portalStart = block.getRelative(0, 2, 0).getLocation();
                foundSigns = this.checkBlocksOutside(l.getWorld().getBlockAt(portalStart), l.getWorld().getBlockAt(portalEnd), Axis.Z);
                this.plugin.log(Level.FINER, "Found normal Z");

            } else if (block.getRelative(0, 0, -1).getType() == Material.PORTAL) {
                portalEnd = block.getLocation();
                portalStart = block.getRelative(0, 2, -1).getLocation();
                foundSigns = this.checkBlocksOutside(l.getWorld().getBlockAt(portalStart), l.getWorld().getBlockAt(portalEnd), Axis.Z);
                this.plugin.log(Level.FINER, "Found inverse Z");
            }
        }
        if (foundSigns != null) {
            this.plugin.log(Level.FINE, "Woo! Notch Portal!");
            return processSigns(foundSigns);
        } else {
            this.plugin.log(Level.FINE, ":( No Notch Portal Here...");
        }
        return null;
    }

    private String processSigns(List<Sign> foundSigns) throws MoreThanOneSignFoundException, NoMultiverseSignFoundException {
        String destString = null;
        for (Sign s : foundSigns) {
            if (s.getLine(0).equalsIgnoreCase("[multiverse]") || s.getLine(0).equalsIgnoreCase("[mv]")) {
                this.plugin.log(Level.FINER, "Found a MV Sign");
                if (destString != null) {
                    // 2 MV signs were found around this portal. Whoops.
                    throw new MoreThanOneSignFoundException();
                }
                destString = s.getLine(1);
            } else {
                this.plugin.log(Level.FINER, "Sign data: " + s.getLine(0));
            }
        }
        if (destString == null) {
            throw new NoMultiverseSignFoundException();
        }
        return destString;

    }

    /**
     * @param top The top left of the portal
     * @param bottom The bottom right of the portal
     * @param a
     * @return
     */
    private List<Sign> checkBlocksOutside(Block top, Block bottom, Axis a) {
        int xM = (a == Axis.X) ? 1 : 0;
        int zM = (a == Axis.Z) ? 1 : 0;
        // Check the top 2
        if (top.getRelative(0, 1, 0).getType() != Material.OBSIDIAN) {
            return null;
        }
        if (top.getRelative(1 * xM, 1, 1 * zM).getType() != Material.OBSIDIAN) {
            return null;
        }
        this.plugin.log(Level.FINER, "Found top 2");
        // Check the bottom 2
        if (bottom.getRelative(0, -1, 0).getType() != Material.OBSIDIAN) {
            return null;
        }
        if (bottom.getRelative(-1 * xM, -1, -1 * zM).getType() != Material.OBSIDIAN) {
            return null;
        }
        this.plugin.log(Level.FINER, "Found bottom 2");
        // Check the Left 3
        if (top.getRelative(-1 * xM, 0, -1 * zM).getType() != Material.OBSIDIAN) {
            return null;
        }
        if (top.getRelative(-1 * xM, -1, -1 * zM).getType() != Material.OBSIDIAN) {
            return null;
        }
        if (top.getRelative(-1 * xM, -2, -1 * zM).getType() != Material.OBSIDIAN) {
            return null;
        }
        this.plugin.log(Level.FINER, "Found left 3");
        // Check the Right 3
        if (bottom.getRelative(1 * xM, 0, 1 * zM).getType() != Material.OBSIDIAN) {
            return null;
        }
        if (bottom.getRelative(1 * xM, 1, 1 * zM).getType() != Material.OBSIDIAN) {
            return null;
        }
        if (bottom.getRelative(1 * xM, 2, 1 * zM).getType() != Material.OBSIDIAN) {
            return null;
        }
        this.plugin.log(Level.FINER, "Found right 3");
        Block topper = top.getRelative(-1 - (xM * 1), 1, -1 - (zM * 1));
        Block bottomer = bottom.getRelative(1 + (xM * 1), -1, 1 + (zM * 1));
        return this.checkZoneForSigns(topper, bottomer);
    }

    private List<Sign> checkZoneForSigns(Block topper, Block bottomer) {
        Location looking = new Location(topper.getWorld(), 0, 0, 0);
        List<Sign> signs = new ArrayList<Sign>();
        for (int x = topper.getX(); x <= bottomer.getX(); x++) {
            looking.setX(x);
            for (int y = bottomer.getY(); y <= topper.getY(); y++) {
                looking.setY(y);
                for (int z = topper.getZ(); z <= bottomer.getZ(); z++) {
                    looking.setZ(z);
                    this.plugin.log(Level.FINEST, "Looking for sign at " + LocationManipulation.strCoordsRaw(looking));
                    Material isASign = topper.getWorld().getBlockAt(looking).getType();
                    if (isASign == Material.WALL_SIGN || isASign == Material.SIGN_POST) {
                        this.plugin.log(Level.FINER, "WOO Found one! " + LocationManipulation.strCoordsRaw(looking));
                        signs.add((Sign) topper.getWorld().getBlockAt(looking).getState());
                    }
                }
            }
        }
        return signs;
    }
}
