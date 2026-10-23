package net.bichal.bplb.platform;

//? if !waypoints {
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
//?}

import java.nio.file.Path;

/**
 * The few things the common code needs from the mod loader. Each loader installs its
 * {@link Provider} from its entrypoint before any other mod class is touched.
 */
public final class Platform {
    public interface Provider {
        Path configDir();

        Path gameDir();

        //? if !waypoints {
        /** Whether the client behind {@code player} registered the payload channel. */
        boolean canSend(ServerPlayer player, CustomPacketPayload.Type<?> type);

        void send(ServerPlayer player, CustomPacketPayload payload);
        //?}
    }

    private static volatile Provider provider;

    private Platform() {
    }

    public static void install(Provider newProvider) {
        provider = newProvider;
    }

    public static Path configDir() {
        Provider p = provider;
        return p != null ? p.configDir() : Path.of("config");
    }

    public static Path gameDir() {
        Provider p = provider;
        return p != null ? p.gameDir() : Path.of(".");
    }

    //? if !waypoints {
    public static boolean canSend(ServerPlayer player, CustomPacketPayload.Type<?> type) {
        Provider p = provider;
        return p != null && p.canSend(player, type);
    }

    public static void send(ServerPlayer player, CustomPacketPayload payload) {
        Provider p = provider;
        if (p != null) p.send(player, payload);
    }
    //?}
}
