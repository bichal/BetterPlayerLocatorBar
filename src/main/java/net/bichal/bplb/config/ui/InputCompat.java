package net.bichal.bplb.config.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
//? if >=1.21.9 {
/*import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
*///?}

/** Input calls whose signatures changed in 1.21.9, when mouse and key arguments became event objects. */
public final class InputCompat {
    private InputCompat() {}

    public static boolean click(EditBox box, double x, double y, int button) {
        //? if >=1.21.9 {
        /*return box.mouseClicked(new MouseButtonEvent(x, y, new MouseButtonInfo(button, 0)), false);
        *///?} else {
        return box.mouseClicked(x, y, button);
        //?}
    }

    public static boolean key(EditBox box, int keyCode, int scanCode, int modifiers) {
        //? if >=1.21.9 {
        /*return box.keyPressed(new KeyEvent(keyCode, scanCode, modifiers));
        *///?} else {
        return box.keyPressed(keyCode, scanCode, modifiers);
        //?}
    }

    public static boolean typed(EditBox box, char chr, int modifiers) {
        //? if >=26.1 {
        /*return box.charTyped(new CharacterEvent(chr));
        *///?} else if >=1.21.9 {
        /*return box.charTyped(new CharacterEvent(chr, modifiers));
        *///?} else {
        return box.charTyped(chr, modifiers);
        //?}
    }

    //? if >=26.3 {
    /*public static int scancode(KeyEvent event) {
        return 0;
    }
    *///?} else if >=1.21.9 {
    /*public static int scancode(KeyEvent event) {
        return event.scancode();
    }
    *///?}

    //? if >=26.1 {
    /*public static int modifiers(CharacterEvent event) {
        return 0;
    }
    *///?} else if >=1.21.9 {
    /*public static int modifiers(CharacterEvent event) {
        return event.modifiers();
    }
    *///?}

    /** EditBox drawing: render(...) became extractRenderState(...) in 26.1. */
    public static void draw(EditBox box, net.minecraft.client.gui.GuiGraphics g, int mouseX, int mouseY) {
        //? if >=26.1 {
        /*box.extractRenderState(g, mouseX, mouseY, 0f);
        *///?} else {
        box.render(g, mouseX, mouseY, 0f);
        //?}
    }

    public static boolean shiftDown() {
        //? if >=1.21.9 {
        /*return Minecraft.getInstance().hasShiftDown();
        *///?} else {
        return net.minecraft.client.gui.screens.Screen.hasShiftDown();
        //?}
    }
}
