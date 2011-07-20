package com.onarandombox.MultiverseSignPortals;

import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;

import com.onarandombox.MultiverseCore.MultiverseCore;

public class MVSPPluginListener extends ServerListener {

    MultiverseSignPortals plugin;

    public MVSPPluginListener(MultiverseSignPortals plugin) {
        this.plugin = plugin;
    }

    public void onPluginEnable(PluginEnableEvent event) {
        if (event.getPlugin().getDescription().getName().equals("Multiverse-Core")) {
            this.plugin.core = ((MultiverseCore) plugin.getServer().getPluginManager().getPlugin("Multiverse-Core"));
            this.plugin.getServer().getPluginManager().enablePlugin(this.plugin);
        }
    }
}
