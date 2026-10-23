package net.bichal.bplb.client;

import net.bichal.bplb.client.render.GuiCompat;
import net.bichal.bplb.client.render.RenderAddons;
import net.bichal.bplb.client.render.RenderUtils;
import net.bichal.bplb.config.Config;
//? if !waypoints {
import net.bichal.bplb.network.PositionUpdatePayload;
//?}
import net.bichal.bplb.util.Constants;
import net.bichal.bplb.util.HeadGear;
import net.bichal.bplb.util.DistanceUtils;
import net.bichal.bplb.util.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.round;
import static net.bichal.bplb.util.ColorUtils.generateColorFromUUID;
import static net.bichal.bplb.util.Constants.CONFIG;

public class Hud {
    public record PlayerPosition(UUID uuid, String name, double x, double y, double z) {
    }
    private static final Map<UUID, PlayerPosition> playerPositions = new HashMap<>();
    private static final Map<Object, Float> currentIconPositions = new HashMap<>();
    public static final List<Vec3> deathMarkers = new ArrayList<>();
    private static final int MAX_DEATH_MARKERS = 4;
    private static List<PlayerPosition> positionsToRenderCache = new ArrayList<>();
    private static final Map<Object, Vec3> lastKnownPositions = new HashMap<>();
    private static final Map<Object, Long> lastPositionUpdateTime = new HashMap<>();
    private static boolean shouldApplyHudOffset = false;
    private static final float MIN_Z_DEPTH = 500f;
    /** -Dbplb.debug=true logs the tracked and drawn icon counts about every two seconds. */
    private static final boolean DEBUG = Boolean.getBoolean("bplb.debug");
    private static int debugTicks = 0;
    private static int lastDrawnIcons = 0;

    private static void updateRenderCache(Minecraft client) {
        if (client.player == null) {
            positionsToRenderCache.clear();
            return;
        }

        List<PlayerPosition> positions = getPositionsToRender(client);
        positions.removeIf(pos -> pos.uuid().equals(client.player.getUUID()));

        Vec3 playerPos = client.player.position();
        final int maxIcons = CONFIG.getMaxVisibleIcons();

        positions.sort(Comparator.comparingDouble(pos -> {
            double dx = playerPos.x - pos.x;
            double dz = playerPos.z - pos.z;
            return dx * dx + dz * dz;
        }));

        for (PlayerPosition pos : positions) {
            if (!currentIconPositions.containsKey(pos.uuid())) {
                float initialPos = calculateRelativePosition(client.player, pos);
                if (initialPos >= 0) currentIconPositions.put(pos.uuid(), initialPos * Constants.BAR_WIDTH);
            } else {
                float currentPos = calculateRelativePosition(client.player, pos);
                if (currentPos < 0) currentIconPositions.remove(pos.uuid());
            }
        }

        for (Vec3 marker : deathMarkers) {
            if (!currentIconPositions.containsKey(marker)) {
                PlayerPosition markerPos = new PlayerPosition(new UUID(marker.hashCode(), marker.hashCode()), "Death", marker.x, marker.y, marker.z);
                float initialPos = calculateRelativePosition(client.player, markerPos);
                if (initialPos >= 0) currentIconPositions.put(marker, initialPos * Constants.BAR_WIDTH);
            }
        }

        positionsToRenderCache = positions.size() > maxIcons ? new ArrayList<>(positions.subList(0, maxIcons)) : new ArrayList<>(positions);
    }

    public static void tick(Minecraft client) {
        if (client.level == null) return;
        updateRenderCache(client);
        if (DEBUG && ++debugTicks % 40 == 0) {
            Constants.LOGGER.info("[{}] debug: remote={} serverPlayers={} levelPlayers={} candidates={} drawn={}", Constants.MOD_NAME_SHORT, !Client.isLocalMode(), playerPositions.size(), client.level.players().size(), positionsToRenderCache.size(), lastDrawnIcons);
        }
        if (client.player != null) {
            deathMarkers.removeIf(marker -> {
                if (client.player.position().distanceTo(marker) < 10) {
                    currentIconPositions.remove(marker);
                    return true;
                }
                return false;
            });
        }
    }

    public static void addDeathLocation(Vec3 location) {
        if (deathMarkers.stream().anyMatch(marker -> marker.distanceTo(location) < 5)) {
            return;
        }
        deathMarkers.add(location);
        while (deathMarkers.size() > MAX_DEATH_MARKERS) {
            Vec3 oldest = deathMarkers.removeFirst();
            currentIconPositions.remove(oldest);
        }
    }

