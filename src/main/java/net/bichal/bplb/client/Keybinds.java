package net.bichal.bplb.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.bichal.bplb.config.ConfigScreen;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class Keybinds {
    public static final KeyMapping SHOW_PLAYER_NAME = new KeyMapping("key.bplb.show_player_name", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_TAB, Constants.MOD_NAME_LARGE);
    public static final KeyMapping OPEN_CONFIG = new KeyMapping("key.bplb.open_config", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F8, Constants.MOD_NAME_LARGE);

    /** Called once per client tick by the loader. */
    public static void onClientTick(Minecraft client) {
        while (OPEN_CONFIG.consumeClick()) {
            client.setScreen(new ConfigScreen(client.screen));
        }
    }

    public static boolean shouldShowPlayerNames() {
        return SHOW_PLAYER_NAME.isDown();
    }
}
