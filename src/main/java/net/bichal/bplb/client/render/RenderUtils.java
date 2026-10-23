package net.bichal.bplb.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.awt.Dimension;

/**
 * Texture drawing in one place, because the GuiGraphics blit API changed in 1.21.2.
 */
public class RenderUtils {
    public static Minecraft getClient() {
        return Minecraft.getInstance();
    }

    /**
     * Draws the region {@code (u, v, regionWidth, regionHeight)} of a {@code textureWidth x textureHeight}
     * texture into the box {@code (x, y, width, height)}, tinted with {@code color} (RGB) and {@code alpha}.
     */
    public static void renderTexture(GuiGraphics context, ResourceLocation texture, int x, int y, int width, int height, float u, float v, int regionWidth, int regionHeight, int textureWidth, int textureHeight, int color, float alpha) {
        //? if >=1.21.6 {
        /*int argb = (Math.round(Math.max(0f, Math.min(1f, alpha)) * 255f) << 24) | (color & 0xFFFFFF);
        context.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, regionWidth, regionHeight, textureWidth, textureHeight, argb);
        *///?} else if >=1.21.2 {
        /*int argb = (Math.round(Math.max(0f, Math.min(1f, alpha)) * 255f) << 24) | (color & 0xFFFFFF);
        context.blit(net.minecraft.client.renderer.RenderType::guiTextured, texture, x, y, u, v, width, height, regionWidth, regionHeight, textureWidth, textureHeight, argb);
        *///?} else {
        setShaderColorRGBA(color, alpha);
        context.blit(texture, x, y, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight);
        GuiCompat.resetShaderColor();
        //?}
    }

    public static void renderTintedTexture(GuiGraphics context, ResourceLocation texture, float x, float y, float width, float height, int color, float alpha) {
        Dimension dim = AssetScanner.getTextureDimensions(texture);
        renderTexture(context, texture, (int) x, (int) y, (int) width, (int) height, 0f, 0f, dim.width, dim.height, dim.width, dim.height, color, alpha);
    }

    public static void drawNineSlicedTexture(GuiGraphics context, ResourceLocation texture, int x, int y, int width, int height, int tint, float alpha) {
        int corner = 2;
        int textureWidth = 12;
        int textureHeight = 12;

        float u2 = textureWidth - corner;
        float v2 = textureHeight - corner;

        int middleWidth = textureWidth - corner * 2;
        int middleHeight = textureHeight - corner * 2;
        int destMiddleWidth = width - corner * 2;
        int destMiddleHeight = height - corner * 2;

        renderTexture(context, texture, x, y, corner, corner, 0, 0, corner, corner, textureWidth, textureHeight, tint, alpha);
        renderTexture(context, texture, x + width - corner, y, corner, corner, u2, 0, corner, corner, textureWidth, textureHeight, tint, alpha);
        renderTexture(context, texture, x, y + height - corner, corner, corner, 0, v2, corner, corner, textureWidth, textureHeight, tint, alpha);
        renderTexture(context, texture, x + width - corner, y + height - corner, corner, corner, u2, v2, corner, corner, textureWidth, textureHeight, tint, alpha);

        renderTexture(context, texture, x + corner, y, destMiddleWidth, corner, corner, 0, middleWidth, corner, textureWidth, textureHeight, tint, alpha);
        renderTexture(context, texture, x + corner, y + height - corner, destMiddleWidth, corner, corner, v2, middleWidth, corner, textureWidth, textureHeight, tint, alpha);
        renderTexture(context, texture, x, y + corner, corner, destMiddleHeight, 0, corner, corner, middleHeight, textureWidth, textureHeight, tint, alpha);
        renderTexture(context, texture, x + width - corner, y + corner, corner, destMiddleHeight, u2, corner, corner, middleHeight, textureWidth, textureHeight, tint, alpha);
        renderTexture(context, texture, x + corner, y + corner, destMiddleWidth, destMiddleHeight, corner, corner, middleWidth, middleHeight, textureWidth, textureHeight, tint, alpha);
    }

    public static void withMatrixPush(GuiGraphics context, float x, float y, Runnable action) {
        GuiCompat.push(context);
        GuiCompat.translate(context, x, y, 0);
        try {
            action.run();
        } finally {
            GuiCompat.pop(context);
        }
    }

    public static void setShaderColorRGBA(int color, float alpha) {
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        GuiCompat.setShaderColor(r, g, b, alpha);
    }
}
