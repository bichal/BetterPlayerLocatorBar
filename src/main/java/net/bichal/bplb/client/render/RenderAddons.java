package net.bichal.bplb.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.bichal.bplb.config.Config;
import net.bichal.bplb.config.PlayerConfig;
import net.bichal.bplb.util.ColorUtils;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

import static net.bichal.bplb.util.ColorUtils.generateColorFromUUID;

public class RenderAddons {
    private static final Map<UUID, Identifier> playerSkins = new java.util.HashMap<>();

    private static void renderIcon(DrawContext context, float x, float y, int size, float alpha, String dotId, String borderStyle, String borderType, int color, int textureIndex, Config config) {
        Identifier dotTexture = TextureManager.getPlayerDotTexture(dotId, textureIndex);
        Identifier outlineTexture = TextureManager.getPlayerDotOutlineTexture(dotId, borderStyle, borderType, textureIndex);

        int borderColor = config.isInheritBorderColor() ? ColorUtils.darkerColoring(color) : 0xFF1A1A1A;
        RenderUtils.renderTintedTexture(context, outlineTexture, x, y, size, size, borderColor, alpha);
        RenderUtils.renderTintedTexture(context, dotTexture, x, y, size, size, color, alpha);
    }

    public static void renderPlayerIcon(DrawContext context, String playerName, UUID playerUuid, double distance, float x, float y, int size, float alpha, boolean showHead, Config config) {
        PlayerConfig appearance = config.getPlayerConfig(playerName);
        int textureIndex = getAdjustedTextureIndex(distance, config);

        String dotId = appearance != null && appearance.dotType != null ? appearance.dotType : config.getDotType();
        String borderStyle = appearance != null && appearance.iconBorderStyle != null ? appearance.iconBorderStyle : config.getIconBorderStyle();
        int color = appearance != null && appearance.color != null ? appearance.color : generateColorFromUUID(playerUuid);

        renderIcon(context, x, y, size, alpha, dotId, borderStyle, config.getIconBorderType(), color, textureIndex, config);

        if (showHead) {
            String textureOverride = appearance != null ? appearance.textureHeadOverride : null;
            renderPlayerHeadOverlay(context, playerUuid, x, y, size, alpha, textureOverride);
        }
    }

    private static void renderPlayerHeadOverlay(DrawContext context, UUID playerUuid, float x, float y, int size, float alpha, String textureOverride) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        Identifier skin;
        if (textureOverride != null && !textureOverride.isEmpty()) {
            UUID overrideUuid = findPlayerUUIDByName(textureOverride);
            if (overrideUuid != null) {
                skin = playerSkins.computeIfAbsent(overrideUuid, id -> {
                    AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) client.world.getPlayerByUuid(id);
                    return player != null ? player.getSkinTextures().texture() : Constants.STEVE_SKIN_TEXTURE;
                });
            } else {
                skin = Constants.STEVE_SKIN_TEXTURE;
            }
        } else {
            skin = playerSkins.computeIfAbsent(playerUuid, id -> {
                AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) client.world.getPlayerByUuid(id);
                return player != null ? player.getSkinTextures().texture() : Constants.STEVE_SKIN_TEXTURE;
            });
        }

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        int padding = 2;
        context.drawTexture(skin, (int) x + padding, (int) y + padding, size - padding * 2, size - padding * 2, 8, 8, 8, 8, 64, 64);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static void renderDeathMarker(DrawContext context, float x, float y, int size, float alpha, Config config) {
        String markerType = config.getDeathMarkerType();
        int color = config.getDeathMarkerColor();

        Identifier markerTexture = TextureManager.getDeathMarkerTexture(markerType);
        Identifier outlineTexture = TextureManager.getDeathMarkerOutlineTexture(markerType, config.getDeathMarkerBorderStyle(), config.getDeathMarkerBorderType());

        int borderColor = config.isDeathMarkerInheritBorderColor() ? ColorUtils.darkerColoring(color) : 0xFF1A1A1A;

        RenderUtils.renderTintedTexture(context, outlineTexture, x, y, size, size, borderColor, alpha);
        RenderUtils.renderTintedTexture(context, markerTexture, x, y, size, size, color, alpha);
    }

    public static void renderArrow(DrawContext context, String arrowType, boolean isUp, float x, float y, int size, float alpha) {
        Identifier arrowTexture = TextureManager.getArrowTexture(arrowType);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha * 0.6f);

        float u = isUp ? 0 : Constants.ICON_BASE_SIZE;
        int textureHeight = "mojang".equals(arrowType) ? Constants.ICON_BASE_SIZE * 2 : Constants.ICON_BASE_SIZE;

        context.drawTexture(arrowTexture, (int) x, (int) y, size, size, u, 0,
                Constants.ICON_BASE_SIZE, Constants.ICON_BASE_SIZE,
                Constants.ICON_BASE_SIZE * 2, textureHeight);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static void renderNameplate(DrawContext context, String text, String borderStyle, int color, float x, float y, float alpha, float scale) {
        MinecraftClient client = MinecraftClient.getInstance();
        int textWidth = client.textRenderer.getWidth(text);
        int boxWidth = (int) (textWidth * scale) + 4;
        int boxHeight = (int) (12 * scale);
        Identifier nameplateTexture = TextureManager.getNameplateTexture(borderStyle);
        int tintColor = ColorUtils.darkerColoring(color);
        context.getMatrices().translate(x, y, 0);
        float r = ((tintColor >> 16) & 0xFF) / 255.0f;
        float g = ((tintColor >> 8) & 0xFF) / 255.0f;
        float b = (tintColor & 0xFF) / 255.0f;
        RenderSystem.setShaderColor(r, g, b, alpha);
        RenderUtils.drawNineSlicedTexture(context, nameplateTexture, 0, 0, boxWidth, boxHeight);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        context.getMatrices().translate(boxWidth / 2f, boxHeight / 2f, 0);
        context.getMatrices().scale(scale, scale, 1.0f);
        context.drawText(client.textRenderer, text, -textWidth / 2, -client.textRenderer.fontHeight / 2, 0xFFFFFF, false);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static int getAdjustedTextureIndex(double distance, Config config) {
        int baseIndex = TextureManager.getTextureIndexFromDistance(distance);
        int configSize = config.getIconSize();
        int offset = 4 - configSize;
        return Math.min(3, Math.max(0, baseIndex + offset));
    }

    private static UUID findPlayerUUIDByName(String name) {
        return getUuid(name);
    }

    @Nullable
    public static UUID getUuid(String name) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null) {
            for (var player : client.world.getPlayers()) {
                if (player.getName().getString().equals(name)) {
                    return player.getUuid();
                }
            }
        }
        return null;
    }
}