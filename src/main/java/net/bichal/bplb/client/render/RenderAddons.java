package net.bichal.bplb.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.bichal.bplb.config.Config;
import net.bichal.bplb.util.ColorUtils;
import net.bichal.bplb.util.Constants;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.bichal.bplb.util.ColorUtils.generateColorFromUUID;

public class RenderAddons {
    private static final Map<String, TextureAnimator> textureAnimators = new HashMap<>();

    private static void renderIcon(DrawContext context, float x, float y, int size, float alpha, String dotId, String borderStyle, String borderType, int color, int textureIndex, Config config) {
        Identifier dotTexture = TextureManager.getPlayerDotTexture(dotId, textureIndex);
        Identifier outlineTexture = TextureManager.getPlayerDotOutlineTexture(dotId, borderStyle, borderType, textureIndex);
        int borderColor = config.isInheritBorderColor() ? ColorUtils.darkerColoring(color) : 0xFF000000;
        RenderUtils.renderTintedTexture(context, outlineTexture, x, y, size, size, borderColor, alpha);
        RenderUtils.renderTintedTexture(context, dotTexture, x, y, size, size, color, alpha);
    }

    public static void renderPlayerIcon(DrawContext context, String playerName, UUID playerUuid, double distance, float x, float y, int size, boolean showHead, Config config, float alpha) {
        Config.PlayerAppearance appearance = config.getPlayerConfig(playerName);
        int textureIndex = getAdjustedTextureIndex(distance, config);
        String dotId = appearance != null && appearance.dotType != null ? appearance.dotType : config.getDotType();
        String borderStyle = appearance != null && appearance.iconBorderStyle != null ? appearance.iconBorderStyle : config.getIconBorderStyle();
        int color = appearance != null && appearance.color != null ? appearance.color : generateColorFromUUID(playerUuid);
        renderIcon(context, x, y, size, alpha, dotId, borderStyle, config.getIconBorderType(), color, showHead ? 0 : textureIndex, config);
        String textureOverride = appearance != null ? appearance.textureHeadOverride : null;
        renderPlayerHeadOverlay(context, playerUuid, x, y, size, textureOverride, showHead ? 1 * alpha : 0);
    }

    private static void renderPlayerHeadOverlay(DrawContext context, UUID playerUuid, float x, float y, int size, String textureOverride, float alpha) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        Identifier skin;

        if (textureOverride != null && !textureOverride.isEmpty()) {
            UUID overrideUuid = getUuidFromCache(textureOverride);
            if (overrideUuid != null) {
                AbstractClientPlayerEntity overridePlayer = (AbstractClientPlayerEntity) client.world.getPlayerByUuid(overrideUuid);
                skin = overridePlayer != null ? overridePlayer.getSkinTextures().texture() : Constants.STEVE_SKIN_TEXTURE;
            } else {
                skin = Constants.STEVE_SKIN_TEXTURE;
            }
        } else {
            AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) client.world.getPlayerByUuid(playerUuid);
            skin = player != null ? player.getSkinTextures().texture() : Constants.STEVE_SKIN_TEXTURE;
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
        int borderColor = config.isDeathMarkerInheritBorderColor() ? ColorUtils.darkerColoring(color) : 0xFF000000;
        RenderUtils.renderTintedTexture(context, outlineTexture, x, y, size, size, borderColor, alpha);
        RenderUtils.renderTintedTexture(context, markerTexture, x, y, size, size, color, alpha);
    }

    public static void renderArrow(DrawContext context, String arrowType, boolean isUp, float x, float y, int size, float alpha) {
        TextureAnimator animator = textureAnimators.computeIfAbsent(arrowType + "_" + isUp, k -> new TextureAnimator(10, 4));
        Identifier arrowTexture = TextureManager.getArrowTexture(arrowType);
        context.getMatrices().push();
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        int frame = animator.getCurrentFrame();
        float u = isUp ? 0 : Constants.ICON_BASE_SIZE;
        float v = frame * Constants.ICON_BASE_SIZE;
        context.drawTexture(arrowTexture, (int) x, (int) y, size, size, u, v, Constants.ICON_BASE_SIZE, Constants.ICON_BASE_SIZE, Constants.ICON_BASE_SIZE * 2, Constants.ICON_BASE_SIZE * 2);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
        context.getMatrices().pop();
    }

    public static void renderNameplate(DrawContext context, String text, String borderStyle, int color, float x, float y, float alpha, float scale) {
        if (alpha <= 0.01f) return;
        MinecraftClient client = MinecraftClient.getInstance();
        int textWidth = client.textRenderer.getWidth(text);
        int boxWidth = (int) (textWidth * scale) + 4;
        int boxHeight = (int) (12 * scale);
        Identifier nameplateTexture = TextureManager.getNameplateTexture(borderStyle);
        int tintColor = ColorUtils.darkerColoring(color);
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        RenderUtils.setShaderColorRGBA(tintColor, alpha);
        RenderUtils.drawNineSlicedTexture(context, nameplateTexture, 0, 0, boxWidth, boxHeight);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        context.getMatrices().translate(boxWidth / 2f, boxHeight / 2f, 0);
        context.getMatrices().scale(scale, scale, 1.0f);
        int textColor = Constants.WHITE_COLOR | ((int) (alpha * 255) << 24);
        context.drawText(client.textRenderer, text, -textWidth / 2, -client.textRenderer.fontHeight / 2, textColor, true);
        context.getMatrices().pop();
    }

    private static int getAdjustedTextureIndex(double distance, Config config) {
        int baseIndex = TextureManager.getTextureIndexFromDistance(distance);
        int configSize = config.getIconSize();
        int offset = 4 - configSize;
        return Math.min(3, Math.max(0, baseIndex + offset));
    }

    @Nullable
    public static UUID getUuidFromCache(String nameOrUuid) {
        try {
            return UUID.fromString(nameOrUuid);
        } catch (IllegalArgumentException ignored) {
        }

        UUID directUuid = getUuid(nameOrUuid);
        if (directUuid != null) return directUuid;

        try {
            File cacheFile = FabricLoader.getInstance().getGameDir().resolve("usercache.json").toFile();
            if (!cacheFile.exists()) return null;

            String json = new String(java.nio.file.Files.readAllBytes(cacheFile.toPath()));
            com.google.gson.JsonArray array = com.google.gson.JsonParser.parseString(json).getAsJsonArray();

            for (com.google.gson.JsonElement element : array) {
                com.google.gson.JsonObject obj = element.getAsJsonObject();
                if (obj.has("name") && obj.get("name").getAsString().equalsIgnoreCase(nameOrUuid)) {
                    return UUID.fromString(obj.get("uuid").getAsString());
                }
            }
        } catch (Exception e) {
            Constants.LOGGER.warn("Could not read usercache.json", e);
        }
        return null;
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
