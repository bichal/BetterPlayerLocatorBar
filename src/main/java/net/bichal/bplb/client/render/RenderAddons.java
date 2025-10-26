package net.bichal.bplb.client.render;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.systems.RenderSystem;
import net.bichal.bplb.config.Config;
import net.bichal.bplb.util.ColorUtils;
import net.bichal.bplb.util.Constants;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Files;
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
        if (alpha <= 0.01f) return;
        if (client.world == null || playerUuid == null) return;
        Identifier skin = Constants.STEVE_SKIN_TEXTURE;
        try {
            if (textureOverride != null && !textureOverride.isEmpty()) {
                UUID overrideUuid = getUuidFromCache(textureOverride);
                if (overrideUuid != null) {
                    AbstractClientPlayerEntity overridePlayer = (AbstractClientPlayerEntity) client.world.getPlayerByUuid(overrideUuid);
                    if (overridePlayer != null && overridePlayer.getSkinTextures() != null)
                        skin = overridePlayer.getSkinTextures().texture();
                }
            } else {
                AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) client.world.getPlayerByUuid(playerUuid);
                if (player != null && player.getSkinTextures() != null) skin = player.getSkinTextures().texture();
            }
        } catch (Exception e) {
            Constants.LOGGER.debug("Error loading skin texture", e);
        }

        int padding = 2;
        int texSize = Math.max(1, size - padding * 2);
        context.drawTexture(RenderLayer::getGuiTextured, skin, (int) x + padding, (int) y + padding, 8, 8, texSize, texSize, 8, 8, 64, 64, 0xFFFFFF | ((int) (alpha * 255) << 24));
    }

    public static void renderDeathMarker(DrawContext context, float x, float y, int size, float alpha, Config config) {
        String markerType = config.getDeathMarkerType();
        int color = config.getDeathMarkerColor();
        Identifier markerTexture = TextureManager.getDeathMarkerTexture(markerType);
        Identifier outlineTexture = TextureManager.getDeathMarkerOutlineTexture(markerType, config.getDeathMarkerBorderStyle(), config.getDeathMarkerBorderType());
        int borderColor = config.isDeathMarkerInheritBorderColor() ? ColorUtils.darkerColoring(color) : 0xFF000000;
        try {
            RenderUtils.renderTintedTexture(context, outlineTexture, x, y, size, size, borderColor, alpha);
            RenderUtils.renderTintedTexture(context, markerTexture, x, y, size, size, color, alpha);
        } finally {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    public static void renderArrow(DrawContext context, String arrowType, boolean isUp, float x, float y, int size, float alpha) {
        if (alpha <= 0.01f) return;

        TextureAnimator animator = textureAnimators.computeIfAbsent(arrowType + "_" + isUp, k -> new TextureAnimator(10, 4));
        Identifier arrowTexture = TextureManager.getArrowTexture(arrowType);
        int frame = animator.getCurrentFrame();
        float u = isUp ? 0 : Constants.ICON_BASE_SIZE;
        float v = frame * Constants.ICON_BASE_SIZE;
        int alphaInt = (int) (alpha * 255);
        int whiteWithAlpha = (alphaInt << 24) | 0xFFFFFF;

        context.drawTexture(RenderLayer::getGuiTextured, arrowTexture, (int) x, (int) y, u, v, size, size, size, size, Constants.ICON_BASE_SIZE * 2, Constants.ICON_BASE_SIZE * 2, whiteWithAlpha);
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
        int alphaInt = (int) (alpha * 255);
        int finalColor = (alphaInt << 24) | (tintColor & 0xFFFFFF);

        RenderUtils.drawNineSlicedTexture(context, nameplateTexture, 0, 0, boxWidth, boxHeight, finalColor);
        context.getMatrices().translate(boxWidth / 2f, boxHeight / 2f, 1);
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
        if (nameOrUuid == null || nameOrUuid.isEmpty()) return null;
        try {
            return UUID.fromString(nameOrUuid);
        } catch (IllegalArgumentException ignored) {
        }
        UUID directUuid = getUuid(nameOrUuid);
        if (directUuid != null) return directUuid;
        try {
            File cacheFile = FabricLoader.getInstance().getGameDir().resolve("usercache.json").toFile();
            if (!cacheFile.exists()) return null;
            byte[] data = Files.readAllBytes(cacheFile.toPath());
            if (data.length == 0) return null;
            String json = new String(data);
            if (json.trim().isEmpty()) return null;
            JsonElement element = com.google.gson.JsonParser.parseString(json);
            if (!element.isJsonArray()) return null;
            JsonArray array = element.getAsJsonArray();
            for (JsonElement entry : array) {
                if (!entry.isJsonObject()) continue;
                com.google.gson.JsonObject obj = entry.getAsJsonObject();
                if (!obj.has("name") || !obj.has("uuid")) continue;
                JsonElement nameEl = obj.get("name");
                JsonElement uuidEl = obj.get("uuid");
                if (nameEl == null || uuidEl == null || !nameEl.isJsonPrimitive() || !uuidEl.isJsonPrimitive())
                    continue;
                String objName = nameEl.getAsString();
                if (objName != null && objName.equalsIgnoreCase(nameOrUuid)) {
                    try {
                        return UUID.fromString(uuidEl.getAsString());
                    } catch (Exception e) {
                        Constants.LOGGER.debug("Invalid UUID entry in usercache.json");
                    }
                }
            }
        } catch (Exception e) {
            Constants.LOGGER.debug("Failed to read usercache.json", e);
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
