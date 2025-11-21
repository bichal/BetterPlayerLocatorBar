package net.bichal.bplb.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.bichal.bichalutils.client.render.RenderUtil;
import net.bichal.bichalutils.util.ColorUtil;
import net.bichal.bichalutils.util.Logger;
import net.bichal.bplb.client.Hud;
import net.bichal.bplb.gui.Config;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class RenderAddons {

    public static void renderPlayerIcon(DrawContext context, String playerName, UUID playerUuid, double distance, float x, float y, boolean showHead, Config config, float alpha) {
        renderPlayerIcon(context, playerName, playerUuid, distance, x, y, showHead, config, alpha, null);
    }

    public static void renderPlayerIcon(DrawContext context, String playerName, UUID playerUuid, double distance, float x, float y, boolean showHead, Config config) {
        renderPlayerIcon(context, playerName, playerUuid, distance, x, y, showHead, config, 1.0f, null);
    }

    @SuppressWarnings("unused") // Future Implementation
    public static void renderPlayerIcon(DrawContext context, String playerName, UUID playerUuid, double distance, float x, float y, boolean showHead, Config config, String textureOverride) {
        renderPlayerIcon(context, playerName, playerUuid, distance, x, y, showHead, config, 1.0f, textureOverride);
    }

    public static void renderPlayerIcon(DrawContext context, String playerName, UUID playerUuid, double distance, float x, float y, boolean showHead, Config config, float alpha, String textureOverride) {
        Config.PlayerAppearance appearance = config.getPlayerConfig(playerName);
        int textureIndex = getAdjustedTextureIndex(distance, config);
        String dotId = appearance != null && appearance.dotType != null ? appearance.dotType : config.getDotType();
        String borderStyle = appearance != null && appearance.iconBorderStyle != null ? appearance.iconBorderStyle : config.getIconBorderStyle();
        int color = appearance != null && appearance.color != null ? appearance.color : ColorUtil.generateColorFromUUID(playerUuid);
        renderIcon(context, x, y, alpha, dotId, borderStyle, config.getIconBorderType(), color, showHead ? 0 : textureIndex, config);
        if (showHead) {
            String skinOverride = textureOverride != null ? textureOverride : (appearance != null ? appearance.textureHeadOverride : null);
            renderPlayerHeadOverlay(context, playerUuid, x, y, skinOverride, alpha);
        }
    }

    private static void renderIcon(DrawContext context, float x, float y, float alpha, String dotId, String borderStyle, String borderType, int color, int textureIndex, Config config) {
        Identifier dotTexture = TextureManager.getPlayerDotTexture(dotId, textureIndex);
        Identifier outlineTexture = TextureManager.getPlayerDotOutlineTexture(dotId, borderStyle, borderType, textureIndex);
        int borderColor = config.isInheritBorderColor() ? ColorUtil.darkerColoring(color) : Constants.BLACK_BORDER_COLOR;

        RenderUtil.renderTintedTexture(context, outlineTexture, x, y, Constants.ICON_BASE_SIZE, Constants.ICON_BASE_SIZE, borderColor, alpha, Constants.ICON_BASE_SIZE, Constants.ICON_BASE_SIZE);
        RenderUtil.renderTintedTexture(context, dotTexture, x, y, Constants.ICON_BASE_SIZE, Constants.ICON_BASE_SIZE, color, alpha, Constants.ICON_BASE_SIZE, Constants.ICON_BASE_SIZE);
    }

    private static void renderPlayerHeadOverlay(DrawContext context, UUID playerUuid, float x, float y, String textureOverride, float alpha) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (alpha <= 0.01f || client.world == null || playerUuid == null) return;

        Identifier skin = Constants.STEVE_SKIN_TEXTURE;
        try {
            UUID targetUuid = textureOverride != null && !textureOverride.isEmpty()
                    ? getUuidFromCache(textureOverride)
                    : playerUuid;

            if (targetUuid != null) {
                var player = (net.minecraft.client.network.AbstractClientPlayerEntity) client.world.getPlayerByUuid(targetUuid);
                if (player != null && player.getSkinTextures() != null) {
                    skin = player.getSkinTextures().texture();
                }
            }
        } catch (Exception e) {
            Logger.debug("Error loading skin texture", e);
        }

        Hud.trySetShaderColor(context, (int) x, (int) y, alpha, skin);
    }

    public static void renderDeathMarker(DrawContext context, float x, float y, float alpha, Config config) {
        String markerType = config.getDeathMarkerType();
        int color = config.getDeathMarkerColor();
        Identifier markerTexture = TextureManager.getDeathMarkerTexture(markerType);
        Identifier outlineTexture = TextureManager.getDeathMarkerOutlineTexture(markerType, config.getDeathMarkerBorderStyle(), config.getDeathMarkerBorderType());
        int borderColor = config.isDeathMarkerInheritBorderColor() ? ColorUtil.darkerColoring(color) : Constants.BLACK_BORDER_COLOR;

        try {
            RenderUtil.renderTintedTexture(context, outlineTexture, x, y, Constants.ICON_BASE_SIZE, Constants.ICON_BASE_SIZE, borderColor, alpha, Constants.ICON_BASE_SIZE, Constants.ICON_BASE_SIZE);
            RenderUtil.renderTintedTexture(context, markerTexture, x, y, Constants.ICON_BASE_SIZE, Constants.ICON_BASE_SIZE, color, alpha, Constants.ICON_BASE_SIZE, Constants.ICON_BASE_SIZE);
        } finally {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    public static void renderDeathMarker(DrawContext context, float x, float y, Config config) {
        renderDeathMarker(context, x, y, 1.0f, config);
    }

    public static void renderArrow(DrawContext context, String arrowType, boolean isUp, float x, float y, float alpha/*, AtlasAnimator animator*/) {
        if (alpha <= 0.01f) return;

        Identifier arrowTexture = TextureManager.getArrowTexture(arrowType);
        context.getMatrices().push();
        try {
            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
//            int frame = animator.getCurrentFrame();
            float u = isUp ? 0 : Constants.ICON_BASE_SIZE;
            float v = 1/*frame * Constants.ICON_BASE_SIZE*/;
            context.drawTexture(arrowTexture, (int) x, (int) y, Constants.ARROW_BASE_SIZE_WIDTH, Constants.ARROW_BASE_SIZE_HEIGHT, u, v, Constants.ICON_BASE_SIZE, Constants.ICON_BASE_SIZE, Constants.ICON_BASE_SIZE * 2, Constants.ICON_BASE_SIZE * 2);
        } finally {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.disableBlend();
            context.getMatrices().pop();
        }
    }

    public static void renderArrow(DrawContext context, String arrowType, boolean isUp, float x, float y/*, AtlasAnimator animator*/) {
        renderArrow(context, arrowType, isUp, x, y, 1.0f/*, animator*/);
    }

    public static void renderNameplate(DrawContext context, String text, String borderStyle, int color, float x, float y, float alpha, float scale) {
        if (alpha <= 0.01f) return;

        MinecraftClient client = MinecraftClient.getInstance();
        int textWidth = client.textRenderer.getWidth(text);
        int boxWidth = (int) (textWidth * scale) + 4;
        int boxHeight = (int) (12 * scale);
        Identifier nameplateTexture = TextureManager.getNameplateTexture(borderStyle);
        int tintColor = ColorUtil.darkerColoring(color);

        RenderUtil.withMatrixPush(context, x, y, () -> {
            RenderUtil.setShaderColorRGBA(tintColor, alpha);
            RenderUtil.drawNineSlicedTexture(context, nameplateTexture, 0, 0, boxWidth, boxHeight, 0, boxWidth, boxHeight);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            context.getMatrices().translate(boxWidth / 2f, boxHeight / 2f, 1);
            context.getMatrices().scale(scale, scale, 1.0f);
            int textColor = Constants.WHITE_COLOR | ((int) (alpha * 255) << 24);
            context.drawText(client.textRenderer, text, -textWidth / 2, -client.textRenderer.fontHeight / 2, textColor, true);
        });
    }

    public static void renderLodestoneMarker(DrawContext context, float x, float y, float alpha, Config config, double distance) {
        String markerType = config.getLodestoneMarkerType();
        int textureIndex = getAdjustedTextureIndex(distance, config);
        Identifier markerTexture = TextureManager.getPlayerDotTexture(markerType, textureIndex);
        Identifier outlineTexture = TextureManager.getPlayerDotOutlineTexture(markerType, config.getLodestoneMarkerBorderStyle(), config.getLodestoneMarkerBorderType(), textureIndex);
        int color = ColorUtil.generateColorFromUUID(UUID.randomUUID());
        int borderColor = config.isLodestoneMarkerInheritBorderColor() ? ColorUtil.darkerColoring(color) : Constants.BLACK_BORDER_COLOR;

        try {
            RenderUtil.renderTintedTexture(context, outlineTexture, x, y, Constants.ICON_BASE_SIZE, Constants.ICON_BASE_SIZE, borderColor, alpha, Constants.ICON_BASE_SIZE, Constants.ICON_BASE_SIZE);
            RenderUtil.renderTintedTexture(context, markerTexture, x, y, Constants.ICON_BASE_SIZE, Constants.ICON_BASE_SIZE, color, alpha, Constants.ICON_BASE_SIZE, Constants.ICON_BASE_SIZE);
        } finally {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    private static int getAdjustedTextureIndex(double distance, Config config) {
        int baseIndex = TextureManager.getTextureIndexFromDistance(distance);
        int configSize = config.getIconSize();
        int offset = 4 - configSize;
        return Math.min(3, Math.max(0, baseIndex + offset));
    }

    public static UUID getUuidFromCache(String nameOrUuid) {
        return RenderAddons.getUuid(nameOrUuid);
    }

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
