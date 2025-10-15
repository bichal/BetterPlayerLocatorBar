package net.bichal.bplb.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DistanceUtils {
    private static final int CACHE_SIZE = 512;
    private static final Map<Long, CachedDistance> distanceCache = new ConcurrentHashMap<>(CACHE_SIZE);
    private static final long CACHE_DURATION_MS = 50L;

    public static double calculateDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        Long key = makeKey(x1, z1, x2, z2);
        long now = System.currentTimeMillis();

        CachedDistance cached = distanceCache.get(key);
        if (cached != null && (now - cached.timestamp) < CACHE_DURATION_MS) {
            return cached.distance;
        }
        double dx = x1 - x2;
        double dy = y1 - y2;
        double dz = z1 - z2;
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distanceCache.size() < CACHE_SIZE) {
            distanceCache.put(key, new CachedDistance(distance, now));
        }
        return distance;
    }

    private static long makeKey(double x1, double z1, double x2, double z2) {
        int ix1 = (int) (x1 * 10);
        int iz1 = (int) (z1 * 10);
        int ix2 = (int) (x2 * 10);
        int iz2 = (int) (z2 * 10);
        return ((long) ix1 << 48) | ((long) iz1 << 32) | ((long) ix2 << 16) | iz2;
    }

    private record CachedDistance(double distance, long timestamp) {
    }
}
