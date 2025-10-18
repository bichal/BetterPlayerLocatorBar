package net.bichal.bplb.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.bichal.bplb.client.render.RenderAddons;
import net.bichal.bplb.client.render.RenderUtils;
import net.bichal.bplb.config.Config;
import net.bichal.bplb.network.PositionUpdatePayload;
import net.bichal.bplb.util.Constants;
import net.bichal.bplb.util.DistanceUtils;
import net.bichal.bplb.util.MathUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.round;
import static net.bichal.bplb.util.ColorUtils.generateColorFromUUID;
import static net.bichal.bplb.util.Constants.CONFIG;

@Environment(EnvType.CLIENT)
public class Hud {
    public record PlayerPosition(UUID uuid, String name, double x, double y, double z) {
    }
    private static final Map<UUID, PlayerPosition> playerPositions = new HashMap<>();
    private static final Map<Object, Float> currentIconPositions = new HashMap<>();
    public static final List<Vec3d> deathMarkers = new ArrayList<>();
    private static final int MAX_DEATH_MARKERS = 4;
    private static List<PlayerPosition> positionsToRenderCache = new ArrayList<>();
    private static final Map<Object, Vec3d> lastKnownPositions = new HashMap<>();
    private static final Map<Object, Long> lastPositionUpdateTime = new HashMap<>();
    private static boolean shouldApplyHudOffset = false;
    private static final float MIN_Z_DEPTH = 100f;

    private static void updateRenderCache(MinecraftClient client) {
        if (client.player == null) {
            positionsToRenderCache.clear();
            return;
        }

        List<PlayerPosition> positions = getPositionsToRender(client);
        positions.removeIf(pos -> pos.uuid().equals(client.player.getUuid()));

        Vec3d playerPos = client.player.getPos();
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

        for (Vec3d marker : deathMarkers) {
            if (!currentIconPositions.containsKey(marker)) {
                PlayerPosition markerPos = new PlayerPosition(new UUID(marker.hashCode(), marker.hashCode()), "Death", marker.x, marker.y, marker.z);
                float initialPos = calculateRelativePosition(client.player, markerPos);
                if (initialPos >= 0) currentIconPositions.put(marker, initialPos * Constants.BAR_WIDTH);
            }
        }

        positionsToRenderCache = positions.size() > maxIcons ? new ArrayList<>(positions.subList(0, maxIcons)) : new ArrayList<>(positions);
    }

    public static void tick(MinecraftClient client) {
        if (client.world == null) return;
        updateRenderCache(client);
        if (client.player != null) {
            deathMarkers.removeIf(marker -> {
                if (client.player.getPos().distanceTo(marker) < 10) {
                    currentIconPositions.remove(marker);
                    return true;
                }
                return false;
            });
        }
    }

    public static void addDeathLocation(Vec3d location) {
        if (deathMarkers.stream().anyMatch(marker -> marker.distanceTo(location) < 5)) {
            return;
        }
        deathMarkers.add(location);
        while (deathMarkers.size() > MAX_DEATH_MARKERS) {
            Vec3d oldest = deathMarkers.removeFirst();
            currentIconPositions.remove(oldest);
        }
    }

