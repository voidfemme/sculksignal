package com.sculksignal.network;

import org.bukkit.Location;
import org.bukkit.Chunk;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SculkNode {
    
    private final Location location;
    private final UUID owner;
    private final Set<SculkNode> connectedSensors;
    private boolean isActive;
    
    public SculkNode(Location location, UUID owner) {
        this.location = location.clone();
        this.owner = owner;
        this.connectedSensors = new HashSet<>();
        this.isActive = false;
    }
    
    public Location getLocation() {
        return location.clone();
    }
    
    public UUID getOwner() {
        return owner;
    }
    
    public Set<SculkNode> getConnectedSensors() {
        return new HashSet<>(connectedSensors);
    }
    
    public void addConnection(SculkNode node) {
        connectedSensors.add(node);
    }
    
    public void removeConnection(SculkNode node) {
        connectedSensors.remove(node);
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        this.isActive = active;
    }
    
    public Chunk getChunk() {
        return location.getChunk();
    }
    
    public double distanceTo(SculkNode other) {
        return location.distance(other.location);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SculkNode sculkNode = (SculkNode) obj;
        return location.equals(sculkNode.location);
    }
    
    @Override
    public int hashCode() {
        return location.hashCode();
    }
}
