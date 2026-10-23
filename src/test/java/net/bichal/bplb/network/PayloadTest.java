package net.bichal.bplb.network;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PayloadTest {
    private static FriendlyByteBuf buffer() {
        return new FriendlyByteBuf(Unpooled.buffer());
    }

    @Test
    void handshakeRoundTrip() {
        for (boolean op : new boolean[]{true, false}) {
            FriendlyByteBuf buf = buffer();
            HandshakePayload.CODEC.encode(buf, new HandshakePayload(op));
            assertEquals(op, HandshakePayload.CODEC.decode(buf).playerHasOp());
        }
    }

    @Test
    void positionUpdateRoundTripKeepsPrecisionByDistance() {
        UUID near = new UUID(1, 1);
        UUID mid = new UUID(2, 2);
        UUID far = new UUID(3, 3);
        UUID gone = new UUID(4, 4);
        PositionUpdatePayload sent = new PositionUpdatePayload(
                List.of(new PositionUpdatePayload.PlayerInfo(near, "Near")),
                List.of(
                        new PositionUpdatePayload.PositionData(near, 10.25, 64.5, -3.75, 5.0),
                        new PositionUpdatePayload.PositionData(mid, 100.5, 70.0, 200.5, 50.0),
                        new PositionUpdatePayload.PositionData(far, 1234.0, 80.0, -4321.0, 300.0)),
                List.of(gone));

        FriendlyByteBuf buf = buffer();
        PositionUpdatePayload.CODEC.encode(buf, sent);
        PositionUpdatePayload got = PositionUpdatePayload.CODEC.decode(buf);

        assertEquals(0, buf.readableBytes(), "the whole packet is consumed");
        assertEquals(List.of(new PositionUpdatePayload.PlayerInfo(near, "Near")), got.newPlayers());
        assertEquals(List.of(gone), got.disconnectedPlayers());
        assertEquals(3, got.positions().size());

        PositionUpdatePayload.PositionData a = got.positions().get(0);
        assertEquals(10.25, a.x(), 0.01);
        assertEquals(64.5, a.y(), 0.01);
        assertEquals(-3.75, a.z(), 0.01);
        PositionUpdatePayload.PositionData b = got.positions().get(1);
        assertEquals(100.5, b.x(), 0.1);
        assertEquals(200.5, b.z(), 0.1);
        PositionUpdatePayload.PositionData c = got.positions().get(2);
        assertEquals(1234.0, c.x(), 1.0);
        assertEquals(-4321.0, c.z(), 1.0);
    }

    @Test
    void emptyUpdateRoundTrip() {
        FriendlyByteBuf buf = buffer();
        PositionUpdatePayload.CODEC.encode(buf, new PositionUpdatePayload(List.of(), List.of(), List.of()));
        PositionUpdatePayload got = PositionUpdatePayload.CODEC.decode(buf);
        assertTrue(got.newPlayers().isEmpty());
        assertTrue(got.positions().isEmpty());
        assertFalse(buf.isReadable());
    }

    @Test
    void oversizedListsAreRejected() {
        FriendlyByteBuf buf = buffer();
        buf.writeVarInt(1_000_000);
        assertThrows(DecoderException.class, () -> PositionUpdatePayload.CODEC.decode(buf));

        FriendlyByteBuf negative = buffer();
        negative.writeVarInt(-1);
        assertThrows(DecoderException.class, () -> PositionUpdatePayload.CODEC.decode(negative));
    }

    @Test
    void namesAreCappedAtSixteenCharacters() {
        FriendlyByteBuf buf = buffer();
        buf.writeVarInt(1);
        buf.writeUUID(new UUID(9, 9));
        buf.writeUtf("this name is far too long", 255);
        assertThrows(DecoderException.class, () -> PositionUpdatePayload.CODEC.decode(buf));
    }
}
