package net.bichal.bplb.client;

/**
 * Whether the server is feeding us positions (remote mode) or we fall back to the players the
 * client itself can see (local mode). Time is passed in so the logic can be tested.
 */
public final class ServerLink {
    public static final long TIMEOUT_MS = 5000;

    private boolean local = true;
    private boolean playerHasOp;
    private long lastUpdate;

    public boolean isLocal() {
        return local;
    }

    public boolean playerHasOp() {
        return playerHasOp;
    }

    /** The server announced that it runs the mod. */
    public void onHandshake(boolean playerHasOp, long now) {
        this.local = false;
        this.playerHasOp = playerHasOp;
        this.lastUpdate = now;
    }

    /**
     * A position update arrived. The server sends nothing while a player is alone, so a timeout
     * may already have switched us to local mode; any update proves the server is there again.
     */
    public void onUpdate(long now) {
        this.local = false;
        this.lastUpdate = now;
    }

    /** Returns true when this call switched to local mode because the server went quiet. */
    public boolean checkTimeout(long now) {
        if (!local && now - lastUpdate > TIMEOUT_MS) {
            local = true;
            return true;
        }
        return false;
    }

    public void reset() {
        local = true;
        playerHasOp = false;
        lastUpdate = 0;
    }
}
