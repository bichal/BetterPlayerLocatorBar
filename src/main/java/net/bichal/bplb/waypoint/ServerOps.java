package net.bichal.bplb.waypoint;

import com.google.common.collect.Table;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.waypoints.WaypointTransmitter;

import java.util.Optional;

/** {@link WaypointSync.Ops} over the vanilla manager's connection table. */
public final class ServerOps implements WaypointSync.Ops<ServerPlayer, WaypointTransmitter, WaypointTransmitter.Connection> {
    private final Table<ServerPlayer, WaypointTransmitter, WaypointTransmitter.Connection> connections;

    public ServerOps(Table<ServerPlayer, WaypointTransmitter, WaypointTransmitter.Connection> connections) {
        this.connections = connections;
    }

    @Override
    public WaypointTransmitter.Connection get(ServerPlayer receiver, WaypointTransmitter transmitter) {
        return connections.get(receiver, transmitter);
    }

    @Override
    public void put(ServerPlayer receiver, WaypointTransmitter transmitter, WaypointTransmitter.Connection connection) {
        connections.put(receiver, transmitter, connection);
    }

    @Override
    public WaypointTransmitter.Connection remove(ServerPlayer receiver, WaypointTransmitter transmitter) {
        return connections.remove(receiver, transmitter);
    }

    @Override
    public Optional<WaypointTransmitter.Connection> make(WaypointTransmitter transmitter, ServerPlayer receiver) {
        return transmitter.makeWaypointConnectionWith(receiver);
    }

    @Override
    public boolean isBroken(WaypointTransmitter.Connection connection) {
        return connection.isBroken();
    }

    @Override
    public void update(WaypointTransmitter.Connection connection) {
        connection.update();
    }

    @Override
    public void connect(WaypointTransmitter.Connection connection) {
        connection.connect();
    }

    @Override
    public void disconnect(WaypointTransmitter.Connection connection) {
        connection.disconnect();
    }
}
