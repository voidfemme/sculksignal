package com.sculksignal.config;

import com.sculksignal.SculkSignalPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class SculkSignalConfig {
    
    private final SculkSignalPlugin plugin;
    private FileConfiguration config;
    
    public SculkSignalConfig(SculkSignalPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }
    
    public void reload() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
    }
    
    public int getMaxLoadTime() {
        return config.getInt("chunk-loading.max-load-time", 100);
    }
    
    public int getUnloadDelay() {
        return config.getInt("chunk-loading.unload-delay", 20);
    }
    
    public int getMaxLoadedChunks() {
        return config.getInt("chunk-loading.max-loaded-chunks", 50);
    }
    
    public int getDiscoveryRange() {
        return config.getInt("network.discovery-range", 32);
    }
    
    public boolean isDebugEnabled() {
        return config.getBoolean("network.debug", false);
    }
    
    public boolean isPersistNetworkEnabled() {
        return config.getBoolean("network.persist-network", true);
    }
    
    public List<Integer> getSupportedFrequencies() {
        return config.getIntegerList("transmission.supported-frequencies");
    }
    
    public int getPropagationDelay() {
        return config.getInt("transmission.propagation-delay", 5);
    }
}
