package com.onarandombox.MultiVerseSignPortals;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;

import com.onarandombox.MultiVerseCore.MultiVerseCore;
import com.onarandombox.utils.UpdateChecker;

public class MultiVerseSignPortals extends JavaPlugin {

	public static final Logger log = Logger.getLogger("Minecraft");
	public static final String logPrefix = "[MultiVerse-SignPortals] ";
		
	protected MultiVerseCore core;
	protected MVSPPlayerListener playerListener;
	protected MVSPPluginListener pluginListener;
	
	public UpdateChecker updateCheck;
	
	public void onEnable() {
	    // Register the PLUGIN_ENABLE Event as we will need to keep an eye out for the Core Enabling if we don't find it initally.
	    getServer().getPluginManager().registerEvent(Type.PLUGIN_ENABLE, pluginListener, Priority.Normal, this);
	    
		// Try and grab the Core Plugin, if it doesn't exist it will return null.
	    core = (MultiVerseCore) getServer().getPluginManager().getPlugin("MultiVerse-Core");

	    // Test if the Core was found, if not we'll disable this plugin.
        if (core == null) {
            log.info(logPrefix + "MultiVerse-Core not found, will keep looking.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
	    
        // Setup the Player Listener, we only need to listen out for PLAYER_MOVE Events.
		playerListener = new MVSPPlayerListener(this);		
		getServer().getPluginManager().registerEvent(Type.PLAYER_MOVE, playerListener, Priority.Normal, this);
		
		// Simple Log output to state the plugin is ENABLED
		log.info(logPrefix + "- Version " + this.getDescription().getVersion() + " Enabled");
		
		// Setup the Update Checker, this will check every 30 minutes for an update to the plugin and output to the console.
		updateCheck = new UpdateChecker(this.getDescription().getName(),this.getDescription().getVersion());
	}
	
	public void onDisable() {
	    // The Usual
	    log.info(logPrefix + "- Disabled");
	}
	
    /**
     * This fires before plugins get Enabled... Not needed but saves Console Spam.
     */
    public void onLoad() {
    }
}
