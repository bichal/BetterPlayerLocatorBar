package net.bichal.bplb;

import net.bichal.bplb.platform.Platform;
import net.bichal.bplb.server.Server;
import net.bichal.bplb.util.Constants;

/** Loader independent entry point; every loader calls {@link #init} from its own entrypoint. */
public final class Main {
    private static final Server SERVER = new Server();

    private Main() {
    }

    public static void init(Platform.Provider provider) {
        Platform.install(provider);
        Constants.LOGGER.info("[{}] Mod initialization initialized", Constants.MOD_NAME_SHORT);
        SERVER.init();
        Constants.LOGGER.info("[{}] Mod initialization finished", Constants.MOD_NAME_SHORT);
    }

    public static Server server() {
        return SERVER;
    }
}
