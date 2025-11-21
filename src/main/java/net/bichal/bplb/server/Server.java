package net.bichal.bplb.server;

import net.bichal.bichalutils.util.DistanceUtils;
import net.bichal.bichalutils.util.Logger;
import net.bichal.bplb.network.HandshakePayload;
import net.bichal.bplb.network.PositionUpdatePayload;
import net.bichal.bplb.server.datapack.DatapackExtractor;
import net.bichal.bplb.server.datapack.DatapackHandler;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements DedicatedServerModInitializer {
    private final PlayerTracker playerTracker = new PlayerTracker();
    private static final long PLAYER_LIST_CACHE_DURATION = 50L;
    private final Set<UUID> newPlayers = ConcurrentHashMap.newKeySet(16);
    private final Set<UUID> disconnectedPlayers = ConcurrentHashMap.newKeySet(16);
    private final Map<UUID, Map<UUID, Double>> distanceCache = new ConcurrentHashMap<>(32);
    private final Map<UUID, CachedPlayerData> playerDataCache = new ConcurrentHashMap<>(64);
    private long lastUpdateTime = 0;
    private final ExecutorService executor = Executors.newFixedThreadPool(
            Math.max(1, Runtime.getRuntime().availableProcessors() / 2), r -> {
                Thread t = new Thread(r, "BPLB-Worker");
                t.setDaemon(true);
                return t;
            }
    );
    private List<ServerPlayerEntity> playerListCache = new ArrayList<>();
    private long playerListCacheTime = 0;
    private boolean datapackExtracted = false;

    @Override
    public void onInitializeServer() {
        Logger.info("Initializing server!");
        ServerConfig.getInstance();
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (handler == null || handler.player == null) return;
            UUID playerId = handler.player.getUuid();
            if (playerId == null) return;
            playerTracker.updatePlayer(handler.player);
            newPlayers.add(playerId);
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
            disconnectedPlayers.add(playerId);
            playerTracker.removePlayer(playerId);
            newPlayers.remove(playerId);
            distanceCache.remove(playerId);
            invalidatePlayerListCache();
        });
        ServerTickEvents.END_SERVER_TICK.register(this::tick);
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            if (!datapackExtracted) {
                DatapackExtractor.extractDatapack(server);
                datapackExtracted = true;
            }
        });
        Logger.info("Server initialized!");
    }

    private void tick(@Nullable MinecraftServer server) {
        if (server == null) return;

        ServerConfig config = ServerConfig.getInstance();
        if (config.useDatapackFallback()) {
            List<ServerPlayerEntity> players = getCachedPlayerList(server);
            DatapackHandler.sendDatapackPositions(players);
            return;
        }

        long currentTime = server.getTicks();
        List<ServerPlayerEntity> players = getCachedPlayerList(server);

        if (players.size() > 10) {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            int chunkSize = Math.max(5, players.size() / 4);

            for (int start = 0; start < players.size(); start += chunkSize) {
                int end = Math.min(start + chunkSize, players.size());
                List<ServerPlayerEntity> chunk = players.subList(start, end);

                futures.add(CompletableFuture.runAsync(() -> {
                    for (ServerPlayerEntity player : chunk) {
                        if (player != null) {
                            playerTracker.updatePlayer(player);
                        }
                    }
                }, executor));
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } else {
            for (ServerPlayerEntity player : players) {
                if (player != null) {
                    playerTracker.updatePlayer(player);
                }
            }
        }

        if (currentTime - lastUpdateTime >= config.positionUpdateRateTicks()) {
            sendUpdate(server);
            lastUpdateTime = currentTime;
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

    private void sendUpdate(MinecraftServer server) {
        if (server == null) return;
        List<ServerPlayerEntity> players = getCachedPlayerList(server);
        if (players.isEmpty()) return;
        List<PositionUpdatePayload.PlayerInfo> newPlayerInfos = new ArrayList<>();
        Map<UUID, PlayerTracker.PlayerPosition> initialPositions = new HashMap<>();

        newPlayers.removeIf(uuid -> {
            if (uuid == null) return true;
            PlayerTracker.PlayerInfo info = playerTracker.getPlayerInfo(uuid);
            if (info != null) {
                newPlayerInfos.add(new PositionUpdatePayload.PlayerInfo(uuid, info.name));

                ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
                if (player != null) {
                    PlayerTracker.PlayerPosition pos = new PlayerTracker.PlayerPosition(
                            player.getX(), player.getY(), player.getZ()
                    );
                    initialPositions.put(uuid, pos);
                }
                return true;
            }
            return false;
        });

        Map<UUID, PlayerTracker.PlayerPosition> movedPlayersData = new HashMap<>(playerTracker.getAndClearMovedPlayers());
        movedPlayersData.putAll(initialPositions);

        for (ServerPlayerEntity player : players) {
            if (player != null && !movedPlayersData.containsKey(player.getUuid())) {
                PlayerTracker.PlayerPosition pos = new PlayerTracker.PlayerPosition(
                        player.getX(), player.getY(), player.getZ()
                );
                movedPlayersData.put(player.getUuid(), pos);
            }
        }

        if (movedPlayersData.isEmpty() && newPlayerInfos.isEmpty() && disconnectedPlayers.isEmpty()) {
            return;
        }
        for (ServerPlayerEntity viewer : players) {
            if (viewer == null) continue;
            UUID viewerId = viewer.getUuid();
            if (viewerId == null) continue;
            List<PositionUpdatePayload.PositionData> positions = new ArrayList<>();
            Map<UUID, Double> viewerDistCache = distanceCache.computeIfAbsent(viewerId, k -> new ConcurrentHashMap<>());
            double vx = viewer.getX();
            double vy = viewer.getY();
            double vz = viewer.getZ();

            long now = System.currentTimeMillis();
            for (Map.Entry<UUID, PlayerTracker.PlayerPosition> entry : movedPlayersData.entrySet()) {
                UUID uuid = entry.getKey();
                PlayerTracker.PlayerPosition pos = entry.getValue();
                if (uuid == null || pos == null || uuid.equals(viewerId)) continue;

                CachedPlayerData cached = playerDataCache.get(uuid);
                if (cached != null && cached.isValid(now)) {
                    if (cached.shouldHide) continue;
                    double distance = DistanceUtils.calculateDistance(vx, vy, vz, cached.x, cached.y, cached.z);
                    if (distance > ServerConfig.getInstance().maxRelevantDistance()) {
                        viewerDistCache.remove(uuid);
                        continue;
                    }
                    viewerDistCache.put(uuid, distance);
                    positions.add(new PositionUpdatePayload.PositionData(uuid, pos.x, pos.y, pos.z, distance));
                    continue;
                }

                ServerPlayerEntity targetPlayer = server.getPlayerManager().getPlayer(uuid);
                if (targetPlayer == null) continue;
                if (viewer.getWorld() != targetPlayer.getWorld()) continue;

                boolean shouldHide = shouldHideTarget(targetPlayer);
                String playerName = targetPlayer.getName().getString();
                playerDataCache.put(uuid, new CachedPlayerData(pos.x, pos.y, pos.z, shouldHide, playerName));

                if (shouldHide) continue;
                if (targetPlayer.isSpectator()) continue;

                double distance = DistanceUtils.calculateDistance(vx, vy, vz, pos.x, pos.y, pos.z);
                if (distance > ServerConfig.getInstance().maxRelevantDistance()) {
                    viewerDistCache.remove(uuid);
                    continue;
                }
                viewerDistCache.put(uuid, distance);
                playerTracker.updatePlayer(targetPlayer);
                positions.add(new PositionUpdatePayload.PositionData(uuid, pos.x, pos.y, pos.z, distance));
            }

            if (!newPlayerInfos.isEmpty() || !positions.isEmpty() || !disconnectedPlayers.isEmpty()) {
                PositionUpdatePayload payload = new PositionUpdatePayload(newPlayerInfos, positions, new ArrayList<>(disconnectedPlayers)
                );

                if (ServerPlayNetworking.canSend(viewer, PositionUpdatePayload.ID)) {
                    ServerPlayNetworking.send(viewer, payload);
                }
            }
        }

        disconnectedPlayers.clear();
    }

    private static class CachedPlayerData {
        final double x, y, z;
        final boolean shouldHide;
        final String playerName;
        final long timestamp;

        CachedPlayerData(double x, double y, double z, boolean shouldHide, String playerName) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.shouldHide = shouldHide;
            this.playerName = playerName;
            this.timestamp = System.currentTimeMillis();
        }

        boolean isValid(long now) {
            return (now - timestamp) < 50L;
        }
    }

    private boolean shouldHideTarget(ServerPlayerEntity target) {
        ItemStack headStack = target.getEquippedStack(EquipmentSlot.HEAD);
        return target.isSneaking() || target.isInvisible() || (!headStack.isEmpty() && !(headStack.getItem() instanceof ArmorItem));
    }
}
