package net.bichal.bplb.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WaypointSourceTest {
    /** The HUD's own angle formula, applied to the position the source produces. */
    private static double hudAngle(double viewerX, double viewerZ, double viewerYaw, double[] xz) {
        double angle = Math.toDegrees(Math.atan2(xz[1] - viewerZ, xz[0] - viewerX)) - 90 - viewerYaw;
        return ((angle + 180) % 360 + 360) % 360 - 180;
    }

    @Test
    void positionRoundTripsThroughTheHudAngle() {
        for (double yaw : new double[]{-170, -90, 0, 45, 135, 179}) {
            for (double offset : new double[]{-80, -30, 0, 25, 60}) {
                double[] xz = WaypointSource.alongYaw(10, -20, yaw, offset, 40);
                assertEquals(offset, hudAngle(10, -20, yaw, xz), 1e-9, "yaw " + yaw + " offset " + offset);
                assertEquals(40, Math.hypot(xz[0] - 10, xz[1] + 20), 1e-9);
            }
        }
    }
}
