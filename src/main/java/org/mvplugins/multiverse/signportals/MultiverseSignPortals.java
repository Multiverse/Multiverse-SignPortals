/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project.
 */

package org.mvplugins.multiverse.signportals;

import com.dumptruckman.minecraft.util.Logging;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.mvplugins.multiverse.core.MultiversePlugin;
import org.mvplugins.multiverse.core.config.MVCoreConfig;
import org.mvplugins.multiverse.core.inject.PluginServiceLocator;
import org.mvplugins.multiverse.core.inject.PluginServiceLocatorFactory;
import org.mvplugins.multiverse.core.utils.StringFormatter;
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.vavr.control.Option;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.signportals.listeners.SignPortalsListener;

import java.util.logging.Level;

@Service
public class MultiverseSignPortals extends MultiversePlugin {

    private MultiverseCoreApi core;
    private PluginServiceLocator serviceLocator;

    private final static int requiresProtocol = 50;

    /** This fires before I get Enabled. */
    public void onLoad() {
        Logging.init(this);
        this.getDataFolder().mkdirs();
    }

    public void onEnable() {
        Logging.init(this);

        this.core = MultiverseCoreApi.get();

        initializeDependencyInjection();
        registerEvents();
        Logging.setDebugLevel(serviceLocator.getActiveService(MVCoreConfig.class).getGlobalDebug());

        Logging.log(true, Level.INFO, " Enabled - By %s", StringFormatter.joinAnd(getDescription().getAuthors()));
    }

    public void onDisable() {
        // The Usual
        shutdownDependencyInjection();
        Logging.info("- Disabled");
    }

    private void initializeDependencyInjection() {
        serviceLocator = PluginServiceLocatorFactory.get()
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

    @Override
    public PluginServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    public String getVersionInfo() {
        return "[Multiverse-SignPortals] Multiverse-SignPortals Version: " + this.getDescription().getVersion() + '\n';
    }

    @Override
    public int getTargetCoreProtocolVersion() {
        return requiresProtocol;
    }
}
