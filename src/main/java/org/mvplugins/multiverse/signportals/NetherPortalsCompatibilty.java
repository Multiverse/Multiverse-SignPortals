package org.mvplugins.multiverse.signportals;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.entity.Player;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.netherportals.MultiverseNetherPortalsApi;
import org.mvplugins.multiverse.signportals.utils.PortalDetector;

final class NetherPortalsCompatibilty {

    static void registerNetherPortalsHandleCheck(PortalDetector pd) {
        MultiverseNetherPortalsApi.whenLoaded(api -> {
            api.getCustomPortalsHandler().registerHandleCheck(((entity, portalLocation) -> {
                if (!(entity instanceof Player player)) {
                        return false;
                    }
                    Logging.finer("Checking if player %s can use NetherPortals at %s",
                            player.getName(), portalLocation);
                    return Try.of(() -> pd.getNotchPortalDestination(player, portalLocation) != null)
                            .getOrElse(false);
                }));
            Logging.finer("Registered NetherPortals handle check with SignPortals.");
            });
    }
}
