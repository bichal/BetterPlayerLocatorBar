package net.bichal.bplb.waypoint;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runs {@link WaypointSync} and a port of vanilla's {@code ServerWaypointManager} (1.21.6 to 26.3) on the
 * same random worlds and compares what every (receiver, transmitter) pair ends up with and the events
 * its connection saw, in order.
 */
class WaypointSyncTest {
    record Node(int id) {
    }

    /** A connection: only its kind and the epoch it was made in matter. */
    record Conn(Node receiver, Node transmitter, int kind, int epoch) {
    }

    /** The game: pure functions of (transmitter, receiver, epoch) plus an event log per pair. */
    static final class World {
        int epoch;
        final long seed;
        final Map<String, List<String>> events = new LinkedHashMap<>();

        World(long seed) {
            this.seed = seed;
        }

        int hash(Node t, Node p, int salt) {
            long h = seed * 31 + t.id() * 1_000_003L + p.id() * 10_007L + epoch * 101L + salt * 7L;
            h ^= (h >>> 17);
            h *= 0x9E3779B97F4A7C15L;
            h ^= (h >>> 29);
            return (int) (h & 0x7fffffff);
        }

        Optional<Conn> make(Node t, Node p) {
            if (hash(t, p, 1) % 3 == 0) return Optional.empty();
            return Optional.of(new Conn(p, t, hash(t, p, 2) % 3, epoch));
        }

        boolean broken(Conn c) {
            return hash(c.transmitter(), c.receiver(), 3 + c.kind()) % 5 == 0;
        }

        void log(Conn c, String event) {
            events.computeIfAbsent(c.receiver().id() + "<-" + c.transmitter().id(), k -> new ArrayList<>())
                .add(event + " k" + c.kind() + " e" + c.epoch());
        }
    }

    /** Vanilla's algorithm, line for line, with the game rule as a flag. */
    static final class Vanilla {
        final Set<Node> waypoints = new HashSet<>();
        final Set<Node> players = new HashSet<>();
        final Table<Node, Node, Conn> connections = HashBasedTable.create();
        final World world;
        boolean rule = true;

        Vanilla(World world) {
            this.world = world;
        }

        void updateWaypoint(Node t) {
            if (waypoints.contains(t)) {
                Map<Node, Conn> map = Tables.transpose(connections).row(t);
                Sets.SetView<Node> setView = Sets.difference(players, map.keySet());
                for (Map.Entry<Node, Conn> entry : ImmutableSet.copyOf(map.entrySet())) {
                    updateConnection(entry.getKey(), t, entry.getValue());
                }
                for (Node p : setView) {
                    createConnection(p, t);
                }
            }
        }

        void updatePlayer(Node p) {
            Map<Node, Conn> map = connections.row(p);
            Sets.SetView<Node> setView = Sets.difference(waypoints, map.keySet());
            for (Map.Entry<Node, Conn> entry : ImmutableSet.copyOf(map.entrySet())) {
                updateConnection(p, entry.getKey(), entry.getValue());
            }
            for (Node t : setView) {
                createConnection(p, t);
            }
        }

        void createConnection(Node p, Node t) {
            if (p != t && rule) {
                Optional<Conn> made = world.make(t, p);
                if (made.isPresent()) {
                    connections.put(p, t, made.get());
                    world.log(made.get(), "connect");
                } else {
                    Conn old = connections.remove(p, t);
                    if (old != null) world.log(old, "disconnect");
                }
            }
        }

        void updateConnection(Node p, Node t, Conn c) {
            if (p != t && rule) {
                if (!world.broken(c)) {
                    world.log(c, "update");
                } else {
                    Optional<Conn> made = world.make(t, p);
                    if (made.isPresent()) {
                        world.log(made.get(), "connect");
                        connections.put(p, t, made.get());
                    } else {
                        world.log(c, "disconnect");
                        connections.remove(p, t);
                    }
                }
            }
        }
    }

    /** The same state driven by {@link WaypointSync}, the way the mixin does. */
    static final class Optimised {
        final Set<Node> waypoints = new HashSet<>();
        final Set<Node> players = new HashSet<>();
        final Table<Node, Node, Conn> connections = HashBasedTable.create();
        final World world;
        boolean rule = true;
        final WaypointSync.Ops<Node, Node, Conn> ops;

