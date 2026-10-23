package net.bichal.bplb.config.ui;

import net.minecraft.client.gui.GuiGraphics;

/**
 * One line of the settings list. Rows draw and hit-test themselves so the screen does not depend on
 * any of the vanilla list widgets, whose API changes between Minecraft versions.
 */
public interface Row {
    int HEIGHT = 24;

    /** The option key used by the search box, or {@code null} for rows that are never filtered. */
    String key();

    default int height() {
        return HEIGHT;
    }

    void render(GuiGraphics g, int x, int y, int width, int mouseX, int mouseY, boolean hovered);

    default boolean mouseClicked(double mouseX, double mouseY, int button, int x, int y, int width) {
        return false;
    }

    default boolean mouseDragged(double mouseX, double mouseY, int x, int y, int width) {
        return false;
    }

    default void mouseReleased() {
    }

    default boolean mouseScrolled(double mouseX, double mouseY, double amount, int x, int y, int width) {
        return false;
    }

    default boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    default boolean charTyped(char chr, int modifiers) {
        return false;
    }

    /** Drops keyboard focus, if the row holds any. */
    default void blur() {
    }

    default boolean hasFocus() {
        return false;
    }
}
