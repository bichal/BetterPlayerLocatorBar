package net.bichal.bplb.client;

import net.bichal.bichalutils.util.Logger;
import net.bichal.bichalutils.util.ModIdentifier;
import net.bichal.bplb.client.command.ConfigCommand;
import net.bichal.bplb.client.render.AssetScanner;
import net.bichal.bplb.gui.Config;
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

@Environment(EnvType.CLIENT)
public class Client implements ClientModInitializer {
    public static List<String> availableDots = new ArrayList<>();
    public static List<String> availableArrows = new ArrayList<>();
    public static List<String> availableIconBorders = new ArrayList<>();
    public static List<String> availableNameBorders = new ArrayList<>();
    public static List<String> availableDeathMarkers = new ArrayList<>();
    private static long lastServerUpdateTime = 0;
    private static boolean isLocalMode = true;
    private static boolean playerHasOp = false;
    public static List<String> availableLodestoneMarkers = new ArrayList<>();
    public static boolean isLocalMode() {
        return isLocalMode;
    }
    public static void updateLastServerUpdateTime() {
        lastServerUpdateTime = System.currentTimeMillis();
    }

    @Override
    public void onInitializeClient() {
        Logger.setModName(Constants.MOD_NAME_LARGE);
        Logger.info("Initializing mod client side!");

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return ModIdentifier.ofMod("asset_scanner");
            }

            @Override
            public void reload(ResourceManager manager) {
                availableDots = AssetScanner.getPlayerDots(manager);
                availableArrows = AssetScanner.getArrowTypes(manager);
                availableIconBorders = AssetScanner.getIconBorderStyles(manager);
                availableNameBorders = AssetScanner.getNameplateBorderStyles(manager);
                availableDeathMarkers = AssetScanner.getDeathMarkerTypes(manager);
                availableLodestoneMarkers = AssetScanner.getIconBorderStyles(manager);

                Logger.info("Scanned assets: {} dots, {} arrows, {} icon borders, {} name borders, {} death markers", availableDots.size(), availableArrows.size(), availableIconBorders.size(), availableNameBorders.size(), availableDeathMarkers.size());

                if (!availableDots.isEmpty() && !availableDots.contains(Config.getInstance().getDotType())) {
                    Config.getInstance().setDotType(availableDots.getFirst());
                }
                if (!availableArrows.isEmpty() && !availableArrows.contains(Config.getInstance().getArrowType())) {
                    Config.getInstance().setArrowType(availableArrows.getFirst());
                }
            }
        });

        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            if (!isLocalMode && System.currentTimeMillis() - lastServerUpdateTime > 5000) {
                isLocalMode = true;
                Logger.info("Server timeout, switching to local mode");
            }
            Hud.render(context);
        });

        ClientTickEvents.END_CLIENT_TICK.register(Hud::tick);
        Hud.registerEvents();
        Config.getInstance();
        Keybinds.register();
        ConfigCommand.register();

        ClientPlayNetworking.registerGlobalReceiver(HandshakePayload.ID, (payload, context) -> {
            isLocalMode = false;
            playerHasOp = payload.playerHasOp();
            lastServerUpdateTime = System.currentTimeMillis();
            Logger.info("Server has mod installed (OP: {}), switching to remote mode", playerHasOp);
        });


        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            isLocalMode = true;
            playerHasOp = false;
            lastServerUpdateTime = 0;
        });

        Logger.info("Client side initialized!");
    }
}
