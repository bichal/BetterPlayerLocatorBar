package net.bichal.bplb.client;

/**
 * Whether the server we are connected to runs the mod.
 * <ul>
 *   <li>Remote mode: the server sent its handshake. Positions of every player come from the server,
 *       however far away they are, for as long as the connection lasts. Silence only means nobody
 *       else is around (the server sends nothing while a player is alone); it never ends remote mode.</li>
 *   <li>Local mode: no handshake. Only the players the client itself can see are shown.</li>
 * </ul>
 */
public final class ServerLink {
    private boolean remote;
    private boolean playerHasOp;

    public boolean isLocal() {
        return !remote;
    }

    public boolean playerHasOp() {
        return playerHasOp;
    }

    /** The server announced that it runs the mod. */
    public void onHandshake(boolean playerHasOp) {
        this.remote = true;
        this.playerHasOp = playerHasOp;
    }

    /** Back to local mode when the connection ends. */
    public void reset() {
        remote = false;
        playerHasOp = false;
    }
}
