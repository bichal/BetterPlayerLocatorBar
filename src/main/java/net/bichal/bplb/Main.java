package net.bichal.bplb;

import net.bichal.bplb.client.HudManager;
import net.bichal.bplb.network.HandshakePayload;
import net.bichal.bplb.network.PositionUpdatePayload;
import net.bichal.bplb.server.Server;
import net.bichal.bplb.util.Constants;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

/**
 * BetterPlayerLocatorBar - A Fabric mod for tracking players on HUD.
 * Refactored for Minecraft 1.21.6+ with improved architecture.
 */
public class Main implements ModInitializer {
    @Override
    public void onInitialize() {
        Constants.LOGGER.info("[{}] Mod initialization started", Constants.MOD_NAME_SHORT);

        // Register network payloads
        registerNetworkPayloads();

        // Initialize server-side components
        new Server().onInitializeServer();

        // Initialize client-side components
        initializeClient();

        Constants.LOGGER.info("[{}] Mod initialization finished", Constants.MOD_NAME_SHORT);
    }

    /**
     * Register custom network payloads for player position synchronization.
     */
    private void registerNetworkPayloads() {
        PayloadTypeRegistry.playS2C().register(PositionUpdatePayload.ID, PositionUpdatePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(PositionUpdatePayload.ID, PositionUpdatePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(HandshakePayload.ID, HandshakePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(HandshakePayload.ID, HandshakePayload.CODEC);
    }

    /**
     * Initialize client-side components and event handlers.
     */
    private void initializeClient() {
        HudManager hudManager = HudManager.getInstance();
        hudManager.registerEvents();
    }
}
