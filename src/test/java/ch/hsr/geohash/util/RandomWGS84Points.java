package ch.hsr.geohash.util;

import ch.hsr.geohash.WGS84Point;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomWGS84Points {
    private static final Random RAND = new Random(System.currentTimeMillis());

    public static WGS84Point get() {
        return createRandomPoint();
    }

    public static List<WGS84Point> get(int n) {
        List<WGS84Point> result = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            result.add(createRandomPoint());
        }
        return result;
    }

    private static WGS84Point createRandomPoint() {
        double latitude = (RAND.nextDouble() - 0.5) * 180;
        double longitude = (RAND.nextDouble() - 0.5) * 360;
        return new WGS84Point(latitude, longitude);
    }
}
