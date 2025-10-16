package net.bichal.bplb.server;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerTracker {
    private final Map<UUID, PlayerInfo> playerInfoCache = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerPosition> currentPositions = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerPosition> movedPlayers = new ConcurrentHashMap<>();

    private final double positionThresholdSq;

    public PlayerTracker() {
        ServerConfig config = ServerConfig.getInstance();
        this.positionThresholdSq = Math.pow(config.positionChangeThreshold(), 2);
    }

    public void updatePlayer(ServerPlayerEntity player) {
        if (player == null) return;
        UUID uuid = player.getUuid();
        PlayerPosition newPos = new PlayerPosition(player.getX(), player.getY(), player.getZ());

        PlayerPosition oldPos = currentPositions.put(uuid, newPos);
        if (newPos.hasSignificantChange(oldPos, positionThresholdSq)) {
            movedPlayers.put(uuid, newPos);
        }

        playerInfoCache.computeIfAbsent(uuid, k -> new PlayerInfo(k, player.getName().getString()));
    }

    public void removePlayer(@Nullable UUID uuid) {
        if (uuid == null) return;
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

    @Nullable
    public PlayerInfo getPlayerInfo(@Nullable UUID uuid) {
        if (uuid == null) return null;
        return playerInfoCache.get(uuid);
    }

    @SuppressWarnings("unused") // Future implementation
    public void cleanup() {
        long cutoff = System.currentTimeMillis() - (ServerConfig.getInstance().cleanupIntervalTicks() * 50L);
        currentPositions.entrySet().removeIf(entry -> entry.getValue() == null || entry.getValue().timestamp < cutoff);
        playerInfoCache.entrySet().removeIf(entry -> entry.getValue() == null || entry.getValue().firstSeen < cutoff);
        movedPlayers.entrySet().removeIf(entry -> entry.getValue() == null || entry.getValue().timestamp < cutoff);
    }

    public static class PlayerInfo {
        @SuppressWarnings("unused")
        public final UUID uuid;
        public final String name;
        public final long firstSeen;

        public PlayerInfo(UUID uuid, String name) {
            this.uuid = uuid;
            this.name = name != null ? name : "Unknown";
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

        public boolean hasSignificantChange(@Nullable PlayerPosition other, double thresholdSq) {
            if (other == null) return true;
            double dx = x - other.x;
            double dy = y - other.y;
            double dz = z - other.z;
            return (dx * dx + dy * dy + dz * dz) > thresholdSq;
        }
    }
}
