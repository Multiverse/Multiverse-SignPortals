package com.onarandombox.MultiVerseSignPortals;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import com.onarandombox.MultiVerseCore.MVPlayerSession;

public class MVSPPlayerListener extends PlayerListener {
	
	private final MultiVerseSignPortals plugin;
	
	protected MVSPPlayerListener(MultiVerseSignPortals plugin) {
		this.plugin = plugin;
	}
	
	public void onPlayerMove(PlayerMoveEvent event) {
		if (event.isCancelled()) return;
		
		Player p = event.getPlayer();
		Location loc = p.getLocation();
		World w = p.getWorld();
		
		/**
		 * Check the Player has actually moved a block to prevent unneeded calculations...
		 * This is to prevent huge performance drops on high player count servers.
		 */
		MVPlayerSession ps = this.plugin.core.getPlayerSession(p);
        if(ps.loc.getBlockX()==loc.getBlockX() && ps.loc.getBlockY()==loc.getBlockY() && ps.loc.getBlockZ()==loc.getBlockZ()) {
            return;
        } else {
            ps.loc = loc;
        }
		
		int locX = loc.getBlockX();
		int locY = loc.getBlockY();
	    int locZ = loc.getBlockZ();

	    Block b = p.getWorld().getBlockAt(locX, locY, locZ);
		if (!b.getType().equals(Material.PORTAL)) return;

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
		if(orientX){
		    lowPoint = new Vector(lowLocX-1,lowLocY-1,lowLocZ);
            highPoint = new Vector(highLocX+1,highLocY+1,highLocZ);
		} else {
		    lowPoint = new Vector(lowLocX,lowLocY-1,lowLocZ-1);
            highPoint = new Vector(highLocX,highLocY+1,highLocZ+1); 
		}
        
        List<Block> portalBlocks = new ArrayList<Block>();
        List<Block> obsidianBlocks = new ArrayList<Block>();
        
        for (int x = lowPoint.getBlockX(); x <= highPoint.getBlockX(); x++) {
            for (int y = lowPoint.getBlockY(); y <= highPoint.getBlockY(); y++) {
                for (int z = lowPoint.getBlockZ(); z <= highPoint.getBlockZ(); z++) {
                    Block block = w.getBlockAt(x,y,z);
                    if(block.getType().equals(Material.PORTAL))
                        portalBlocks.add(block);
                    if(block.getType().equals(Material.OBSIDIAN))
                        obsidianBlocks.add(block);
                }
            }
        }
	
        // DEBUG
        System.out.print("Portal Blocks -" + portalBlocks.size()); // A legitimate portal will contain 6 Portal blocks.
        System.out.print("Obsidian Blocks -" + obsidianBlocks.size()); // A legitimate portal will contain between 10 and 14 Obsidian Blocks.
        System.out.print("X Orientation - " + orientX);
        // DEBUG
        
        
        // WE CAN ALSO PERFORM A CHECK TO SEE IF WE WANT LEGITIMATE PORTALS ONLY... SO 6 PORTAL BLOCKS.

        
		List<Sign> s = new ArrayList<Sign>();

		// SCRAP THE FOLLOWING LOOP FOR A NEW LOOP USING THE FOLLOWING.
        // PERFORM A LOOP THROUGH ALL OBSIDIAN BLOCKS TO FIND A SIGN :)...
		for (int x = -2; x <= 2; x++) {
			for (int y = -1; y <= 3; y++) {
				for (int z = -2; z <= 2; z++) {
					BlockState block = b.getRelative(x, y, z).getState();
					if (block.getType() == Material.WALL_SIGN) {
						s.add((Sign) block);
					}
				}
			}
		}
		
		// THEN CYCLE THROUGH THE SIGNS FOUND.
		for (int i = 0; i < s.size(); i++) {
			Sign sign = s.get(i);
			if ((sign.getLine(1).equalsIgnoreCase("[multiverse]") || sign.getLine(1).equalsIgnoreCase("[mv]")) && (sign.getLine(2).length() > 0)) {

				World world = this.plugin.getServer().getWorld(sign.getLine(2).toString());
				if (world != null) {
					if (!plugin.core.getTeleporter().teleport(world, p))
					    plugin.core.getPlayerSession(p).message("You can't teleport to this destination."); // Send a message to the player which won't spam them.
					break;
				}
			}
		}
		
	}

}
