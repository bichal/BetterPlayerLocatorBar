package net.bichal.bplb.client;

import net.bichal.bplb.BetterPlayerLocatorBar;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class BetterPlayerLocatorBarClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BetterPlayerLocatorBar.LOGGER.info("           " + BetterPlayerLocatorBar.MOD_LARGE_NAME);
        BetterPlayerLocatorBar.LOGGER.info("|-----------------------------------------------|");
        BetterPlayerLocatorBar.LOGGER.info("[{}] Initializing mod client side!", BetterPlayerLocatorBar.MOD_SHORT_NAME);

        HudRenderCallback.EVENT.register((context, tickDelta) -> BetterPlayerLocatorBarHud.render(context));
        BetterPlayerLocatorBarHud.registerEvents();
        Keybinds.register();

        BetterPlayerLocatorBar.LOGGER.info("[{}] Client side initialized!", BetterPlayerLocatorBar.MOD_SHORT_NAME);
        BetterPlayerLocatorBar.LOGGER.info("|-----------------------------------------------|");
    }
}
