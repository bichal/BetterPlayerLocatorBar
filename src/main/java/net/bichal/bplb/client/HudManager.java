package net.bichal.bplb.client;

import net.bichal.bplb.client.render.PositionTracker;
import net.bichal.bplb.config.Config;
import net.bichal.bplb.util.Constants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.bichal.bplb.network.PositionUpdatePayload;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central manager for all HUD-related operations.
 * Provides clean API for rendering, tick updates, and event handling.
 * Thread-safe implementation using concurrent collections.
 */
@Environment(EnvType.CLIENT)
public final class HudManager {
    private static volatile HudManager instance;
    
    private final PositionTracker positionTracker;
    private final DeathMarkerManager deathMarkers;
    private final Config config;
    
    private volatile boolean isEnabled = true;
    private volatile boolean shouldApplyOffset = false;
    
    // Caches for performance
    private final Map<Object, Float> iconPositions = new ConcurrentHashMap<>();
    private final Map<Object, Vec3d> lastKnownPositions = new ConcurrentHashMap<>();
    private final Map<Object, Long> lastUpdateTimes = new ConcurrentHashMap<>();
    
    private HudManager() {
        this.positionTracker = new PositionTracker();
        this.deathMarkers = new DeathMarkerManager();
        this.config = Config.getInstance();
    }
    
