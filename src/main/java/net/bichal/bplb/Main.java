package net.bichal.bplb;

import net.bichal.bichalutils.util.Logger;
import net.bichal.bichalutils.util.ModIdentifier;
import net.bichal.bplb.network.HandshakePayload;
import net.bichal.bplb.network.PositionUpdatePayload;
import net.bichal.bplb.server.Server;
import net.bichal.bplb.server.command.ServerConfigCommand;
import net.bichal.bplb.util.Constants;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class Main implements ModInitializer {
    @Override
    public void onInitialize() {
        Logger.setModName(Constants.MOD_NAME_LARGE);
        Logger.info("Mod initialization initialized");
        ModIdentifier.setNamespace(Constants.MOD_ID);

        PayloadTypeRegistry.playS2C().register(PositionUpdatePayload.ID, PositionUpdatePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(HandshakePayload.ID, HandshakePayload.CODEC);

        new Server().onInitializeServer();
        ServerConfigCommand.register();

        Logger.info("Mod initialization finished");
    }
}
