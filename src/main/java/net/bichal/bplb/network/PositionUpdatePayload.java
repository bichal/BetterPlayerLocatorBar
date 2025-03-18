package net.bichal.bplb.network;

import net.bichal.bplb.BetterPlayerLocatorBar;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.UUID;

public record PositionUpdatePayload(List<PlayerPosition> positions) implements CustomPayload {
    public static final CustomPayload.Id<PositionUpdatePayload> ID = new CustomPayload.Id<>(Identifier.of(BetterPlayerLocatorBar.MOD_ID, "position_update"));

    public static PositionUpdatePayload read(PacketByteBuf buf) {
        int size = buf.readInt();
        List<PlayerPosition> list = new java.util.ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(new PlayerPosition(buf.readUuid(), buf.readDouble(), buf.readDouble(), buf.readDouble()));
        }
        return new PositionUpdatePayload(list);
    }

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
        buf.writeInt(positions.size());
        for (PlayerPosition pos : positions) {
            buf.writeUuid(pos.uuid());
            buf.writeDouble(pos.x());
            buf.writeDouble(pos.y());
            buf.writeDouble(pos.z());
        }
    }

    public record PlayerPosition(UUID uuid, double x, double y, double z) {
    }
}
