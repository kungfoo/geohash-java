package ch.hsr.geohash.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class GeoHashSizeTableTest {

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
		assertEquals(d, GeoHashSizeTable.dLon(i), 1-128);
	}

}
