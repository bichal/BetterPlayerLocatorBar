package net.bichal.bplb.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.bichal.bplb.BetterPlayerLocatorBar;
import net.bichal.bplb.config.BetterPlayerLocatorBarConfig;
import net.bichal.bplb.network.PositionUpdatePayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class BetterPlayerLocatorBarHud {
    private static final BetterPlayerLocatorBarConfig config = BetterPlayerLocatorBarConfig.getInstance();

    private static final int BAR_Y_OFFSET = -30;
    private static final int BAR_WIDTH = 182;
    private static final int ICON_PRIMARY_SIZE = 5;
    private static final int ICON_BORDER_SIZE = 1;
    private static final int ICON_SIZE = ICON_PRIMARY_SIZE + ICON_BORDER_SIZE * 2;
    private static final int ARROW_WIDTH = 7;
    private static final int ARROW_HEIGHT = 5;

    private static final Identifier ICON_TEXTURE = Identifier.of(BetterPlayerLocatorBar.MOD_ID, "textures/gui/icon_overlay.png");
    private static final Identifier ARROW_UP_TEXTURE = Identifier.of(BetterPlayerLocatorBar.MOD_ID, "textures/gui/arrow_up.png");
    private static final Identifier ARROW_DOWN_TEXTURE = Identifier.of(BetterPlayerLocatorBar.MOD_ID, "textures/gui/arrow_down.png");

    private static final Map<UUID, PositionUpdatePayload.PlayerPosition> playerPositions = new HashMap<>();
    private static final Map<UUID, Identifier> playerSkins = new HashMap<>();
    private static final Map<UUID, Float> currentIconPositions = new HashMap<>();
    private static final Map<UUID, Long> joinAnimations = new HashMap<>();
    private static final Map<UUID, Boolean> activePlayers = new HashMap<>();
    private static final Map<UUID, Float> playerNameOffsets = new HashMap<>();

    public static void registerEvents() {
        ClientPlayNetworking.registerGlobalReceiver(PositionUpdatePayload.ID, (payload, context) -> {
            Set<UUID> currentPlayers = payload.positions().stream().map(PositionUpdatePayload.PlayerPosition::uuid).collect(Collectors.toSet());

            playerPositions.keySet().removeIf(uuid -> !currentPlayers.contains(uuid));
            activePlayers.keySet().removeIf(uuid -> !currentPlayers.contains(uuid));

            for (PositionUpdatePayload.PlayerPosition pos : payload.positions()) {
                if (!activePlayers.containsKey(pos.uuid())) {
                    joinAnimations.put(pos.uuid(), System.currentTimeMillis());
                    activePlayers.put(pos.uuid(), true);
                }
                playerPositions.put(pos.uuid(), pos);
            }
        });
    }

    public static void render(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        boolean showDetails = Keybinds.SHOW_PLAYER_NAME.isPressed() || config.isToggleTab();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        int barX = screenWidth / 2 - BAR_WIDTH / 2 - ICON_SIZE / 2;
        int barY = screenHeight + BAR_Y_OFFSET;

        List<PositionUpdatePayload.PlayerPosition> players = new ArrayList<>(playerPositions.values());
        players.sort(Comparator.comparingDouble(pos -> client.player.squaredDistanceTo(pos.x(), pos.y(), pos.z())));

        for (int i = 0; i < players.size(); i++) {
            PositionUpdatePayload.PlayerPosition pos = players.get(i);
            if (pos.uuid().equals(client.player.getUuid())) continue;

            PlayerEntity targetPlayer = client.world != null ? client.world.getPlayerByUuid(pos.uuid()) : null;
            if (targetPlayer != null && shouldHideTarget(targetPlayer)) continue;

            float relativePos = calculateRelativePosition(client.player, pos);
            if (relativePos == Float.MIN_VALUE) continue;

            renderPlayerIcon(context, client, pos, barX, barY, relativePos, i, showDetails);
        }
    }

    private static void renderPlayerIcon(DrawContext context, MinecraftClient client, PositionUpdatePayload.PlayerPosition pos, int barX, int barY, float relativePos, int index, boolean showDetails) {
        float targetPos = relativePos * BAR_WIDTH;
        float currentPos = currentIconPositions.getOrDefault(pos.uuid(), targetPos);
        float lerpSpeed = config.getLerpSpeed();

        float delta = Math.abs(targetPos - currentPos);
        currentPos = delta > (float) BAR_WIDTH / 2 ? targetPos : MathHelper.lerp(lerpSpeed, currentPos, targetPos);
        currentIconPositions.put(pos.uuid(), currentPos);

        int iconX = barX + (int) currentPos;
        float alpha = getAlpha(Objects.requireNonNull(client.player), pos);
        float zDepth = 1 + index;
        float highZDepth = 1000 + index * 10;

        double distance = client.player.getPos().distanceTo(new Vec3d(pos.x(), pos.y(), pos.z()));
        float distanceScale = calculateDistanceScale(distance);
        float edgeScale = calculateEdgeScale(iconX, barX);
        float totalScale = distanceScale * edgeScale;

        int edgeDistance = Math.min(iconX - barX, BAR_WIDTH - (iconX - barX));
        if (edgeDistance < 10) {
            alpha = MathHelper.lerp(edgeDistance / 10f, config.getMinAlpha(), alpha);
        }

        context.getMatrices().push();
        context.getMatrices().translate(0, 0, highZDepth);

        float scaleOffsetX = (iconX + ICON_SIZE / 2f) * (1 - totalScale);
        float scaleOffsetY = (barY + ICON_SIZE / 2f) * (1 - totalScale);
        context.getMatrices().translate(scaleOffsetX, scaleOffsetY, 0);
        context.getMatrices().scale(totalScale, totalScale, 1.0f);

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1, 1, 1, alpha);

        double heightDifference = pos.y() - client.player.getY();
        if (Math.abs(heightDifference) > 4) {
            renderHeightArrow(context, iconX, barY, alpha, heightDifference);
        }

        if (showDetails || config.isAlwaysShowPlayerNames()) {
            renderPlayerName(context, client, pos, iconX, barY - 10, alpha, (int) zDepth);
        }

        if (showDetails || config.isAlwaysShowPlayerHeads()) {
            renderPlayerHead(context, pos.uuid(), pos, iconX, barY, alpha);
        } else {
            renderIcon(context, iconX, barY, generateColorFromUUID(pos.uuid()), config.getIconOpacity() * alpha);
        }

        renderJoinAnimation(context, pos.uuid(), pos, iconX, barY, alpha, totalScale);

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
        context.getMatrices().pop();
    }

    private static float calculateDistanceScale(double distance) {
        float fadeStart = config.getFadeStartDistance();
        float fadeMax = config.getMaxFadeDistance();

        if (distance <= fadeStart) return 1.0f;
        return MathHelper.clamp(1.0f - (float) (distance - fadeStart) / (fadeMax - fadeStart) * 0.25f, 0.75f, 1.0f);
    }

    private static float calculateEdgeScale(int iconX, int barX) {
        int edgeDistance = Math.min(iconX - barX, BAR_WIDTH - (iconX - barX));
        return MathHelper.lerp(Math.min(edgeDistance / 15f, 1f), 0.5f, 1.0f);
    }

    private static boolean shouldHideTarget(PlayerEntity target) {
        ItemStack headStack = target.getEquippedStack(EquipmentSlot.HEAD);
        boolean isHelmet = headStack.isOf(Items.LEATHER_HELMET) || headStack.isOf(Items.CHAINMAIL_HELMET) || headStack.isOf(Items.IRON_HELMET) || headStack.isOf(Items.GOLDEN_HELMET) || headStack.isOf(Items.DIAMOND_HELMET) || headStack.isOf(Items.NETHERITE_HELMET);

        return target.isSneaking() || target.isInvisible() || !(headStack.isEmpty() || isHelmet);
    }

    private static float calculateRelativePosition(PlayerEntity viewer, PositionUpdatePayload.PlayerPosition target) {
        double dx = target.x() - viewer.getX();
        double dz = target.z() - viewer.getZ();

        double angle = Math.toDegrees(Math.atan2(dz, dx));
        double playerYaw = (viewer.getYaw() + 360) % 360;
        double relativeAngle = (angle - playerYaw + 360) % 360;
        float relativePos = ((float) ((relativeAngle + 90) / 180.0)) - 0.5f;

        return (relativePos >= 0 && relativePos <= 1) ? relativePos : Float.MIN_VALUE;
    }

    private static float getAlpha(PlayerEntity viewer, PositionUpdatePayload.PlayerPosition target) {
        Vec3d targetPos = new Vec3d(target.x(), target.y(), target.z());
        double distance = viewer.getPos().distanceTo(targetPos);
        float fadeStart = config.getFadeStartDistance();
        float fadeMax = config.getMaxFadeDistance();
        float minAlpha = config.getMinAlpha();

        if (distance > fadeMax) return minAlpha;
        if (distance < fadeStart) return 1f;

        float distanceProgress = (float) ((distance - fadeStart) / (fadeMax - fadeStart));
        return 1f - distanceProgress * (1 - minAlpha);
    }

    private static int generateColorFromUUID(UUID uuid) {
        Random random = new Random(uuid.hashCode());
        return (255 << 24) | ((random.nextInt(150) + 50) << 16) | ((random.nextInt(150) + 50) << 8) | (random.nextInt(150) + 50);
    }

    private static void renderIcon(DrawContext context, int x, int y, int color, float alpha) {
        RenderSystem.setShaderColor(((color >> 16) & 0xFF) / 255.0f, ((color >> 8) & 0xFF) / 255.0f, (color & 0xFF) / 255.0f, alpha);
        context.drawTexture(ICON_TEXTURE, x, y, ICON_SIZE, ICON_SIZE, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
    }

    private static void renderHeightArrow(DrawContext context, int x, int y, float alpha, double heightDifference) {
        float arrowAlpha = alpha * MathHelper.lerp((float) MathHelper.clamp((Math.abs(heightDifference) - 4) / 196, 0, 1), 1.0f, 0.1f);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, arrowAlpha);

        if (heightDifference > 0) {
            context.drawTexture(ARROW_UP_TEXTURE, x, y - ICON_SIZE + 1, ARROW_WIDTH, ARROW_HEIGHT, 0, 0, ARROW_WIDTH, ARROW_HEIGHT, ARROW_WIDTH, ARROW_HEIGHT);
        } else {
            context.drawTexture(ARROW_DOWN_TEXTURE, x, y + ICON_SIZE + 1, ARROW_WIDTH, ARROW_HEIGHT, 0, 0, ARROW_WIDTH, ARROW_HEIGHT, ARROW_WIDTH, ARROW_HEIGHT);
        }
    }

    private static void renderPlayerName(DrawContext context, MinecraftClient client, PositionUpdatePayload.PlayerPosition pos, int x, int y, float alpha, int translateZ) {
        PlayerEntity player = client.world != null ? client.world.getPlayerByUuid(pos.uuid()) : null;
        if (player == null) return;

        String name = pos.name();
        int textPadding = 3;
        float scale = 0.65f;
        int textWidth = client.textRenderer.getWidth(name) + textPadding * 2;
        int scaledTextWidth = (int) (textWidth * scale);
        int scaledFontHeight = (int) (client.textRenderer.fontHeight * scale);

        int iconRelativeX = x - (client.getWindow().getScaledWidth() / 2 - BAR_WIDTH / 2);
        int adjustedX = getAdjustedX(x, iconRelativeX, textWidth);
        int playerColor = generateColorFromUUID(pos.uuid());

        int padding = 1;
        int backgroundX = adjustedX + (textWidth - scaledTextWidth) / 2 + textWidth / (scaledTextWidth / 2);
        int backgroundY = y + (client.textRenderer.fontHeight - scaledFontHeight) / 2;

        PlayerEntity currentPlayer = client.player;
        if (currentPlayer != null && Math.abs(pos.y() - currentPlayer.getY()) > 4) {
            backgroundY -= 6;
        }

        float currentYOffset = playerNameOffsets.getOrDefault(pos.uuid(), (float) backgroundY);
        currentYOffset = MathHelper.lerp(config.getLerpSpeed(), currentYOffset, backgroundY);
        playerNameOffsets.put(pos.uuid(), currentYOffset);

        context.getMatrices().push();
        context.getMatrices().translate(backgroundX, currentYOffset, translateZ);

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1, 1, 1, 0.7f * alpha);

        context.fill(0, 0, scaledTextWidth, scaledFontHeight, darkenColor(playerColor, 0.4f));
        drawRoundedBorder(context, -padding, -padding, scaledTextWidth + padding, scaledFontHeight + padding, darkenColor(playerColor, 0.6f));
        drawSquaredBorder(context, 0, 0, scaledTextWidth, scaledFontHeight, darkenColor(playerColor, 0.5f));

        context.getMatrices().scale(scale, scale, 1.0f);
        int textAlpha = (int) (alpha * 255) << 24;
        context.drawText(client.textRenderer, name, textPadding, 0, 0xFFFFFF | textAlpha, true);

        RenderSystem.disableBlend();
        context.getMatrices().pop();
    }

    private static void renderPlayerHead(DrawContext context, UUID playerId, PositionUpdatePayload.PlayerPosition pos, int x, int y, float alpha) {
        MinecraftClient client = MinecraftClient.getInstance();
        Identifier skin = playerSkins.computeIfAbsent(playerId, id -> {
            if (client.world == null) return Identifier.of("minecraft", "textures/entity/steve.png");
            AbstractClientPlayerEntity p = (AbstractClientPlayerEntity) client.world.getPlayerByUuid(id);
            return p != null ? p.getSkinTextures().texture() : Identifier.of("minecraft", "textures/entity/steve.png");
        });

        int playerColor = generateColorFromUUID(pos.uuid());
        int borderX = x + ICON_BORDER_SIZE;
        int borderY = y + ICON_BORDER_SIZE;

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);

        drawRoundedBorder(context, borderX, borderY, borderX + ICON_PRIMARY_SIZE, borderY + ICON_PRIMARY_SIZE, darkenColor(playerColor, 0.8f));
        context.drawTexture(skin, borderX, borderY, ICON_PRIMARY_SIZE, ICON_PRIMARY_SIZE, 8, 8, 8, 8, 64, 64);
    }

    private static void renderJoinAnimation(DrawContext context, UUID uuid, PositionUpdatePayload.PlayerPosition pos, int x, int y, float alpha, float baseScale) {
        Long startTime = joinAnimations.get(uuid);
        if (startTime == null) return;

        float progress = (System.currentTimeMillis() - startTime) / 400.0f;
        if (progress > 2.0f) {
            joinAnimations.remove(uuid);
            return;
        }

        float cycleProgress = progress % 1.0f;
        float animScale = baseScale * (1.0f + cycleProgress * 0.5f);
        float animAlpha = (1.0f - cycleProgress) * 0.75f * alpha;

        context.getMatrices().push();
        context.getMatrices().translate(x + ICON_SIZE / 2f, y + ICON_SIZE / 2f, 0);
        context.getMatrices().scale(animScale, animScale, 1.0f);
        context.getMatrices().translate(-(x + ICON_SIZE / 2f), -(y + ICON_SIZE / 2f), 0);

        renderIcon(context, x, y, generateColorFromUUID(pos.uuid()), animAlpha);

        context.getMatrices().pop();
    }

    private static int darkenColor(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = (int) (((color >> 16) & 0xFF) * (1 - factor));
        int g = (int) (((color >> 8) & 0xFF) * (1 - factor));
        int b = (int) ((color & 0xFF) * (1 - factor));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static void drawRoundedBorder(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        context.fill(x1 - 1, y1, x1, y2, color);
        context.fill(x2, y1, x2 + 1, y2, color);
        context.fill(x1, y1 - 1, x2, y1, color);
        context.fill(x1, y2, x2, y2 + 1, color);
    }

    private static void drawSquaredBorder(DrawContext context, int x1, int y1, int x2, int y2, int color) {
        context.fill(x1 - 1, y1 - 1, x1, y2 + 1, color);
        context.fill(x2, y1 - 1, x2 + 1, y2 + 1, color);
        context.fill(x1, y1 - 1, x2, y1, color);
        context.fill(x1, y2, x2, y2 + 1, color);
    }

    public static boolean shouldApplyArrowOffset(MinecraftClient client) {
        if (client.player == null || client.world == null) return false;
        return client.world.getPlayers().stream().anyMatch(p -> Math.abs(p.getY() - client.player.getY()) > 4 && p.getY() - client.player.getY() > 0);
    }

    private static int getAdjustedX(int x, int iconRelativeX, int textWidth) {
        float edgeMargin = 40.0f;
        float progress = MathHelper.clamp(1.0f - (Math.min(iconRelativeX, BAR_WIDTH - iconRelativeX) / edgeMargin), 0.0f, 1.0f);

        if (iconRelativeX < edgeMargin) {
            return MathHelper.lerp(progress, x - textWidth / 2, x);
        } else if (iconRelativeX > BAR_WIDTH - edgeMargin) {
            return MathHelper.lerp(progress, x - textWidth / 2, x - textWidth);
        } else {
            return x - textWidth / 2;
        }
    }
}
