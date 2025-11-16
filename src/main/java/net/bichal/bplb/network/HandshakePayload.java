package net.bichal.bplb.network;

import net.bichal.bichalutils.util.ModIdentifier;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record HandshakePayload(boolean playerHasOp) implements CustomPayload {
    public static final Id<HandshakePayload> ID = new Id<>(ModIdentifier.ofMod("handshake"));

    public static final PacketCodec<PacketByteBuf, HandshakePayload> CODEC = new PacketCodec<>() {
        @Override
        public HandshakePayload decode(PacketByteBuf buf) {
            return new HandshakePayload(buf.readBoolean());
        }

        @Override
        public void encode(PacketByteBuf buf, HandshakePayload payload) {
            buf.writeBoolean(payload.playerHasOp());
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
