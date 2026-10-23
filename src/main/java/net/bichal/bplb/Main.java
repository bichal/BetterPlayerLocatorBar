package net.bichal.bplb;

import net.bichal.bplb.platform.Platform;
//? if <1.21.6 {
import net.bichal.bplb.server.Server;
//?}
import net.bichal.bplb.util.Constants;

/** Loader independent entry point; every loader calls {@link #init} from its own entrypoint. */
public final class Main {
    //? if <1.21.6 {
    private static final Server SERVER = new Server();
    //?}

    private Main() {
    }

    public static void init(Platform.Provider provider) {
        Platform.install(provider);
        Constants.LOGGER.info("[{}] Mod initialization initialized", Constants.MOD_NAME_SHORT);
        //? if <1.21.6 {
        SERVER.init();
        //?}
        Constants.LOGGER.info("[{}] Mod initialization finished", Constants.MOD_NAME_SHORT);
    }

    //? if <1.21.6 {
    public static Server server() {
        return SERVER;
    }
    //?}
}
