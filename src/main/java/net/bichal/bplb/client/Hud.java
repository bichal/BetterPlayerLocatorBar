package net.bichal.bplb.client;

import net.bichal.bplb.client.render.RenderAddons;
import net.bichal.bplb.client.render.RenderUtils;
import net.bichal.bplb.config.PlayerConfig;
import net.bichal.bplb.network.PositionUpdatePayload;
import net.bichal.bplb.util.Constants;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.stream.Collectors;

import static net.bichal.bplb.util.ColorUtils.generateColorFromUUID;
import static net.bichal.bplb.util.Constants.CONFIG;

@Environment(EnvType.CLIENT)
public class Hud {
    public record PlayerPosition(UUID uuid, String name, double x, double y, double z) {
    }

    private static final Map<UUID, PlayerPosition> playerPositions = new HashMap<>();
    private static final Map<UUID, Identifier> playerSkins = new HashMap<>();
    private static final Map<Object, Float> currentIconPositions = new HashMap<>();
    private static final Map<Object, Float> nameplateYOffsets = new HashMap<>();
    private static final Map<Object, Float> nameplateAlphas = new HashMap<>();
    private static final List<Vec3d> deathMarkers = new ArrayList<>();
    private static final int MAX_DEATH_MARKERS = 4;
    private static List<PlayerPosition> positionsToRenderCache = new ArrayList<>();
    private static int tickCounter = 0;

    public static void tick(MinecraftClient client) {
        tickCounter++;
        if (tickCounter >= CONFIG.getPositionUpdateRateTicks()) {
            tickCounter = 0;
            updateRenderCache(client);
        }
        if (client.player != null) {
            deathMarkers.removeIf(marker -> {
                if (client.player.getPos().distanceTo(marker) < 10) {
                    currentIconPositions.remove(marker);
                    nameplateYOffsets.remove(marker);
                    nameplateAlphas.remove(marker);
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
            nameplateYOffsets.remove(oldest);
            nameplateAlphas.remove(oldest);
        }
    }

    private static void updateRenderCache(MinecraftClient client) {
        if (client.player == null) {
            positionsToRenderCache.clear();
            return;
        }
        List<PlayerPosition> positions = getPositionsToRender(client);
        positions.removeIf(pos -> pos.uuid().equals(client.player.getUuid()));
        positions.sort(Comparator.comparingDouble(pos -> client.player.squaredDistanceTo(pos.x, pos.y, pos.z)));
        final int maxIcons = CONFIG.getMaxVisibleIcons();
        positionsToRenderCache = new ArrayList<>(positions.size() > maxIcons ? positions.subList(0, maxIcons) : positions);
    }

    public static void registerEvents() {
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            playerPositions.clear();
            playerSkins.clear();
            currentIconPositions.clear();
            nameplateYOffsets.clear();
            nameplateAlphas.clear();
            deathMarkers.clear();
            ClientPlayNetworking.registerReceiver(PositionUpdatePayload.ID, (payload, context) -> {
                Client.updateLastServerUpdateTime();
                context.client().execute(() -> {
                    for (UUID disconnectedId : payload.disconnectedPlayers()) {
                        playerPositions.remove(disconnectedId);
                        playerSkins.remove(disconnectedId);
                        currentIconPositions.remove(disconnectedId);
                        nameplateYOffsets.remove(disconnectedId);
                        nameplateAlphas.remove(disconnectedId);
                    }
                    for (PositionUpdatePayload.PlayerInfo newPlayer : payload.newPlayers()) {
                        PlayerPosition existingData = playerPositions.get(newPlayer.uuid());
                        double x = existingData != null ? existingData.x() : 0;
                        double y = existingData != null ? existingData.y() : 0;
                        double z = existingData != null ? existingData.z() : 0;
                        playerPositions.put(newPlayer.uuid(), new PlayerPosition(newPlayer.uuid(), newPlayer.name(), x, y, z));
                    }
                    for (PositionUpdatePayload.PositionData posUpdate : payload.positions()) {
                        PlayerPosition existingData = playerPositions.get(posUpdate.uuid());
                        String name = existingData != null ? existingData.name() : "Player";
                        playerPositions.put(posUpdate.uuid(), new PlayerPosition(posUpdate.uuid(), name, posUpdate.x(), posUpdate.y(), posUpdate.z()));
                    }
                });
            });
        });
    }

    public static void render(DrawContext context, float tickDelta) {
        final MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || !CONFIG.isModEnabled()) return;
        if (positionsToRenderCache.isEmpty() && deathMarkers.isEmpty()) return;
        final int barX = (context.getScaledWindowWidth() - Constants.BAR_WIDTH) / 2;
        final int barY = context.getScaledWindowHeight() + Constants.BAR_Y_OFFSET;

        final boolean showDetails = Keybinds.shouldShowPlayerNames() || CONFIG.isAlwaysShowPlayerNames();

        for (int i = 0; i < positionsToRenderCache.size(); i++) {
            final PlayerPosition pos = positionsToRenderCache.get(i);
            final PlayerEntity targetPlayer = client.world.getPlayerByUuid(pos.uuid());
            if (targetPlayer != null && shouldHideTarget(targetPlayer)) continue;
            renderPlayerIcon(context, pos, barX, barY, i, showDetails, tickDelta);
        }
        for (int i = 0; i < deathMarkers.size(); i++) {
            renderDeathMarker(context, deathMarkers.get(i), barX, barY, i + positionsToRenderCache.size(), showDetails, tickDelta);
        }
    }

