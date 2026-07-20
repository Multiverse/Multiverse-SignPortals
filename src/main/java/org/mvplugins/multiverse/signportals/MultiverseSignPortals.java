/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project.
 */

package org.mvplugins.multiverse.signportals;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.core.config.CoreConfig;
import org.mvplugins.multiverse.core.module.MultiverseModule;
import org.mvplugins.multiverse.core.utils.ReflectHelper;
import org.mvplugins.multiverse.core.utils.StringFormatter;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jakarta.inject.Provider;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.netherportals.MultiverseNetherPortalsApi;
import org.mvplugins.multiverse.signportals.listeners.SignPortalsListener;
import org.mvplugins.multiverse.signportals.utils.PortalDetector;

import java.util.logging.Logger;

@Service
public class MultiverseSignPortals extends MultiverseModule {

    private static final double TARGET_CORE_API_VERSION = 5.0;

    @Inject
    private Provider<PortalDetector> portalDetectorProvider;

    /** This fires before I get Enabled. */
    @Override
    public void onLoad() {
        Logging.init(this);
        this.getDataFolder().mkdirs();
    }

    @Override
    public void onEnable() {
        initializeDependencyInjection(new MultiverseSignPortalsPluginBinder(this));
        Logging.setDebugLevel(serviceLocator.getActiveService(CoreConfig.class).getGlobalDebug());
        registerDynamicListeners(SignPortalsListener.class);
        registerNetherPortalsHandleCheck();

        Logging.config("Version %s (API v%s) Enabled - By %s",
                this.getDescription().getVersion(), getVersionAsNumber(), StringFormatter.joinAnd(this.getDescription().getAuthors()));
    }

    private void registerNetherPortalsHandleCheck() {
        if (!ReflectHelper.hasClass("org.mvplugins.multiverse.netherportals.MultiverseNetherPortalsApi")) {
            return;
        }
        MultiverseNetherPortalsApi.whenLoaded(api ->
                api.getCustomPortalsHandler().registerHandleCheck(((entity, portalLocation) -> {
                    if (!(entity instanceof Player player)) {
                        return false;
                    }
                    Logging.finer("Checking if player %s can use NetherPortals at %s", player.getName(), portalLocation);
                    return Try.of(() -> portalDetectorProvider.get().getNotchPortalDestination(player, portalLocation) != null)
                            .getOrElse(false);
                })));
    }

    @Override
    public void onDisable() {
        shutdownDependencyInjection();
        Logging.info("- Disabled");
        Logging.shutdown();
    }

    public String getVersionInfo() {
        return "[Multiverse-SignPortals] Multiverse-SignPortals Version: " + this.getDescription().getVersion() + '\n';
    }

    @Override
    public double getTargetCoreVersion() {
        return TARGET_CORE_API_VERSION;
    }

    @Override
    public @NotNull Logger getLogger() {
        return Logging.getLogger();
    }
}