    public static void onJoin() {
        playerPositions.clear();
        currentIconPositions.clear();
        deathMarkers.clear();
        lastKnownPositions.clear();
        lastPositionUpdateTime.clear();
        Constants.LOGGER.info("[{}] Cleared all caches on join", Constants.MOD_NAME_SHORT);
    }

    //? if !waypoints {
    public static void applyPositionUpdate(PositionUpdatePayload payload) {
        final Minecraft client = Minecraft.getInstance();
        if (payload == null) return;

        List<UUID> disconnectedList = payload.disconnectedPlayers();
        if (disconnectedList != null) {
            for (UUID disconnectedId : disconnectedList) {
                if (disconnectedId == null) continue;
                playerPositions.remove(disconnectedId);
                currentIconPositions.remove(disconnectedId);
            }
        }

        List<PositionUpdatePayload.PlayerInfo> newPlayersList = payload.newPlayers();
        if (newPlayersList != null) {
            for (PositionUpdatePayload.PlayerInfo newPlayer : newPlayersList) {
                if (newPlayer == null || newPlayer.uuid() == null) continue;
                PlayerPosition existingData = playerPositions.get(newPlayer.uuid());
                if (existingData != null) {
                    playerPositions.put(newPlayer.uuid(), new PlayerPosition(newPlayer.uuid(), newPlayer.name(), existingData.x(), existingData.y(), existingData.z()));
                } else {
                    playerPositions.put(newPlayer.uuid(), new PlayerPosition(newPlayer.uuid(), newPlayer.name(), 0, 0, 0));
                }
            }
        }

        List<PositionUpdatePayload.PositionData> positionsList = payload.positions();
        if (positionsList != null) {
            for (PositionUpdatePayload.PositionData posUpdate : positionsList) {
                if (posUpdate == null || posUpdate.uuid() == null) continue;
                PlayerPosition existingPosData = playerPositions.get(posUpdate.uuid());
                String name;
                if (existingPosData != null) {
                    name = existingPosData.name();
                } else {
                    Player localPlayer = client.level != null ? client.level.getPlayerByUUID(posUpdate.uuid()) : null;
                    name = localPlayer != null ? localPlayer.getName().getString() : "Player";
                }
                playerPositions.put(posUpdate.uuid(), new PlayerPosition(posUpdate.uuid(), name, posUpdate.x(), posUpdate.y(), posUpdate.z()));
            }
        }
    }

    //?}

    public static void render(GuiGraphics context) {
        final Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null || !CONFIG.isModEnabled()) {
            shouldApplyHudOffset = false;
            return;
        }

        if (positionsToRenderCache.isEmpty() && deathMarkers.isEmpty()) {
            shouldApplyHudOffset = false;
            return;
        }
        final int barX = (context.guiWidth() - Constants.BAR_WIDTH) / 2;
        final int barY = context.guiHeight() + Constants.BAR_Y_OFFSET;
        final boolean showDetails = Keybinds.shouldShowPlayerNames() || CONFIG.isAlwaysShowPlayerNames();

        List<RenderEntry> allEntries = new ArrayList<>();
        for (PlayerPosition pos : positionsToRenderCache) {
            final Player targetPlayer = client.level.getPlayerByUUID(pos.uuid());
            //? if !waypoints {
            if (targetPlayer != null && shouldHideTarget(targetPlayer)) continue;
            //?}
            double distance = DistanceUtils.calculateDistance(client.player.getX(), client.player.getY(), client.player.getZ(), pos.x, pos.y, pos.z);
            float alpha = getDistanceAlpha(distance);
            allEntries.add(new RenderEntry(pos, pos.uuid(), distance, alpha, false));
        }

        for (Vec3 marker : deathMarkers) {
            double distance = DistanceUtils.calculateDistance(client.player.getX(), client.player.getY(), client.player.getZ(), marker.x, marker.y, marker.z);
            PlayerPosition markerPos = new PlayerPosition(new UUID(marker.hashCode(), marker.hashCode()), "Death", marker.x, marker.y, marker.z);
            allEntries.add(new RenderEntry(markerPos, marker, distance, 1.0f, true));
        }

        allEntries.sort(Comparator.comparingDouble(RenderEntry::distance).reversed());
        lastDrawnIcons = allEntries.size();

