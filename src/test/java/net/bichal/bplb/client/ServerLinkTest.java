package net.bichal.bplb.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerLinkTest {
    @Test
    void startsLocalWithoutAHandshake() {
        ServerLink link = new ServerLink();
        assertTrue(link.isLocal());
        assertFalse(link.playerHasOp());
    }

    @Test
    void handshakeSwitchesToRemoteMode() {
        ServerLink link = new ServerLink();
        link.onHandshake(true);
        assertFalse(link.isLocal());
        assertTrue(link.playerHasOp());
    }

    /**
     * Regression: the server sends no updates while a player is alone. Silence used to drop the client
     * back to local mode for good, so players that joined later were only seen inside render distance.
     * A server known to have the mod stays in remote mode for the whole connection.
     */
    @Test
    void remoteModeDoesNotDependOnUpdatesArriving() {
        ServerLink link = new ServerLink();
        link.onHandshake(false);
        // nothing else ever happens: there is no timeout path any more
        assertFalse(link.isLocal());
    }

    @Test
    void disconnectReturnsToLocalMode() {
        ServerLink link = new ServerLink();
        link.onHandshake(true);
        link.reset();
        assertTrue(link.isLocal());
        assertFalse(link.playerHasOp());
    }
}
