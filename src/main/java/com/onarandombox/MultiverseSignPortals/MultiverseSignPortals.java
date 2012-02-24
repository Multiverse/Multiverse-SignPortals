/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project.
 */

package com.onarandombox.MultiverseSignPortals;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVPlugin;
import com.onarandombox.MultiverseCore.utils.DebugLog;
import com.onarandombox.MultiverseSignPortals.listeners.MVSPBlockListener;
import com.onarandombox.MultiverseSignPortals.listeners.MVSPPlayerListener;
import com.onarandombox.MultiverseSignPortals.listeners.MVSPPluginListener;
import com.onarandombox.MultiverseSignPortals.listeners.MVSPVersionListener;
import com.onarandombox.MultiverseSignPortals.utils.PortalDetector;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MultiverseSignPortals extends JavaPlugin implements MVPlugin {

    public static final Logger log = Logger.getLogger("Minecraft");
    public static final String logPrefix = "[Multiverse-SignPortals] ";
    protected static DebugLog debugLog;
    protected MultiverseCore core;
    protected MVSPPlayerListener playerListener;
    protected MVSPPluginListener pluginListener;
    protected MVSPBlockListener blockListener;
    private final static int requiresProtocol = 7;

    private PortalDetector portalDetector;

    public void onEnable() {
        this.core = (MultiverseCore) getServer().getPluginManager().getPlugin("Multiverse-Core");
        // Test if the Core was found, if not we'll disable this plugin.
        if (this.core == null) {
            log.info(logPrefix + "Multiverse-Core not found, will keep looking.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (this.core.getProtocolVersion() < requiresProtocol) {
            log.severe(logPrefix + "Your Multiverse-Core is OUT OF DATE");
            log.severe(logPrefix + "This version of SignPortals requires Protocol Level: " + requiresProtocol);
            log.severe(logPrefix + "Your of Core Protocol Level is: " + this.core.getProtocolVersion());
            log.severe(logPrefix + "Grab an updated copy at: ");
            log.severe(logPrefix + "http://bukkit.onarandombox.com/?dir=multiverse-core");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        debugLog = new DebugLog("Multiverse-SignPortals", getDataFolder() + File.separator + "debug.log");

        this.core.incrementPluginCount();

        // Init our listeners
        this.pluginListener = new MVSPPluginListener(this);
        this.playerListener = new MVSPPlayerListener(this);
        this.blockListener = new MVSPBlockListener(this);

        // Init our events
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(this.pluginListener, this);
        pm.registerEvents(this.playerListener, this);
        pm.registerEvents(this.blockListener, this);
        pm.registerEvents(new MVSPVersionListener(this), this);

        this.portalDetector = new PortalDetector(this);

        log.info(logPrefix + "- Version " + this.getDescription().getVersion() + " Enabled - By " + getAuthors());
    }

    public void onDisable() {
        // The Usual
        log.info(logPrefix + "- Disabled");
    }

    /** This fires before I get Enabled. */
    public void onLoad() {
        this.getDataFolder().mkdirs();
        debugLog = new DebugLog("Multiverse-SignPortals", getDataFolder() + File.separator + "debug.log");
    }

    /**
     * Parse the Authors Array into a readable String with ',' and 'and'.
     *
     * @return An comma separated string of authors
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
        if (level == Level.FINE && MultiverseCore.getStaticConfig().getGlobalDebug() >= 1) {
            staticDebugLog(Level.INFO, msg);
        } else if (level == Level.FINER && MultiverseCore.getStaticConfig().getGlobalDebug() >= 2) {
            staticDebugLog(Level.INFO, msg);
        } else if (level == Level.FINEST && MultiverseCore.getStaticConfig().getGlobalDebug() >= 3) {
            staticDebugLog(Level.INFO, msg);
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

    // No longer using, use getVersionInfo instead.
    @Override
    @Deprecated
    public String dumpVersionInfo(String buffer) {
        buffer += logAndAddToPasteBinBuffer(this.getVersionInfo());
        return buffer;
    }

    public String getVersionInfo() {
        return new StringBuffer("[Multiverse-SignPortals] Multiverse-SignPortals Version: ").append(this.getDescription().getVersion()).append('\n').toString();
    }

    // No longer using, use getVersionInfo instead.
    @Deprecated
    private String logAndAddToPasteBinBuffer(String string) {
        this.log(Level.INFO, string);
        return logPrefix + string + "\n";
    }

    @Override
    public MultiverseCore getCore() {
        return this.core;
    }

    @Override
    public void setCore(MultiverseCore core) {
        this.core = core;
    }

    @Override
    public int getProtocolVersion() {
        return 1;
    }

    public PortalDetector getPortalDetector() {
        return this.portalDetector;
    }


}
