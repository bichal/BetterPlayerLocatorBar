package net.bichal.bplb.client.render;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.bichal.bplb.util.ColorUtils.generateColorFromUUID;

/**
 * Render addon utilities for BetterPlayerLocatorBar.
 * Handles rendering of player icons, death markers, arrows, and nameplates.
 * Updated for Minecraft 1.21.6 API.
 */
public final class RenderAddons {
    /** Texture animator cache for animated elements */
    private static final Map<String, TextureAnimator> textureAnimators = new HashMap<>();

    /**
     * Renders a player icon with border and dot.
     * 
     * @param context       The draw context
     * @param x            X position
     * @param y            Y position
     * @param size         Icon size
     * @param alpha        Transparency (0.0 - 1.0)
     * @param dotId        Type of dot texture
     * @param borderStyle  Style of border (rounded, squared)
     * @param borderType   Type of border (default, minimal)
     * @param color        Main color
     * @param textureIndex Index for distance-based textures
     * @param config       Configuration instance
     * @param zOffset      Z-offset for layering
     */
    private static void renderIcon(
            DrawContext context, float x, float y, int size, float alpha,
            String dotId, String borderStyle, String borderType, int color, 
            int textureIndex, Config config, int zOffset) {
        Identifier dotTexture = TextureManager.getPlayerDotTexture(dotId, textureIndex);
        Identifier outlineTexture = TextureManager.getPlayerDotOutlineTexture(
            dotId, borderStyle, borderType, textureIndex
        );
        
        int borderColor = config.isInheritBorderColor() 
            ? ColorUtils.darkerColoring(color) 
            : 0xFF000000;
            
        RenderUtils.renderTintedTextureZ(context, outlineTexture, x, y, size, size, borderColor, alpha, zOffset);
        RenderUtils.renderTintedTextureZ(context, dotTexture, x, y, size, size, color, alpha, zOffset + 1);
    }

    /**
     * Renders a player icon with optional head overlay.
     * 
     * @param context     The draw context
     * @param playerName  Player's name
     * @param playerUuid  Player's UUID
     * @param distance    Distance to player
     * @param x           X position
     * @param y           Y position
     * @param size        Icon size
     * @param showHead    Whether to show player head
     * @param config      Configuration instance
     * @param alpha       Transparency (0.0 - 1.0)
     * @param zOffset     Z-offset for layering
     */
    public static void renderPlayerIcon(
            DrawContext context, String playerName, UUID playerUuid, double distance,
            float x, float y, int size, boolean showHead, Config config, float alpha, int zOffset) {
        Config.PlayerAppearance appearance = config.getPlayerConfig(playerName);
        int textureIndex = getAdjustedTextureIndex(distance, config);
        
        String dotId = (appearance != null && appearance.dotType != null) 
            ? appearance.dotType 
            : config.getDotType();
        String borderStyle = (appearance != null && appearance.iconBorderStyle != null) 
            ? appearance.iconBorderStyle 
            : config.getIconBorderStyle();
        int color = (appearance != null && appearance.color != null) 
            ? appearance.color 
            : generateColorFromUUID(playerUuid);
            
        renderIcon(
            context, x, y, size, alpha, dotId, borderStyle, 
            config.getIconBorderType(), color, 
            showHead ? 0 : textureIndex, config, zOffset
        );
        
        String textureOverride = appearance != null ? appearance.textureHeadOverride : null;
        renderPlayerHeadOverlay(
            context, playerUuid, x, y, size, textureOverride, 
            showHead ? 1 * alpha : 0, zOffset + 2
        );
    }

