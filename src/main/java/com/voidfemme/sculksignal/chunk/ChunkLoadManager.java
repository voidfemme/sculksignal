// === src/main/java/com/sculksignal/chunk/ChunkLoadManager.java ===

package com.sculksignal.chunk;

import com.sculksignal.SculkSignalPlugin;
import com.sculksignal.util.ChunkCoordinate;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages chunk loading for signal propagation with automatic cleanup
 */
public class ChunkLoadManager {

    private final SculkSignalPlugin plugin;
    private final Map<ChunkCoordinate, LoadedChunkInfo> loadedChunks;

    public ChunkLoadManager(SculkSignalPlugin plugin) {
        this.plugin = plugin;
        this.loadedChunks = new ConcurrentHashMap<>();
    }

    /**
     * Load a chunk by world name and coordinates
     */
    public void loadChunk(String worldName, int chunkX, int chunkZ) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().warning("World not found: " + worldName);
            return;
        }

        loadChunk(world.getChunkAt(chunkX, chunkZ));
    }

    /**
     * Load a chunk with automatic unload scheduling
     */
    public void loadChunk(Chunk chunk) {
        ChunkCoordinate coord = new ChunkCoordinate(chunk);

        if (loadedChunks.size() >= plugin.getPluginConfig().getMaxLoadedChunks()) {
            plugin.getLogger().warning("Maximum loaded chunks reached (" +
                    plugin.getPluginConfig().getMaxLoadedChunks() + "), cannot load more");
            return;
        }

        LoadedChunkInfo info = loadedChunks.get(coord);
        if (info != null) {
            // Chunk already loaded, extend its lifetime
            info.extendLifetime();
            return;
        }

        // Load the chunk
        chunk.setForceLoaded(true);
        info = new LoadedChunkInfo(chunk, System.currentTimeMillis());
        loadedChunks.put(coord, info);

        if (plugin.getPluginConfig().isDebugEnabled()) {
            plugin.getLogger().info("Force loaded chunk: " + coord);
        }

        // Schedule unload
        scheduleUnload(coord, plugin.getPluginConfig().getMaxLoadTime());
    }

    /**
     * Unload a specific chunk
     */
    public void unloadChunk(ChunkCoordinate coord) {
        LoadedChunkInfo info = loadedChunks.remove(coord);
        if (info != null) {
            info.chunk.setForceLoaded(false);

            if (plugin.getPluginConfig().isDebugEnabled()) {
                plugin.getLogger().info("Unloaded chunk: " + coord);
            }
        }
    }

    /**
     * Schedule a chunk to be unloaded after the specified delay
     */
    private void scheduleUnload(ChunkCoordinate coord, long delayTicks) {
        new BukkitRunnable() {
            @Override
            public void run() {
                LoadedChunkInfo info = loadedChunks.get(coord);
                if (info != null && info.shouldUnload()) {
                    unloadChunk(coord);
                }
            }
        }.runTaskLater(plugin, delayTicks);
    }

    /**
     * Get the number of currently loaded chunks
     */
    public int getLoadedChunkCount() {
        return loadedChunks.size();
    }

    /**
     * Check if a chunk is currently force-loaded by this manager
     */
    public boolean isChunkLoaded(ChunkCoordinate coord) {
        return loadedChunks.containsKey(coord);
    }

    /**
     * Clean up all loaded chunks (called on plugin disable)
     */
    public void cleanup() {
        for (LoadedChunkInfo info : loadedChunks.values()) {
            info.chunk.setForceLoaded(false);
        }
        loadedChunks.clear();

        plugin.getLogger().info("Cleaned up all force-loaded chunks");
    }

    /**
     * Run periodic cleanup to remove chunks that have exceeded their lifetime
     */
    public void runPeriodicCleanup() {
        new BukkitRunnable() {
            @Override
            public void run() {
                loadedChunks.entrySet().removeIf(entry -> {
                    LoadedChunkInfo info = entry.getValue();
                    if (info.shouldUnload()) {
                        info.chunk.setForceLoaded(false);

                        if (plugin.getPluginConfig().isDebugEnabled()) {
                            plugin.getLogger().info("Periodic cleanup unloaded chunk: " + entry.getKey());
                        }

                        return true;
                    }
                    return false;
                });
            }
        }.runTaskTimerAsynchronously(plugin, 20 * 60, 20 * 60); // Run every minute
    }

    /**
     * Information about a loaded chunk
     */
    private static class LoadedChunkInfo {
        final Chunk chunk;
        long loadTime;
        long lastExtended;

        LoadedChunkInfo(Chunk chunk, long loadTime) {
            this.chunk = chunk;
            this.loadTime = loadTime;
            this.lastExtended = loadTime;
        }

        void extendLifetime() {
            this.lastExtended = System.currentTimeMillis();
        }

        boolean shouldUnload() {
            long currentTime = System.currentTimeMillis();
            long maxLifetime = 30000; // 30 seconds default max lifetime
            return (currentTime - lastExtended) > maxLifetime;
        }
    }
}
