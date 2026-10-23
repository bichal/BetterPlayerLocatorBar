package net.bichal.bplb.waypoint;

import java.util.Collection;
import java.util.Optional;

/**
 * The per-move work of the vanilla server waypoint manager ({@code ServerWaypointManager.updateWaypoint}
 * and {@code updatePlayer}), written as one pass over the other side's collection.
 * <p>
 * Vanilla builds the receivers of a transmitter through a Guava column view, copies the entries into an
 * immutable set, and then walks a second, lazily filtered view for the pairs without a connection. Each
 * step costs a hash lookup or an allocation per player, and every movement of every player runs it
 * twice. The result is the same set of connection changes, so this class keeps the work to one lookup
 * per pair and reads the locator bar game rule once per call instead of once per pair.
 * <p>
 * What stays the same, pair by pair: which connection is updated, created, replaced or dropped, and the
 * packets each connection sends. What can differ: the order of packets between different pairs of one
 * call (vanilla sends all updates before all creations). Every packet belongs to one waypoint of one
 * receiver and the client keeps them apart by UUID, so the order between pairs is not observable.
 * {@code WaypointSyncTest} compares both against a port of the vanilla algorithm.
 * <p>
 * Precondition for both entry points: the game rule is on. Vanilla does nothing for any pair when it is
 * off, and all players of one manager share the server's rule, so callers return before calling.
 */
public final class WaypointSync {
    private WaypointSync() {
    }

    /** Property that switches the optimisation off: {@code -Dbplb.optimizeWaypoints=false}. */
    public static boolean enabled() {
        return !"false".equalsIgnoreCase(System.getProperty("bplb.optimizeWaypoints"));
    }

    /** Access to the manager's state; the game classes sit behind it so the algorithm can be tested alone. */
    public interface Ops<P, T, C> {
        C get(P receiver, T transmitter);

        void put(P receiver, T transmitter, C connection);

        /** Removes and returns the connection, or null. */
        C remove(P receiver, T transmitter);

        /** {@code transmitter.makeWaypointConnectionWith(receiver)}. */
        Optional<C> make(T transmitter, P receiver);

        boolean isBroken(C connection);

        void update(C connection);

        void connect(C connection);

        void disconnect(C connection);
    }

    /** A transmitter moved or changed: bring every receiver's connection to it up to date. */
    public static <P, T, C> void updateWaypoint(Collection<P> players, T transmitter, Ops<P, T, C> ops) {
        for (P player : players) {
            if ((Object) player == transmitter) continue;
            C connection = ops.get(player, transmitter);
            if (connection != null) {
                updateConnection(player, transmitter, connection, ops);
            } else {
                createConnection(player, transmitter, ops);
            }
        }
    }

    /** A receiver moved: bring its connections to every transmitter up to date. */
    public static <P, T, C> void updatePlayer(P player, Collection<T> waypoints, Ops<P, T, C> ops) {
        for (T transmitter : waypoints) {
            if ((Object) player == transmitter) continue;
            C connection = ops.get(player, transmitter);
            if (connection != null) {
                updateConnection(player, transmitter, connection, ops);
            } else {
                createConnection(player, transmitter, ops);
            }
        }
    }

    private static <P, T, C> void createConnection(P player, T transmitter, Ops<P, T, C> ops) {
        Optional<C> made = ops.make(transmitter, player);
        if (made.isPresent()) {
            C connection = made.get();
            ops.put(player, transmitter, connection);
            ops.connect(connection);
        } else {
            C old = ops.remove(player, transmitter);
            if (old != null) ops.disconnect(old);
        }
    }

    private static <P, T, C> void updateConnection(P player, T transmitter, C connection, Ops<P, T, C> ops) {
        if (!ops.isBroken(connection)) {
            ops.update(connection);
            return;
        }
        Optional<C> made = ops.make(transmitter, player);
        if (made.isPresent()) {
            C replacement = made.get();
            ops.connect(replacement);
            ops.put(player, transmitter, replacement);
        } else {
            ops.disconnect(connection);
            ops.remove(player, transmitter);
        }
    }
}
