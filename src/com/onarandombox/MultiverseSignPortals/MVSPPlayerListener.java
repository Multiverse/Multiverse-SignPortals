package com.onarandombox.MultiverseSignPortals;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import com.onarandombox.MultiverseCore.MVPlayerSession;
import com.onarandombox.MultiverseCore.MVTeleport;

public class MVSPPlayerListener extends PlayerListener {
	
	private final MultiverseSignPortals plugin;
	
	protected MVSPPlayerListener(MultiverseSignPortals plugin) {
		this.plugin = plugin;
	}
	
	public void onPlayerMove(PlayerMoveEvent event) {
		if (event.isCancelled())
			return;
		
		final Player p = event.getPlayer();
		Location loc = p.getLocation();
		World w = p.getWorld();
		
		/**
		 * Check the Player has actually moved a block to prevent unneeded calculations... This is to prevent huge performance drops on high player count servers.
		 */
		MVPlayerSession ps = this.plugin.core.getPlayerSession(p);
		if (ps.loc.getBlockX() == loc.getBlockX() && ps.loc.getBlockY() == loc.getBlockY() && ps.loc.getBlockZ() == loc.getBlockZ()) {
			return;
		}
		
		int locX = loc.getBlockX();
		int locY = loc.getBlockY();
		int locZ = loc.getBlockZ();
		
		Block b = p.getWorld().getBlockAt(locX, locY, locZ);
		if (!b.getType().equals(Material.PORTAL))
			return;
		
		// Possibly a tidier way of setting these base values up, but hey...
		int lowLocX = loc.getBlockX();
		int highLocX = loc.getBlockX();
		int lowLocY = loc.getBlockY();
		int highLocY = loc.getBlockY();
		int lowLocZ = loc.getBlockZ();
		int highLocZ = loc.getBlockZ();
		
		while (w.getBlockAt(lowLocX, lowLocY - 1, lowLocZ).getType().equals(Material.PORTAL))
			--lowLocY;
		while (w.getBlockAt(lowLocX - 1, lowLocY, lowLocZ).getType().equals(Material.PORTAL))
			--lowLocX;
		while (w.getBlockAt(lowLocX, lowLocY, lowLocZ - 1).getType().equals(Material.PORTAL))
			--lowLocZ;
		while (w.getBlockAt(highLocX, highLocY + 1, highLocZ).getType().equals(Material.PORTAL))
			++highLocY;
		while (w.getBlockAt(highLocX + 1, highLocY, highLocZ).getType().equals(Material.PORTAL))
			++highLocX;
		while (w.getBlockAt(highLocX, highLocY, highLocZ + 1).getType().equals(Material.PORTAL))
			++highLocZ;
		
		// Grab whether the Portal is along the X Axis.
		boolean orientX = w.getBlockAt(lowLocX + 1, lowLocY, lowLocZ).getType().equals(Material.PORTAL);
		
		// Set Vectors for the Highest and Lowest Possible points of the portal.
		Vector lowPoint, highPoint;
		if (orientX) {
			lowPoint = new Vector(lowLocX - 1, lowLocY - 1, lowLocZ);
			highPoint = new Vector(highLocX + 1, highLocY + 1, highLocZ);
		} else {
			lowPoint = new Vector(lowLocX, lowLocY - 1, lowLocZ - 1);
			highPoint = new Vector(highLocX, highLocY + 1, highLocZ + 1);
		}
		
		List<Block> portalBlocks = new ArrayList<Block>();
		List<Block> obsidianBlocks = new ArrayList<Block>();
		List<Sign> s = new ArrayList<Sign>();
		
		for (int x = lowPoint.getBlockX(); x <= highPoint.getBlockX(); x++) {
			for (int y = lowPoint.getBlockY(); y <= highPoint.getBlockY(); y++) {
				for (int z = lowPoint.getBlockZ(); z <= highPoint.getBlockZ(); z++) {
					Block block = w.getBlockAt(x, y, z);
					if (block.getType().equals(Material.PORTAL))
						portalBlocks.add(block);
					if (block.getType().equals(Material.OBSIDIAN)) {
						obsidianBlocks.add(block);
						
						// This next bit of mess grabs the BlockState for all 4 possible sides which a sign could be placed on.
						// Checks if a WALL_SIGN is present... if so it then checks if the Sign is attached to the Obsidian block or not.
						BlockState bs = null;
						bs = block.getRelative(BlockFace.NORTH).getState();
						if (bs.getType() == Material.WALL_SIGN) {
							if (((org.bukkit.material.Sign) bs.getData()).getAttachedFace() == BlockFace.SOUTH) {
								s.add((Sign) bs);
							}
						}
						bs = block.getRelative(BlockFace.EAST).getState();
						if (bs.getType() == Material.WALL_SIGN) {
							if (((org.bukkit.material.Sign) bs.getData()).getAttachedFace() == BlockFace.WEST) {
								s.add((Sign) bs);
							}
						}
						bs = block.getRelative(BlockFace.SOUTH).getState();
						if (bs.getType() == Material.WALL_SIGN) {
							if (((org.bukkit.material.Sign) bs.getData()).getAttachedFace() == BlockFace.NORTH) {
								s.add((Sign) bs);
							}
						}
						bs = block.getRelative(BlockFace.WEST).getState();
						if (bs.getType() == Material.WALL_SIGN) {
							if (((org.bukkit.material.Sign) bs.getData()).getAttachedFace() == BlockFace.EAST) {
								s.add((Sign) bs);
							}
						}
					}
					
				}
			}
		}
		
		// DEBUG
		// MultiVerseCore.debugMsg(portalBlocks.size() + " - Portal Blocks");
		// MultiVerseCore.debugMsg(obsidianBlocks.size() + " - Obsidian Blocks");
		// MultiVerseCore.debugMsg(s.size() + " - Signs");
		// MultiVerseCore.debugMsg(orientX + " - X Orientation");
		// DEBUG
		
		// TODO: Check if a sign exists full stop. -- Below is wrong... cycle all signs for a destination.
		// CYCLE THROUGH ALL SIGNS FOUND TO FIND A DESTINATION
		World world = null;
		for (int i = 0; i < s.size(); i++) {
			Sign sign = (Sign) s.get(i);
			if ((sign.getLine(1).equalsIgnoreCase("[multiverse]") || sign.getLine(1).equalsIgnoreCase("[mv]")) && (sign.getLine(2).length() > 0)) {
				world = this.plugin.getServer().getWorld(sign.getLine(2).toString());
				if (world != null) {
					break;
				} else {
					this.plugin.core.getPlayerSession(p).message(ChatColor.RED + "The World \"" + sign.getLine(2).toString() + "\" does not EXIST!");
					return;
				}
			}
		}
		
		MVTeleport mvtp = plugin.core.getTeleporter();
		
		Location dest;
		
		dest = mvtp.getCompressedLocation(p, world);
		MultiverseSignPortals.debugLog.log(Level.INFO, dest.toString());
		
		Location portal = mvtp.findPortal(dest);
		if (portal != null) {
			dest = portal;
		}
		
		MultiverseSignPortals.debugLog.log(Level.INFO, dest.toString());
		
		final Location d = mvtp.getSafeDestination(dest);
		
		if (d != null) {
			MultiverseSignPortals.debugLog.log(Level.INFO, dest.toString());
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				@Override
				public void run() {
					p.teleport(d);
				}
			});
		} else {
			MultiverseSignPortals.debugLog.log(Level.INFO, "Cannot find a safe location, try another portal/location.");
			ps.message(ChatColor.RED + "Cannot find a safe location, try another portal/location.");
			return;
		}
	}
}
