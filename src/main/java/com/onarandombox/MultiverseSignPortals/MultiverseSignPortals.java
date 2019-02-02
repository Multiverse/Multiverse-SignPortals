/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project.
 */

package com.onarandombox.MultiverseSignPortals;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVPlugin;
import com.onarandombox.MultiverseSignPortals.listeners.MVSPBlockListener;
import com.onarandombox.MultiverseSignPortals.listeners.MVSPPlayerListener;
import com.onarandombox.MultiverseSignPortals.listeners.MVSPPluginListener;
import com.onarandombox.MultiverseSignPortals.listeners.MVSPVersionListener;
import com.onarandombox.MultiverseSignPortals.utils.PortalDetector;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class MultiverseSignPortals extends JavaPlugin implements MVPlugin {

    protected MultiverseCore core;
    protected MVSPPlayerListener playerListener;
    protected MVSPPluginListener pluginListener;
    protected MVSPBlockListener blockListener;
    private final static int requiresProtocol = 22;

    private PortalDetector portalDetector;

    public void onEnable() {
        this.core = (MultiverseCore) getServer().getPluginManager().getPlugin("Multiverse-Core");
        // Test if the Core was found, if not we'll disable this plugin.
        if (this.core == null) {
            Logging.info("Multiverse-Core not found, will keep looking.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (this.core.getProtocolVersion() < requiresProtocol) {
            Logging.severe("Your Multiverse-Core is OUT OF DATE");
            Logging.severe("This version of SignPortals requires Protocol Level: " + requiresProtocol);
            Logging.severe("Your of Core Protocol Level is: " + this.core.getProtocolVersion());
            Logging.severe("Grab an updated copy at: ");
            Logging.severe("http://dev.bukkit.org/bukkit-plugins/multiverse-core/");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

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

        Logging.log(true, Level.INFO, " Enabled - By %s", getAuthors());
    }

    public void onDisable() {
        // The Usual
        Logging.info("- Disabled");
    }

    /** This fires before I get Enabled. */
    public void onLoad() {
        Logging.init(this);
        this.getDataFolder().mkdirs();
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
        Logging.log(level, msg);
    }

    // No longer using, use getVersionInfo instead.
    @Override
    @Deprecated
    public String dumpVersionInfo(String buffer) {
        buffer += logAndAddToPasteBinBuffer(this.getVersionInfo());
        return buffer;
    }

    public String getVersionInfo() {
        return new StringBuilder("[Multiverse-SignPortals] Multiverse-SignPortals Version: ").append(this.getDescription().getVersion()).append('\n').toString();
    }

    // No longer using, use getVersionInfo instead.
    @Deprecated
    private String logAndAddToPasteBinBuffer(String string) {
        this.log(Level.INFO, string);
        return Logging.getPrefixedMessage(string, false);
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
