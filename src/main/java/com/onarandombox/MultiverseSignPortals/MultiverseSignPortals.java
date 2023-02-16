/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project.
 */

package com.onarandombox.MultiverseSignPortals;

import java.util.List;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.api.MVCore;
import com.onarandombox.MultiverseCore.api.MVPlugin;
import com.onarandombox.MultiverseSignPortals.listeners.MVSPBlockListener;
import com.onarandombox.MultiverseSignPortals.listeners.MVSPPlayerListener;
import com.onarandombox.MultiverseSignPortals.listeners.MVSPVersionListener;
import com.onarandombox.MultiverseSignPortals.utils.PortalDetector;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MultiverseSignPortals extends JavaPlugin implements MVPlugin {
    private static final int PROTOCOL = 50;

    private MVCore core;
    private PortalDetector portalDetector;

    /**
     * This fires before I get Enabled.
     */
    public void onLoad() {
        Logging.init(this);
        this.getDataFolder().mkdirs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void onEnable() {
        this.core = (MVCore) this.getServer().getPluginManager().getPlugin("Multiverse-Core");
        if (this.core == null) {
            Logging.severe("Core not found! You must have Multiverse-Core installed to use this plugin!");
            Logging.severe("Grab a copy at: ");
            Logging.severe("https://dev.bukkit.org/projects/multiverse-core");
            Logging.severe("Disabling!");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (this.core.getProtocolVersion() < this.getProtocolVersion()) {
            Logging.severe("Your Multiverse-Core is OUT OF DATE");
            Logging.severe("This version of " + this.getDescription().getName() + " requires Protocol Level: " + this.getProtocolVersion());
            Logging.severe("Your of Core Protocol Level is: " + this.core.getProtocolVersion());
            Logging.severe("Grab an updated copy at: ");
            Logging.severe("https://dev.bukkit.org/projects/multiverse-core");
            Logging.severe("Disabling!");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
        Logging.setDebugLevel(core.getMVConfig().getGlobalDebug());
        this.core.incrementPluginCount();
        this.onMVPluginEnable();
        Logging.config("Version %s (API v%s) Enabled - By %s", this.getDescription().getVersion(), getProtocolVersion(), getAuthors());
    }

    private void onMVPluginEnable() {
        // Init our events
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(new MVSPPlayerListener(this), this);
        pm.registerEvents(new MVSPBlockListener(this), this);
        pm.registerEvents(new MVSPVersionListener(this), this);

        this.portalDetector = new PortalDetector(this);
    }

    public void onDisable() {
        this.core.decrementPluginCount();
        Logging.shutdown();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAuthors() {
        List<String> authorsList = this.getDescription().getAuthors();
        if (authorsList.size() == 0) {
            return "";
        }

        StringBuilder authors = new StringBuilder();
        authors.append(authorsList.get(0));

        for (int i = 1; i < authorsList.size(); i++) {
            if (i == authorsList.size() - 1) {
                authors.append(" and ").append(authorsList.get(i));
            } else {
                authors.append(", ").append(authorsList.get(i));
            }
        }

        return authors.toString();
    }

    @Override
    public MVCore getCore() {
        return this.core;
    }

    @Override
    public int getProtocolVersion() {
        return PROTOCOL;
    }

    public PortalDetector getPortalDetector() {
        return this.portalDetector;
    }
}
