/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project.
 */

package org.mvplugins.multiverse.signportals;

import com.dumptruckman.minecraft.util.Logging;
import org.mvplugins.multiverse.core.MultiverseCore;
import org.mvplugins.multiverse.core.api.MVConfig;
import org.mvplugins.multiverse.core.api.MVCore;
import org.mvplugins.multiverse.core.api.MVPlugin;
import org.mvplugins.multiverse.core.inject.PluginServiceLocator;
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.vavr.control.Option;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.bukkit.plugin.java.JavaPlugin;
import org.mvplugins.multiverse.signportals.listeners.SignPortalsListener;

import java.util.logging.Level;

@Service
public class MultiverseSignPortals extends JavaPlugin implements MVPlugin {

    private MultiverseCore core;
    private PluginServiceLocator serviceLocator;

    private final static int requiresProtocol = 50;

    /** This fires before I get Enabled. */
    public void onLoad() {
        Logging.init(this);
        this.getDataFolder().mkdirs();
    }

    public void onEnable() {
        Logging.init(this);

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

        initializeDependencyInjection();
        registerEvents();
        Logging.setDebugLevel(serviceLocator.getActiveService(MVConfig.class).getGlobalDebug());

        this.core.incrementPluginCount();
        Logging.log(true, Level.INFO, " Enabled - By %s", getAuthors());
    }

    public void onDisable() {
        // The Usual
        shutdownDependencyInjection();
        Logging.info("- Disabled");
    }

    private void initializeDependencyInjection() {
        serviceLocator = core.getServiceLocatorFactory()
                .registerPlugin(new MultiverseSignPortalsPluginBinder(this), core.getServiceLocator())
                .flatMap(PluginServiceLocator::enable)
                .getOrElseThrow(exception -> {
                    Logging.severe("Failed to initialize dependency injection!");
                    getServer().getPluginManager().disablePlugin(this);
                    return new RuntimeException(exception);
                });
    }

    private void shutdownDependencyInjection() {
        Option.of(serviceLocator)
                .peek(PluginServiceLocator::disable)
                .peek(ignore -> serviceLocator = null);
    }

    /**
     * Function to Register all the Events needed.
     */
    private void registerEvents() {
        var pluginManager = getServer().getPluginManager();

        Try.run(() -> serviceLocator.getAllServices(SignPortalsListener.class).forEach(
                        listener -> {
                            Logging.info(listener.toString());
                            pluginManager.registerEvents(listener, this);
                        }))
                .onFailure(e -> {
                    throw new RuntimeException("Failed to register listeners. Terminating...", e);
                });
    }

    /**
     * Parse the Authors Array into a readable String with ',' and 'and'.
     *
     * @return An comma separated string of authors
     */
    @Override
    public String getAuthors() {
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
    public PluginServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    public String getVersionInfo() {
        return "[Multiverse-SignPortals] Multiverse-SignPortals Version: " + this.getDescription().getVersion() + '\n';
    }

    @Override
    public MVCore getCore() {
        return this.core;
    }

    @Override
    public int getProtocolVersion() {
        return 1;
    }
}
