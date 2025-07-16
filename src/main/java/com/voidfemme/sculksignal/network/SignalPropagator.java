package com.sculksignal.network;

import com.sculksignal.SculkSignalPlugin;
import com.sculksignal.util.ChunkCoordinate;

import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Set;

/**
 * Handles the propagation of signals through the sculk sensor network
 */
public class SignalPropagator {

    private final SculkSignalPlugin plugin;
    private final PathCalculator pathCalculator;

    public SignalPropagator(SculkSignalPlugin plugin) {
        this.plugin = plugin;
        this.pathCalculator = new PathCalculator(plugin.getNetworkRegistry());
    }

    /**
     * Propagate a signal from the source sensor to all reachable sensors
     */
    public void propagateSignal(SculkNode source, int signalStrength) {
        if (plugin.getPluginConfig().isDebugEnabled()) {
            plugin.getLogger().info("Propagating signal from " + source.getLocation() +
                    " with strength " + signalStrength);
        }

        // Find all sensors reachable from this source
        Set<SculkNode> reachableSensors = pathCalculator.findReachableSensors(source, 1000.0); // Max 1000 blocks

        for (SculkNode target : reachableSensors) {
            if (target.equals(source))
                continue;

            // Calculate path and propagate with delay
            List<SculkNode> path = pathCalculator.findPath(source, target);
            if (!path.isEmpty()) {
                propagateAlongPath(path, signalStrength);
            }
        }
    }

    /**
     * Propagate signal along a specific path with proper timing and chunk loading
     */
    private void propagateAlongPath(List<SculkNode> path, int signalStrength) {
        if (path.size() < 2)
            return;

        // Get all chunks that need to be loaded
        Set<ChunkCoordinate> requiredChunks = pathCalculator.getRequiredChunks(path);

        // Load all chunks in the path
        for (ChunkCoordinate chunk : requiredChunks) {
            plugin.getChunkLoadManager().loadChunk(chunk.getWorldName(), chunk.getX(), chunk.getZ());
        }

        // Schedule signal propagation with delays
        int propagationDelay = plugin.getPluginConfig().getPropagationDelay();

        for (int i = 1; i < path.size(); i++) {
            SculkNode targetNode = path.get(i);
            long delay = (long) i * propagationDelay;

            new BukkitRunnable() {
                @Override
                public void run() {
                    activateSensor(targetNode, signalStrength);
                }
            }.runTaskLater(plugin, delay);
        }
    }

    /**
     * Activate a specific sensor with the given signal strength
     */
    private void activateSensor(SculkNode sensor, int signalStrength) {
        // TODO: Implement actual sensor activation
        // This would involve creating a redstone signal at the sensor location

        if (plugin.getPluginConfig().isDebugEnabled()) {
            plugin.getLogger().info("Activating sensor at " + sensor.getLocation() +
                    " with strength " + signalStrength);
        }

        // Mark sensor as active temporarily
        sensor.setActive(true);

        // Schedule deactivation
        new BukkitRunnable() {
            @Override
            public void run() {
                sensor.setActive(false);
            }
        }.runTaskLater(plugin, 10); // Deactivate after 10 ticks
    }
}
