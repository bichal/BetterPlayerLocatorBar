package net.bichal.bplb.client;

import net.bichal.bplb.client.render.AssetScanner;
import net.bichal.bplb.config.Config;
import net.bichal.bplb.network.HandshakePayload;
import net.bichal.bplb.util.Constants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT) public class Client implements ClientModInitializer {
    public static List<String> availableDots = new ArrayList<>();
    public static List<String> availableArrows = new ArrayList<>();
    private static long lastServerUpdateTime = 0;
    private static boolean isLocalMode = true;

    public static long getLastServerUpdateTime() {
        return lastServerUpdateTime;
    }

    public static boolean isLocalMode() {
        return isLocalMode;
    }

    public static void updateLastServerUpdateTime() {
        lastServerUpdateTime = System.currentTimeMillis();
    }

    @Override public void onInitializeClient() {
        Constants.LOGGER.info("           " + Constants.MOD_NAME_LARGE);
        Constants.LOGGER.info("|-----------------------------------------------|");
        Constants.LOGGER.info("[{}] Initializing mod client side!", Constants.MOD_NAME_SHORT);

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override public Identifier getFabricId() {
                return Identifier.of(Constants.MOD_ID, "asset_scanner");
            }

            @Override public void reload(ResourceManager manager) {
                availableDots = AssetScanner.getSpriteNames(manager, "dots").stream().map(path -> path.split("/")[0]).distinct().filter(s -> !s.equals("bowtie")).sorted().collect(Collectors.toList());

                availableArrows = AssetScanner.getSpriteNames(manager, "arrows");
                Constants.LOGGER.info("[{}] Scanned assets: Found {} dot types and {} arrow sprites.", Constants.MOD_NAME_SHORT, availableDots.size(), availableArrows.size());
            }
        });

        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            if (!isLocalMode && System.currentTimeMillis() - lastServerUpdateTime > 5000) {
                isLocalMode = true;
                Constants.LOGGER.info("[{}] Server timeout, switching to local mode", Constants.MOD_NAME_SHORT);
            }
            Hud.render(context, tickCounter.getTickDelta(false));
        });

        ClientTickEvents.END_CLIENT_TICK.register(Hud::tick);
        Hud.registerEvents();
        Config.getInstance();
        Keybinds.register();

        ClientPlayNetworking.registerGlobalReceiver(HandshakePayload.ID, (payload, context) -> {
            isLocalMode = false;
            lastServerUpdateTime = System.currentTimeMillis();
            Constants.LOGGER.info("[{}] Server has mod installed, switching to remote mode", Constants.MOD_NAME_SHORT);
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            isLocalMode = true;
            lastServerUpdateTime = 0;
        });

        Constants.LOGGER.info("[{}] Client side initialized!", Constants.MOD_NAME_SHORT);
        Constants.LOGGER.info("|-----------------------------------------------|");
    }
}
