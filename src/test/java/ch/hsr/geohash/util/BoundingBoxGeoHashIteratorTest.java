package ch.hsr.geohash.util;

import ch.hsr.geohash.BoundingBox;
import ch.hsr.geohash.GeoHash;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: kevin
 * Date: Jan 6, 2011
 * Time: 3:05:43 PM
 */
public class BoundingBoxGeoHashIteratorTest {
    @Test
    public void testIter() {
        BoundingBox box = new BoundingBox(37.7, 37.84, -122.52, -122.35);
        BoundingBoxGeoHashIterator iter = BoundingBoxGeoHashIterator.withBitPrecision(box, 10);
        BoundingBox newBox = iter.getBoundingBox();
        List<GeoHash> hashes = new ArrayList<GeoHash>();
        while (iter.hasNext()) {
            hashes.add(iter.next());
        }
        GeoHash prev = null;
        for (GeoHash gh : hashes) {
            if (prev != null) Assert.assertTrue(prev.compareTo(gh) < 0);
            Assert.assertTrue(newBox.contains(gh.getPoint()));
            prev = gh;
        }

    }

    @Test
    public void testIter2() {
        BoundingBox box = new BoundingBox(37.7, 37.84, -122.52, -122.35);
        BoundingBoxGeoHashIterator iter = BoundingBoxGeoHashIterator.withBitPrecision(box, 35);
        BoundingBox newBox = iter.getBoundingBox();
        List<GeoHash> hashes = new ArrayList<GeoHash>();
        while (iter.hasNext()) {
            hashes.add(iter.next());
        }
        GeoHash prev = null;
        int idx = 0;
        //System.out.println("idx,lat,lon");
        for (GeoHash gh : hashes) {
            if (prev != null) Assert.assertTrue(prev.compareTo(gh) < 0);
            Assert.assertTrue(newBox.contains(gh.getPoint()));
            System.out.println(idx++ + "," + gh.getPoint().getLatitude() + "," + gh.getPoint().getLongitude());
            prev = gh;
        }

    }
}
