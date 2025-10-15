package net.bichal.bplb.client;

import net.bichal.bplb.config.ConfigScreen;
import net.bichal.bplb.util.Constants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class Keybinds {
    private static final KeyBinding SHOW_PLAYER_NAME = new KeyBinding("key.bplb.show_player_name", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_TAB, Constants.MOD_NAME_LARGE);
    private static final KeyBinding OPEN_CONFIG = new KeyBinding("key.bplb.open_config", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_F8, Constants.MOD_NAME_LARGE);

    public static void register() {
        Constants.LOGGER.info("[{}] Registering Keybinds", Constants.MOD_NAME_SHORT);
        KeyBindingHelper.registerKeyBinding(SHOW_PLAYER_NAME);
        KeyBindingHelper.registerKeyBinding(OPEN_CONFIG);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (OPEN_CONFIG.wasPressed()) {
                client.setScreen(new ConfigScreen(client.currentScreen));
            }
        });
        Constants.LOGGER.info("[{}] Keybinds registered", Constants.MOD_NAME_SHORT);
    }

    public static boolean shouldShowPlayerNames() {
        return SHOW_PLAYER_NAME.isPressed();
    }
}
