package net.bichal.bplb;

import net.bichal.bplb.network.HandshakePayload;
import net.bichal.bplb.network.PositionUpdatePayload;
import net.bichal.bplb.server.Server;
import net.bichal.bplb.util.Constants;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class Main implements ModInitializer {
    @Override
    public void onInitialize() {
        Constants.LOGGER.info("[{}] Mod initialization initialized", Constants.MOD_NAME_SHORT);

        PayloadTypeRegistry.playS2C().register(PositionUpdatePayload.ID, PositionUpdatePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(PositionUpdatePayload.ID, PositionUpdatePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(HandshakePayload.ID, HandshakePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(HandshakePayload.ID, HandshakePayload.CODEC);

        new Server().onInitializeServer();

        Constants.LOGGER.info("[{}] Mod initialization finished", Constants.MOD_NAME_SHORT);
    }
}
