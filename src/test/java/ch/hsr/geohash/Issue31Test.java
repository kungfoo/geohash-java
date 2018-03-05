package ch.hsr.geohash;


import ch.hsr.geohash.queries.GeoHashBoundingBoxQuery;
import ch.hsr.geohash.util.BoundingBoxGeoHashIterator;
import ch.hsr.geohash.util.TwoGeoHashBoundingBox;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static ch.hsr.geohash.GeoHashMatchers.hashThatContains;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class Issue31Test {
    @Test
    public void orderOfPointsShouldNotMatterToBoundingBoxQuery() {
        WGS84Point p1_1 = new WGS84Point(10.557597041722232, -35.52832642341309);
        WGS84Point p1_2 = new WGS84Point(-41.76269104573268, -68.00914348298193);

        GeoHashBoundingBoxQuery query1 = new GeoHashBoundingBoxQuery(new BoundingBox(p1_1, p1_2));

        System.out.println(query1.getWktBox());
        System.out.println(query1);

        WGS84Point p2_1 = new WGS84Point(10.557597041722232, -68.00914348298193);
        WGS84Point p2_2 = new WGS84Point(-41.76269104573268, -35.52832642341309);

        GeoHashBoundingBoxQuery query2 = new GeoHashBoundingBoxQuery(new BoundingBox(p2_1, p2_2));

        assertThat(query1.getSearchHashes(), is(query2.getSearchHashes()));
    }

    @Test
    public void generateListOfQueriesAtHigherResolution() {
        GeoHash h2 = GeoHash.withBitPrecision(10.557597041722232, -35.52832642341309, 6);
        GeoHash h1 = GeoHash.withBitPrecision(-41.76269104573268, -68.00914348298193, 6);

        assertThat(h1, hashThatContains(-41.76269104573268, -68.00914348298193));
        assertThat(h2, hashThatContains(10.557597041722232, -35.52832642341309));

        ImmutableList<GeoHash> searchHashes = ImmutableList.copyOf(new BoundingBoxGeoHashIterator(new TwoGeoHashBoundingBox(h1, h2)));
        for(GeoHash hash: searchHashes) {
            System.out.println(hash + " as query string: " + hash.toBinaryString());
        }

        assertThat(searchHashes, hasItem(hashThatContains(10.557597041722232, -35.52832642341309)));
        assertThat(searchHashes, hasItem(hashThatContains(-41.76269104573268, -68.00914348298193)));
        assertThat(searchHashes, not(hasItem(hashThatContains(51.473854, -9.388135))));
    }

}