        int totalVisibleIcons = allEntries.size();
        for (int i = 0; i < allEntries.size(); i++) {
            RenderEntry entry = allEntries.get(i);
            float baseZ = calculateBaseZ(i, totalVisibleIcons);
            GuiCompat.resetShaderColor();
            GuiCompat.push(context);
            try {
                renderBarIcon(context, entry.pos, entry.key, barX, barY, baseZ, showDetails, entry.alpha);
            } finally {
                GuiCompat.pop(context);
            }
        }
        shouldApplyHudOffset = hasVisibleIconsInVisibleRange(client);
    }

    private record RenderEntry(PlayerPosition pos, Object key, double distance, float alpha, boolean isDeathMarker) {}

    public static boolean hasVisibleIconsInVisibleRange(Minecraft client) {
        if (client.player == null) return false;

        final int barLeft = (client.getWindow().getGuiScaledWidth() - Constants.BAR_WIDTH) / 2;
        final int barRight = barLeft + Constants.BAR_WIDTH;

        for (PlayerPosition pos : positionsToRenderCache) {
            float targetPos = calculateRelativePosition(client.player, pos);
            if (targetPos < 0) continue;

            Float currentPos = currentIconPositions.get(pos.uuid());
            if (currentIconPos(barLeft, barRight, currentPos)) return true;
        }

        for (Vec3 marker : deathMarkers) {
            PlayerPosition markerPos = new PlayerPosition(new UUID(marker.hashCode(), marker.hashCode()), "Death", marker.x, marker.y, marker.z);
            float targetPos = calculateRelativePosition(client.player, markerPos);
            if (targetPos < 0) continue;

            Float currentPos = currentIconPositions.get(marker);
            if (currentIconPos(barLeft, barRight, currentPos)) return true;
        }

        return false;
    }

    private static boolean currentIconPos(int barLeft, int barRight, Float currentPos) {
        if (currentPos != null) {
            int iconCenterX = barLeft + Math.round(currentPos);
            int iconLeft = iconCenterX - 5;
            int iconRight = iconCenterX + 5;
            return iconRight >= barLeft && iconLeft <= barRight;
        }
        return false;
    }

    private static void renderBarIcon(GuiGraphics context, PlayerPosition pos, Object key, int barX, int barY, float baseZ, boolean showDetails, float alpha) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        Float targetPos = calculateRelativePosition(client.player, pos);
        if (targetPos < 0) return;
        targetPos *= Constants.BAR_WIDTH;

        Float currentPos = currentIconPositions.getOrDefault(key, targetPos);
        float distance = Math.abs(currentPos - targetPos);
        if (distance > Constants.BAR_WIDTH * 0.75f) {
            currentPos = targetPos;
            currentIconPositions.put(key, currentPos);
        } else {
            float delta = targetPos - currentPos;
            float t = Math.min(CONFIG.getLerpSpeed(), 1.0f);
            currentPos = currentPos + delta * MathUtils.easeInOutQuad(t);
            currentIconPositions.put(key, currentPos);
        }

        int iconCenterX = barX + round(currentPos);
        float edgeAlpha = calculateEdgeAlpha(iconCenterX, barX);
        alpha *= edgeAlpha;

        if (alpha <= 0.01f && Math.abs(currentPos - targetPos) < 1.0f) return;

        float topLeftX = iconCenterX - Constants.ICON_BASE_SIZE / 2f;
        float topLeftY = barY - Constants.ICON_BASE_SIZE / 2f;

        float finalAlpha = alpha;
        RenderUtils.withMatrixPush(context, 0, 0, () -> {
            boolean isDeathMarker = key instanceof Vec3;
            boolean showHead = !isDeathMarker && (CONFIG.isAlwaysShowPlayerHeads() || Keybinds.shouldShowPlayerNames());
            Config.PlayerAppearance appearance = isDeathMarker ? null : CONFIG.getPlayerConfig(pos.name());
            String borderStyle;
            int color;

            if (isDeathMarker) {
                color = CONFIG.getDeathMarkerColor();
                borderStyle = CONFIG.getDeathMarkerBorderStyle();
            } else {
                color = appearance != null && appearance.color != null ? appearance.color : generateColorFromUUID(pos.uuid());
                borderStyle = appearance != null && appearance.iconBorderStyle != null ? appearance.iconBorderStyle : CONFIG.getNameBorderStyle();
            }
            float nameplateAlpha = showDetails ? finalAlpha : 0f;
            String text = isDeathMarker ? (int) pos.x + " " + (int) pos.y + " " + (int) pos.z : pos.name();

            GuiCompat.translate(context, 0, 0, baseZ);

            if (isDeathMarker) {
                RenderAddons.renderDeathMarker(context, topLeftX, topLeftY, Constants.ICON_BASE_SIZE, finalAlpha, CONFIG);
            } else {
                double iconDistance = DistanceUtils.calculateDistance(client.player.getX(), client.player.getY(), client.player.getZ(), pos.x, pos.y, pos.z);
                RenderAddons.renderPlayerIcon(context, pos.name(), pos.uuid(), iconDistance, topLeftX, topLeftY, Constants.ICON_BASE_SIZE, showHead, CONFIG, finalAlpha);
            }

            GuiCompat.resetShaderColor();
            renderHeightIndicator(context, pos, iconCenterX, (int) (topLeftY + Constants.ICON_BASE_SIZE / 2f), finalAlpha, appearance);

            GuiCompat.translate(context, 0, 0, 1);
            RenderAddons.renderNameplate(context, text, borderStyle, color, iconCenterX - (client.font.width(text) * CONFIG.getNameplateScale() + 4) / 2, topLeftY - (12 * CONFIG.getNameplateScale()) - 4, nameplateAlpha, CONFIG.getNameplateScale());
            GuiCompat.resetShaderColor();
        });
    }

    public static boolean shouldApplyHudOffset() {
        return shouldApplyHudOffset;
    }

    private static boolean showUp(Minecraft client, PlayerPosition pos) {
        return isArrowUp(client, pos);
    }

    private static boolean showDown(Minecraft client, PlayerPosition pos) {
        return isArrowDown(client, pos);
    }

    private static void renderHeightIndicator(GuiGraphics context, PlayerPosition pos, int centerX, int centerY, float alpha, Config.PlayerAppearance appearance) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        if (showUp(client, pos) || showDown(client, pos)) {
            String arrowId = (appearance != null && appearance.arrowType != null) ? appearance.arrowType : CONFIG.getArrowType();
            renderHeightArrow(context, centerX, centerY, alpha, showUp(client, pos), arrowId);
        }
    }

    private static void renderHeightArrow(GuiGraphics context, int centerX, int centerY, float alpha, boolean isUp, String arrowId) {
        int arrowX = centerX - 5;
        int arrowY = isUp ? (centerY - Constants.ICON_BASE_SIZE - 2 - CONFIG.getVerticalPadding()) : (centerY + 2 + CONFIG.getVerticalPadding());
        RenderAddons.renderArrow(context, arrowId, isUp, arrowX, arrowY, Constants.ICON_BASE_SIZE, alpha);
    }

    private static List<PlayerPosition> getPositionsToRender(Minecraft client) {
        if (client.level == null || client.player == null) return Collections.emptyList();
        //? if waypoints {
        /*if (true) return WaypointSource.collect(client);
        *///?}
        // Server has the mod: its list is the truth (it also hides sneaking or invisible players),
        // even when empty. Without the mod only what this client can see is known.
        if (!Client.isLocalMode()) {
            return new ArrayList<>(playerPositions.values());
        }
        return client.level.players().stream().filter(p -> !p.getUUID().equals(client.player.getUUID())).map(p -> new PlayerPosition(p.getUUID(), p.getName().getString(), p.getX(), p.getY(), p.getZ())).collect(Collectors.toList());
    }

    private static float getDistanceAlpha(double distance) {
        if (distance > CONFIG.getFadeEndDistance()) return CONFIG.getFadeAlphaMin();
        if (distance < CONFIG.getFadeStartDistance()) return CONFIG.getFadeAlphaMax();
        float progress = (float) ((distance - CONFIG.getFadeStartDistance()) / (CONFIG.getFadeEndDistance() - CONFIG.getFadeStartDistance()));
        return Mth.lerp(MathUtils.easeInOutQuad(progress), CONFIG.getFadeAlphaMax(), CONFIG.getFadeAlphaMin());
    }

    private static boolean isArrowUp(Minecraft client, PlayerPosition pos) {
        return shouldShowArrow(client, pos, true);
    }

    private static boolean isArrowDown(Minecraft client, PlayerPosition pos) {
        return shouldShowArrow(client, pos, false);
    }

    private static double getDynamicAngleThreshold(Minecraft client, PlayerPosition pos) {
        if (client.player == null) return 0.0;
        double dx = pos.x - client.player.getX();
        double dz = pos.z - client.player.getZ();
        double deltaH = Math.sqrt(dx * dx + dz * dz);
        double deltaY = Math.abs(pos.y - (client.player.getY() + 1.0));
        double minAngle = 8.0;
        double maxAngle = 45.0;
        double maxEffectiveDeltaY = 30.0;
        double maxEffectiveDeltaH = 64.0;
        double yFactor = 1.0 - Math.min(deltaY, maxEffectiveDeltaY) / maxEffectiveDeltaY;
        double hFactor = 1.0 - Math.min(deltaH, maxEffectiveDeltaH) / maxEffectiveDeltaH;
        return Mth.lerp(yFactor * hFactor, minAngle, maxAngle);
    }

    private static double getCameraAngleDifference(Camera camera, PlayerPosition pos) {
        //? if >=1.21.11 {
        /*Vec3 cameraPos = camera.position();
        *///?} else {
        Vec3 cameraPos = camera.getPosition();
        //?}
        double dx = pos.x - cameraPos.x;
        double dz = pos.z - cameraPos.z;
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        double targetY = pos.y + 1.62;
        return -(Math.toDegrees(Math.atan2(targetY - cameraPos.y, horizontalDist)) + cameraXRot(camera));
    }

    private static float cameraXRot(Camera camera) {
        //? if >=1.21.11 {
        /*return camera.xRot();
        *///?} else {
        return camera.getXRot();
        //?}
    }

    private static float calculateRelativePosition(Player viewer, PlayerPosition target) {
        if (viewer == null) return -1f;

        Vec3 currentPos = new Vec3(target.x, target.y, target.z);
        Vec3 smoothedPos = currentPos;

        Long currentTime = System.currentTimeMillis();
        Vec3 lastPos = lastKnownPositions.get(target.uuid());
        Long lastTime = lastPositionUpdateTime.get(target.uuid());

        if (lastPos != null && lastTime != null) {
            long timeDelta = currentTime - lastTime;
            if (timeDelta < 1000) {
                float alpha = Math.min(timeDelta / 100f * CONFIG.getLerpSpeed(), 1f);
                smoothedPos = lastPos.lerp(currentPos, MathUtils.easeInOutQuad(alpha));
            }
        }

        lastKnownPositions.put(target.uuid(), smoothedPos);
        lastPositionUpdateTime.put(target.uuid(), currentTime);

        double relativeAngle = getRelativeAngle(viewer, smoothedPos);
        if (Math.abs(relativeAngle) > 90) return -1f;

        return (float) (relativeAngle + 90) / 180.0f;
    }

    private static float calculateBaseZ(int index, int totalVisibleIcons) {
        float minZ = MIN_Z_DEPTH;
        if (totalVisibleIcons <= 1) return minZ;

        float maxAvailableZ = 1000f;
        float availableRange = maxAvailableZ - minZ;
        float spacingPerIcon = availableRange / (totalVisibleIcons - 1);
        return maxAvailableZ - (index * spacingPerIcon);
    }

    private static double getRelativeAngle(Player viewer, Vec3 smoothedPos) {
        double relativeAngle = Mth.wrapDegrees(Math.toDegrees(Math.atan2(smoothedPos.z - viewer.getZ(), smoothedPos.x - viewer.getX())) - 90 - Mth.wrapDegrees(viewer.getYRot()));
        if (CONFIG.isAdjustToFov()) {
            Minecraft client = RenderUtils.getClient();
            float fov = (float) client.options.fov().get();
            float fovFactor = (90.0f / fov) * CONFIG.getFovMultiplier();
            relativeAngle *= fovFactor;
        }
        return relativeAngle;
    }

    private static float calculateEdgeAlpha(int iconX, int barX) {
        int edgeDistance = Math.min(iconX - barX, Constants.BAR_WIDTH - (iconX - barX));
        if (edgeDistance >= Constants.EDGE_ALPHA_FADE_MARGIN) return 1.0f;
        return Mth.clamp(edgeDistance / (float) Constants.EDGE_ALPHA_FADE_MARGIN, 0.0f, 1.0f);
    }

    private static boolean shouldHideTarget(Player target) {
        return HeadGear.hidesPlayer(target);
    }

    private static boolean shouldShowArrow(Minecraft client, PlayerPosition pos, boolean up) {
        if (client.player == null) return false;

        if (CONFIG.getHeightDifferenceMode().equalsIgnoreCase("player")) {
            double diff = pos.y - (client.player.getY() + 1.0);
            return up ? diff > 5.5 : diff < -5.5;
        }

        double angleDiff = getCameraAngleDifference(client.gameRenderer.getMainCamera(), pos);
        double threshold = getDynamicAngleThreshold(client, pos);
        return up ? angleDiff < -threshold : angleDiff > threshold;
    }
}
