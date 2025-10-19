package net.bichal.bplb.network;

import net.bichal.bplb.util.Constants;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record PositionUpdatePayload(List<PlayerInfo> newPlayers, List<PositionData> positions, List<UUID> disconnectedPlayers) implements CustomPayload {
    public static final CustomPayload.Id<PositionUpdatePayload> ID = new CustomPayload.Id<>(Identifier.of(Constants.MOD_ID, "position_update"));
    public static final PacketCodec<PacketByteBuf, PositionUpdatePayload> CODEC = PacketCodec.of(PositionUpdatePayload::write, PositionUpdatePayload::read);
    private static final byte PRECISION_ULTRA = 0;
    private static final byte PRECISION_HIGH = 1;
    private static final byte PRECISION_MEDIUM = 2;

    public static PositionUpdatePayload read(PacketByteBuf buf) {
        int newPlayersSize = buf.readVarInt();
        List<PlayerInfo> newPlayers = new ArrayList<>(newPlayersSize);
        for (int i = 0; i < newPlayersSize; i++) {
            newPlayers.add(new PlayerInfo(buf.readUuid(), buf.readString(16)));
        }
        int positionsSize = buf.readVarInt();
        List<PositionData> positions = new ArrayList<>(positionsSize);
        for (int i = 0; i < positionsSize; i++) {
            byte precision = buf.readByte();
            UUID uuid = buf.readUuid();
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
        int disconnectedSize = buf.readVarInt();
        List<UUID> disconnected = new ArrayList<>(disconnectedSize);
        for (int i = 0; i < disconnectedSize; i++) {
            disconnected.add(buf.readUuid());
        }
        return new PositionUpdatePayload(newPlayers, positions, disconnected);
    }

    private static byte determinePrecision(double distance) {
        if (distance <= 32.0) return PRECISION_ULTRA;
        if (distance < 64.0) return PRECISION_HIGH;
        return PRECISION_MEDIUM;
    }

    public void write(PacketByteBuf buf) {
        buf.writeVarInt(newPlayers.size());
        for (PlayerInfo player : newPlayers) {
            buf.writeUuid(player.uuid());
            buf.writeString(player.name(), 16);
        }
        buf.writeVarInt(positions.size());
        for (PositionData pos : positions) {
            byte precision = determinePrecision(pos.distance());
            buf.writeByte(precision);
            buf.writeUuid(pos.uuid());
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
            buf.writeUuid(uuid);
        }
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public record PlayerInfo(UUID uuid, String name) {
    }

    public record PositionData(UUID uuid, double x, double y, double z, double distance) {
    }
}
