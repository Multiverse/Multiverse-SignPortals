package com.onarandombox.MultiverseSignPortals;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.java.JavaPlugin;

import com.onarandombox.MultiverseCore.MVPlugin;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseSignPortals.listeners.MVSPBlockListener;
import com.onarandombox.MultiverseSignPortals.listeners.MVSPPlayerListener;
import com.onarandombox.MultiverseSignPortals.listeners.MVSPPluginListener;
import com.onarandombox.utils.DebugLog;
import com.onarandombox.utils.UpdateChecker;

public class MultiverseSignPortals extends JavaPlugin implements MVPlugin {

    public static final Logger log = Logger.getLogger("Minecraft");
    public static final String logPrefix = "[MultiVerse-SignPortals] ";
    protected static DebugLog debugLog;
    protected MultiverseCore core;
    protected MVSPPlayerListener playerListener;
    protected MVSPPluginListener pluginListener;
    protected MVSPBlockListener blockListener;

    public UpdateChecker updateCheck;

    public void onEnable() {
        this.core = (MultiverseCore) getServer().getPluginManager().getPlugin("Multiverse-Core");

        // Test if the Core was found, if not we'll disable this plugin.
        if (this.core == null) {
            log.info(logPrefix + "Multiverse-Core not found, will keep looking.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        debugLog = new DebugLog("Multiverse-SignPortals", getDataFolder() + File.separator + "debug.log");
        
        this.core.incrementPluginCount();
        
        // Init our listeners
        pluginListener = new MVSPPluginListener(this);
        playerListener = new MVSPPlayerListener(this);
        blockListener = new MVSPBlockListener(this);
        
        // Init our events
        getServer().getPluginManager().registerEvent(Type.PLUGIN_ENABLE, pluginListener, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Type.PLAYER_PORTAL, playerListener, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Type.SIGN_CHANGE, blockListener, Priority.Normal, this);


        // Setup the Player Listener, we only need to listen out for PLAYER_MOVE Events.
        
        

        // Simple Log output to state the plugin is ENABLED
        log.info(logPrefix + "- Version " + this.getDescription().getVersion() + " Enabled - By " + getAuthors());
    }

    public void onDisable() {
        // The Usual
        log.info(logPrefix + "- Disabled");
    }

    /**
     * This fires before I get Enabled.
     */
    public void onLoad() {
        getDataFolder().mkdirs();
        debugLog = new DebugLog("Multiverse-SignPortals", getDataFolder() + File.separator + "debug.log");
    }

    /**
     * Parse the Authors Array into a readable String with ',' and 'and'.
     *
     * @return
     */
    private String getAuthors() {
        String authors = "";
        for (int i = 0; i < this.getDescription().getAuthors().size(); i++) {
            if (i == this.getDescription().getAuthors().size() - 1) {
                authors += " and " + this.getDescription().getAuthors().get(i);
            } else {
                authors += ", " + this.getDescription().getAuthors().get(i);
            }
        }
        return authors.substring(2);
    }

    @Override
    public void log(Level level, String msg) {
        if (level == Level.FINE && MultiverseCore.GlobalDebug >= 1) {
            staticDebugLog(Level.INFO, msg);
            return;
        } else if (level == Level.FINER && MultiverseCore.GlobalDebug >= 2) {
            staticDebugLog(Level.INFO, msg);
            return;
        } else if (level == Level.FINEST && MultiverseCore.GlobalDebug >= 3) {
            staticDebugLog(Level.INFO, msg);
            return;
        } else if (level != Level.FINE && level != Level.FINER && level != Level.FINEST) {
            staticLog(level, msg);
        }
    }

    private void staticLog(Level level, String msg) {
        log.log(level, logPrefix + " " + msg);
        debugLog.log(level, logPrefix + " " + msg);
    }

    private void staticDebugLog(Level level, String msg) {
        log.log(level, "[MVSignPortals-Debug] " + msg);
        debugLog.log(level, "[MVSignPortals-Debug] " + msg);
    }

    @Override
    public String dumpVersionInfo(String buffer) {
        buffer += logAndAddToPasteBinBuffer("Multiverse-SignPortals Version: " + this.getDescription().getVersion());
        buffer += logAndAddToPasteBinBuffer("Bukkit Version: " + this.getServer().getVersion());
        buffer += logAndAddToPasteBinBuffer("Special Code: FRN001");
        return buffer;
    }
    
    private String logAndAddToPasteBinBuffer(String string) {
        this.log(Level.INFO, string);
        return "[Multiverse-SignPortals] " + string + "\n";
    }

    @Override
    public MultiverseCore getCore() {
        return this.core;
    }

    @Override
    public void setCore(MultiverseCore core) {
        this.core = core;
    }
}
