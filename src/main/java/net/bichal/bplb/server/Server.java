package net.bichal.bplb.server;

import net.bichal.bplb.network.HandshakePayload;
import net.bichal.bplb.network.PositionUpdatePayload;
import net.bichal.bplb.util.Constants;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;
import java.util.stream.Collectors;

public class Server implements DedicatedServerModInitializer {
    private final PlayerTracker playerTracker = new PlayerTracker();
    private final Set<UUID> newPlayers = Collections.synchronizedSet(new HashSet<>());
    private final Set<UUID> disconnectedPlayers = Collections.synchronizedSet(new HashSet<>());
    private long lastUpdateTime = 0;
    private long lastCleanupTime = 0;

    @Override
    public void onInitializeServer() {
        Constants.LOGGER.info("[{}] Initializing optimized server!", Constants.MOD_NAME_SHORT);
        ServerConfig.getInstance();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            UUID playerId = handler.player.getUuid();
            playerTracker.updatePlayer(handler.player);
            newPlayers.add(playerId);

            if (ServerPlayNetworking.canSend(handler.player, HandshakePayload.ID)) {
                ServerPlayNetworking.send(handler.player, new HandshakePayload());
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            UUID playerId = handler.player.getUuid();
            disconnectedPlayers.add(playerId);
            playerTracker.removePlayer(playerId);
            newPlayers.remove(playerId);
        });

        ServerTickEvents.END_SERVER_TICK.register(this::tick);
        Constants.LOGGER.info("[{}] Optimized server initialized!", Constants.MOD_NAME_SHORT);
    }

    private void tick(MinecraftServer server) {
        long currentTime = server.getTicks();

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            playerTracker.updatePlayer(player);
        }

        if (currentTime - lastUpdateTime >= ServerConfig.getInstance().positionUpdateRateTicks) {
            sendOptimizedUpdate(server);
            lastUpdateTime = currentTime;
        }

        if (currentTime - lastCleanupTime >= ServerConfig.getInstance().cleanupIntervalTicks) {
            playerTracker.cleanup();
            lastCleanupTime = currentTime;
        }
    }

    private void sendOptimizedUpdate(MinecraftServer server) {
        List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();
        if (players.isEmpty()) return;

        List<PositionUpdatePayload.PlayerInfo> newPlayerInfos = new ArrayList<>();
        newPlayers.removeIf(uuid -> {
            PlayerTracker.PlayerInfo info = playerTracker.getPlayerInfo(uuid);
            if (info != null) {
                newPlayerInfos.add(new PositionUpdatePayload.PlayerInfo(uuid, info.name));
                return true;
            }
            return false;
        });

        Map<UUID, PlayerTracker.PlayerPosition> movedPlayersData = playerTracker.getAndClearMovedPlayers();
        List<PositionUpdatePayload.PositionData> positions = movedPlayersData.entrySet().stream().map(entry -> new PositionUpdatePayload.PositionData(entry.getKey(), entry.getValue().x, entry.getValue().y, entry.getValue().z)).collect(Collectors.toList());

        if (!newPlayerInfos.isEmpty() || !positions.isEmpty() || !disconnectedPlayers.isEmpty()) {
            PositionUpdatePayload payload = new PositionUpdatePayload(newPlayerInfos, positions, new ArrayList<>(disconnectedPlayers));

            for (ServerPlayerEntity player : players) {
                if (ServerPlayNetworking.canSend(player, PositionUpdatePayload.ID)) {
                    ServerPlayNetworking.send(player, payload);
                }
            }

            disconnectedPlayers.clear();
        }
    }
}
