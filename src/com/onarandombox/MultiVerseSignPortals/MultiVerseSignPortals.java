package com.onarandombox.MultiVerseSignPortals;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;

import com.onarandombox.MultiVerseCore.MultiVerseCore;

public class MultiVerseSignPortals extends JavaPlugin {

	public static final Logger log = Logger.getLogger("Minecraft");
	
	protected MultiVerseCore core;
	protected MVSPPlayerListener listener; 
	
	public void onEnable() {
		core = (MultiVerseCore) getServer().getPluginManager().getPlugin("MultiVerse-Core"); 
		if (core == null) {
			log.log(Level.SEVERE, "MultiVerse-SignPortals will not run; Core not found.");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		listener = new MVSPPlayerListener(this);
		getServer().getPluginManager().registerEvent(Type.PLAYER_MOVE, listener, Priority.Normal, this);
		log.info("MultiVerse-SignPortals is running.");
	}
	
	public void onDisable() {
		log.info("MultiVerse-SignPortals was disabled.");
	}
	
}
