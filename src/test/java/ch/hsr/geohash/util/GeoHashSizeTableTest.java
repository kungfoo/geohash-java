package ch.hsr.geohash.util;

import static org.junit.Assert.assertEquals;

import java.util.Random;

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

	/**
	 * the entire box is a little too small, thus it must fit nicely into the
	 * hash.
	 */
	private static class ALittleTooSmallVerifier implements BoundingBoxSizeTableVerifier {
		public ALittleTooSmallVerifier() {
		}

		@Override
		public BoundingBox generate(int bits) {
			// make the bounding box a little smaller than dLat/dLon
			double dLat = GeoHashSizeTable.dLat(bits) - DELTA;
			double dLon = GeoHashSizeTable.dLon(bits) - DELTA;
			return new BoundingBox(45 - dLat, 45, 30 - dLon, 30);
		}

		@Override
		public int getExpectedBits(int bits) {
			return bits;
		}
	}

	/**
	 * if both lat and lon are a little too large, we must use a bigger hash,
	 * i.e. less bits.
	 */
	private static class BothALittleTooLargeVerifier implements BoundingBoxSizeTableVerifier {
		public BothALittleTooLargeVerifier() {
		}

		@Override
		public BoundingBox generate(int bits) {
			double dLat = GeoHashSizeTable.dLat(bits);
			double dLon = GeoHashSizeTable.dLon(bits);
			return new BoundingBox(0, dLat + DELTA, 0, dLon + DELTA);
		}

		@Override
		public int getExpectedBits(int bits) {
			return bits - 2;
		}
	}

	/**
	 * depending on whether we're currently at an even or odd nuber of bits, one
	 * or two bits have to be removed.
	 */
	private static class OnlyOneALittleTooLargeVerifier implements BoundingBoxSizeTableVerifier {
		public OnlyOneALittleTooLargeVerifier() {
		}

		private Random rand = new Random();
		private boolean latitudeAffected;

		@Override
		public BoundingBox generate(int bits) {
			double dLat = GeoHashSizeTable.dLat(bits);
			double dLon = GeoHashSizeTable.dLon(bits);

			if (latitudeAffected = rand.nextBoolean()) {
				dLat += DELTA;
			} else {
				dLon += DELTA;
			}
			return new BoundingBox(0, dLat, 0, dLon);
		}

		@Override
		public int getExpectedBits(int bits) {
			if (latitudeAffected) {
				if (bits % 2 != 0) {
					return bits - 2;
				} else {
					return bits - 1;
				}
			} else {
				if (bits % 2 != 0) {
					return bits - 1;
				} else {
					return bits - 2;
				}
			}
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
		checkWithGenerator(new OnlyOneALittleTooLargeVerifier());
	}

	private void checkWithGenerator(BoundingBoxSizeTableVerifier generator) {
		for (int bits = 4; bits < 64; bits++) {
			BoundingBox bbox = generator.generate(bits);
			assertEquals(generator.getExpectedBits(bits), GeoHashSizeTable.numberOfBitsForOverlappingGeoHash(bbox));
		}
	}
}
