package net.bichal.bplb;

import net.bichal.bplb.network.PositionUpdatePayload;
import net.bichal.bplb.server.BetterPlayerLocatorBarServer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.codec.PacketCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterPlayerLocatorBar implements ModInitializer {
    public static final String MOD_ID = "bplb";
    public static final String MOD_SHORT_NAME = "BPLB";
    public static final String MOD_LARGE_NAME = "Better Player Locator Bar";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_LARGE_NAME);

    @Override
    public void onInitialize() {
        LOGGER.info("           " + MOD_LARGE_NAME);
        LOGGER.info("|-----------------------------------------------|");
        LOGGER.info("[{}] Mod initialization initialized", MOD_SHORT_NAME);

        PayloadTypeRegistry.playS2C().register(PositionUpdatePayload.ID, PacketCodec.of(PositionUpdatePayload::write, PositionUpdatePayload::read));
        PayloadTypeRegistry.playC2S().register(PositionUpdatePayload.ID, PacketCodec.of(PositionUpdatePayload::write, PositionUpdatePayload::read));

        new BetterPlayerLocatorBarServer().onInitializeServer();

        LOGGER.info("[{}] Mod initialization finished", MOD_SHORT_NAME);
        LOGGER.info("|-----------------------------------------------|");
    }
}
