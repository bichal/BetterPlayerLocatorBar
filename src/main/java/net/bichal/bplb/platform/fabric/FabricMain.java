package net.bichal.bplb.platform.fabric;

import net.bichal.bplb.Main;
import net.bichal.bplb.platform.Platform;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
//? if !waypoints {
import net.bichal.bplb.network.HandshakePayload;
import net.bichal.bplb.network.PositionUpdatePayload;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
//?}

import java.nio.file.Path;

public class FabricMain implements ModInitializer {
    @Override
    public void onInitialize() {
        Main.init(new Platform.Provider() {
            @Override
            public Path configDir() {
                return FabricLoader.getInstance().getConfigDir();
            }

            @Override
            public Path gameDir() {
                return FabricLoader.getInstance().getGameDir();
            }
            //? if !waypoints {

            @Override
            public boolean canSend(ServerPlayer player, CustomPacketPayload.Type<?> type) {
                return ServerPlayNetworking.canSend(player, type);
            }

            @Override
            public void send(ServerPlayer player, CustomPacketPayload payload) {
                ServerPlayNetworking.send(player, payload);
            }
            //?}
        });

        //? if !waypoints {
        PayloadTypeRegistry.playS2C().register(PositionUpdatePayload.TYPE, PositionUpdatePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(HandshakePayload.TYPE, HandshakePayload.CODEC);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> Main.server().onJoin(handler.player, server));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> Main.server().onDisconnect(handler.player));
        ServerTickEvents.END_SERVER_TICK.register(Main.server()::tick);
        //?}
    }
}
