package net.bichal.bplb.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.zip.Deflater;

/**
 * Not a regression test: measures what one position update costs, to judge whether a faster
 * serializer or packet compression would matter.
 * Run with {@code ./gradlew :1.21.1-fabric:test -Dbplb.bench=true -i --tests '*PayloadCostBench'}.
 * A plain nanoTime loop, not JMH: the numbers only need to be right to an order of magnitude.
 */
@EnabledIfSystemProperty(named = "bplb.bench", matches = "true")
class PayloadCostBench {
    private static PositionUpdatePayload payload(int players, Random random) {
        List<PositionUpdatePayload.PositionData> positions = new ArrayList<>();
        for (int i = 0; i < players; i++) {
            double distance = random.nextDouble() * 400;
            positions.add(new PositionUpdatePayload.PositionData(new UUID(random.nextLong(), random.nextLong()),
                    random.nextDouble() * 2000 - 1000, 60 + random.nextDouble() * 40, random.nextDouble() * 2000 - 1000, distance));
        }
        return new PositionUpdatePayload(List.of(), positions, List.of());
    }

    private static int deflated(byte[] data, int level) {
        Deflater deflater = new Deflater(level);
        deflater.setInput(data);
        deflater.finish();
        byte[] out = new byte[data.length * 2 + 64];
        int size = deflater.deflate(out);
        deflater.end();
        return size;
    }

    @Test
    void measure() {
        Random random = new Random(42);
        for (int players : new int[]{10, 50, 200}) {
            PositionUpdatePayload payload = payload(players, random);
            FriendlyByteBuf first = new FriendlyByteBuf(Unpooled.buffer());
            PositionUpdatePayload.CODEC.encode(first, payload);
            byte[] bytes = new byte[first.readableBytes()];
            first.getBytes(0, bytes);

            int rounds = 20_000;
            for (int i = 0; i < rounds; i++) { // warm up
                FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer(bytes.length));
                PositionUpdatePayload.CODEC.encode(buf, payload);
                PositionUpdatePayload.CODEC.decode(buf);
            }
            long start = System.nanoTime();
            for (int i = 0; i < rounds; i++) {
                FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer(bytes.length));
                PositionUpdatePayload.CODEC.encode(buf, payload);
                PositionUpdatePayload.CODEC.decode(buf);
            }
            double roundTripMicros = (System.nanoTime() - start) / 1000.0 / rounds;

            long dStart = System.nanoTime();
            int compressed = 0;
            for (int i = 0; i < 2000; i++) compressed = deflated(bytes, 1);
            double deflateMicros = (System.nanoTime() - dStart) / 1000.0 / 2000;

            System.out.printf("BENCH players=%d raw=%dB deflate1=%dB (%.0f%%) encode+decode=%.1f us deflate=%.1f us%n",
                    players, bytes.length, compressed, 100.0 * compressed / bytes.length, roundTripMicros, deflateMicros);
        }
    }
}
