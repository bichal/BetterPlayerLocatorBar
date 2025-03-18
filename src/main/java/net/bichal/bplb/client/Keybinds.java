package net.bichal.bplb.client;

import net.bichal.bplb.BetterPlayerLocatorBar;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class Keybinds {
    public static final KeyBinding SHOW_PLAYER_NAME = new KeyBinding(
            "key.bplb.show_player_name",
            GLFW.GLFW_KEY_TAB,
            BetterPlayerLocatorBar.MOD_LARGE_NAME
    );

    public static void register() {
        KeyBindingHelper.registerKeyBinding(SHOW_PLAYER_NAME);
    }
}
