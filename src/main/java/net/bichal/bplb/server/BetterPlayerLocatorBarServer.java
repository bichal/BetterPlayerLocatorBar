package net.bichal.bplb.server;

import net.bichal.bplb.BetterPlayerLocatorBar;
import net.bichal.bplb.network.HandshakePayload;
import net.bichal.bplb.network.PositionUpdatePayload;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;
import java.util.stream.Collectors;

public class BetterPlayerLocatorBarServer implements DedicatedServerModInitializer {
    private static final long UPDATE_DELAY = 2;
    private long lastUpdateTime = 0;

    public static void syncPlayerPositions(List<ServerPlayerEntity> players) {
        if (players.isEmpty()) return;
        List<PositionUpdatePayload.PlayerPosition> positions = players.stream()
                .map(p -> new PositionUpdatePayload.PlayerPosition(p.getUuid(), p.getName().getString(), p.getX(), p.getY(), p.getZ()))
                .collect(Collectors.toList());
        PositionUpdatePayload payload = new PositionUpdatePayload(positions);
        players.forEach(player -> ServerPlayNetworking.send(player, payload));
    }

    @Override
    public void onInitializeServer() {
        BetterPlayerLocatorBar.LOGGER.info("[{}] Initializing mod server side!", BetterPlayerLocatorBar.MOD_SHORT_NAME);
        ServerPlayNetworking.registerGlobalReceiver(HandshakePayload.ID, (payload, context) -> ServerPlayNetworking.send(context.player(), new HandshakePayload()));
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (ServerPlayNetworking.canSend(handler.player, HandshakePayload.ID)) {
                ServerPlayNetworking.send(handler.player, new HandshakePayload());
            }
        });
        ServerTickEvents.END_SERVER_TICK.register(this::tick);
        BetterPlayerLocatorBar.LOGGER.info("[{}] Server side initialized!", BetterPlayerLocatorBar.MOD_SHORT_NAME);
    }

    private void tick(MinecraftServer server) {
        long currentTime = server.getTicks();
        if (currentTime - lastUpdateTime >= UPDATE_DELAY) {
            syncPlayerPositions(server.getPlayerManager().getPlayerList());
            lastUpdateTime = currentTime;
        }
    }
}
