package net.bichal.bplb.client;

import net.bichal.bplb.client.render.AssetScanner;
import net.bichal.bplb.command.ConfigCommand;
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

/**
 * Client-side initialization for BetterPlayerLocatorBar.
 * Uses Hud for all HUD operations.
 */
@Environment(EnvType.CLIENT)
public class Client implements ClientModInitializer {
    public static List<String> availableDots = new ArrayList<>();
    public static List<String> availableArrows = new ArrayList<>();
    public static List<String> availableIconBorders = new ArrayList<>();
    public static List<String> availableNameBorders = new ArrayList<>();
    public static List<String> availableDeathMarkers = new ArrayList<>();
    
    private static long lastServerUpdateTime = 0;
    private static volatile boolean isLocalMode = true;
    private static boolean playerHasOp = false;
    
    public static boolean isLocalMode() {
        return isLocalMode;
    }
    
    public static void updateLastServerUpdateTime() {
        lastServerUpdateTime = System.currentTimeMillis();
    }

    @Override
    public void onInitializeClient() {
        Constants.LOGGER.info("[{}] Initializing mod client side!", Constants.MOD_NAME_SHORT);

        // Register asset scanner for resource reload
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(
            new SimpleSynchronousResourceReloadListener() {
                @Override
                public Identifier getFabricId() {
                    return Identifier.of(Constants.MOD_ID, "asset_scanner");
                }

                @Override
                public void reload(ResourceManager manager) {
                    availableDots = AssetScanner.getPlayerDots(manager);
                    availableArrows = AssetScanner.getArrowTypes(manager);
                    availableIconBorders = AssetScanner.getIconBorderStyles(manager);
                    availableNameBorders = AssetScanner.getNameplateBorderStyles(manager);
                    availableDeathMarkers = AssetScanner.getDeathMarkerTypes(manager);

                    Constants.LOGGER.info("[{}] Scanned assets: {} dots, {} arrows, {} icon borders, {} name borders, {} death markers", 
                        Constants.MOD_NAME_SHORT, availableDots.size(), availableArrows.size(), 
                        availableIconBorders.size(), availableNameBorders.size(), availableDeathMarkers.size());

                    Config config = Config.getInstance();
                    if (!availableDots.isEmpty() && !availableDots.contains(config.getDotType())) {
                        config.setDotType(availableDots.getFirst());
                    }
                    if (!availableArrows.isEmpty() && !availableArrows.contains(config.getArrowType())) {
                        config.setArrowType(availableArrows.getFirst());
                    }
                }
            }
        );

        // Initialize HUD and register events
        Hud.registerEvents();

        // Register HUD render callback
        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            // Check for server timeout
            if (!isLocalMode && System.currentTimeMillis() - lastServerUpdateTime > 5000) {
                isLocalMode = true;
                Constants.LOGGER.info("[{}] Server timeout, switching to local mode", Constants.MOD_NAME_SHORT);
            }
            
            // Render HUD
            Hud.render(context);
        });

        // Register tick handler
        ClientTickEvents.END_CLIENT_TICK.register(Hud::tick);

        // Initialize other components
        Config.getInstance();
        Keybinds.register();
        ConfigCommand.register();

        // Register handshake handler
        ClientPlayNetworking.registerGlobalReceiver(HandshakePayload.ID, (payload, context) -> {
            isLocalMode = false;
            playerHasOp = payload.playerHasOp();
            lastServerUpdateTime = System.currentTimeMillis();
            Constants.LOGGER.info("[{}] Server has mod installed (OP: {}), switching to remote mode", 
                Constants.MOD_NAME_SHORT, playerHasOp);
        });

        // Handle disconnect
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            isLocalMode = true;
            playerHasOp = false;
            lastServerUpdateTime = 0;
        });

        Constants.LOGGER.info("[{}] Client side initialized!", Constants.MOD_NAME_SHORT);
    }
}