    /**
     * Renders a player head overlay on the icon.
     * 
     * @param context           The draw context
     * @param playerUuid        Player's UUID
     * @param x                X position
     * @param y                Y position
     * @param size             Icon size
     * @param textureOverride   Override texture identifier
     * @param alpha            Transparency (0.0 - 1.0)
     * @param zOffset          Z-offset for layering
     */
    private static void renderPlayerHeadOverlay(
            DrawContext context, UUID playerUuid, float x, float y, int size,
            String textureOverride, float alpha, int zOffset) {
        MinecraftClient client = MinecraftClient.getInstance();
        
        // Skip if not visible
        if (alpha <= 0.01f) return;
        if (client.world == null || playerUuid == null) return;
        
        Identifier skin = Constants.STEVE_SKIN_TEXTURE;
        
        try {
            if (textureOverride != null && !textureOverride.isEmpty()) {
                UUID overrideUuid = getUuidFromCache(textureOverride);
                if (overrideUuid != null) {
                    AbstractClientPlayerEntity overridePlayer = 
                        (AbstractClientPlayerEntity) client.world.getPlayerByUuid(overrideUuid);
                    if (overridePlayer != null && overridePlayer.getSkinTextures() != null) {
                        skin = overridePlayer.getSkinTextures().texture();
                    }
                }
            } else {
                AbstractClientPlayerEntity player = 
                    (AbstractClientPlayerEntity) client.world.getPlayerByUuid(playerUuid);
                if (player != null && player.getSkinTextures() != null) {
                    skin = player.getSkinTextures().texture();
                }
            }
        } catch (Exception e) {
            Constants.LOGGER.debug("Error loading skin texture", e);
        }

        int padding = 2;
        int texSize = Math.max(1, size - padding * 2);
        int colorWithAlpha = 0xFFFFFF | ((int) (alpha * 255) << 24);

        RenderUtils.renderTextureDirectZ(context, skin,
            (int) x + padding, (int) y + padding,
            8, 8,
            texSize, texSize,
            8, 8,
            64, 64,
            colorWithAlpha, zOffset);
    }

    /**
     * Renders a death marker icon.
     * 
     * @param context  The draw context
     * @param x        X position
     * @param y        Y position
     * @param size     Icon size
     * @param alpha    Transparency (0.0 - 1.0)
     * @param config   Configuration instance
     * @param zOffset  Z-offset for layering
     */
    public static void renderDeathMarker(
            DrawContext context, float x, float y, int size, float alpha, Config config, int zOffset) {
        String markerType = config.getDeathMarkerType();
        int color = config.getDeathMarkerColor();
        
        Identifier markerTexture = TextureManager.getDeathMarkerTexture(markerType);
        Identifier outlineTexture = TextureManager.getDeathMarkerOutlineTexture(
            markerType, config.getDeathMarkerBorderStyle(), config.getDeathMarkerBorderType()
        );
        
        int borderColor = config.isDeathMarkerInheritBorderColor() 
            ? ColorUtils.darkerColoring(color) 
            : 0xFF000000;
            
        RenderUtils.renderTintedTextureZ(context, outlineTexture, x, y, size, size, borderColor, alpha, zOffset);
        RenderUtils.renderTintedTextureZ(context, markerTexture, x, y, size, size, color, alpha, zOffset + 1);
    }

    /**
     * Renders a height difference arrow.
     * 
     * @param context    The draw context
     * @param arrowType  Type of arrow texture
     * @param isUp       Whether arrow points up
     * @param x          X position
     * @param y          Y position
     * @param size       Arrow size
     * @param alpha      Transparency (0.0 - 1.0)
     * @param zOffset    Z-offset for layering
     */
    public static void renderArrow(
            DrawContext context, String arrowType, boolean isUp, 
            float x, float y, int size, float alpha, int zOffset) {
        if (alpha <= 0.01f) return;

        // Get or create animator for this arrow type
        TextureAnimator animator = textureAnimators.computeIfAbsent(
            arrowType + "_" + isUp, k -> new TextureAnimator(10, 4)
        );
        
        Identifier arrowTexture = TextureManager.getArrowTexture(arrowType);
        int frame = animator.getCurrentFrame();
        
        float u = isUp ? 0 : Constants.ICON_BASE_SIZE;
        float v = frame * Constants.ICON_BASE_SIZE;
        int alphaInt = (int) (alpha * 255);
        int whiteWithAlpha = (alphaInt << 24) | 0xFFFFFF;

        RenderUtils.renderTextureDirectZ(context, arrowTexture,
            (int) x, (int) y,
            u, v,
            size, size,
            Constants.ICON_BASE_SIZE, Constants.ICON_BASE_SIZE,
            Constants.ICON_BASE_SIZE * 2, Constants.ICON_BASE_SIZE * 2,
            whiteWithAlpha, zOffset);
    }

