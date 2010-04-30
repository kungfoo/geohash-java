package ch.hsr.geohash.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.hsr.geohash.BoundingBox;

public class GeoHashSizeTableTest {

	private static final double DELTA = 1e-10;

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

	private interface BoundingBoxSizeTableVerifier {
		/**
		 * generate a bounding box using a certain strategy for the given numer
		 * of bits.
		 */
		public BoundingBox generate(int bits);

		/**
		 * return the number of expected bits.
		 */
		public int getExpectedBits(int bits);
	}

	private static class ALittleTooSmallVerifier implements BoundingBoxSizeTableVerifier {
		@Override
		public BoundingBox generate(int bits) {
			// make the bounding box a little smaller than dLat/dLon
			double dLat = GeoHashSizeTable.dLat(bits) - DELTA;
			double dLon = GeoHashSizeTable.dLon(bits) - DELTA;
			return new BoundingBox(45 - dLat, 30 - dLon, 45, 30);
		}

		@Override
		public int getExpectedBits(int bits) {
			return bits;
		}
	}

	private static class BothALittleTooLargeVerifier implements BoundingBoxSizeTableVerifier {
		public BoundingBox generate(int bits) {
			double dLat = GeoHashSizeTable.dLat(bits);
			double dLon = GeoHashSizeTable.dLon(bits);
			return new BoundingBox(0, 0, dLat + DELTA, dLon + DELTA);
		}

		@Override
		public int getExpectedBits(int bits) {
			return bits - 2;
		}
	}

	private static class OnlyOneALittleTooLargeVerifier implements BoundingBoxSizeTableVerifier {
		@Override
		public BoundingBox generate(int bits) {
			return null;
		}

		@Override
		public int getExpectedBits(int bits) {
			return bits - 2;
		}
	}

	@Test
	public void testKnownSmallerBoundingBoxSizes() {
		checkWithGenerator(new ALittleTooSmallVerifier());
	}

	@Test
	public void testKnownLargerBoundingBoxSizes() {
		checkWithGenerator(new BothALittleTooLargeVerifier());
	}

	@Test
	public void testKnownOneBitLargerBoxSizes() {
		// TODO: verify the number of bits for just one dimension too large.
	}

	private void checkWithGenerator(BoundingBoxSizeTableVerifier generator) {
		for (int bits = 4; bits < 64; bits++) {
			BoundingBox bbox = generator.generate(bits);
			assertEquals(generator.getExpectedBits(bits), GeoHashSizeTable.numberOfBitsForOverlappingGeoHash(bbox));
		}
	}
}
