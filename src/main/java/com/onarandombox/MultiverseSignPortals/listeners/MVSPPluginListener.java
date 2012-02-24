/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project.
 */

package com.onarandombox.MultiverseSignPortals.listeners;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseSignPortals.MultiverseSignPortals;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;

public class MVSPPluginListener implements Listener {

    private MultiverseSignPortals plugin;

    public MVSPPluginListener(MultiverseSignPortals plugin) {
        this.plugin = plugin;
    }

    /**
     * This method is fired when any plugin enables.
     * @param event The PluginEnable event.
     */
    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        if (event.getPlugin().getDescription().getName().equals("Multiverse-Core")) {
            this.plugin.setCore(((MultiverseCore) plugin.getServer().getPluginManager().getPlugin("Multiverse-Core")));
            this.plugin.getServer().getPluginManager().enablePlugin(this.plugin);
        }
    }
}
