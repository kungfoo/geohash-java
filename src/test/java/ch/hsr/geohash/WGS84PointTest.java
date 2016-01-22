/*
 * Copyright 2010, Silvio Heuberger @ IFS www.ifs.hsr.ch
 *
 * This code is release under the Apache License 2.0.
 * You should have received a copy of the license
 * in the LICENSE file. If you have not, see
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package ch.hsr.geohash;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;

import org.junit.Before;
import org.junit.Test;

import ch.hsr.geohash.util.VincentyGeodesy;

public class WGS84PointTest {
	private static final double DELTA = 0.00001;
	private WGS84Point a;
	private WGS84Point b;
	private WGS84Point c;
	private WGS84Point d;

	@Before
	public void setUp() {
		a = new WGS84Point(47.2342, 15.7465465);
		b = new WGS84Point(a);
		c = new WGS84Point(-47.234, b.getLongitude());
		d = new WGS84Point(-32.9687253, 12.42334242);
	}

	@Test
	public void testVincenty() {
		WGS84Point startPoint = new WGS84Point(40, 40);

		int distanceInMeters = 10000;
		WGS84Point result = VincentyGeodesy.moveInDirection(startPoint, 120,
				distanceInMeters);
		assertEquals(40.10134882, result.getLongitude(), DELTA);
		assertEquals(39.9549245, result.getLatitude(), DELTA);

		assertEquals(distanceInMeters, VincentyGeodesy.distanceInMeters(
				startPoint, result), DELTA);

		WGS84Point p1 = new WGS84Point(1, 1);
		int tenThousandKilometers = 10000000;
		WGS84Point p2 = VincentyGeodesy.moveInDirection(p1, 270, tenThousandKilometers);
		System.out.println(p2);
		assertEquals(tenThousandKilometers, VincentyGeodesy.distanceInMeters(p1, p2), DELTA);
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

	@Test(expected = IllegalArgumentException.class)
	public void testRangeCheck() {
		new WGS84Point(180, 240);
	}
}