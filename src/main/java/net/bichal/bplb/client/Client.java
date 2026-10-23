package net.bichal.bplb.client;

import net.bichal.bplb.client.render.AssetScanner;
import net.bichal.bplb.config.Config;
//? if <1.21.6 {
import net.bichal.bplb.network.HandshakePayload;
import net.bichal.bplb.network.PositionUpdatePayload;
//?}
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
    private static final ServerLink LINK = new ServerLink();

    public static boolean isLocalMode() {
        return LINK.isLocal();
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
        LINK.reset();
    }

    //? if <1.21.6 {
    public static void onHandshake(HandshakePayload payload) {
        LINK.onHandshake(payload.playerHasOp());
        Constants.LOGGER.info("[{}] Server has mod installed (OP: {}), switching to remote mode", Constants.MOD_NAME_SHORT, LINK.playerHasOp());
    }

    public static void onPositionUpdate(PositionUpdatePayload payload) {
        Minecraft.getInstance().execute(() -> Hud.applyPositionUpdate(payload));
    }
    //?}
}
