package com.sculksignal;

import com.sculksignal.commands.SculkSignalCommands;
import com.sculksignal.config.SculkSignalConfig;
import com.sculksignal.network.NetworkRegistry;
import com.sculksignal.chunk.ChunkLoadManager;
import com.sculksignal.listeners.SculkSensorListener;
import com.sculksignal.storage.NetworkDataStorage;

import org.bukkit.plugin.java.JavaPlugin;

public class SculkSignalPlugin extends JavaPlugin {
    
    private SculkSignalConfig config;
    private NetworkRegistry networkRegistry;
    private ChunkLoadManager chunkLoadManager;
    private NetworkDataStorage dataStorage;
    
    @Override
    public void onEnable() {
        // Initialize configuration
        saveDefaultConfig();
        this.config = new SculkSignalConfig(this);
        
        // Initialize core components
        this.dataStorage = new NetworkDataStorage(this);
        this.networkRegistry = new NetworkRegistry(this);
        this.chunkLoadManager = new ChunkLoadManager(this);
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new SculkSensorListener(this), this);
        
        // Register commands
        getCommand("sculksignal").setExecutor(new SculkSignalCommands(this));
        
        // Load saved network data
        dataStorage.loadNetworkData();
        
        getLogger().info("SculkSignal plugin enabled!");
    }
    
    @Override
    public void onDisable() {
        // Save network data
        if (dataStorage != null) {
            dataStorage.saveNetworkData();
        }
        
        // Clean up chunk loading
        if (chunkLoadManager != null) {
            chunkLoadManager.cleanup();
        }
        
        getLogger().info("SculkSignal plugin disabled!");
    }
    
    public SculkSignalConfig getPluginConfig() {
        return config;
    }
    
    public NetworkRegistry getNetworkRegistry() {
        return networkRegistry;
    }
    
    public ChunkLoadManager getChunkLoadManager() {
        return chunkLoadManager;
    }
    
    public NetworkDataStorage getDataStorage() {
        return dataStorage;
    }
}
