package net.bichal.bplb.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.bichal.bplb.BetterPlayerLocatorBar;
import net.bichal.bplb.network.PositionUpdatePayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class BetterPlayerLocatorBarHud {
    private static final int BAR_Y_OFFSET = -30;
    private static final int BAR_WIDTH = 182;
    private static final int ICON_PRIMARY_SIZE = 5;
    private static final int ICON_BORDER_SIZE = 1;
    private static final int ICON_SIZE = ICON_PRIMARY_SIZE + ICON_BORDER_SIZE * 2;
    private static final float MAX_FADE_DISTANCE = 5000f;
    private static final float FADE_START_DISTANCE = 100f;
    private static final float MIN_ALPHA = 0.1f;
    private static final float ICON_OPACITY = 1f;
    private static final Identifier ICON_TEXTURE = Identifier.of(BetterPlayerLocatorBar.MOD_ID, "textures/gui/icon_overlay.png");
    private static final Map<UUID, PositionUpdatePayload.PlayerPosition> playerPositions = new HashMap<>();
    private static final Map<UUID, int[]> playerColors = new HashMap<>();
    private static final Map<UUID, Identifier> playerSkins = new HashMap<>();
    private static final Random RANDOM = new Random();
    private static final Map<UUID, Float> currentIconPositions = new HashMap<>();
    private static final float LERP_SPEED = 0.2f;
    private static final Map<UUID, Long> joinAnimations = new HashMap<>();
    private static final Map<UUID, Boolean> activePlayers = new HashMap<>();
    private static final Map<UUID, Float> playerNameOffsets = new HashMap<>();
    private static final int ARROW_WIDTH = 7;
    private static final int ARROW_HEIGHT = 5;
    private static final Identifier ARROW_UP_TEXTURE = Identifier.of(BetterPlayerLocatorBar.MOD_ID, "textures/gui/arrow_up.png");
    private static final Identifier ARROW_DOWN_TEXTURE = Identifier.of(BetterPlayerLocatorBar.MOD_ID, "textures/gui/arrow_down.png");

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

        boolean showDetails = Keybinds.SHOW_PLAYER_NAME.isPressed();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        int barX = screenWidth / 2 - BAR_WIDTH / 2 - ICON_SIZE / 2;
        int barY = screenHeight + BAR_Y_OFFSET;

        List<PositionUpdatePayload.PlayerPosition> players = new ArrayList<>(playerPositions.values());
        players.sort(Comparator.comparingDouble(pos -> client.player.squaredDistanceTo(pos.x(), pos.y(), pos.z())));

        for (int i = 0; i < players.size(); i++) {
            PositionUpdatePayload.PlayerPosition pos = players.get(i);
            if (pos.uuid().equals(client.player.getUuid())) continue;

            float relativePos = calculateRelativePosition(client.player, pos);
            if (relativePos != Float.MIN_VALUE) {
                float targetPos = relativePos * BAR_WIDTH;
                float currentPos = currentIconPositions.getOrDefault(pos.uuid(), targetPos);
                currentPos = MathHelper.lerp(LERP_SPEED, currentPos, targetPos);
                currentIconPositions.put(pos.uuid(), currentPos);
                int iconX = barX + (int) currentPos;
                float alpha = getAlpha(client.player, pos);

                float translateZ = 1000 + i * 10;

                Vec3d targetPosVec = new Vec3d(pos.x(), pos.y(), pos.z());
                double distance = client.player.getPos().distanceTo(targetPosVec);

                float distanceScale = 1.0f;
                if (distance > 1000) {
                    distanceScale = MathHelper.clamp((float) (1 - (distance - 1000) / 4000 * 0.25f), 0.75f, 1.0f);
                }

                int edgeDistance = Math.min(iconX - barX, BAR_WIDTH - (iconX - barX));
                float edgeScale = MathHelper.lerp(Math.min(edgeDistance / 35f, 1f), 0.5f, 1.0f);
                float totalScale = distanceScale * edgeScale;

                if (edgeDistance < 15) {
                    alpha = MathHelper.lerp(edgeDistance / 15f, 0.0f, alpha);
                }

                context.getMatrices().push();
                context.getMatrices().translate(0, 0, translateZ);

                float scaleOffsetX = (iconX + ICON_SIZE / 2f) * (1 - totalScale);
                float scaleOffsetY = (barY + ICON_SIZE / 2f) * (1 - totalScale);
                context.getMatrices().translate(scaleOffsetX, scaleOffsetY, 0);
                context.getMatrices().scale(totalScale, totalScale, 1.0f);

                playerColors.computeIfAbsent(pos.uuid(), k -> new int[]{generateRandomColor()});

                double heightDifference = pos.y() - client.player.getY();
                if (Math.abs(heightDifference) > 4) {
                    if (heightDifference > 0) {
                        renderArrowUp(context, iconX, barY, alpha, heightDifference);
                    } else {
                        renderArrowDown(context, iconX, barY, alpha, heightDifference);
                    }
                }
                if (showDetails) {
                    renderPlayerHead(context, client, pos.uuid(), pos, iconX, barY, ICON_OPACITY * alpha);
                    renderPlayerName(context, client, pos, iconX, barY - 10, totalScale, (int) translateZ);
                } else {
                    renderIcon(context, iconX, barY, playerColors.get(pos.uuid()), ICON_OPACITY * alpha);
                }
                renderJoinAnimation(context, pos.uuid(), iconX, barY, alpha, totalScale);
                context.getMatrices().pop();
            }
        }
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
        if (distance > MAX_FADE_DISTANCE) return MIN_ALPHA;
        if (distance < FADE_START_DISTANCE) return 1f;
        return 1f - ((float) (distance - FADE_START_DISTANCE) / (MAX_FADE_DISTANCE - FADE_START_DISTANCE)) * (1 - MIN_ALPHA);
    }

    private static int generateRandomColor() {
        return (255 << 24) | ((RANDOM.nextInt(150) + 50) << 16 | ((RANDOM.nextInt(150) + 50) << 8 | (RANDOM.nextInt(150) + 50)));
    }

    static void renderIcon(DrawContext context, int x, int y, int[] colorPalette, float alpha) {
        context.getMatrices().push();
        RenderSystem.enableBlend();
        colorsPalette(context, x, y, colorPalette, alpha);
        RenderSystem.disableBlend();
        context.getMatrices().pop();
    }

    private static void colorsPalette(DrawContext context, int x, int y, int[] colorPalette, float alpha) {
        int color = colorPalette[0];
        RenderSystem.setShaderColor(((color >> 16) & 0xFF) / 255.0f, ((color >> 8) & 0xFF) / 255.0f, (color & 0xFF) / 255.0f, alpha);
        context.drawTexture(ICON_TEXTURE, x, y, ICON_SIZE, ICON_SIZE, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    static void renderArrowUp(DrawContext context, int x, int y, float alpha, double heightDifference) {
        context.getMatrices().push();
        arrowAlpha(alpha, heightDifference);
        context.drawTexture(ARROW_UP_TEXTURE, x, y - ICON_SIZE + 1, ARROW_WIDTH, ARROW_HEIGHT, 0, 0, ARROW_WIDTH, ARROW_HEIGHT, ARROW_WIDTH, ARROW_HEIGHT);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
        context.getMatrices().pop();
    }

    private static void renderArrowDown(DrawContext context, int x, int y, float alpha, double heightDifference) {
        context.getMatrices().push();
        int arrowY = y + ICON_SIZE + 1;
        arrowAlpha(alpha, heightDifference);
        context.drawTexture(ARROW_DOWN_TEXTURE, x, arrowY, ARROW_WIDTH, ARROW_HEIGHT, 0, 0, ARROW_WIDTH, ARROW_HEIGHT, ARROW_WIDTH, ARROW_HEIGHT);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
        context.getMatrices().pop();
    }

    private static void arrowAlpha(float alpha, double heightDifference) {
        float arrowAlpha = MathHelper.lerp((float) MathHelper.clamp((Math.abs(heightDifference) - 4) / 196, 0, 1), 1.0f, 0.1f);
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha * arrowAlpha);
    }

    static void renderPlayerName(DrawContext context, MinecraftClient client, PositionUpdatePayload.PlayerPosition pos, int x, int y, float alpha, int translateZ) {
        PlayerEntity player = client.world != null ? client.world.getPlayerByUuid(pos.uuid()) : null;
        if (player != null) {
            String name = player.getName().getString();
            int textPadding = 3;
            int textWidth = client.textRenderer.getWidth(name) + textPadding * 2;
            int iconRelativeX = x - (client.getWindow().getScaledWidth() / 2 - BAR_WIDTH / 2);
            int adjustedX = getAdjustedX(x, iconRelativeX, textWidth);
            int[] colors = playerColors.get(pos.uuid());
            int playerColor = colors != null ? colors[0] : 0xFFFFFF;

            float scale = 0.65f;
            int scaledTextWidth = (int) (textWidth * scale);
            int scaledFontHeight = (int) (client.textRenderer.fontHeight * scale);

            int padding = 1;
            float borderAlpha = 0.7f;
            float backgroundAlpha = 0.7f;

            int backgroundX = adjustedX + (textWidth - scaledTextWidth) / 2 + 3;
            int backgroundY = y + 1 + (client.textRenderer.fontHeight - scaledFontHeight) / 2;
            int textAlpha = (int) (alpha * 255) << 24;
            int allAlpha = (int) (ICON_OPACITY * textAlpha);
            int nameOffset = -4;

            if (shouldApplyArrowOffset(client)) {
                backgroundY += nameOffset;
            }

            float currentYOffset = playerNameOffsets.getOrDefault(pos.uuid(), (float) backgroundY);
            currentYOffset = MathHelper.lerp(LERP_SPEED, currentYOffset, backgroundY);
            playerNameOffsets.put(pos.uuid(), currentYOffset);

            context.getMatrices().push();
            context.getMatrices().translate(backgroundX, currentYOffset, translateZ);

            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1, 1, 1, ICON_OPACITY * backgroundAlpha);
            context.fill(0, -0, scaledTextWidth, scaledFontHeight, darkenColor(playerColor, 0.4f));
            drawRoundedBorder(context, -padding, -padding, scaledTextWidth + padding, scaledFontHeight + padding, darkenColor(playerColor, 0.6f));
            RenderSystem.setShaderColor(1, 1, 1, ICON_OPACITY * borderAlpha);
            drawSquaredBorder(context, 0, 0, scaledTextWidth, scaledFontHeight, darkenColor(playerColor, 0.5f));
            RenderSystem.setShaderColor(1, 1, 1, 1);
            context.getMatrices().scale(scale, scale, 1.0f);

            context.drawText(client.textRenderer, name, textPadding, 0, 0xFFFFFF | allAlpha, true);
            RenderSystem.disableBlend();
            context.getMatrices().pop();
        }
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

        context.fill(x1, y1 - 1, x2, y2, color);
        context.fill(x1, y2, x2, y2 + 1, color);
    }

    public static boolean shouldApplyArrowOffset(MinecraftClient client) {
        return client.player != null && Objects.requireNonNull(client.world).getPlayers().stream()
                .anyMatch(p -> Math.abs(p.getY() - client.player.getY()) > 4 && p.getY() - client.player.getY() > 0);
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

    private static void renderPlayerHead(DrawContext context, MinecraftClient client, UUID playerId, PositionUpdatePayload.PlayerPosition pos, int x, int y, float alpha) {
        Identifier skin = playerSkins.computeIfAbsent(playerId, id -> {
            assert MinecraftClient.getInstance().world != null;
            AbstractClientPlayerEntity p = (AbstractClientPlayerEntity) MinecraftClient.getInstance().world.getPlayerByUuid(id);
            return p != null ? p.getSkinTextures().texture() : Identifier.of("minecraft", "textures/entity/steve.png");
        });

        int[] colors = playerColors.get(pos.uuid());
        int playerColor = colors != null ? colors[0] : 0xFFFFFF;

        int iconRelativeX = x - (client.getWindow().getScaledWidth() / 2 - BAR_WIDTH / 2);

        context.getMatrices().push();
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        drawRoundedBorder(context, x + iconRelativeX, x + ICON_PRIMARY_SIZE, ICON_PRIMARY_SIZE * 2, ICON_PRIMARY_SIZE * 2, playerColor);
        context.drawTexture(skin, x + ICON_BORDER_SIZE, y + ICON_BORDER_SIZE, ICON_PRIMARY_SIZE, ICON_PRIMARY_SIZE, 8, 8, 8, 8, 64, 64);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
        context.getMatrices().pop();
    }

    private static void renderJoinAnimation(DrawContext context, UUID uuid, int x, int y, float alpha, float baseScale) {
        Long startTime = joinAnimations.get(uuid);
        if (startTime == null) return;

        float progress = (System.currentTimeMillis() - startTime) / 400.0f;
        if (progress > 2.0f) {
            joinAnimations.remove(uuid);
            return;
        }

        float cycleProgress = progress % 1.0f;
        float animScale = baseScale * (1.0f + cycleProgress * 0.5f);
        float animAlpha = (1.0f - cycleProgress) * 0.75f;

        context.getMatrices().push();
        context.getMatrices().translate(x + ICON_SIZE / 2f, y + ICON_SIZE / 2f, 0);
        context.getMatrices().scale(animScale, animScale, 1.0f);
        context.getMatrices().translate(-(x + ICON_SIZE / 2f), -(y + ICON_SIZE / 2f), 0);
        renderIcon(context, x, y, playerColors.get(uuid), animAlpha * alpha);
        context.getMatrices().pop();
    }
}