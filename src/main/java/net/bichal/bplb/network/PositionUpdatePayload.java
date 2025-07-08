package net.bichal.bplb.network;

import net.bichal.bplb.util.Constants;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record PositionUpdatePayload(List<PlayerInfo> newPlayers, List<PositionData> positions,
                                    List<UUID> disconnectedPlayers) implements CustomPayload {

    public static final CustomPayload.Id<PositionUpdatePayload> ID = new CustomPayload.Id<>(Identifier.of(Constants.MOD_ID, "optimized_position_update"));
    public static final PacketCodec<PacketByteBuf, PositionUpdatePayload> CODEC = PacketCodec.of(PositionUpdatePayload::write, PositionUpdatePayload::read);

    public static PositionUpdatePayload read(PacketByteBuf buf) {
        int newPlayersSize = buf.readVarInt();
        List<PlayerInfo> newPlayers = new ArrayList<>(newPlayersSize);
        for (int i = 0; i < newPlayersSize; i++) {
            newPlayers.add(new PlayerInfo(buf.readUuid(), buf.readString()));
        }

        int positionsSize = buf.readVarInt();
        List<PositionData> positions = new ArrayList<>(positionsSize);
        for (int i = 0; i < positionsSize; i++) {
            positions.add(new PositionData(buf.readUuid(), buf.readDouble(), buf.readDouble(), buf.readDouble()));
        }

        int disconnectedSize = buf.readVarInt();
        List<UUID> disconnected = new ArrayList<>(disconnectedSize);
        for (int i = 0; i < disconnectedSize; i++) {
            disconnected.add(buf.readUuid());
        }

        return new PositionUpdatePayload(newPlayers, positions, disconnected);
    }

    public void write(PacketByteBuf buf) {
        buf.writeVarInt(newPlayers.size());
        for (PlayerInfo player : newPlayers) {
            buf.writeUuid(player.uuid());
            buf.writeString(player.name());
        }

        buf.writeVarInt(positions.size());
        for (PositionData pos : positions) {
            buf.writeUuid(pos.uuid());
            buf.writeDouble(pos.x());
            buf.writeDouble(pos.y());
            buf.writeDouble(pos.z());
        }

        buf.writeVarInt(disconnectedPlayers.size());
        for (UUID uuid : disconnectedPlayers) {
            buf.writeUuid(uuid);
        }
    }

    @Override public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public record PlayerInfo(UUID uuid, String name) {
    }

    public record PositionData(UUID uuid, double x, double y, double z) {
    }
}
