package net.bichal.bplb.server;

import net.bichal.bplb.network.HandshakePayload;
import net.bichal.bplb.util.Constants;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.bichal.bplb.network.PositionUpdatePayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Server-side manager for player tracking and position synchronization.
 * Refactored to use NetworkManager for network operations.
 */
public final class Server implements DedicatedServerModInitializer {
    private final PlayerTracker playerTracker = new PlayerTracker();
    private final NetworkManager networkManager = new NetworkManager();
    private static final long PLAYER_LIST_CACHE_DURATION = 50L;
    private final ExecutorService executor;
    private List<ServerPlayerEntity> playerListCache = new ArrayList<>();
    private long playerListCacheTime = 0;
    private long lastUpdateTime = 0;

    public Server() {
        int threads = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);
        this.executor = Executors.newFixedThreadPool(threads, r -> {
            Thread t = new Thread(r, "BPLB-Worker");
            t.setDaemon(true);
            return t;
        });
    }

    @Override
    public void onInitializeServer() {
        Constants.LOGGER.info("[{}] Initializing server!", Constants.MOD_NAME_SHORT);
        ServerConfig.getInstance();
        registerConnectionHandlers();
        ServerTickEvents.END_SERVER_TICK.register(this::tick);
        Constants.LOGGER.info("[{}] Server initialized!", Constants.MOD_NAME_SHORT);
    }

    private void registerConnectionHandlers() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (handler == null || handler.player == null) return;
            
            UUID playerId = handler.player.getUuid();
            if (playerId == null) return;
            
            playerTracker.updatePlayer(handler.player);
            networkManager.notifyNewPlayer(playerId);
            invalidatePlayerListCache();
            
            if (ServerPlayNetworking.canSend(handler.player, HandshakePayload.ID)) {
                boolean hasOp = server.getPlayerManager().isOperator(handler.player.getGameProfile());
                ServerPlayNetworking.send(handler.player, new HandshakePayload(hasOp));
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            if (handler == null || handler.player == null) return;
            
            UUID playerId = handler.player.getUuid();
            if (playerId == null) return;
            
            networkManager.notifyDisconnected(playerId);
            playerTracker.removePlayer(playerId);
            invalidatePlayerListCache();
        });
    }

    private void tick(@Nullable MinecraftServer server) {
        if (server == null) return;

        long currentTime = server.getTicks();
        List<ServerPlayerEntity> players = getCachedPlayerList(server);

        // Process players - parallel for large counts
        if (players.size() > 10) {
            processPlayersParallel(players);
        } else {
            processPlayersSequential(players);
        }

        // Send updates at configured rate
        if (currentTime - lastUpdateTime >= ServerConfig.getInstance().positionUpdateRateTicks()) {
            sendUpdates(server, players);
            lastUpdateTime = currentTime;
        }
    }

    private void processPlayersParallel(List<ServerPlayerEntity> players) {
        int chunkSize = Math.max(5, players.size() / 4);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < players.size(); i += chunkSize) {
            int end = Math.min(i + chunkSize, players.size());
            List<ServerPlayerEntity> chunk = players.subList(i, end);

            futures.add(CompletableFuture.runAsync(() -> {
                for (ServerPlayerEntity player : chunk) {
                    if (player != null) {
                        playerTracker.updatePlayer(player);
                    }
                }
            }, executor));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private void processPlayersSequential(List<ServerPlayerEntity> players) {
        for (ServerPlayerEntity player : players) {
            if (player != null) {
                playerTracker.updatePlayer(player);
            }
        }
    }

    private void sendUpdates(MinecraftServer server, List<ServerPlayerEntity> players) {
        if (players.isEmpty()) return;

        PositionUpdatePayload payload = networkManager.createUpdatePayload(
            server, players, playerTracker, 
            (int) ServerConfig.getInstance().maxRelevantDistance()
        );

        if (payload != null) {
            for (ServerPlayerEntity player : players) {
                if (player != null) {
                    networkManager.sendUpdate(player, payload);
                }
            }
        }
    }

    private List<ServerPlayerEntity> getCachedPlayerList(@Nullable MinecraftServer server) {
        if (server == null) return Collections.emptyList();
        long now = System.currentTimeMillis();
        if (now - playerListCacheTime > PLAYER_LIST_CACHE_DURATION) {
            playerListCache = server.getPlayerManager().getPlayerList();
            playerListCacheTime = now;
        }
        return playerListCache;
    }

    private void invalidatePlayerListCache() {
        playerListCacheTime = 0;
    }
}
