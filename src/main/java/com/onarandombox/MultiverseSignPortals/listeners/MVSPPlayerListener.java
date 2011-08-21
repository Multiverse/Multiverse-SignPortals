package com.onarandombox.MultiverseSignPortals.listeners;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerPortalEvent;

import com.onarandombox.MultiverseCore.MVTeleport;
import com.onarandombox.MultiverseSignPortals.MultiverseSignPortals;
import com.onarandombox.MultiverseSignPortals.exceptions.MoreThanOneSignFoundException;
import com.onarandombox.MultiverseSignPortals.utils.NoMultiverseSignFoundException;
import com.onarandombox.MultiverseSignPortals.utils.PortalDetector;
import com.onarandombox.utils.DestinationFactory;
import com.onarandombox.utils.MVDestination;

public class MVSPPlayerListener extends PlayerListener {

    private final MultiverseSignPortals plugin;

    public MVSPPlayerListener(MultiverseSignPortals plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event.isCancelled()) {
            return;
        }
        PortalDetector detector = new PortalDetector(this.plugin);
        try {
            detector.getNotchPortalDestination(event.getPlayer().getLocation());
            this.plugin.log(Level.FINER, "Found a Multiverse Sign");
        } catch (NoMultiverseSignFoundException e) {
            // This will simply act as a notch portal.
            this.plugin.log(Level.FINER, "Did NOT find a Multiverse Sign");
        } catch (MoreThanOneSignFoundException e) {
            event.getPlayer().sendMessage(ChatColor.RED + "Sorry " + ChatColor.WHITE + "but more than 1 sign was found where the first line was [mv] or [multiverse]. Please remove one of them.");
        }

        event.setCancelled(true);
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getClickedBlock().getState() instanceof Sign) {
                this.plugin.log(Level.FINER, "Found a Sign!");
                Sign s = (Sign) event.getClickedBlock().getState();
                PortalDetector pd = new PortalDetector(this.plugin);
                String destString = pd.processSign(s);
                if (destString != null) {
                    this.plugin.log(Level.FINER, "Found a SignPortal! (" + destString + ")");
                    MVTeleport teleporter = new MVTeleport(this.plugin.getCore());
                    DestinationFactory df = this.plugin.getCore().getDestinationFactory();
                    MVDestination d = df.getDestination(destString);
                    this.plugin.log(Level.FINER, "Found a Destination! (" + d + ")");
                    if (!teleporter.safelyTeleport(event.getPlayer(), d)) {
                        event.getPlayer().sendMessage("The Destination was not safe! (" + ChatColor.RED + d + ChatColor.WHITE + ")");
                    }
                    event.setCancelled(true);
                }
            }
        }
    }
}
