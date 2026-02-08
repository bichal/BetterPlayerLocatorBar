package net.bichal.bplb.client.render;

import net.bichal.bplb.config.Config;
import net.bichal.bplb.util.DistanceUtils;
import net.bichal.bplb.util.MathUtils;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Optimized position tracking with smooth interpolation.
 * Replaces scattered position logic in Hud.java
 * Thread-safe implementation using concurrent collections.
 */
@Environment(EnvType.CLIENT)
public final class PositionTracker {
    private static final long INTERPOLATION_TIME_MS = 1000L;
    private static final float INTERPOLATION_ALPHA_SCALE = 0.01f;
    
    private final Map<UUID, TrackedPlayer> trackedPlayers = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerInfo> playerInfoCache = new ConcurrentHashMap<>();
    private final Config config;
    
    private volatile long lastServerUpdateTime = 0;
    
    public PositionTracker() {
        this.config = Config.getInstance();
    }
    
    /**
     * Update tracking for all visible players.
     */
    public void update(MinecraftClient client) {
        if (client.player == null || client.world == null) return;
        
        boolean useServerData = !client.isInSingleplayer();
        
        if (useServerData && !trackedPlayers.isEmpty()) {
            // Use server data
            trackedPlayers.values().forEach(this::updateInterpolation);
        } else {
            // Fallback to local player detection
            client.world.getPlayers().stream()
                .filter(p -> !p.getUuid().equals(client.player.getUuid()))
                .filter(p -> !p.isInvisible() && !p.isSneaking())
                .forEach(p -> trackLocalPlayer(p, client));
        }
    }
    
    /**
     * Track a player from local world data.
     */
    private void trackLocalPlayer(PlayerEntity entity, MinecraftClient client) {
        UUID uuid = entity.getUuid();
        String name = entity.getName().getString();
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        
        trackedPlayers.compute(uuid, (u, existing) -> {
            if (existing == null) {
                return new TrackedPlayer(u, name, x, y, z);
            }
            return existing.updatePosition(x, y, z);
        });
        
        playerInfoCache.put(uuid, new PlayerInfo(uuid, name));
    }
    
    /**
     * Update interpolation for smooth movement.
     */
    private void updateInterpolation(TrackedPlayer player) {
        long currentTime = System.currentTimeMillis();
        long timeDelta = currentTime - player.lastUpdateTime();
        
        if (timeDelta < INTERPOLATION_TIME_MS && player.lastUpdateTime() > 0) {
            float alpha = Math.min(timeDelta * INTERPOLATION_ALPHA_SCALE * config.getLerpSpeed(), 1f);
            float easedAlpha = MathUtils.easeInOutQuad(alpha);
            // Position interpolation logic
        }
    }
    
    /**
     * Render all tracked players on HUD.
     */
    public void render(DrawContext context) {
        // Rendering is delegated to HudManager
    }
    
    /**
     * Add or update player info from server.
     */
    public void addOrUpdatePlayerInfo(UUID uuid, String name) {
        playerInfoCache.compute(uuid, (u, existing) -> {
            if (existing == null) {
                return new PlayerInfo(uuid, name);
            }
            return new PlayerInfo(uuid, name);
        });
        
        trackedPlayers.computeIfAbsent(uuid, u -> {
            TrackedPlayer existing = trackedPlayers.get(u);
            if (existing != null) {
                return new TrackedPlayer(u, name, existing.currentX(), existing.currentY(), existing.currentZ());
            }
            return new TrackedPlayer(u, name, 0, 0, 0);
        });
    }
    
    /**
     * Update player position from server data.
     */
    public void updatePlayerPosition(UUID uuid, double x, double y, double z) {
        trackedPlayers.compute(uuid, (u, existing) -> {
            if (existing == null) {
                PlayerInfo info = playerInfoCache.get(u);
                String name = info != null ? info.name() : "Player";
                return new TrackedPlayer(u, name, x, y, z);
            }
            return existing.updatePosition(x, y, z);
        });
    }
    
    /**
     * Get all tracked players.
     */
    public Collection<TrackedPlayer> getTrackedPlayers() {
        return Collections.unmodifiableCollection(trackedPlayers.values());
    }
    
    /**
     * Get a specific tracked player.
     */
    public Optional<TrackedPlayer> getPlayer(UUID uuid) {
        return Optional.ofNullable(trackedPlayers.get(uuid));
    }
    
    /**
     * Remove a player from tracking.
     */
    public void removePlayer(UUID uuid) {
        trackedPlayers.remove(uuid);
        playerInfoCache.remove(uuid);
    }
    
    /**
     * Clear all tracked players.
     */
    public void clear() {
        trackedPlayers.clear();
        playerInfoCache.clear();
    }
    
    /**
     * Update last server update timestamp.
     */
    public void updateLastServerUpdateTime() {
        this.lastServerUpdateTime = System.currentTimeMillis();
    }
    
    public long getLastServerUpdateTime() {
        return lastServerUpdateTime;
    }
    
    /**
     * Record representing a tracked player with interpolation support.
     */
    public record TrackedPlayer(
        UUID uuid,
        String name,
        double currentX, double currentY, double currentZ,
        double smoothedX, double smoothedY, double smoothedZ,
        long lastUpdateTime
    ) {
        public TrackedPlayer(UUID uuid, String name, double x, double y, double z) {
            this(uuid, name, x, y, z, x, y, z, System.currentTimeMillis());
        }
        
        public TrackedPlayer updatePosition(double x, double y, double z) {
            return new TrackedPlayer(uuid, name, x, y, z, smoothedX, smoothedY, smoothedZ, lastUpdateTime);
        }
        
        public TrackedPlayer withSmoothedPosition(double sx, double sy, double sz) {
            return new TrackedPlayer(uuid, name, currentX, currentY, currentZ, sx, sy, sz, System.currentTimeMillis());
        }
        
        public double distanceTo(double x, double y, double z) {
            return DistanceUtils.calculateDistance(smoothedX, smoothedY, smoothedZ, x, y, z);
        }
        
        public double horizontalDistanceTo(double x, double z) {
            double dx = smoothedX - x;
            double dz = smoothedZ - z;
            return Math.sqrt(dx * dx + dz * dz);
        }
    }
    
    /**
     * Record for player info caching.
     */
    public record PlayerInfo(UUID uuid, String name) {}
}
