package net.bichal.bplb.server;

import net.bichal.bplb.network.PositionUpdatePayload;
import net.bichal.bplb.util.DistanceUtils;
import net.bichal.bplb.util.Constants;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Dedicated network management for position updates.
 * Thread-safe implementation using concurrent collections.
 */
public final class NetworkManager {
    private final Set<UUID> newPlayers = ConcurrentHashMap.newKeySet(16);
    private final Set<UUID> disconnectedPlayers = ConcurrentHashMap.newKeySet(16);
    private final Map<UUID, Map<UUID, Double>> distanceCache = new ConcurrentHashMap<>(32);
    
    /**
     * Notify that a new player has joined.
     */
    public void notifyNewPlayer(UUID playerId) {
        newPlayers.add(playerId);
    }
    
    /**
     * Notify that a player has disconnected.
     */
    public void notifyDisconnected(UUID playerId) {
        disconnectedPlayers.add(playerId);
        newPlayers.remove(playerId);
    }
    
    /**
     * Create position update payload for all players.
     * Returns null if no updates are needed.
     */
    public @Nullable PositionUpdatePayload createUpdatePayload(
            MinecraftServer server,
            List<ServerPlayerEntity> players,
            PlayerTracker tracker,
            int maxRelevantDistance) {
        
        List<PositionUpdatePayload.PlayerInfo> newPlayerInfos = new CopyOnWriteArrayList<>();
        Map<UUID, PlayerTracker.PlayerPosition> movedPlayers = new ConcurrentHashMap<>(
            tracker.getAndClearMovedPlayers()
        );
        
        // Process new players
        Iterator<UUID> newPlayerIterator = newPlayers.iterator();
        while (newPlayerIterator.hasNext()) {
            UUID uuid = newPlayerIterator.next();
            PlayerTracker.PlayerInfo info = tracker.getPlayerInfo(uuid);
            
            if (info != null) {
                newPlayerInfos.add(new PositionUpdatePayload.PlayerInfo(uuid, info.name));
                
                ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
                if (player != null) {
                    movedPlayers.put(uuid, new PlayerTracker.PlayerPosition(
                        player.getX(), player.getY(), player.getZ()
                    ));
                }
                newPlayerIterator.remove();
            }
        }
        
        // Check if any updates are needed
        if (movedPlayers.isEmpty() && newPlayerInfos.isEmpty() && disconnectedPlayers.isEmpty()) {
            return null;
        }
        
        List<PositionUpdatePayload.PositionData> positions = new CopyOnWriteArrayList<>();
        
        // Build position data for each viewer
        for (ServerPlayerEntity viewer : players) {
            UUID viewerId = viewer.getUuid();
            Map<UUID, Double> viewerDistCache = distanceCache.computeIfAbsent(
                viewerId, k -> new ConcurrentHashMap<>()
            );
            
            double vx = viewer.getX();
            double vy = viewer.getY();
            double vz = viewer.getZ();
            
            for (Map.Entry<UUID, PlayerTracker.PlayerPosition> entry : movedPlayers.entrySet()) {
                UUID uuid = entry.getKey();
                if (uuid.equals(viewerId)) continue;
                
                PlayerTracker.PlayerPosition pos = entry.getValue();
                double distance = DistanceUtils.calculateDistance(
                    vx, vy, vz, pos.x, pos.y, pos.z
                );
                
                if (distance > maxRelevantDistance) {
                    viewerDistCache.remove(uuid);
                    continue;
                }
                
                viewerDistCache.put(uuid, distance);
                positions.add(new PositionUpdatePayload.PositionData(
                    uuid, pos.x, pos.y, pos.z, distance
                ));
            }
        }
        
        List<UUID> disconnected = new ArrayList<>(disconnectedPlayers);
        disconnectedPlayers.clear();
        
        return new PositionUpdatePayload(newPlayerInfos, positions, disconnected);
    }
    
    /**
     * Send position update to a specific player.
     */
    public void sendUpdate(ServerPlayerEntity player, PositionUpdatePayload payload) {
        if (ServerPlayNetworking.canSend(player, PositionUpdatePayload.ID)) {
            ServerPlayNetworking.send(player, payload);
        }
    }
    
    /**
     * Check if player can receive position updates.
     */
    public boolean canReceiveUpdates(ServerPlayerEntity player) {
        return ServerPlayNetworking.canSend(player, PositionUpdatePayload.ID);
    }
    
    /**
     * Clear distance cache for a player.
     */
    public void clearPlayerCache(UUID playerId) {
        distanceCache.remove(playerId);
    }
    
    /**
     * Clear all caches.
     */
    public void clearAllCaches() {
        newPlayers.clear();
        disconnectedPlayers.clear();
        distanceCache.clear();
    }
}
