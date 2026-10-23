package net.bichal.bplb.platform.neoforge;

import net.bichal.bplb.Main;
import net.bichal.bplb.platform.Platform;
import net.bichal.bplb.util.Constants;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
//? if <1.21.6 {
import net.bichal.bplb.network.HandshakePayload;
import net.bichal.bplb.network.PositionUpdatePayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
//?}

import java.nio.file.Path;

@Mod(Constants.MOD_ID)
public class NeoForgeMain {
    public NeoForgeMain(IEventBus modBus) {
        Main.init(new Platform.Provider() {
            @Override
            public Path configDir() {
                return FMLPaths.CONFIGDIR.get();
            }

            @Override
            public Path gameDir() {
                return FMLPaths.GAMEDIR.get();
            }

            //? if <1.21.6 {

            @Override
            public boolean canSend(ServerPlayer player, CustomPacketPayload.Type<?> type) {
                return player.connection.hasChannel(type);
            }

            @Override
            public void send(ServerPlayer player, CustomPacketPayload payload) {
                PacketDistributor.sendToPlayer(player, payload);
            }
            //?}
        });

        //? if <1.21.6 {
        modBus.addListener(NeoForgeMain::registerPayloads);
        NeoForge.EVENT_BUS.register(ServerEvents.class);
        //?}
    }
    //? if <1.21.6 {

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        // optional(): a client or server without this mod can still connect
        PayloadRegistrar registrar = event.registrar("1").optional();
        registrar.playToClient(HandshakePayload.TYPE, HandshakePayload.CODEC, (payload, context) -> ClientBridge.onHandshake(payload));
        registrar.playToClient(PositionUpdatePayload.TYPE, PositionUpdatePayload.CODEC, (payload, context) -> ClientBridge.onPositionUpdate(payload));
    }

    public static final class ServerEvents {
        @SubscribeEvent
        public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                Main.server().onJoin(player, player.server);
            }
        }

        @SubscribeEvent
        public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
            if (event.getEntity() instanceof ServerPlayer player) {
                Main.server().onDisconnect(player);
            }
        }

        @SubscribeEvent
        public static void onServerTick(ServerTickEvent.Post event) {
            Main.server().tick(event.getServer());
        }
    }

    /**
     * Forwards payloads to the client code. It is a separate class so that the dedicated server
     * never loads client classes: the handlers above only touch it when a payload arrives.
     */
    static final class ClientBridge {
        static void onHandshake(HandshakePayload payload) {
            net.bichal.bplb.client.Client.onHandshake(payload);
        }

        static void onPositionUpdate(PositionUpdatePayload payload) {
            net.bichal.bplb.client.Client.onPositionUpdate(payload);
        }
    }
    //?}
}
