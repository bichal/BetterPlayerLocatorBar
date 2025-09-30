package net.bichal.bplb.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class RenderUtils {
    public static void renderTintedTexture(DrawContext context, Identifier texture, float x, float y, float width, float height, int color, float alpha) {
        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;
        GL11.glEnable(GL11.GL_BLEND);
        RenderSystem.setShaderColor(r, g, b, alpha);
        Dimension dim = AssetScanner.getTextureDimensions(texture);
        context.drawTexture(texture, (int) x, (int) y, (int) width, (int) height, 0f, 0f, dim.width, dim.height, dim.width, dim.height);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static void drawNineSlicedTexture(DrawContext context, Identifier texture, int x, int y, int width, int height) {
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

        context.drawTexture(texture, x, y, corner, corner, u0, v0, corner, corner, textureWidth, textureHeight);
        context.drawTexture(texture, x + width - corner, y, corner, corner, u2, v0, corner, corner, textureWidth, textureHeight);
        context.drawTexture(texture, x, y + height - corner, corner, corner, u0, v2, corner, corner, textureWidth, textureHeight);
        context.drawTexture(texture, x + width - corner, y + height - corner, corner, corner, u2, v2, corner, corner, textureWidth, textureHeight);

        context.drawTexture(texture, x + corner, y, destMiddleWidth, corner, (float) corner, v0, middleWidth, corner, textureWidth, textureHeight);
        context.drawTexture(texture, x + corner, y + height - corner, destMiddleWidth, corner, (float) corner, v2, middleWidth, corner, textureWidth, textureHeight);
        context.drawTexture(texture, x, y + corner, corner, destMiddleHeight, u0, (float) corner, corner, middleHeight, textureWidth, textureHeight);
        context.drawTexture(texture, x + width - corner, y + corner, corner, destMiddleHeight, u2, (float) corner, corner, middleHeight, textureWidth, textureHeight);
        context.drawTexture(texture, x + corner, y + corner, destMiddleWidth, destMiddleHeight, (float) corner, (float) corner, middleWidth, middleHeight, textureWidth, textureHeight);
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
