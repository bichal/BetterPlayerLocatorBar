package net.bichal.bplb.server;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerTracker {
    private final Map<UUID, PlayerInfo> playerInfoCache = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerPosition> currentPositions = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerPosition> movedPlayers = new ConcurrentHashMap<>();
    private final double positionThreshold;

    public PlayerTracker() {
        this.positionThreshold = ServerConfig.getInstance().positionChangeThreshold;
    }

    public static class PlayerInfo {
        public final UUID uuid;
        public final String name;
        public final long firstSeen;

        public PlayerInfo(UUID uuid, String name) {
            this.uuid = uuid;
            this.name = name;
            this.firstSeen = System.currentTimeMillis();
        }
    }

    public static class PlayerPosition {
        public final double x, y, z;
        public final long timestamp;

        public PlayerPosition(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.timestamp = System.currentTimeMillis();
        }

        public boolean hasSignificantChange(PlayerPosition other, double threshold) {
            if (other == null) return true;
            return Math.abs(x - other.x) > threshold || Math.abs(y - other.y) > threshold || Math.abs(z - other.z) > threshold;
        }
    }

    public void updatePlayer(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        playerInfoCache.putIfAbsent(uuid, new PlayerInfo(uuid, player.getName().getString()));

        PlayerPosition newPos = new PlayerPosition(player.getX(), player.getY(), player.getZ());
        PlayerPosition oldPos = currentPositions.get(uuid);

        if (newPos.hasSignificantChange(oldPos, this.positionThreshold)) {
            currentPositions.put(uuid, newPos);
            movedPlayers.put(uuid, newPos);
        }
    }

    public void removePlayer(UUID uuid) {
        playerInfoCache.remove(uuid);
        currentPositions.remove(uuid);
        movedPlayers.remove(uuid);
    }

    public Map<UUID, PlayerPosition> getAndClearMovedPlayers() {
        if (movedPlayers.isEmpty()) {
            return Map.of();
        }
        Map<UUID, PlayerPosition> moved = new ConcurrentHashMap<>(movedPlayers);
        movedPlayers.clear();
        return moved;
    }

    public record PlayerData(UUID uuid, String name, double x, double y, double z, long timestamp) {
        public boolean hasSignificantChange(PlayerData other, double threshold) {
            if (other == null) return true;
            return Math.abs(x - other.x) > threshold ||
                    Math.abs(y - other.y) > threshold ||
                    Math.abs(z - other.z) > threshold;
        }
    }

    public PlayerPosition getPosition(UUID uuid) {
        return currentPositions.get(uuid);
    }

    public PlayerInfo getPlayerInfo(UUID uuid) {
        return playerInfoCache.get(uuid);
    }

    public void cleanup() {
        long cutoff = System.currentTimeMillis() - (ServerConfig.getInstance().cleanupIntervalTicks * 50L);
        currentPositions.entrySet().removeIf(entry -> entry.getValue().timestamp < cutoff);
        playerInfoCache.entrySet().removeIf(entry -> entry.getValue().firstSeen < cutoff);
        movedPlayers.entrySet().removeIf(entry -> entry.getValue().timestamp < cutoff);
    }
}
