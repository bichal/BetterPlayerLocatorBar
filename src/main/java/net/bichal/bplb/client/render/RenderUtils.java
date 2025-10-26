package net.bichal.bplb.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

import java.awt.*;

public class RenderUtils {
    private static MinecraftClient clientInstance;

    public static MinecraftClient getClient() {
        if (clientInstance == null) {
            clientInstance = MinecraftClient.getInstance();
        }
        return clientInstance;
    }

    public static void renderTintedTexture(DrawContext context, Identifier texture, float x, float y, float width, float height, int color, float alpha) {
        Dimension dim = AssetScanner.getTextureDimensions(texture);
        int aI = (int) (alpha * 255);
        int finalColor = (aI << 24) | (color & 0xFFFFFF);
        context.drawTexture(RenderLayer::getGuiTextured, texture, (int) x, (int) y, 0f, 0f, (int) width, (int) height, dim.width, dim.height, dim.width, dim.height, finalColor);
    }

    @SuppressWarnings("SameParameterValue")
    static void drawNineSlicedTexture(DrawContext context, Identifier texture, int x, int y, int width, int height, int color) {
        int corner = 2;
        int textureWidth = 12;
        int textureHeight = 12;

        float u0 = 0f;
        float v0 = 0f;
        float u2 = textureWidth - corner;
        float v2 = textureHeight - corner;

        int middleWidth = textureWidth - corner * 2;
        int middleHeight = textureHeight - corner * 2;
        int destMiddleWidth = width - corner * 2;
        int destMiddleHeight = height - corner * 2;

        context.drawTexture(RenderLayer::getGuiTextured, texture, x, y, u0, v0, corner, corner, textureWidth, textureHeight, color);
        context.drawTexture(RenderLayer::getGuiTextured, texture, x + width - corner, y, u2, v0, corner, corner, textureWidth, textureHeight, color);
        context.drawTexture(RenderLayer::getGuiTextured, texture, x, y + height - corner, u0, v2, corner, corner, textureWidth, textureHeight, color);
        context.drawTexture(RenderLayer::getGuiTextured, texture, x + width - corner, y + height - corner, u2, v2, corner, corner, textureWidth, textureHeight, color);
        context.drawTexture(RenderLayer::getGuiTextured, texture, x + corner, y, (float) corner, v0, destMiddleWidth, corner, middleWidth, corner, textureWidth, textureHeight, color);
        context.drawTexture(RenderLayer::getGuiTextured, texture, x + corner, y + height - corner, (float) corner, v2, destMiddleWidth, corner, middleWidth, corner, textureWidth, textureHeight, color);
        context.drawTexture(RenderLayer::getGuiTextured, texture, x, y + corner, u0, (float) corner, corner, destMiddleHeight, corner, middleHeight, textureWidth, textureHeight, color);
        context.drawTexture(RenderLayer::getGuiTextured, texture, x + width - corner, y + corner, u2, (float) corner, corner, destMiddleHeight, corner, middleHeight, textureWidth, textureHeight, color);
        context.drawTexture(RenderLayer::getGuiTextured, texture, x + corner, y + corner, (float) corner, (float) corner, destMiddleWidth, destMiddleHeight, middleWidth, middleHeight, textureWidth, textureHeight, color);
    }

    public static void withMatrixPush(DrawContext context, float x, float y, Runnable action) {
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        try {
            action.run();
        } finally {
            context.getMatrices().pop();
        }
    }
}