        Optimised(World world) {
            this.world = world;
            this.ops = new WaypointSync.Ops<>() {
                public Conn get(Node r, Node t) { return connections.get(r, t); }
                public void put(Node r, Node t, Conn c) { connections.put(r, t, c); }
                public Conn remove(Node r, Node t) { return connections.remove(r, t); }
                public Optional<Conn> make(Node t, Node r) { return world.make(t, r); }
                public boolean isBroken(Conn c) { return world.broken(c); }
                public void update(Conn c) { world.log(c, "update"); }
                public void connect(Conn c) { world.log(c, "connect"); }
                public void disconnect(Conn c) { world.log(c, "disconnect"); }
            };
        }

        void updateWaypoint(Node t) {
            if (!waypoints.contains(t) || players.isEmpty() || !rule) return;
            WaypointSync.updateWaypoint(players, t, ops);
        }

        void updatePlayer(Node p) {
            if (!rule) return;
            WaypointSync.updatePlayer(p, waypoints, ops);
        }
    }

    private static Map<String, String> snapshot(Table<Node, Node, Conn> table) {
        Map<String, String> out = new java.util.TreeMap<>();
        table.cellSet().forEach(cell -> out.put(cell.getRowKey().id() + "<-" + cell.getColumnKey().id(), cell.getValue().toString()));
        return out;
    }

    private static void compare(long seed, int nodes, int players, int steps) {
        World wv = new World(seed);
        World wo = new World(seed);
        Vanilla vanilla = new Vanilla(wv);
        Optimised optimised = new Optimised(wo);
        List<Node> all = new ArrayList<>();
        for (int i = 0; i < nodes; i++) all.add(new Node(i));
        // The first `players` nodes are players, who are also transmitters; the rest are other transmitters.
        for (int i = 0; i < nodes; i++) {
            vanilla.waypoints.add(all.get(i));
            optimised.waypoints.add(all.get(i));
            if (i < players) {
                vanilla.players.add(all.get(i));
                optimised.players.add(all.get(i));
            }
        }
        Random random = new Random(seed);
        for (int step = 0; step < steps; step++) {
            int action = random.nextInt(20);
            if (action < 2) {
                wv.epoch++;
                wo.epoch++;
            } else if (action == 2) {
                boolean rule = random.nextInt(4) != 0;
                vanilla.rule = rule;
                optimised.rule = rule;
            } else if (action < 12) {
                Node t = all.get(random.nextInt(nodes));
                vanilla.updateWaypoint(t);
                optimised.updateWaypoint(t);
            } else if (players > 0) {
                Node p = all.get(random.nextInt(players));
                vanilla.updatePlayer(p);
                optimised.updatePlayer(p);
            }
            assertEquals(snapshot(vanilla.connections), snapshot(optimised.connections), "table, seed " + seed + " step " + step);
        }
        assertEquals(wv.events, wo.events, "events, seed " + seed);
    }

    @Test
    void sameConnectionsAndEventsAsVanilla() {
        for (long seed = 1; seed <= 300; seed++) {
            compare(seed, 12, 8, 400);
        }
    }

    @Test
    void sameWithManyPlayers() {
        for (long seed = 1000; seed < 1010; seed++) {
            compare(seed, 60, 40, 600);
        }
    }

    @Test
    void sameWhenNothingIsTracked() {
        compare(7, 1, 1, 50);
        compare(8, 3, 0, 50);
    }

    @Test
    void theScenarioActuallyExercisesEveryBranch() {
        World world = new World(5);
        Optimised optimised = new Optimised(world);
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Node node = new Node(i);
            nodes.add(node);
            optimised.waypoints.add(node);
            optimised.players.add(node);
        }
        for (int epoch = 0; epoch < 30; epoch++) {
            world.epoch = epoch;
            for (Node node : nodes) {
                optimised.updateWaypoint(node);
            }
        }
        String all = world.events.values().toString();
        assertTrue(all.contains("connect"), "connect");
        assertTrue(all.contains("update"), "update");
        assertTrue(all.contains("disconnect"), "disconnect");
    }
}
