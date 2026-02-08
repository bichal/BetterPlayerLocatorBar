package net.bichal.bplb.client.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gl.RenderPipelines;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class RenderUtils {
    private static MinecraftClient clientInstance;
    
    /**
     * Gets the Minecraft client instance (singleton pattern for efficiency).
     */
    public static MinecraftClient getClient() {
        if (clientInstance == null) {
            clientInstance = MinecraftClient.getInstance();
        }
        return clientInstance;
    }
    
    private static RenderPipeline getGuiTexturedPipeline() {
        return RenderPipelines.GUI_TEXTURED;
    }
    
    /**
     * Renders a tinted texture with the specified color and alpha.
     * Updated for Minecraft 1.21.6 API using Fabric API DrawContextHelper.
     * 
     * @param context  The draw context
     * @param texture  The texture identifier
     * @param x        X position
     * @param y        Y position
     * @param width    Width of the texture area
     * @param height   Height of the texture area
     * @param color    Base color (RGB)
     * @param alpha    Alpha transparency (0.0 - 1.0)
     */
    public static void renderTintedTexture(DrawContext context, Identifier texture, float x, float y, float width, float height, int color, float alpha) {
        if (alpha <= 0.0f) return;
        
        int finalColor = ((int) (alpha * 255) << 24) | (color & 0xFFFFFF);
        RenderPipeline pipeline = getGuiTexturedPipeline();
        var dim = AssetScanner.getTextureDimensions(texture);
        context.drawTexture(pipeline, texture, (int) x, (int) y, 0f, 0f, (int) width, (int) height, dim.width, dim.height, dim.width, dim.height, finalColor);
    }
    
    /**
     * Renders a tinted texture with Z-offset for layering.
     * Updated for Minecraft 1.21.6 API.
     * 
     * @param context  The draw context
     * @param texture  The texture identifier
     * @param x        X position
     * @param y        Y position
     * @param width    Width of the texture area
     * @param height   Height of the texture area
     * @param color    Base color (RGB)
     * @param alpha    Alpha transparency (0.0 - 1.0)
     * @param z        Z-offset for layering
     */
    public static void renderTintedTextureZ(DrawContext context, Identifier texture, float x, float y, float width, float height, int color, float alpha, int z) {
        if (alpha <= 0.0f) return;
        
        int finalColor = ((int) (alpha * 255) << 24) | (color & 0xFFFFFF);
        RenderPipeline pipeline = getGuiTexturedPipeline();
        var dim = AssetScanner.getTextureDimensions(texture);
        context.drawTexture(pipeline, texture, (int) x, (int) y, 0f, 0f, (int) width, (int) height, dim.width, dim.height, dim.width, dim.height, finalColor);
    }
    
    /**
     * Renders a texture with direct UV coordinates.
     * Updated for Minecraft 1.21.6 API.
     * 
     * @param context    The draw context
     * @param texture    The texture identifier
     * @param x          X position
     * @param y          Y position
     * @param u          U coordinate in texture
     * @param v          V coordinate in texture
     * @param width      Width of the texture area
     * @param height     Height of the texture area
     * @param texWidth   Texture width
     * @param texHeight  Texture height
     * @param color      Color with alpha
     */
    public static void renderTextureDirect(DrawContext context, Identifier texture, int x, int y, float u, float v, int width, int height, int texWidth, int texHeight, int color) {
        renderTextureDirect(context, texture, x, y, u, v, width, height, texWidth, texHeight, texWidth, texHeight, color);
    }

    public static void renderTextureDirect(DrawContext context, Identifier texture, int x, int y, float u, float v, int width, int height, int regionWidth, int regionHeight, int texWidth, int texHeight, int color) {
        RenderPipeline pipeline = getGuiTexturedPipeline();
        context.drawTexture(pipeline, texture, x, y, u, v, width, height, regionWidth, regionHeight, texWidth, texHeight, color);
    }
    
    /**
     * Renders a texture with direct UV coordinates and Z-offset.
     * Updated for Minecraft 1.21.6 API.
     * 
     * @param context    The draw context
     * @param texture    The texture identifier
     * @param x          X position
     * @param y          Y position
     * @param u          U coordinate in texture
     * @param v          V coordinate in texture
     * @param width      Width of the texture area
     * @param height     Height of the texture area
     * @param texWidth   Texture width
     * @param texHeight  Texture height
     * @param color      Color with alpha
     * @param z          Z-offset for layering
     */
    public static void renderTextureDirectZ(DrawContext context, Identifier texture, int x, int y, float u, float v, int width, int height, int texWidth, int texHeight, int color, int z) {
        renderTextureDirectZ(context, texture, x, y, u, v, width, height, texWidth, texHeight, texWidth, texHeight, color, z);
    }

    public static void renderTextureDirectZ(DrawContext context, Identifier texture, int x, int y, float u, float v, int width, int height, int regionWidth, int regionHeight, int texWidth, int texHeight, int color, int z) {
        RenderPipeline pipeline = getGuiTexturedPipeline();
        context.drawTexture(pipeline, texture, x, y, u, v, width, height, regionWidth, regionHeight, texWidth, texHeight, color);
    }
    
    /**
     * Draws a nine-sliced texture (texture with stretchable borders).
     * Uses 12x12 texture with 2px corners (standard Minecraft GUI style).
     * Updated for Minecraft 1.21.6 API.
     * 
     * @param context   The draw context
     * @param texture   The texture identifier
     * @param x         X position
     * @param y         Y position
     * @param width     Width of the area
     * @param height    Height of the area
     * @param color     Color with alpha
     */
    public static void drawNineSlicedTexture(DrawContext context, Identifier texture, int x, int y, int width, int height, int color) {
        drawNineSlicedTextureInternal(context, texture, x, y, width, height, color);
    }
    
    /**
     * Draws a nine-sliced texture with Z-offset.
     * Updated for Minecraft 1.21.6 API.
     * 
     * @param context   The draw context
     * @param texture   The texture identifier
     * @param x         X position
     * @param y         Y position
     * @param width     Width of the area
     * @param height    Height of the area
     * @param color     Color with alpha
     * @param z         Z-offset for layering
     */
    public static void drawNineSlicedTextureZ(DrawContext context, Identifier texture, int x, int y, int width, int height, int color, int z) {
        drawNineSlicedTextureInternal(context, texture, x, y, width, height, color);
    }
    
    /**
     * Internal method for nine-sliced texture rendering.
     * Uses 12x12 texture with 2px corners (standard Minecraft GUI style).
     * Updated for Minecraft 1.21.6 API using Fabric API DrawContextHelper.
     */
    private static void drawNineSlicedTextureInternal(DrawContext context, Identifier texture, int x, int y, int width, int height, int color) {
        int corner = 2;
        var dim = AssetScanner.getTextureDimensions(texture);
        int textureWidth = dim.width;
        int textureHeight = dim.height;
        
        RenderPipeline pipeline = getGuiTexturedPipeline();
        
        float u0 = 0f;
        float v0 = 0f;
        float u2 = textureWidth - corner;
        float v2 = textureHeight - corner;
        
        int middleWidth = textureWidth - corner * 2;
        int middleHeight = textureHeight - corner * 2;
        int destMiddleWidth = Math.max(0, width - corner * 2);
        int destMiddleHeight = Math.max(0, height - corner * 2);
        
        // Top-left corner
        context.drawTexture(pipeline, texture, x, y, u0, v0, corner, corner, textureWidth, textureHeight, color);
        // Top-right corner
        context.drawTexture(pipeline, texture, x + width - corner, y, u2, v0, corner, corner, textureWidth, textureHeight, color);
        // Bottom-left corner
        context.drawTexture(pipeline, texture, x, y + height - corner, u0, v2, corner, corner, textureWidth, textureHeight, color);
        // Bottom-right corner
        context.drawTexture(pipeline, texture, x + width - corner, y + height - corner, u2, v2, corner, corner, textureWidth, textureHeight, color);
        
        // Top edge
        context.drawTexture(pipeline, texture, x + corner, y, (float) corner, v0, destMiddleWidth, corner, middleWidth, corner, textureWidth, textureHeight, color);
        // Bottom edge
        context.drawTexture(pipeline, texture, x + corner, y + height - corner, (float) corner, v2, destMiddleWidth, corner, middleWidth, corner, textureWidth, textureHeight, color);
        // Left edge
        context.drawTexture(pipeline, texture, x, y + corner, u0, (float) corner, corner, destMiddleHeight, corner, middleHeight, textureWidth, textureHeight, color);
        // Right edge
        context.drawTexture(pipeline, texture, x + width - corner, y + corner, u2, (float) corner, corner, destMiddleHeight, corner, middleHeight, textureWidth, textureHeight, color);
        
        // Center fill
        context.drawTexture(pipeline, texture, x + corner, y + corner, (float) corner, (float) corner, destMiddleWidth, destMiddleHeight, middleWidth, middleHeight, textureWidth, textureHeight, color);
    }
    
    /**
     * Executes an action with matrix push/pop for transformations.
     * In Minecraft 1.21.6, DrawContext handles matrices internally.
     * 
     * @param context  The draw context
     * @param x        X translation
     * @param y        Y translation
     * @param action   The action to execute
     */
    public static void withMatrixPush(DrawContext context, float x, float y, Runnable action) {
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(x, y);
        try {
            action.run();
        } finally {
            context.getMatrices().popMatrix();
        }
    }
}
