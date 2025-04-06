package net.bichal.bplb.client;

import net.bichal.bplb.BetterPlayerLocatorBar;
import net.bichal.bplb.config.BetterPlayerLocatorBarConfig;
import net.bichal.bplb.network.HandshakePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class BetterPlayerLocatorBarClient implements ClientModInitializer {
    private static final long SERVER_TIMEOUT_MS = 5000;
    private static long lastServerUpdateTime = 0;
    private static boolean serverHasMod = false;

    public static long getLastServerUpdateTime() {
        return lastServerUpdateTime;
    }

    public static boolean isServerHasMod() {
        return serverHasMod;
    }

    public static void updateLastServerUpdateTime() {
        lastServerUpdateTime = System.currentTimeMillis();
    }

    @Override
    public void onInitializeClient() {
        BetterPlayerLocatorBar.LOGGER.info("           " + BetterPlayerLocatorBar.MOD_LARGE_NAME);
        BetterPlayerLocatorBar.LOGGER.info("|-----------------------------------------------|");
        BetterPlayerLocatorBar.LOGGER.info("[{}] Initializing mod client side!", BetterPlayerLocatorBar.MOD_SHORT_NAME);

        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            if (serverHasMod && System.currentTimeMillis() - lastServerUpdateTime > SERVER_TIMEOUT_MS) {
                serverHasMod = false;
                BetterPlayerLocatorBar.LOGGER.info("[{}] Server timeout, switching to local mode", BetterPlayerLocatorBar.MOD_SHORT_NAME);
            }
            BetterPlayerLocatorBarHud.render(context);
        });

        BetterPlayerLocatorBarHud.registerEvents();
        BetterPlayerLocatorBarConfig.getInstance();
        Keybinds.register();

        ClientPlayNetworking.registerGlobalReceiver(HandshakePayload.ID, (payload, context) -> {
            serverHasMod = true;
            lastServerUpdateTime = System.currentTimeMillis();
            BetterPlayerLocatorBar.LOGGER.info("[{}] Server has mod installed", BetterPlayerLocatorBar.MOD_SHORT_NAME);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            serverHasMod = false;
            lastServerUpdateTime = 0;
        });

        BetterPlayerLocatorBar.LOGGER.info("[{}] Client side initialized!", BetterPlayerLocatorBar.MOD_SHORT_NAME);
        BetterPlayerLocatorBar.LOGGER.info("|-----------------------------------------------|");
    }
}
