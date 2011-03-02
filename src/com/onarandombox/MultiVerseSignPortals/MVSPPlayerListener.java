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

public class MVSPPlayerListener extends PlayerListener {
	
	private final MultiVerseSignPortals plugin;
	
	protected MVSPPlayerListener(MultiVerseSignPortals plugin) {
		this.plugin = plugin;
	}
	
	public void onPlayerMove(PlayerMoveEvent event) {
		if (event.isCancelled()) return;
		
		Player p = event.getPlayer();
		Location l = p.getLocation();
		Block b = p.getWorld().getBlockAt(l.getBlockX(), l.getBlockY(), l.getBlockZ());
		
		if (!b.getType().equals(Material.PORTAL)) return;
		
		List<Sign> s = new ArrayList<Sign>();

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

		for (int i = 0; i < s.size(); i++) {
			Sign sign = s.get(i);
			if ((sign.getLine(1).equalsIgnoreCase("[multiverse]") || sign.getLine(1).equalsIgnoreCase("[mv]"))
					&& (sign.getLine(2).length() > 0)) {
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