    private static void renderPlayerIcon(DrawContext context, PlayerPosition pos, int barX, int barY, int index, boolean showDetails, float tickDelta) {
        renderBarIcon(context, pos, pos.uuid(), barX, barY, index, showDetails, tickDelta);
    }

    private static void renderDeathMarker(DrawContext context, Vec3d pos, int barX, int barY, int index, boolean showDetails, float tickDelta) {
        PlayerPosition markerPos = new PlayerPosition(new UUID(pos.hashCode(), pos.hashCode()), "Death", pos.x, pos.y, pos.z);
        renderBarIcon(context, markerPos, pos, barX, barY, index, showDetails, tickDelta);
    }

    private static void renderBarIcon(DrawContext context, PlayerPosition pos, Object key, int barX, int barY, int index, boolean showDetails, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        float targetPos = calculateRelativePosition(client.player, pos);
        if (targetPos < 0) return;
        targetPos *= Constants.BAR_WIDTH;
        float currentPos = currentIconPositions.getOrDefault(key, targetPos);
        if (Math.abs(currentPos - targetPos) > Constants.BAR_WIDTH) currentPos = targetPos;
        currentPos = MathHelper.lerp(CONFIG.getLerpSpeed() * tickDelta * 2f, currentPos, targetPos);
        currentIconPositions.put(key, currentPos);
        int iconCenterX = barX + (int) currentPos;
        double distance = client.player.getPos().distanceTo(new Vec3d(pos.x, pos.y, pos.z));
        float alpha = getDistanceAlpha(distance) * calculateEdgeAlpha(iconCenterX, barX);
        if (alpha <= 0.01f) return;
        float topLeftX = iconCenterX - Constants.ICON_BASE_SIZE / 2f;
        float topLeftY = barY - Constants.ICON_BASE_SIZE / 2f;
        RenderUtils.withMatrixPush(context, 0, 0, () -> {
            context.getMatrices().translate(0, 0, Constants.HIGH_Z_DEPTH_START + index * Constants.Z_DEPTH_INCREMENT);

            boolean isDeathMarker = key instanceof Vec3d;
            boolean showHead = !isDeathMarker && (CONFIG.isToggleTab() || CONFIG.isAlwaysShowPlayerHeads() || Keybinds.shouldShowPlayerNames());

            if (isDeathMarker) {
                RenderAddons.renderDeathMarker(context, topLeftX, topLeftY, Constants.ICON_BASE_SIZE, alpha, CONFIG);
            } else {
                RenderAddons.renderPlayerIcon(context, pos.name(), pos.uuid(), distance, topLeftX, topLeftY, Constants.ICON_BASE_SIZE, alpha, showHead, CONFIG);
            }

            String text = isDeathMarker ? String.format("%d, %d, %d", (int) pos.x, (int) pos.y, (int) pos.z) : pos.name();
            int color;
            String borderStyle;

            if (isDeathMarker) {
                color = CONFIG.getDeathMarkerColor();
                borderStyle = CONFIG.getDeathMarkerBorderStyle();
            } else {
                PlayerConfig appearance = CONFIG.getPlayerConfig(pos.name());
                color = appearance != null && appearance.color != null ? appearance.color : generateColorFromUUID(pos.uuid());
                borderStyle = appearance != null && appearance.iconBorderStyle != null ? appearance.iconBorderStyle : CONFIG.getNameBorderStyle();
            }
            PlayerConfig appearance = isDeathMarker ? null : CONFIG.getPlayerConfig(pos.name());
            float nameplateAlpha = showDetails ? alpha : 0f;

            RenderAddons.renderNameplate(context, text, borderStyle, color, iconCenterX - (client.textRenderer.getWidth(text) * CONFIG.getNameplateScale() + 4) / 2, topLeftY - (12 * CONFIG.getNameplateScale()) - 4, nameplateAlpha, CONFIG.getNameplateScale());

            renderHeightIndicator(context, pos, iconCenterX, (int) (topLeftY + Constants.ICON_BASE_SIZE / 2f), alpha, appearance);
        });
    }

