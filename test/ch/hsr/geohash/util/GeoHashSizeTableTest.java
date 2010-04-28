package ch.hsr.geohash.util;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import ch.hsr.geohash.BoundingBox;
import ch.hsr.geohash.WGS84Point;

public class GeoHashSizeTableTest {

	private Random random;

	@Before
	public void setUp() {
		random = new Random();
	}

	@Test
	public void testDLat() {
		assertDLatIs(180d, 0);
		assertDLatIs(180d, 1);
		assertDLatIs(90d, 2);
		assertDLatIs(0.3515625, 18);
		assertDLatIs(0.3515625, 19);
	}

	private void assertDLatIs(double d, int i) {
		assertEquals(d, GeoHashSizeTable.dLat(i), 0);
	}

	@Test
	public void testDLon() {
		assertDLonIs(360, 0);
		assertDLonIs(180, 1);
		assertDLonIs(0.0439453125, 25);
		assertDLonIs(0.0439453125, 26);
	}

	private void assertDLonIs(double d, int i) {
		assertEquals(d, GeoHashSizeTable.dLon(i), 1 - 128);
	}

	@Test
	public void testKnownBoundingBoxSizes() {
		for (int bits = 3; bits < 64; bits++) {
			// make the bounding box a little smaller than dLat/dLon
			double delta = 1e-10;
			double dLat = GeoHashSizeTable.dLat(bits) - delta;
			double dLon = GeoHashSizeTable.dLon(bits) - delta;

			WGS84Point upperLeft = new WGS84Point(45 - dLat, 30 - dLon);
			WGS84Point lowerRight = new WGS84Point(45, 30);
			BoundingBox boundingBox = new BoundingBox(upperLeft, lowerRight);
			// TODO: make sure the number of bits matches the expected one for
			// this specific bounding box
		}
	}
}
