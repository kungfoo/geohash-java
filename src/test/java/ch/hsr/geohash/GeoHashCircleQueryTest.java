package ch.hsr.geohash;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.hsr.geohash.queries.GeoHashCircleQuery;

public class GeoHashCircleQueryTest {
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
}
