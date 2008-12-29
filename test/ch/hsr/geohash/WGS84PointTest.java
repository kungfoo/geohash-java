package ch.hsr.geohash;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class WGS84PointTest {
	private static final double DELTA = 0.00001;

	@Before
	public void setUp() {

	}

	@Test
	public void testVincenty() {
		WGS84Point startPoint = new WGS84Point(40, 40);

		int distanceInMeters = 10000;
		WGS84Point result = WGS84Point.moveInDirection(startPoint, 120,
				distanceInMeters);
		Assert.assertEquals(40.10134882, result.longitude, DELTA);
		Assert.assertEquals(39.9549245, result.latitude, DELTA);

		Assert.assertEquals(distanceInMeters, WGS84Point.distanceInMeters(
				startPoint, result), DELTA);
		
		
		WGS84Point p1 = new WGS84Point(1,1);
		int tenThousandKilometers = 10000000;
		WGS84Point p2 = WGS84Point.moveInDirection(p1, 270, tenThousandKilometers);
		System.out.println(p2);
		Assert.assertEquals(tenThousandKilometers, WGS84Point.distanceInMeters(p1, p2), DELTA);
	}
}