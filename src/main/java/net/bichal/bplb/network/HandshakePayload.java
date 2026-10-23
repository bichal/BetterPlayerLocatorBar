package net.bichal.bplb.network;

import net.bichal.bplb.util.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record HandshakePayload(boolean playerHasOp) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<HandshakePayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "handshake"));

    public static final StreamCodec<FriendlyByteBuf, HandshakePayload> CODEC = new StreamCodec<>() {
        @Override
        public HandshakePayload decode(FriendlyByteBuf buf) {
            return new HandshakePayload(buf.readBoolean());
        }

        @Override
        public void encode(FriendlyByteBuf buf, HandshakePayload payload) {
            buf.writeBoolean(payload.playerHasOp());
        }
    };

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
