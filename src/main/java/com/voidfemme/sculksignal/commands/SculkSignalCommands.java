package com.sculksignal.commands;

import com.sculksignal.SculkSignalPlugin;
import com.sculksignal.network.SculkNode;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Location;

public class SculkSignalCommands implements CommandExecutor {
    
    private final SculkSignalPlugin plugin;
    
    public SculkSignalCommands(SculkSignalPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            showHelp(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "register":
                registerSensor(player);
                break;
            case "unregister":
                unregisterSensor(player);
                break;
            case "list":
                listSensors(player);
                break;
            case "info":
                showSensorInfo(player);
                break;
            case "reload":
                if (player.hasPermission("sculksignal.admin")) {
                    reloadConfig(player);
                } else {
                    player.sendMessage("§cYou don't have permission to reload the config.");
                }
                break;
            default:
                showHelp(player);
        }
        
        return true;
    }
    
    private void showHelp(Player player) {
        player.sendMessage("§6=== SculkSignal Commands ===");
        player.sendMessage("§e/sculksignal register §7- Register the calibrated sculk sensor you're looking at");
        player.sendMessage("§e/sculksignal unregister §7- Unregister the sensor you're looking at");
        player.sendMessage("§e/sculksignal list §7- List all registered sensors");
        player.sendMessage("§e/sculksignal info §7- Show info about the sensor you're looking at");
        if (player.hasPermission("sculksignal.admin")) {
            player.sendMessage("§e/sculksignal reload §7- Reload the plugin configuration");
        }
    }
    
    private void registerSensor(Player player) {
        Block targetBlock = player.getTargetBlockExact(10);
        
        if (targetBlock == null || targetBlock.getType() != Material.CALIBRATED_SCULK_SENSOR) {
            player.sendMessage("§cYou must be looking at a calibrated sculk sensor to register it.");
            return;
        }
        
        Location location = targetBlock.getLocation();
        
        if (plugin.getNetworkRegistry().isRegistered(location)) {
            player.sendMessage("§cThis sensor is already registered.");
            return;
        }
        
        SculkNode node = new SculkNode(location, player.getUniqueId());
        plugin.getNetworkRegistry().registerSensor(node);
        
        player.sendMessage("§aCalibrated sculk sensor registered successfully!");
        player.sendMessage("§7Location: " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
    }
    
    private void unregisterSensor(Player player) {
        Block targetBlock = player.getTargetBlockExact(10);
        
        if (targetBlock == null || targetBlock.getType() != Material.CALIBRATED_SCULK_SENSOR) {
            player.sendMessage("§cYou must be looking at a calibrated sculk sensor to unregister it.");
            return;
        }
        
        Location location = targetBlock.getLocation();
        
        if (!plugin.getNetworkRegistry().isRegistered(location)) {
            player.sendMessage("§cThis sensor is not registered.");
            return;
        }
        
        SculkNode node = plugin.getNetworkRegistry().getSensor(location);
        if (node != null && (!node.getOwner().equals(player.getUniqueId()) && !player.hasPermission("sculksignal.admin"))) {
            player.sendMessage("§cYou can only unregister sensors you own.");
            return;
        }
        
        plugin.getNetworkRegistry().unregisterSensor(location);
        player.sendMessage("§aCalibrated sculk sensor unregistered successfully!");
    }
    
    private void listSensors(Player player) {
        var sensors = plugin.getNetworkRegistry().getAllSensors();
        
        if (sensors.isEmpty()) {
            player.sendMessage("§7No sensors are currently registered.");
            return;
        }
        
        player.sendMessage("§6=== Registered Sculk Sensors ===");
        sensors.forEach((location, node) -> {
            String owner = node.getOwner().equals(player.getUniqueId()) ? "§a(You)" : "§7(Other)";
            player.sendMessage("§e" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + " " + owner);
        });
    }
    
    private void showSensorInfo(Player player) {
        Block targetBlock = player.getTargetBlockExact(10);
        
        if (targetBlock == null || targetBlock.getType() != Material.CALIBRATED_SCULK_SENSOR) {
            player.sendMessage("§cYou must be looking at a calibrated sculk sensor.");
            return;
        }
        
        Location location = targetBlock.getLocation();
        SculkNode node = plugin.getNetworkRegistry().getSensor(location);
        
        if (node == null) {
            player.sendMessage("§cThis sensor is not registered. Use §e/sculksignal register §cto register it.");
            return;
        }
        
        player.sendMessage("§6=== Sensor Information ===");
        player.sendMessage("§eLocation: §7" + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
        player.sendMessage("§eConnected Sensors: §7" + node.getConnectedSensors().size());
        player.sendMessage("§eOwner: §7" + (node.getOwner().equals(player.getUniqueId()) ? "You" : "Another player"));
    }
    
    private void reloadConfig(Player player) {
        plugin.reloadConfig();
        plugin.getPluginConfig().reload();
        player.sendMessage("§aConfiguration reloaded successfully!");
    }
}