    /**
     * Thread-safe singleton access.
     */
    public static HudManager getInstance() {
        if (instance == null) {
            synchronized (HudManager.class) {
                if (instance == null) {
                    instance = new HudManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Initialize event handlers for networking and connection events.
     */
    public void registerEvents() {
        // Clear caches on server join
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            clearCaches();
            Constants.LOGGER.info("[{}] Cleared HUD caches on join", Constants.MOD_NAME_SHORT);
            
            // Register position update handler
            ClientPlayNetworking.registerReceiver(PositionUpdatePayload.ID, (payload, context) -> {
                positionTracker.updateLastServerUpdateTime();
                client.execute(() -> {
                    if (payload == null) return;
                    processPositionUpdate(payload, client);
                });
            });
        });
    }
    
    /**
     * Main tick update method called every game tick.
     */
    public void tick(MinecraftClient client) {
        if (!isEnabled || client.world == null) {
            shouldApplyOffset = false;
            return;
        }
        
        positionTracker.update(client);
        deathMarkers.update(client);
        updateIconPositions(client);
        
        shouldApplyOffset = checkVisibleIcons(client);
    }
    
    /**
     * Main render method called every frame.
     */
    public void render(DrawContext context) {
        if (!isEnabled) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;
        
        positionTracker.render(context);
        deathMarkers.render(context);
    }
    
    /**
     * Update icon positions with smooth interpolation.
     */
    private void updateIconPositions(MinecraftClient client) {
        if (client.player == null) return;
        
        Vec3d playerPos = client.player.getPos();
        final int maxIcons = config.getMaxVisibleIcons();
        
        positionTracker.getTrackedPlayers().stream()
            .sorted(Comparator.comparingDouble(p -> {
                double dx = playerPos.x - p.currentX();
                double dz = playerPos.z - p.currentZ();
                return dx * dx + dz * dz;
            }))
            .limit(maxIcons)
            .forEach(p -> updateIconPosition(p, client));
        
        deathMarkers.getMarkers().forEach(marker -> updateMarkerPosition(marker, client));
    }
    
    private void updateIconPosition(PositionTracker.TrackedPlayer player, MinecraftClient client) {
        float targetPos = calculateRelativePosition(client.player, player.smoothedX(), player.smoothedY(), player.smoothedZ());
        if (targetPos >= 0) {
            iconPositions.put(player.uuid(), targetPos * Constants.BAR_WIDTH);
        } else {
            iconPositions.remove(player.uuid());
        }
    }
    
    private void updateMarkerPosition(Vec3d marker, MinecraftClient client) {
        if (iconPositions.containsKey(marker)) return;
        
        float targetPos = calculateRelativePosition(client.player, marker.x, marker.y, marker.z);
        if (targetPos >= 0) {
            iconPositions.put(marker, targetPos * Constants.BAR_WIDTH);
        }
    }
    
    /**
     * Calculate relative position on HUD bar (-1 to 1).
     */
    private float calculateRelativePosition(net.minecraft.entity.player.PlayerEntity viewer, 
                                             double targetX, double targetY, double targetZ) {
        if (viewer == null) return -1f;
        
        double relativeAngle = getRelativeAngle(viewer, targetX, targetZ);
        if (Math.abs(relativeAngle) > 90) return -1f;
        
        return (float) (relativeAngle + 90) / 180.0f;
    }
    
    private double getRelativeAngle(net.minecraft.entity.player.PlayerEntity viewer, 
                                     double targetX, double targetZ) {
        double angle = Math.toDegrees(Math.atan2(targetZ - viewer.getZ(), targetX - viewer.getX())) - 90;
        angle -= viewer.getYaw();
        angle = ((angle + 180) % 360) - 180;
        
        if (config.isAdjustToFov()) {
            float fov = (float) MinecraftClient.getInstance().options.getFov().getValue();
            float fovFactor = (90.0f / fov) * config.getFovMultiplier();
            angle *= fovFactor;
        }
        
        return angle;
    }
    
    /**
     * Process position update from server.
     */
    private void processPositionUpdate(PositionUpdatePayload payload, MinecraftClient client) {
        // Process disconnected players
        List<UUID> disconnected = payload.disconnectedPlayers();
        if (disconnected != null) {
            disconnected.forEach(id -> {
                if (id != null) {
                    positionTracker.removePlayer(id);
                    iconPositions.remove(id);
                }
            });
        }
        
        // Process new players
        List<PositionUpdatePayload.PlayerInfo> newPlayers = payload.newPlayers();
        if (newPlayers != null) {
            newPlayers.forEach(info -> {
                if (info != null && info.uuid() != null) {
                    positionTracker.addOrUpdatePlayerInfo(info.uuid(), info.name());
                }
            });
        }
        
        // Process position updates
        List<PositionUpdatePayload.PositionData> positions = payload.positions();
        if (positions != null) {
            positions.forEach(pos -> {
                if (pos != null && pos.uuid() != null) {
                    positionTracker.updatePlayerPosition(
                        pos.uuid(), pos.x(), pos.y(), pos.z()
                    );
                }
            });
        }
    }
    
    /**
     * Check if any icons are visible in the HUD range.
     */
    private boolean checkVisibleIcons(MinecraftClient client) {
        if (client.player == null) return false;
        
        int barLeft = (client.getWindow().getScaledWidth() - Constants.BAR_WIDTH) / 2;
        int barRight = barLeft + Constants.BAR_WIDTH;
        
        return positionTracker.getTrackedPlayers().stream()
            .map(p -> iconPositions.get(p.uuid()))
            .anyMatch(pos -> pos != null && isIconInRange(pos, barLeft, barRight));
    }
    
    private boolean isIconInRange(float pos, int left, int right) {
        int center = left + Math.round(pos);
        int iconHalfWidth = 5;
        return (center + iconHalfWidth) >= left && (center - iconHalfWidth) <= right;
    }
    
    /**
     * Clear all caches.
     */
    private void clearCaches() {
        iconPositions.clear();
        lastKnownPositions.clear();
        lastUpdateTimes.clear();
        positionTracker.clear();
        deathMarkers.clear();
    }
    
    /**
     * Add a death marker location.
     */
    public void addDeathLocation(Vec3d location) {
        deathMarkers.add(location);
    }
    
    // Getters and setters
    public boolean isEnabled() { return isEnabled; }
    
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        if (!enabled) shouldApplyOffset = false;
    }
    
    public boolean shouldApplyOffset() {
        return isEnabled && shouldApplyOffset && config.isApplyHotbarOffset();
    }
    
    /**
     * Inner class for death marker management.
     */
    public static final class DeathMarkerManager {
        private static final int MAX_MARKERS = 4;
        private final List<Vec3d> markers = new ArrayList<>();
        private final Map<Object, Float> markerPositions = new ConcurrentHashMap<>();
        
        public void add(Vec3d location) {
            if (markers.stream().anyMatch(m -> m.distanceTo(location) < 5)) return;
            
            markers.add(location);
            while (markers.size() > MAX_MARKERS) {
                Vec3d oldest = markers.removeFirst();
                markerPositions.remove(oldest);
            }
        }
        
        public void update(MinecraftClient client) {
            if (client.player == null) return;
            
            markers.removeIf(marker -> {
                if (client.player.getPos().distanceTo(marker) < 10) {
                    markerPositions.remove(marker);
                    return true;
                }
                return false;
            });
        }
        
        public void render(DrawContext context) {
            // Rendering logic delegated to PositionTracker
        }
        
        public void clear() {
            markers.clear();
            markerPositions.clear();
        }
        
        public List<Vec3d> getMarkers() {
            return Collections.unmodifiableList(markers);
        }
    }
}
