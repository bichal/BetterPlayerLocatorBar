package net.bichal.bplb.client;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.waypoints.TrackedWaypoint;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * From 1.21.6 the server already tracks players for the vanilla locator bar and sends them as waypoints,
 * so this mod needs no network of its own. The waypoints only carry an angle (and sometimes a rough
 * position), so each one is turned into a position along that angle at its real distance. The HUD works
 * on positions, which keeps the customisation (icons, fade, arrows) identical in both modes.
 */
final class WaypointSource {
    private WaypointSource() {}

    static List<Hud.PlayerPosition> collect(Minecraft client) {
        var connection = client.getConnection();
        Entity viewer = client.getCameraEntity();
        if (connection == null || viewer == null || client.player == null) return List.of();

        Level level = viewer.level();
        Camera camera = client.gameRenderer.getMainCamera();
        List<Hud.PlayerPosition> out = new ArrayList<>();

        connection.getWaypointManager().forEachWaypoint(viewer, waypoint -> {
            UUID id = waypoint.id().left().orElse(null);
            if (id == null || id.equals(client.player.getUUID())) return;

            double yawOffset = yawOffset(client, waypoint, level, camera);
            double distance = Math.sqrt(waypoint.distanceSquared(viewer));
            if (Double.isNaN(yawOffset) || Double.isNaN(distance)) return;

            double[] xz = alongYaw(viewer.getX(), viewer.getZ(), viewer.getYRot(), yawOffset, distance);
            double x = xz[0];
            double z = xz[1];
            double rise = Math.max(6.0, distance * 0.5) + 2.0;
            double y = switch (pitchDirection(client, waypoint, level)) {
                case UP -> viewer.getY() + rise;
                case DOWN -> viewer.getY() - rise;
                default -> viewer.getY();
            };
            out.add(new Hud.PlayerPosition(id, nameOf(client, level, id), x, y, z));
        });
        return out;
    }

    /**
     * The point {@code distance} blocks away from the viewer in the direction {@code yawOffset} degrees to the
     * right of where the viewer looks. The HUD measures angles as {@code atan2(dz, dx) - 90 - yaw}, so this is
     * that formula solved the other way round.
     */
    static double[] alongYaw(double viewerX, double viewerZ, double viewerYawDegrees, double yawOffset, double distance) {
        double heading = Math.toRadians(yawOffset + 90.0 + viewerYawDegrees);
        return new double[]{viewerX + Math.cos(heading) * distance, viewerZ + Math.sin(heading) * distance};
    }

    private static double yawOffset(Minecraft client, TrackedWaypoint waypoint, Level level, Camera camera) {
        //? if >=1.21.9 {
        /*return waypoint.yawAngleToCamera(level, camera, entity -> client.getDeltaTracker().getGameTimeDeltaPartialTick(false));
        *///?} else {
        return waypoint.yawAngleToCamera(level, camera);
        //?}
    }

    private static TrackedWaypoint.PitchDirection pitchDirection(Minecraft client, TrackedWaypoint waypoint, Level level) {
        //? if >=1.21.9 {
        /*return waypoint.pitchDirectionToCamera(level, client.gameRenderer, entity -> client.getDeltaTracker().getGameTimeDeltaPartialTick(false));
        *///?} else {
        return waypoint.pitchDirectionToCamera(level, client.gameRenderer);
        //?}
    }

    private static String nameOf(Minecraft client, Level level, UUID id) {
        PlayerInfo info = client.getConnection().getPlayerInfo(id);
        //? if >=1.21.9 {
        /*if (info != null) return info.getProfile().name();
        *///?} else {
        if (info != null) return info.getProfile().getName();
        //?}
        var player = level.getPlayerByUUID(id);
        return player != null ? player.getName().getString() : "Player";
    }
}
