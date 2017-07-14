package ch.hsr.geohash;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import ch.hsr.geohash.util.RandomWGS84Points;
import ch.hsr.geohash.util.VincentyGeodesy;
import org.hamcrest.CoreMatchers;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Test;

import ch.hsr.geohash.queries.GeoHashCircleQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GeoHashCircleQueryTest {

	private static final int NUMBER_OF_RANDOM_POINTS = 1000000;
	public static final int ONE_HUNDRED_KM = 100 * 1000;

	@Test
	public void testIssue3WithCircleQuery() throws Exception {
		WGS84Point center = new WGS84Point(39.86391280373075, 116.37356590048701);
		GeoHashCircleQuery query = new GeoHashCircleQuery(center, 589);

		// the distance between center and test1 is about 430 meters
		WGS84Point test1 = new WGS84Point(39.8648866576058, 116.378465869303);
		// the distance between center and test2 is about 510 meters
		WGS84Point test2 = new WGS84Point(39.8664787092599, 116.378552856158);

		assertTrue(query.contains(test1));
		assertTrue(query.contains(test2));
	}

	@Test
	public void testStoringManyPointsAndPerformingCircleQuery() throws Exception {
		List<GeoHash> hashes = createRandomHashes();
		WGS84Point queryPoint = RandomWGS84Points.get();

		GeoHashCircleQuery oneHundredKilometresQuery = new GeoHashCircleQuery(queryPoint, ONE_HUNDRED_KM);
		assertThat(oneHundredKilometresQuery.toString(), containsString("100.0km"));

		// filter points/hashes based on prefix, no expensive vincenty math here.
		long t1 = System.currentTimeMillis();
		List<GeoHash> queryResult = new ArrayList<>();
		for(GeoHash hash: hashes) {
			if(oneHundredKilometresQuery.contains(hash)) {
				queryResult.add(hash);
			}
		}
		long t2 = System.currentTimeMillis();

		System.out.println(String.format("Checking %d hashes took %dms", NUMBER_OF_RANDOM_POINTS, t2-t1));

		t1 = System.currentTimeMillis();
		List<GeoHash> closerThan100km = new ArrayList<>();
		for(GeoHash hash: hashes)  {
			if(VincentyGeodesy.distanceInMeters(queryPoint, hash.getPoint()) <= ONE_HUNDRED_KM) {
				closerThan100km.add(hash);
			}
		}
		t2 = System.currentTimeMillis();

		System.out.println(String.format("Checking points with VincentyGeodesy took %dms", t2-t1));
		System.out.println(String.format("Number of points actually closer than 100km: %d", closerThan100km.size()));
		System.out.println(String.format("Number of points matched by query: %d", queryResult.size()));

		// check all positives are matched
		for(GeoHash shouldBeInResult: closerThan100km) {
			assertThat(queryResult, hasItem(shouldBeInResult));
		}
	}

	private List<GeoHash> createRandomHashes() {
		List<WGS84Point> points = RandomWGS84Points.get(NUMBER_OF_RANDOM_POINTS);
		List<GeoHash> result = new ArrayList<>(NUMBER_OF_RANDOM_POINTS);

		for(WGS84Point point : points) {
			result.add(
					GeoHash.withBitPrecision(point.getLatitude(), point.getLongitude(), GeoHash.MAX_BIT_PRECISION)
			);
		}
		return result;
	}


}
