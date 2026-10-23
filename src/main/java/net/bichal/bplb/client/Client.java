package net.bichal.bplb.client;

import net.bichal.bplb.client.render.AssetScanner;
import net.bichal.bplb.config.Config;
import net.bichal.bplb.network.HandshakePayload;
import net.bichal.bplb.network.PositionUpdatePayload;
import net.bichal.bplb.util.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Client side logic. The loader entrypoints forward their events to the {@code on*} methods.
 */
public class Client {
    public static List<String> availableDots = new ArrayList<>();
    public static List<String> availableArrows = new ArrayList<>();
    public static List<String> availableIconBorders = new ArrayList<>();
    public static List<String> availableNameBorders = new ArrayList<>();
    public static List<String> availableDeathMarkers = new ArrayList<>();
    private static long lastServerUpdateTime = 0;
    private static boolean isLocalMode = true;
    private static boolean playerHasOp = false;

    public static boolean isLocalMode() {
        return isLocalMode;
    }

    public static void updateLastServerUpdateTime() {
        lastServerUpdateTime = System.currentTimeMillis();
    }

    public static void init() {
        Constants.LOGGER.info("[{}] Initializing mod client side!", Constants.MOD_NAME_SHORT);
        Config.getInstance();
        Constants.LOGGER.info("[{}] Client side initialized!", Constants.MOD_NAME_SHORT);
    }

    public static void onResourcesReloaded(ResourceManager manager) {
        availableDots = AssetScanner.getPlayerDots(manager);
        availableArrows = AssetScanner.getArrowTypes(manager);
        availableIconBorders = AssetScanner.getIconBorderStyles(manager);
        availableNameBorders = AssetScanner.getNameplateBorderStyles(manager);
        availableDeathMarkers = AssetScanner.getDeathMarkerTypes(manager);

        Constants.LOGGER.info("[{}] Scanned assets: {} dots, {} arrows, {} icon borders, {} name borders, {} death markers", Constants.MOD_NAME_SHORT, availableDots.size(), availableArrows.size(), availableIconBorders.size(), availableNameBorders.size(), availableDeathMarkers.size());

        if (!availableDots.isEmpty() && !availableDots.contains(Config.getInstance().getDotType())) {
            Config.getInstance().setDotType(availableDots.getFirst());
        }
        if (!availableArrows.isEmpty() && !availableArrows.contains(Config.getInstance().getArrowType())) {
            Config.getInstance().setArrowType(availableArrows.getFirst());
        }
    }

    public static void onHudRender(GuiGraphics context) {
        if (!isLocalMode && System.currentTimeMillis() - lastServerUpdateTime > 5000) {
            isLocalMode = true;
            Constants.LOGGER.info("[{}] Server timeout, switching to local mode", Constants.MOD_NAME_SHORT);
        }
        Hud.render(context);
    }

    public static void onClientTick(Minecraft client) {
        Hud.tick(client);
        Keybinds.onClientTick(client);
    }

    public static void onJoin(Minecraft client) {
        Hud.onJoin();
    }

    public static void onDisconnect() {
        isLocalMode = true;
        playerHasOp = false;
        lastServerUpdateTime = 0;
    }

    public static void onHandshake(HandshakePayload payload) {
        isLocalMode = false;
        playerHasOp = payload.playerHasOp();
        lastServerUpdateTime = System.currentTimeMillis();
        Constants.LOGGER.info("[{}] Server has mod installed (OP: {}), switching to remote mode", Constants.MOD_NAME_SHORT, playerHasOp);
    }

    public static void onPositionUpdate(PositionUpdatePayload payload) {
        updateLastServerUpdateTime();
        Minecraft.getInstance().execute(() -> Hud.applyPositionUpdate(payload));
    }
}
