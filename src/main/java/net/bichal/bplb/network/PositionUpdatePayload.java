package net.bichal.bplb.network;

import net.bichal.bplb.util.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record PositionUpdatePayload(List<PlayerInfo> newPlayers, List<PositionData> positions, List<UUID> disconnectedPlayers) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PositionUpdatePayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "position_update"));
    public static final StreamCodec<FriendlyByteBuf, PositionUpdatePayload> CODEC = StreamCodec.ofMember(PositionUpdatePayload::write, PositionUpdatePayload::read);
    private static final byte PRECISION_ULTRA = 0;
    private static final byte PRECISION_HIGH = 1;
    private static final byte PRECISION_MEDIUM = 2;
    /** Upper bound for every list read from the wire; anything larger is a malformed packet. */
    private static final int MAX_ENTRIES = 4096;

    private static int readSize(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        if (size < 0 || size > MAX_ENTRIES) {
            throw new io.netty.handler.codec.DecoderException("Payload list too large: " + size);
        }
        return size;
    }

    public static PositionUpdatePayload read(FriendlyByteBuf buf) {
        int newPlayersSize = readSize(buf);
        List<PlayerInfo> newPlayers = new ArrayList<>(newPlayersSize);
        for (int i = 0; i < newPlayersSize; i++) {
            newPlayers.add(new PlayerInfo(buf.readUUID(), buf.readUtf(16)));
        }
        int positionsSize = readSize(buf);
        List<PositionData> positions = new ArrayList<>(positionsSize);
        for (int i = 0; i < positionsSize; i++) {
            byte precision = buf.readByte();
            UUID uuid = buf.readUUID();
            double x, y, z;
            switch (precision) {
                case PRECISION_ULTRA -> {
                    x = buf.readInt() / 100.0;
                    y = buf.readInt() / 100.0;
                    z = buf.readInt() / 100.0;
                }
                case PRECISION_HIGH -> {
                    x = buf.readInt() / 10.0;
                    y = buf.readInt() / 10.0;
                    z = buf.readInt() / 10.0;
                }
                default -> {
                    x = buf.readInt();
                    y = buf.readInt();
                    z = buf.readInt();
                }
            }
            positions.add(new PositionData(uuid, x, y, z, 0.0));
        }
        int disconnectedSize = readSize(buf);
        List<UUID> disconnected = new ArrayList<>(disconnectedSize);
        for (int i = 0; i < disconnectedSize; i++) {
            disconnected.add(buf.readUUID());
        }
        return new PositionUpdatePayload(newPlayers, positions, disconnected);
    }

    private static byte determinePrecision(double distance) {
        if (distance <= 32.0) return PRECISION_ULTRA;
        if (distance < 64.0) return PRECISION_HIGH;
        return PRECISION_MEDIUM;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(newPlayers.size());
        for (PlayerInfo player : newPlayers) {
            buf.writeUUID(player.uuid());
            buf.writeUtf(player.name(), 16);
        }
        buf.writeVarInt(positions.size());
        for (PositionData pos : positions) {
            byte precision = determinePrecision(pos.distance());
            buf.writeByte(precision);
            buf.writeUUID(pos.uuid());
            switch (precision) {
                case PRECISION_ULTRA -> {
                    buf.writeInt((int) (pos.x() * 100.0));
                    buf.writeInt((int) (pos.y() * 100.0));
                    buf.writeInt((int) (pos.z() * 100.0));
                }
                case PRECISION_HIGH -> {
                    buf.writeInt((int) (pos.x() * 10.0));
                    buf.writeInt((int) (pos.y() * 10.0));
                    buf.writeInt((int) (pos.z() * 10.0));
                }
                default -> {
                    buf.writeInt((int) pos.x());
                    buf.writeInt((int) pos.y());
                    buf.writeInt((int) pos.z());
                }
            }
        }
        buf.writeVarInt(disconnectedPlayers.size());
        for (UUID uuid : disconnectedPlayers) {
            buf.writeUUID(uuid);
        }
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record PlayerInfo(UUID uuid, String name) {
    }

    public record PositionData(UUID uuid, double x, double y, double z, double distance) {
    }
}
