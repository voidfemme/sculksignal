// === src/main/java/com/sculksignal/network/NetworkRegistry.java ===

package com.sculksignal.network;

import com.sculksignal.SculkSignalPlugin;
import com.sculksignal.util.ChunkCoordinate;

import org.bukkit.Location;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for managing sculk sensor network with efficient spatial indexing
 */
public class NetworkRegistry {

    private final SculkSignalPlugin plugin;
    private final Map<Location, SculkNode> registeredSensors;
    private final Map<ChunkCoordinate, Set<SculkNode>> sensorsByChunk;

    public NetworkRegistry(SculkSignalPlugin plugin) {
        this.plugin = plugin;
        this.registeredSensors = new ConcurrentHashMap<>();
        this.sensorsByChunk = new ConcurrentHashMap<>();
    }

    /**
     * Register a new sculk sensor in the network
     */
    public void registerSensor(SculkNode node) {
        Location location = node.getLocation();

        // Add to main registry
        registeredSensors.put(location, node);

        // Add to spatial index
        ChunkCoordinate chunkCoord = new ChunkCoordinate(location);
        sensorsByChunk.computeIfAbsent(chunkCoord, k -> ConcurrentHashMap.newKeySet()).add(node);

        // Calculate connections to nearby sensors
        calculateConnections(node);

        if (plugin.getPluginConfig().isDebugEnabled()) {
            plugin.getLogger().info("Registered sensor at " + location +
                    " with " + node.getConnectedSensors().size() + " connections");
        }
    }

    /**
     * Unregister a sculk sensor from the network
     */
    public void unregisterSensor(Location location) {
        SculkNode node = registeredSensors.remove(location);
        if (node == null) {
            return;
        }

        // Remove from spatial index
        ChunkCoordinate chunkCoord = new ChunkCoordinate(location);
        Set<SculkNode> chunkSensors = sensorsByChunk.get(chunkCoord);
        if (chunkSensors != null) {
            chunkSensors.remove(node);
            if (chunkSensors.isEmpty()) {
                sensorsByChunk.remove(chunkCoord);
            }
        }

        // Remove all connections to this node
        registeredSensors.values().forEach(other -> other.removeConnection(node));

        if (plugin.getPluginConfig().isDebugEnabled()) {
            plugin.getLogger().info("Unregistered sensor at " + location);
        }
    }

    /**
     * Check if a sensor is registered at the given location
     */
    public boolean isRegistered(Location location) {
        return registeredSensors.containsKey(location);
    }

    /**
     * Get the sensor at the given location
     */
    public SculkNode getSensor(Location location) {
        return registeredSensors.get(location);
    }

    /**
     * Get all registered sensors
     */
    public Map<Location, SculkNode> getAllSensors() {
        return new HashMap<>(registeredSensors);
    }

    /**
     * Get all sensors in a specific chunk
     */
    public Set<SculkNode> getSensorsInChunk(ChunkCoordinate chunkCoord) {
        Set<SculkNode> sensors = sensorsByChunk.get(chunkCoord);
        return sensors != null ? new HashSet<>(sensors) : Collections.emptySet();
    }

    /**
     * Get all sensors within a certain range of a location
     */
    public Set<SculkNode> getSensorsInRange(Location center, double maxDistance) {
        Set<SculkNode> nearBySensors = new HashSet<>();
        ChunkCoordinate centerChunk = new ChunkCoordinate(center);

        // Calculate how many chunks to check based on the range
        int chunkRadius = (int) Math.ceil(maxDistance / 16.0) + 1;

        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                ChunkCoordinate checkChunk = centerChunk.offset(dx, dz);
                Set<SculkNode> chunkSensors = sensorsByChunk.get(checkChunk);

                if (chunkSensors != null) {
                    chunkSensors.stream()
                            .filter(sensor -> center.distance(sensor.getLocation()) <= maxDistance)
                            .forEach(nearBySensors::add);
                }
            }
        }

        return nearBySensors;
    }

    /**
     * Calculate connections for a specific sensor
     */
    private void calculateConnections(SculkNode newNode) {
        final double SENSOR_RANGE = 16.0; // Calibrated sculk sensor range

        // Clear existing connections for this node
        newNode.getConnectedSensors().clear();

        // Find all sensors within range using spatial indexing
        Set<SculkNode> nearBySensors = getSensorsInRange(newNode.getLocation(), SENSOR_RANGE);

        for (SculkNode other : nearBySensors) {
            if (!other.equals(newNode) &&
                    newNode.getLocation().getWorld().equals(other.getLocation().getWorld()) &&
                    newNode.distanceTo(other) <= SENSOR_RANGE) {

                // Create bidirectional connection
                newNode.addConnection(other);
                other.addConnection(newNode);
            }
        }
    }

    /**
     * Recalculate all connections in the network (expensive operation)
     */
    public void recalculateAllConnections() {
        plugin.getLogger().info("Recalculating all network connections...");

        // Clear all connections
        registeredSensors.values().forEach(node -> node.getConnectedSensors().clear());

        // Recalculate all connections
        registeredSensors.values().forEach(this::calculateConnections);

        int totalConnections = registeredSensors.values().stream()
                .mapToInt(node -> node.getConnectedSensors().size())
                .sum() / 2; // Divide by 2 because connections are bidirectional

        plugin.getLogger().info("Network recalculation complete. " +
                registeredSensors.size() + " sensors, " +
                totalConnections + " connections");
    }

    /**
     * Get network statistics
     */
    public NetworkStats getNetworkStats() {
        int totalSensors = registeredSensors.size();
        int totalConnections = registeredSensors.values().stream()
                .mapToInt(node -> node.getConnectedSensors().size())
                .sum() / 2;
        int totalChunks = sensorsByChunk.size();

        return new NetworkStats(totalSensors, totalConnections, totalChunks);
    }

    /**
     * Network statistics data class
     */
    public static class NetworkStats {
        public final int totalSensors;
        public final int totalConnections;
        public final int chunksWithSensors;

        public NetworkStats(int totalSensors, int totalConnections, int chunksWithSensors) {
            this.totalSensors = totalSensors;
            this.totalConnections = totalConnections;
            this.chunksWithSensors = chunksWithSensors;
        }

        @Override
        public String toString() {
            return String.format("NetworkStats{sensors=%d, connections=%d, chunks=%d}",
                    totalSensors, totalConnections, chunksWithSensors);
        }
    }
}
