package ch.hsr.geohash;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class WGS84PointTest {
	private static final double DELTA = 0.00001;
	private WGS84Point a;
	private WGS84Point b;
	private WGS84Point c;
	private WGS84Point d;

	@Before
	public void setUp() {
		a = new WGS84Point(120, 15.7465465);
		b = new WGS84Point(a);
		c = new WGS84Point(-120, b.getLongitude());
		d = new WGS84Point(-32.9687253, 12.42334242);
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
	
	@Test
	public void testEquals() {
		assertEquals(a, a);
		assertEquals(a, b);
		assertEquals(b, a);
		assertNotSame(a, b);
		
		assertFalse(a.equals(c));
		assertFalse(c.equals(a));
		assertFalse(d.equals(c));
		assertFalse(d.equals(a));
		assertFalse(d.equals(new Integer(10)));
	}
	
	@Test
	public void testHashCode() {
		assertEquals(a.hashCode(), a.hashCode());
		assertEquals(a.hashCode(), b.hashCode());
		assertFalse(a.hashCode() == c.hashCode());
		assertFalse(d.hashCode() == c.hashCode());
		assertFalse(d.hashCode() == new Integer(10).hashCode());
	}
}