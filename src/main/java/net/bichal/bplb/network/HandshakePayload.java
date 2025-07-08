package net.bichal.bplb.network;

import net.bichal.bplb.util.Constants;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record HandshakePayload() implements CustomPayload {
    public static final Id<HandshakePayload> ID = new Id<>(Identifier.of(Constants.MOD_ID, "handshake"));
    public static final PacketCodec<PacketByteBuf, HandshakePayload> CODEC = PacketCodec.unit(new HandshakePayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
