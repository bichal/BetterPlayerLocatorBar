package net.bichal.bplb.client;

import net.bichal.bplb.BetterPlayerLocatorBar;
import net.bichal.bplb.config.BetterPlayerLocatorBarConfig;
import net.bichal.bplb.config.BetterPlayerLocatorBarConfigScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class Keybinds {
    public static final KeyBinding SHOW_PLAYER_NAME = new KeyBinding(
            "key.bplb.show_player_name",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_TAB,
            BetterPlayerLocatorBar.MOD_LARGE_NAME
    );

    public static final KeyBinding OPEN_CONFIG = new KeyBinding(
            "key.bplb.open_config",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F8,
            BetterPlayerLocatorBar.MOD_LARGE_NAME
    );

    public static void register() {
        BetterPlayerLocatorBar.LOGGER.info("[{}] Registering Keybinds", BetterPlayerLocatorBar.MOD_SHORT_NAME);

        KeyBindingHelper.registerKeyBinding(SHOW_PLAYER_NAME);
        KeyBindingHelper.registerKeyBinding(OPEN_CONFIG);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (OPEN_CONFIG.wasPressed()) {
                client.setScreen(new BetterPlayerLocatorBarConfigScreen(client.currentScreen));
            }
        });

        BetterPlayerLocatorBar.LOGGER.info("[{}] Keybinds registered", BetterPlayerLocatorBar.MOD_SHORT_NAME);
    }

    public static boolean shouldShowPlayerNames() {
        BetterPlayerLocatorBarConfig config = BetterPlayerLocatorBarConfig.getInstance();
        return config.isToggleTab() || (!config.isToggleTab() && SHOW_PLAYER_NAME.isPressed());
    }
}
