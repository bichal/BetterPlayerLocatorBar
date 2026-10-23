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
        //? if >=1.21.9 {
        /*return box.charTyped(new CharacterEvent(chr, modifiers));
        *///?} else {
        return box.charTyped(chr, modifiers);
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