    public static void registerEvents() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            playerPositions.clear();
            currentIconPositions.clear();
            deathMarkers.clear();
            ClientPlayNetworking.registerReceiver(PositionUpdatePayload.ID, (payload, context) -> {
                Client.updateLastServerUpdateTime();
                client.execute(() -> {
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
                            }
                        }
                    }

                    List<PositionUpdatePayload.PositionData> positionsList = payload.positions();
                    if (positionsList != null) {
                        for (PositionUpdatePayload.PositionData posUpdate : positionsList) {
                            if (posUpdate == null || posUpdate.uuid() == null) continue;
                            PlayerPosition existingPosData = playerPositions.get(posUpdate.uuid());
                            String name = existingPosData != null ? existingPosData.name() : "Player";
                            playerPositions.put(posUpdate.uuid(), new PlayerPosition(posUpdate.uuid(), name, posUpdate.x(), posUpdate.y(), posUpdate.z()));
                        }
                    }
                });
            });
        });
    }

    public static void render(DrawContext context) {
        final MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || !CONFIG.isModEnabled()) {
            shouldApplyHudOffset = false;
            return;
        }

        if (positionsToRenderCache.isEmpty() && deathMarkers.isEmpty()) {
            shouldApplyHudOffset = false;
            return;
        }

        final int barX = (context.getScaledWindowWidth() - Constants.BAR_WIDTH) / 2;
        final int barY = context.getScaledWindowHeight() + Constants.BAR_Y_OFFSET;
        final boolean showDetails = Keybinds.shouldShowPlayerNames() || CONFIG.isAlwaysShowPlayerNames();

        List<RenderEntry> allEntries = new ArrayList<>();
        for (PlayerPosition pos : positionsToRenderCache) {
            final PlayerEntity targetPlayer = client.world.getPlayerByUuid(pos.uuid());
            if (targetPlayer != null && shouldHideTarget(targetPlayer)) continue;
            double distance = DistanceUtils.calculateDistance(client.player.getX(), client.player.getY(), client.player.getZ(), pos.x, pos.y, pos.z);
            float alpha = getDistanceAlpha(distance);
            allEntries.add(new RenderEntry(pos, pos.uuid(), distance, alpha, false));
        }

        for (Vec3d marker : deathMarkers) {
            double distance = DistanceUtils.calculateDistance(client.player.getX(), client.player.getY(), client.player.getZ(), marker.x, marker.y, marker.z);
            PlayerPosition markerPos = new PlayerPosition(new UUID(marker.hashCode(), marker.hashCode()), "Death", marker.x, marker.y, marker.z);
            allEntries.add(new RenderEntry(markerPos, marker, distance, 1.0f, true));
        }

        allEntries.sort(Comparator.comparingDouble(RenderEntry::distance).reversed());

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        int totalVisibleIcons = allEntries.size();
        for (int i = 0; i < allEntries.size(); i++) {
            RenderEntry entry = allEntries.get(i);
            float baseZ = calculateBaseZ(i, totalVisibleIcons);
            renderBarIcon(context, entry.pos, entry.key, barX, barY, baseZ, showDetails, entry.alpha);
        }

        shouldApplyHudOffset = hasVisibleIconsInVisibleRange(client);
        RenderSystem.disableBlend();
    }

    private record RenderEntry(PlayerPosition pos, Object key, double distance, float alpha, boolean isDeathMarker) {}

    public static boolean hasVisibleIconsInVisibleRange(MinecraftClient client) {
        if (client.player == null) return false;

        final int barLeft = (client.getWindow().getScaledWidth() - Constants.BAR_WIDTH) / 2;
        final int barRight = barLeft + Constants.BAR_WIDTH;

        for (PlayerPosition pos : positionsToRenderCache) {
            float targetPos = calculateRelativePosition(client.player, pos);
            if (targetPos < 0) continue;

            Float currentPos = currentIconPositions.get(pos.uuid());
            if (currentIconPos(barLeft, barRight, currentPos)) return true;
        }

        for (Vec3d marker : deathMarkers) {
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

    private static void renderBarIcon(DrawContext context, PlayerPosition pos, Object key, int barX, int barY, float baseZ, boolean showDetails, float alpha) {
        MinecraftClient client = MinecraftClient.getInstance();
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
            boolean isDeathMarker = key instanceof Vec3d;
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

            context.getMatrices().translate(0, 0, baseZ);

            if (isDeathMarker) {
                RenderAddons.renderDeathMarker(context, topLeftX, topLeftY, Constants.ICON_BASE_SIZE, finalAlpha, CONFIG);
            } else {
                double iconDistance = DistanceUtils.calculateDistance(client.player.getX(), client.player.getY(), client.player.getZ(), pos.x, pos.y, pos.z);
                RenderAddons.renderPlayerIcon(context, pos.name(), pos.uuid(), iconDistance, topLeftX, topLeftY, Constants.ICON_BASE_SIZE, showHead, CONFIG, finalAlpha);
            }

            renderHeightIndicator(context, pos, iconCenterX, (int) (topLeftY + Constants.ICON_BASE_SIZE / 2f), finalAlpha, appearance);

            context.getMatrices().translate(0, 0, 1);
            RenderAddons.renderNameplate(context, text, borderStyle, color, iconCenterX - (client.textRenderer.getWidth(text) * CONFIG.getNameplateScale() + 4) / 2, topLeftY - (12 * CONFIG.getNameplateScale()) - 4, nameplateAlpha, CONFIG.getNameplateScale());
        });
    }

    public static boolean shouldApplyHudOffset() {
        return shouldApplyHudOffset;
    }

    private static boolean showUp(MinecraftClient client, PlayerPosition pos) {
        return isArrowUp(client, pos);
    }

    private static boolean showDown(MinecraftClient client, PlayerPosition pos) {
        return isArrowDown(client, pos);
    }

    private static void renderHeightIndicator(DrawContext context, PlayerPosition pos, int centerX, int centerY, float alpha, Config.PlayerAppearance appearance) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        if (showUp(client, pos) || showDown(client, pos)) {
            String arrowId = (appearance != null && appearance.arrowType != null) ? appearance.arrowType : CONFIG.getArrowType();
            renderHeightArrow(context, centerX, centerY, alpha, showUp(client, pos), arrowId);
        }
    }

    private static void renderHeightArrow(DrawContext context, int centerX, int centerY, float alpha, boolean isUp, String arrowId) {
        int arrowX = centerX - 5;
        int arrowY = isUp ? (centerY - Constants.ICON_BASE_SIZE - 2 - CONFIG.getVerticalPadding()) : (centerY + 2 + CONFIG.getVerticalPadding());
        RenderAddons.renderArrow(context, arrowId, isUp, arrowX, arrowY, Constants.ICON_BASE_SIZE, alpha);
    }

    private static List<PlayerPosition> getPositionsToRender(MinecraftClient client) {
        if (client.world == null || client.player == null) return Collections.emptyList();
        boolean useServerData = !Client.isLocalMode();
        if (useServerData && !playerPositions.isEmpty()) {
            return new ArrayList<>(playerPositions.values());
        }
        return client.world.getPlayers().stream().filter(p -> !p.getUuid().equals(client.player.getUuid())).map(p -> new PlayerPosition(p.getUuid(), p.getName().getString(), p.getX(), p.getY(), p.getZ())).collect(Collectors.toList());
    }

    private static float getDistanceAlpha(double distance) {
        if (distance > CONFIG.getFadeEndDistance()) return CONFIG.getFadeAlphaMin();
        if (distance < CONFIG.getFadeStartDistance()) return CONFIG.getFadeAlphaMax();
        float progress = (float) ((distance - CONFIG.getFadeStartDistance()) / (CONFIG.getFadeEndDistance() - CONFIG.getFadeStartDistance()));
        return MathHelper.lerp(MathUtils.easeInOutQuad(progress), CONFIG.getFadeAlphaMax(), CONFIG.getFadeAlphaMin());
    }

    private static boolean isArrowUp(MinecraftClient client, PlayerPosition pos) {
        return shouldShowArrow(client, pos, true);
    }

    private static boolean isArrowDown(MinecraftClient client, PlayerPosition pos) {
        return shouldShowArrow(client, pos, false);
    }

    private static double getDynamicAngleThreshold(MinecraftClient client, PlayerPosition pos) {
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
        return MathHelper.lerp(yFactor * hFactor, minAngle, maxAngle);
    }

    private static double getCameraAngleDifference(Camera camera, PlayerPosition pos) {
        Vec3d cameraPos = camera.getPos();
        double dx = pos.x - cameraPos.x;
        double dz = pos.z - cameraPos.z;
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        double targetY = pos.y + 1.62;
        return -(Math.toDegrees(Math.atan2(targetY - cameraPos.y, horizontalDist)) + camera.getPitch());
    }

    private static float calculateRelativePosition(PlayerEntity viewer, PlayerPosition target) {
        if (viewer == null) return -1f;

        Vec3d currentPos = new Vec3d(target.x, target.y, target.z);
        Vec3d smoothedPos = currentPos;

        Long currentTime = System.currentTimeMillis();
        Vec3d lastPos = lastKnownPositions.get(target.uuid());
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

        if (totalVisibleIcons <= 1) {
            return minZ;
        }

        float maxAvailableZ = 500f;
        float availableRange = maxAvailableZ - minZ;
        float spacingPerIcon = availableRange / (totalVisibleIcons - 1);

        return minZ + (index * spacingPerIcon);
    }

    private static double getRelativeAngle(PlayerEntity viewer, Vec3d smoothedPos) {
        double relativeAngle = MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(smoothedPos.z - viewer.getZ(), smoothedPos.x - viewer.getX())) - 90 - MathHelper.wrapDegrees(viewer.getYaw()));
        if (CONFIG.isAdjustToFov()) {
            MinecraftClient client = RenderUtils.getClient();
            float fov = (float) client.options.getFov().getValue();
            float fovFactor = (90.0f / fov) * CONFIG.getFovMultiplier();
            relativeAngle *= fovFactor;
        }
        return relativeAngle;
    }

    private static float calculateEdgeAlpha(int iconX, int barX) {
        int edgeDistance = Math.min(iconX - barX, Constants.BAR_WIDTH - (iconX - barX));
        if (edgeDistance >= Constants.EDGE_ALPHA_FADE_MARGIN) return 1.0f;
        return MathHelper.clamp(edgeDistance / (float) Constants.EDGE_ALPHA_FADE_MARGIN, 0.0f, 1.0f);
    }

    private static boolean shouldHideTarget(PlayerEntity target) {
        final ItemStack headStack = target.getEquippedStack(EquipmentSlot.HEAD);
        return target.isSneaking() || target.isInvisible() || (!headStack.isEmpty() && !(headStack.getItem() instanceof ArmorItem));
    }

    private static boolean shouldShowArrow(MinecraftClient client, PlayerPosition pos, boolean up) {
        if (client.player == null) return false;

        if (CONFIG.getHeightDifferenceMode().equals("PLAYER")) {
            double diff = pos.y - (client.player.getY() + 1.0);
            return up ? diff > 5.5 : diff < -5.5;
        }

        double angleDiff = getCameraAngleDifference(client.gameRenderer.getCamera(), pos);
        double threshold = getDynamicAngleThreshold(client, pos);
        return up ? angleDiff < -threshold : angleDiff > threshold;
    }
}
