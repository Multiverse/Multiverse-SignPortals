/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project.
 */

package org.mvplugins.multiverse.signportals.listeners;

import org.mvplugins.multiverse.core.dynamiclistener.annotations.EventMethod;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.signportals.MultiverseSignPortals;
import org.bukkit.event.server.PluginEnableEvent;

@Service
final class MVSPPluginListener implements SignPortalsListener {

    private final MultiverseSignPortals plugin;

    @Inject
    MVSPPluginListener(@NotNull MultiverseSignPortals plugin) {
        this.plugin = plugin;
    }

    /**
     * This method is fired when any plugin enables.
     * @param event The PluginEnable event.
     */
    @EventMethod
    public void onPluginEnable(PluginEnableEvent event) {
        if (event.getPlugin().getDescription().getName().equals("Multiverse-Core")) {
            // this.plugin.setCore(((MultiverseCore) plugin.getServer().getPluginManager().getPlugin("Multiverse-Core")));
            this.plugin.getServer().getPluginManager().enablePlugin(this.plugin);
        }
    }
}
