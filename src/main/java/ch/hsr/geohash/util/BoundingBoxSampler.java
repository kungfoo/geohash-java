package ch.hsr.geohash.util;

import ch.hsr.geohash.GeoHash;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kevin
 * Date: Jan 17, 2011
 * Time: 12:02:06 PM
 */
public class BoundingBoxSampler {
    private TwoGeoHashBoundingBox boundingBox;
    private Set<Integer> alreadyUsed = new HashSet<Integer>();
    private int maxSamples;
    private Random rand = new Random();

    public BoundingBoxSampler(TwoGeoHashBoundingBox bbox) {
        this.boundingBox = bbox;
        long maxSamplesLong = GeoHash.stepsBetween(bbox.getBottomLeft(), bbox.getTopRight());
        if (maxSamplesLong > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("This bounding box is too big too sample using this algorithm");
        }
        maxSamples = (int) maxSamplesLong;
    }

    public BoundingBoxSampler(TwoGeoHashBoundingBox bbox, long seed) {
        this(bbox);
        this.rand = new Random(seed);
    }

    public TwoGeoHashBoundingBox getBoundingBox() {
        return boundingBox;
    }

    public GeoHash next() {
        if (alreadyUsed.size() == maxSamples) return null;
        int idx = rand.nextInt(maxSamples + 1);
        while (alreadyUsed.contains(idx)) {
            idx = rand.nextInt(maxSamples + 1);
        }
        alreadyUsed.add(idx);
        GeoHash gh = boundingBox.getBottomLeft().next(idx);
        if (!boundingBox.getBoundingBox().contains(gh.getPoint())) return next();
        return gh;
    }
}
