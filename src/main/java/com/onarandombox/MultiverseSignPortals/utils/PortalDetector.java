/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project.
 */

package com.onarandombox.MultiverseSignPortals.utils;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiverseCore.destination.InvalidDestination;
import com.onarandombox.MultiverseSignPortals.MultiverseSignPortals;
import com.onarandombox.MultiverseSignPortals.enums.Axis;
import com.onarandombox.MultiverseSignPortals.exceptions.MoreThanOneSignFoundException;
import com.onarandombox.MultiverseSignPortals.exceptions.NoMultiverseSignFoundException;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Squid;
import org.bukkit.entity.Villager;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PortalDetector {

    public static final Pattern REDSTONE_TELEPORT_PATTERN = Pattern.compile(".*\\[([pPaAmM]|all|ALL):\\d+(:(north|NORTH|south|SOUTH|east|EAST|west|WEST|up|UP|down|DOWN))?\\]");
    private MultiverseSignPortals plugin;

    public PortalDetector(MultiverseSignPortals plugin) {
        this.plugin = plugin;
    }

    public String getNotchPortalDestination(Player p, Location l) throws MoreThanOneSignFoundException, NoMultiverseSignFoundException {
        // Determine corner, should be 1 of 4
        Block block = l.getBlock();
        Location portalStart;
        Location portalEnd;
        List<Sign> foundSigns = null;
        if (block.getType() == Material.NETHER_PORTAL) {
            // We found the bottom 2: ##
            if (block.getRelative(1, 0, 0).getType() == Material.NETHER_PORTAL) {
                portalEnd = block.getRelative(1, 0, 0).getLocation();
                portalStart = block.getRelative(0, 2, 0).getLocation();
                foundSigns = this.checkBlocksOutside(l.getWorld().getBlockAt(portalStart), l.getWorld().getBlockAt(portalEnd), Axis.X);
                Logging.finer("Found normal X");

            } else if (block.getRelative(-1, 0, 0).getType() == Material.NETHER_PORTAL) {
                portalEnd = block.getLocation();
                portalStart = block.getRelative(-1, 2, 0).getLocation();
                foundSigns = this.checkBlocksOutside(l.getWorld().getBlockAt(portalStart), l.getWorld().getBlockAt(portalEnd), Axis.X);
                Logging.finer("Found inverse X");
            } else if (block.getRelative(0, 0, 1).getType() == Material.NETHER_PORTAL) {
                portalEnd = block.getRelative(0, 0, 1).getLocation();
                portalStart = block.getRelative(0, 2, 0).getLocation();
                foundSigns = this.checkBlocksOutside(l.getWorld().getBlockAt(portalStart), l.getWorld().getBlockAt(portalEnd), Axis.Z);
                Logging.finer("Found normal Z");

            } else if (block.getRelative(0, 0, -1).getType() == Material.NETHER_PORTAL) {
                portalEnd = block.getLocation();
                portalStart = block.getRelative(0, 2, -1).getLocation();
                foundSigns = this.checkBlocksOutside(l.getWorld().getBlockAt(portalStart), l.getWorld().getBlockAt(portalEnd), Axis.Z);
                Logging.finer("Found inverse Z");
            }
        }
        if (foundSigns != null) {
            Logging.fine("Woo! Notch Portal!");
            return processSigns(foundSigns, p);
        } else {
            Logging.fine(":( No Notch Portal Here...");
        }
        return null;
    }

    public void activateSignPortal(Player player, String type, Sign sign) {
        if (this.plugin.getCore().getMVPerms().hasPermission(player, "multiverse.signportal.validate", true)) {
            // Do 2-stage validation
            ChatColor colorToChange = ChatColor.DARK_GREEN;
            if(SignTools.isMVSign("mv", ChatColor.GREEN)) {
                colorToChange = ChatColor.DARK_BLUE;
                player.sendMessage("This vanilla sign portal has been " + ChatColor.GREEN + " Validated!");
            } else {
                player.sendMessage("This MV sign portal has been " + ChatColor.GREEN + " Validated!");
            }
            sign.setLine(1, SignTools.setColor(sign.getLine(1), colorToChange));
            sign.update(true);

        } else {
            player.sendMessage("Sorry you don't have permission to activate this " + type + ChatColor.WHITE + " SignPortal.");
        }
    }

    /**
     * Iterate through the signs and return the text if only one is found.
     *
     * @param foundSigns
     *
     * @return
     *
     * @throws MoreThanOneSignFoundException
     * @throws NoMultiverseSignFoundException
     */
    private String processSigns(List<Sign> foundSigns, Player player) throws MoreThanOneSignFoundException, NoMultiverseSignFoundException {
        Sign foundSign = null;
        Sign legacySign = null;
        Sign normalSign = null;
        for (Sign s : foundSigns) {
            if (this.getSignStatus(s) == SignStatus.NetherPortalSign) {
                if (foundSign != null) {
                    throw new MoreThanOneSignFoundException();
                }
                foundSign = s;
            } else if (foundSign == null && this.getSignStatus(s) == SignStatus.Legacy) {
                // Found an old sign
                if (legacySign != null) {
                    throw new MoreThanOneSignFoundException();
                }
                legacySign = s;
            } else if (foundSign == null && this.getSignStatus(s) == SignStatus.SignPortal) {
                // Found a normal signPortal
                if (normalSign != null) {
                    throw new MoreThanOneSignFoundException();
                }
                normalSign = s;
            }
        }
        if (foundSign == null && legacySign == null && normalSign == null) {
            throw new NoMultiverseSignFoundException();
        }
        if (foundSign != null) {
            this.invalidateOtherSigns(foundSign, foundSigns);
            return foundSign.getLine(2);
        }
        if (legacySign != null) {
            if (this.plugin.getCore().getMVPerms().hasPermission(player, "multiverse.signportal.validate", true)) {
                Logging.fine("Migrating Legacy Sign");
                legacySign.setLine(1, SignTools.setColor(legacySign.getLine(1), ChatColor.DARK_BLUE));
                legacySign.update(true);
                this.invalidateOtherSigns(legacySign, foundSigns);
                return legacySign.getLine(2);
            }
        }
        if (normalSign != null) {
            Logging.fine("Migrating Normal Sign");
            normalSign.setLine(1, SignTools.setColor(normalSign.getLine(1), ChatColor.DARK_BLUE));
            normalSign.update(true);
            return normalSign.getLine(2);
        }
        throw new NoMultiverseSignFoundException();
    }

    private void invalidateOtherSigns(Sign sign, List<Sign> foundSigns) {
        for (Sign s : foundSigns) {
            if (!s.equals(sign)) {
                s.setLine(1, SignTools.setColor(s.getLine(1), ChatColor.DARK_RED));
                s.update(true);
            }
        }
    }

    public String processSign(Sign sign) {
        if (SignTools.isMVSign(sign.getLine(1), ChatColor.DARK_GREEN)) {
            Logging.finer("Found a MV Sign");
            return sign.getLine(2) + sign.getLine(3);
        }
        return null;
    }

    public SignStatus getSignStatus(Sign sign) {
        if (SignTools.isMVSign(sign.getLine(1), ChatColor.DARK_GREEN)) {
            Logging.finer("Found a MV Sign (Sign Portal)");
            return SignStatus.SignPortal;
        }
        if (SignTools.isMVSign(sign.getLine(1), ChatColor.DARK_BLUE)) {
            Logging.finer("Found a MV Sign (Nether Portal that has a Sign)");
            return SignStatus.NetherPortalSign;
        }
        if (SignTools.isMVSign(sign.getLine(1), ChatColor.DARK_RED)) {
            Logging.finer("Found a MV Sign (Disabled)");
            return SignStatus.Disabled;
        }
        if (SignTools.isMVSign(sign.getLine(1), null)) {
            Logging.finer("Found a MV Sign (Legacy)");
            return SignStatus.Legacy;
        }
        return SignStatus.NotASignPortal;
    }

    public Entity[] getRedstoneTeleportEntities(Sign sign) {
        if (REDSTONE_TELEPORT_PATTERN.matcher(sign.getLine(0)).matches()) {
            String line = ChatColor.stripColor(sign.getLine(0).replaceAll("(\\[|\\])", ""));
            String[] data = line.split(":");
            final RedstoneTeleportType type;
            switch (data[0].toUpperCase()) {
                case "ALL":
                    type = RedstoneTeleportType.ALL;
                    break;
                case "P":
                    type = RedstoneTeleportType.PLAYERS;
                    break;
                case "M":
                    type = RedstoneTeleportType.MONSTERS;
                    break;
                case "A":
                    type = RedstoneTeleportType.ANIMALS;
                    break;
                default:
                    return new Entity[0];
            }
            final int radius;
            try {
                radius = Integer.valueOf(data[1]);
            } catch (NumberFormatException e) {
                return new Entity[0];
            }
            int xOff = 0, yOff = 0, zOff = 0;
            // Get offset
            if (data.length > 2) {
                switch (data[2].toUpperCase()) {
                    case "NORTH":
                        xOff = -radius;
                        break;
                    case "SOUTH":
                        xOff = radius;
                        break;
                    case "EAST":
                        zOff = -radius;
                        break;
                    case "WEST":
                        zOff = radius;
                        break;
                    case "UP":
                        yOff = radius;
                        break;
                    case "DOWN":
                        yOff = -radius;
                        break;
                }
            }
            Vector signVector = sign.getBlock().getLocation().toVector();
            Vector min = new Vector(signVector.getX() + (xOff - radius), signVector.getY() + (yOff - radius), signVector.getZ() + (zOff - radius));
            Vector max = new Vector(signVector.getX() + (xOff + radius), signVector.getY() + (yOff + radius), signVector.getZ() + (zOff + radius));
            List<LivingEntity> worldEntities = sign.getBlock().getWorld().getLivingEntities();
            List<LivingEntity> entitiesInRange = new ArrayList<LivingEntity>(worldEntities.size());

            for (LivingEntity entity : worldEntities) {
                boolean tryToAddEntity = false;

                if (type == RedstoneTeleportType.ALL) {
                    tryToAddEntity = true;
                } else if (type == RedstoneTeleportType.ANIMALS && (entity instanceof Animals || entity instanceof Squid || entity instanceof Villager)) {
                    tryToAddEntity = true;
                } else if (type == RedstoneTeleportType.MONSTERS && (entity instanceof Monster)) {
                    tryToAddEntity = true;
                } else if (type == RedstoneTeleportType.PLAYERS && (entity instanceof HumanEntity)) {
                    tryToAddEntity = true;
                }

                if (tryToAddEntity && entity.getLocation().toVector().isInAABB(min, max)) {
                    if (entity.getLocation().toVector().isInAABB(min, max)) {
                        Logging.finest("Found " + entity + " within range!");
                        entitiesInRange.add(entity);
                    }
                }
            }
            return entitiesInRange.toArray(new Entity[entitiesInRange.size()]);
        }
        return new Entity[0];
    }

    /**
     * Check to see if the obsidian blocks are in place for a notch portal, since MV-Portals can make portals out of
     * anything.
     *
     * @param top    The top left of the portal
     * @param bottom The bottom right of the portal
     * @param a The axis to check along.
     *
     * @return A list of the sings around the portal.
     */
    private List<Sign> checkBlocksOutside(Block top, Block bottom, Axis a) {
        int xM = (a == Axis.X) ? 1 : 0;
        int zM = (a == Axis.Z) ? 1 : 0;
        // Check the top 2
        if (top.getRelative(0, 1, 0).getType() != Material.OBSIDIAN) {
            return null;
        }
        if (top.getRelative(xM, 1, zM).getType() != Material.OBSIDIAN) {
            return null;
        }
        Logging.finer("Found top 2");
        // Check the bottom 2
        if (bottom.getRelative(0, -1, 0).getType() != Material.OBSIDIAN) {
            return null;
        }
        if (bottom.getRelative(-1 * xM, -1, -1 * zM).getType() != Material.OBSIDIAN) {
            return null;
        }
        Logging.finer("Found bottom 2");
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
        Logging.finer("Found left 3");
        // Check the Right 3
        if (bottom.getRelative(xM, 0, zM).getType() != Material.OBSIDIAN) {
            return null;
        }
        if (bottom.getRelative(xM, 1, zM).getType() != Material.OBSIDIAN) {
            return null;
        }
        if (bottom.getRelative(xM, 2, zM).getType() != Material.OBSIDIAN) {
            return null;
        }
        Logging.finer("Found right 3");
        Block topper = top.getRelative(-1 - xM, 1, -1 - zM);
        Block bottomer = bottom.getRelative(1 + xM, -1, 1 + zM);
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
                    Logging.finest("Looking for sign at " +
                            this.plugin.getCore().getLocationManipulation().strCoordsRaw(looking));
                    BlockState signBlock = topper.getWorld().getBlockAt(looking).getState();
                    if (signBlock instanceof Sign) {
                        Logging.finer("WOO Found one! " +
                                this.plugin.getCore().getLocationManipulation().strCoordsRaw(looking));
                        signs.add((Sign) signBlock);
                    }
                }
            }
        }
        return signs;
    }

    public boolean playerCanGoToDestination(Player player, MVDestination d) {
            if (d instanceof InvalidDestination || !d.isValid()) {
                this.plugin.getCore().getMessaging().sendMessage(player, "The Destination on this sign is Invalid!", false);
                return false;
            }
            return this.plugin.getCore().getMVPerms().hasPermission(player, d.getRequiredPermission(), true);
    }

    enum RedstoneTeleportType {
        ALL,
        PLAYERS,
        MONSTERS,
        ANIMALS
    }

}
