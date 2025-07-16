package com.sculksignal.util;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;

/**
 * Represents a chunk coordinate for efficient spatial indexing
 */
public class ChunkCoordinate {

    private final String worldName;
    private final int x;
    private final int z;

    public ChunkCoordinate(String worldName, int x, int z) {
        this.worldName = worldName;
        this.x = x;
        this.z = z;
    }

    public ChunkCoordinate(Chunk chunk) {
        this(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public ChunkCoordinate(Location location) {
        this(location.getWorld().getName(),
                location.getBlockX() >> 4,
                location.getBlockZ() >> 4);
    }

    public String getWorldName() {
        return worldName;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    /**
     * Get a chunk coordinate offset by the given amounts
     */
    public ChunkCoordinate offset(int dx, int dz) {
        return new ChunkCoordinate(worldName, x + dx, z + dz);
    }

    /**
     * Get all chunk coordinates in a 3x3 area around this chunk
     */
    public ChunkCoordinate[] getNeighbors() {
        ChunkCoordinate[] neighbors = new ChunkCoordinate[9];
        int index = 0;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                neighbors[index++] = offset(dx, dz);
            }
        }

        return neighbors;
    }

    /**
     * Calculate distance between two chunk coordinates
     */
    public double distance(ChunkCoordinate other) {
        if (!worldName.equals(other.worldName)) {
            return Double.MAX_VALUE;
        }

        int dx = x - other.x;
        int dz = z - other.z;
        return Math.sqrt(dx * dx + dz * dz);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        ChunkCoordinate that = (ChunkCoordinate) obj;
        return x == that.x && z == that.z && Objects.equals(worldName, that.worldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldName, x, z);
    }

    @Override
    public String toString() {
        return String.format("ChunkCoordinate{world=%s, x=%d, z=%d}", worldName, x, z);
    }
}
