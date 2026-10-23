package net.bichal.bplb.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerLinkTest {
    @Test
    void startsLocalAndHandshakeSwitchesToRemote() {
        ServerLink link = new ServerLink();
        assertTrue(link.isLocal());
        link.onHandshake(true, 1000);
        assertFalse(link.isLocal());
        assertTrue(link.playerHasOp());
    }

    @Test
    void silenceSwitchesToLocalAfterTheTimeout() {
        ServerLink link = new ServerLink();
        link.onHandshake(false, 0);
        assertFalse(link.checkTimeout(ServerLink.TIMEOUT_MS));
        assertTrue(link.checkTimeout(ServerLink.TIMEOUT_MS + 1));
        assertTrue(link.isLocal());
        assertFalse(link.checkTimeout(ServerLink.TIMEOUT_MS * 3), "only reports the switch once");
    }

    /**
     * Regression: a lone player gets no updates, so the link timed out to local mode; when a second
     * player (for example a Carpet bot) joined, updates came in again but the client stayed local and
     * never showed players beyond render distance.
     */
    @Test
    void anUpdateAfterATimeoutRestoresRemoteMode() {
        ServerLink link = new ServerLink();
        link.onHandshake(false, 0);
        link.checkTimeout(10_000);
        assertTrue(link.isLocal());

        link.onUpdate(12_000);
        assertFalse(link.isLocal());
        assertFalse(link.checkTimeout(12_000 + ServerLink.TIMEOUT_MS));
    }

    @Test
    void resetReturnsToLocal() {
        ServerLink link = new ServerLink();
        link.onHandshake(true, 5);
        link.reset();
        assertTrue(link.isLocal());
        assertFalse(link.playerHasOp());
    }
}
