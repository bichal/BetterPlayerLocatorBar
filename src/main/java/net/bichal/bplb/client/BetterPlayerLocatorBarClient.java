package net.bichal.bplb.client;

import net.bichal.bplb.BetterPlayerLocatorBar;
import net.bichal.bplb.client.screens.BetterPlayerLocatorBarConfigScreen;
import net.bichal.bplb.client.screens.BetterPlayerLocatorBarJsonEditorScreen;
import net.bichal.bplb.client.screens.BetterPlayerLocatorBarWarningScreen;
import net.bichal.bplb.config.BetterPlayerLocatorBarConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

public class BetterPlayerLocatorBarClient implements ClientModInitializer {
    public static void openExperimentalScreen(Screen parent) {
        if (BetterPlayerLocatorBarWarningScreen.hasWarningBeenShown()) {
            MinecraftClient.getInstance().setScreen(new BetterPlayerLocatorBarJsonEditorScreen(parent));
        } else {
            MinecraftClient.getInstance().setScreen(new BetterPlayerLocatorBarWarningScreen(parent));
        }
    }

    @Override
    public void onInitializeClient() {
        BetterPlayerLocatorBar.LOGGER.info("           " + BetterPlayerLocatorBar.MOD_LARGE_NAME);
        BetterPlayerLocatorBar.LOGGER.info("|-----------------------------------------------|");
        BetterPlayerLocatorBar.LOGGER.info("[{}] Initializing mod client side!", BetterPlayerLocatorBar.MOD_SHORT_NAME);

        HudRenderCallback.EVENT.register((context, tickDelta) -> BetterPlayerLocatorBarHud.render(context));
        BetterPlayerLocatorBarHud.registerEvents();
        BetterPlayerLocatorBarConfig.getInstance();
        Keybinds.register();

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            BetterPlayerLocatorBarConfig.getInstance().saveConfig();
            BetterPlayerLocatorBar.LOGGER.info("[{}] Config saved on game exit", BetterPlayerLocatorBar.MOD_SHORT_NAME);
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.currentScreen instanceof BetterPlayerLocatorBarConfigScreen && client.options.attackKey.isPressed() && !BetterPlayerLocatorBarWarningScreen.hasWarningBeenShown()) {
                client.setScreen(new BetterPlayerLocatorBarWarningScreen(client.currentScreen));
            }
        });

        BetterPlayerLocatorBar.LOGGER.info("[{}] Client side initialized!", BetterPlayerLocatorBar.MOD_SHORT_NAME);
        BetterPlayerLocatorBar.LOGGER.info("|-----------------------------------------------|");
    }
}