    private static void renderHeightIndicator(DrawContext context, PlayerPosition pos, int centerX, int centerY, float alpha, PlayerConfig appearance) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        boolean showUp = isArrowUp(client, pos);
        boolean showDown = isArrowDown(client, pos);

        if (showUp || showDown) {
            String arrowId = (appearance != null && appearance.arrowType != null) ? appearance.arrowType : CONFIG.getArrowType();
            renderHeightArrow(context, centerX, centerY, alpha, showUp, arrowId);
        }
    }

    private static void renderHeightArrow(DrawContext context, int centerX, int centerY, float alpha, boolean isUp, String arrowId) {
        int arrowX = centerX - 5;
        int arrowY = isUp ? (centerY - Constants.ICON_BASE_SIZE / 2 - Constants.ICON_BASE_SIZE - 2)
                : (centerY + Constants.ICON_BASE_SIZE / 2 + 2);
        RenderAddons.renderArrow(context, arrowId, isUp, arrowX, arrowY, Constants.ICON_BASE_SIZE, alpha);
    }

    private static List<PlayerPosition> getPositionsToRender(MinecraftClient client) {
        if (Client.isLocalMode() || (System.currentTimeMillis() - Client.getLastServerUpdateTime() > 5000)) {
            if (client.world == null || client.player == null) return Collections.emptyList();
            return client.world.getPlayers().stream()
                    .filter(p -> !p.getUuid().equals(client.player.getUuid()))
                    .map(p -> new PlayerPosition(p.getUuid(), p.getName().getString(), p.getX(), p.getY(), p.getZ()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>(playerPositions.values());
    }

    public static boolean hasVisiblePlayerIcons() {
        return !positionsToRenderCache.isEmpty() || !deathMarkers.isEmpty();
    }

    public static boolean shouldApplyGlobalArrowOffset(MinecraftClient client) {
        if (client.player == null) return false;
        for (PlayerPosition pos : positionsToRenderCache) {
            if (isArrowUp(client, pos)) return true;
        }
        for (Vec3d vec : deathMarkers) {
            PlayerPosition pos = new PlayerPosition(new UUID(vec.hashCode(), vec.hashCode()), "Death", vec.x, vec.y, vec.z);
            if (isArrowUp(client, pos)) return true;
        }
        return false;
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

    private static float getDistanceAlpha(double distance) {
        if (distance > CONFIG.getMaxFadeDistance()) return CONFIG.getFadeAlphaMin();
        if (distance < CONFIG.getFadeStartDistance()) return CONFIG.getFadeAlphaMax();
        float progress = (float) ((distance - CONFIG.getFadeStartDistance()) /
                (CONFIG.getMaxFadeDistance() - CONFIG.getFadeStartDistance()));
        return MathHelper.lerp(progress, CONFIG.getFadeAlphaMax(), CONFIG.getFadeAlphaMin());
    }

    private static float calculateEdgeAlpha(int iconX, int barX) {
        int edgeDistance = Math.min(iconX - barX, Constants.BAR_WIDTH - (iconX - barX));
        if (edgeDistance >= Constants.EDGE_ALPHA_FADE_MARGIN) return 1.0f;
        return MathHelper.clamp(edgeDistance / (float) Constants.EDGE_ALPHA_FADE_MARGIN, 0.0f, 1.0f);
    }

    private static boolean shouldHideTarget(PlayerEntity target) {
        final ItemStack headStack = target.getEquippedStack(EquipmentSlot.HEAD);
        return target.isSneaking() || target.isInvisible() ||
                (!headStack.isEmpty() && !(headStack.getItem() instanceof ArmorItem));
    }

    private static boolean shouldShowArrow(MinecraftClient client, PlayerPosition pos, boolean up) {
        if (client.player == null) return false;

        if (CONFIG.getHeightDifferenceMode().equals("PLAYER")) {
            double diff = pos.y - (client.player.getY() + 1.0);
            return up ? diff > 2.5 : diff < -2.5;
        }

        double angleDiff = getCameraAngleDifference(client.gameRenderer.getCamera(), pos);
        double threshold = getDynamicAngleThreshold(client, pos);
        return up ? angleDiff < -threshold : angleDiff > threshold;
    }

    private static float calculateRelativePosition(PlayerEntity viewer, PlayerPosition target) {
        if (viewer == null) return -1f;
        double relativeAngle = MathHelper.wrapDegrees(
                Math.toDegrees(Math.atan2(target.z - viewer.getZ(), target.x - viewer.getX())) - 90 -
                        MathHelper.wrapDegrees(viewer.getYaw())
        );
        if (Math.abs(relativeAngle) > 90) return -1f;
        return (float) (relativeAngle + 90) / 180.0f;
    }
}
