// === src/main/java/com/sculksignal/listeners/SculkSensorListener.java ===

package com.sculksignal.listeners;

import com.sculksignal.SculkSignalPlugin;
import com.sculksignal.network.SculkNode;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

public class SculkSensorListener implements Listener {

    private final SculkSignalPlugin plugin;

    public SculkSensorListener(SculkSignalPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSculkSensorActivate(BlockRedstoneEvent event) {
        if (event.getBlock().getType() != Material.CALIBRATED_SCULK_SENSOR) {
            return;
        }

        // Only handle activation (new power > old power)
        if (event.getNewCurrent() <= event.getOldCurrent()) {
            return;
        }

        SculkNode node = plugin.getNetworkRegistry().getSensor(event.getBlock().getLocation());
        if (node == null) {
            return; // Not a registered sensor
        }

        if (plugin.getPluginConfig().isDebugEnabled()) {
            plugin.getLogger().info("Sculk sensor activated at " + node.getLocation() +
                    " with signal strength " + event.getNewCurrent());
        }

        // Use the SignalPropagator to handle the signal transmission
        plugin.getSignalPropagator().propagateSignal(node, event.getNewCurrent());
    }
}
