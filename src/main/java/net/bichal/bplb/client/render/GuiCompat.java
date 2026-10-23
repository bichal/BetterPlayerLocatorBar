package net.bichal.bplb.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;

/** Matrix stack and colour calls that changed in 1.21.6, when GuiGraphics moved to a 2D matrix stack. */
public final class GuiCompat {
    private GuiCompat() {}

    public static void push(GuiGraphics context) {
        //? if >=1.21.6 {
        /*context.pose().pushMatrix();
        *///?} else {
        context.pose().pushPose();
        //?}
    }

    public static void pop(GuiGraphics context) {
        //? if >=1.21.6 {
        /*context.pose().popMatrix();
        *///?} else {
        context.pose().popPose();
        //?}
    }

    /** Depth is ignored from 1.21.6 on, draw order decides what is on top. */
    public static void translate(GuiGraphics context, float x, float y, float z) {
        //? if >=1.21.6 {
        /*context.pose().translate(x, y);
        *///?} else {
        context.pose().translate(x, y, z);
        //?}
    }

    public static void scale(GuiGraphics context, float x, float y) {
        //? if >=1.21.6 {
        /*context.pose().scale(x, y);
        *///?} else {
        context.pose().scale(x, y, 1.0f);
        //?}
    }

    public static net.minecraft.client.Camera camera(net.minecraft.client.Minecraft client) {
        //? if >=26.2 {
        /*return client.gameRenderer.mainCamera();
        *///?} else {
        return client.gameRenderer.getMainCamera();
        //?}
    }

    public static net.minecraft.client.gui.screens.Screen screen(net.minecraft.client.Minecraft client) {
        //? if >=26.2 {
        /*return client.gui.screen();
        *///?} else {
        return client.screen;
        //?}
    }

    /** A 1 pixel border; GuiGraphics.renderOutline is not available from 1.21.9, so it is drawn with fills. */
    public static void outline(GuiGraphics g, int x, int y, int width, int height, int color) {
        g.fill(x, y, x + width, y + 1, color);
        g.fill(x, y + height - 1, x + width, y + height, color);
        g.fill(x, y + 1, x + 1, y + height - 1, color);
        g.fill(x + width - 1, y + 1, x + width, y + height - 1, color);
    }

    /** Tint for the legacy blit path; later versions pass the colour with each draw call. */
    public static void setShaderColor(float r, float g, float b, float a) {
        //? if <1.21.6 {
        RenderSystem.setShaderColor(r, g, b, a);
        //?}
    }

    public static void resetShaderColor() {
        setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
}