    /**
     * Renders a player nameplate.
     * Updated for Minecraft 1.21.6 API.
     * 
     * @param context     The draw context
     * @param text        Name text
     * @param borderStyle Style of border
     * @param color       Text color
     * @param x           X position
     * @param y           Y position
     * @param alpha       Transparency (0.0 - 1.0)
     * @param scale       Text scale
     * @param zOffset     Z-offset for layering
     */
    public static void renderNameplate(
            DrawContext context, String text, String borderStyle, int color,
            float x, float y, float alpha, float scale, int zOffset) {
        if (alpha <= 0.01f) return;

        MinecraftClient client = MinecraftClient.getInstance();
        int textWidth = client.textRenderer.getWidth(text);
        int boxWidth = (int) (textWidth * scale) + 4;
        int boxHeight = (int) (12 * scale);
        
        Identifier nameplateTexture = TextureManager.getNameplateTexture(borderStyle);
        int tintColor = ColorUtils.darkerColoring(color);
        int alphaInt = (int) (alpha * 255);
        int finalColor = (alphaInt << 24) | (tintColor & 0xFFFFFF);
        
        // Draw nine-sliced background with z-offset
        RenderUtils.drawNineSlicedTextureZ(
            context, nameplateTexture, (int) x, (int) y, boxWidth, boxHeight, finalColor, zOffset
        );
        
        // Draw centered and scaled text directly with calculated position
        int textColor = Constants.WHITE_COLOR | ((int) (alpha * 255) << 24);
        float scaledTextX = x + boxWidth / 2f - (textWidth * scale) / 2f;
        float scaledTextY = y + boxHeight / 2f - (client.textRenderer.fontHeight * scale) / 2f;
        
        context.drawText(
            client.textRenderer, text,
            (int) scaledTextX, (int) scaledTextY,
            textColor, true
        );
    }

    /**
     * Calculates adjusted texture index based on distance.
     * 
     * @param distance Current distance to target
     * @param config   Configuration instance
     * @return Texture index (0-3)
     */
    private static int getAdjustedTextureIndex(double distance, Config config) {
        int baseIndex = TextureManager.getTextureIndexFromDistance(distance);
        int configSize = config.getIconSize();
        int offset = 4 - configSize;
        return Math.min(3, Math.max(0, baseIndex + offset));
    }

    /**
     * Gets UUID from cache file or player name.
     * 
     * @param nameOrUuid Player name or UUID string
     * @return UUID if found, null otherwise
     */
    @Nullable
    public static UUID getUuidFromCache(String nameOrUuid) {
        if (nameOrUuid == null || nameOrUuid.isEmpty()) return null;
        
        // Try parsing as UUID first
        try {
            return UUID.fromString(nameOrUuid);
        } catch (IllegalArgumentException ignored) {
            // Not a UUID string, continue
        }
        
        // Check online players
        UUID directUuid = getUuid(nameOrUuid);
        if (directUuid != null) return directUuid;
        
        // Check usercache.json
        try {
            File cacheFile = FabricLoader.getInstance()
                .getGameDir()
                .resolve("usercache.json")
                .toFile();
                
            if (!cacheFile.exists()) return null;
            
            byte[] data = Files.readAllBytes(cacheFile.toPath());
            if (data.length == 0) return null;
            
            String json = new String(data).trim();
            if (json.isEmpty()) return null;
            
            JsonElement element = com.google.gson.JsonParser.parseString(json);
            if (!element.isJsonArray()) return null;
            
            JsonArray array = element.getAsJsonArray();
            for (JsonElement entry : array) {
                if (!entry.isJsonObject()) continue;
                
                JsonElement nameEl = entry.getAsJsonObject().get("name");
                JsonElement uuidEl = entry.getAsJsonObject().get("uuid");
                
                if (nameEl == null || uuidEl == null) continue;
                
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

    /**
     * Gets UUID from online players by name.
     * 
     * @param name Player name
     * @return UUID if player is online, null otherwise
     */
    @Nullable
    public static UUID getUuid(String name) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null) {
            for (var player : client.world.getPlayers()) {
                if (player.getName().getString().equalsIgnoreCase(name)) {
                    return player.getUuid();
                }
            }
        }
        return null;
    }
}
