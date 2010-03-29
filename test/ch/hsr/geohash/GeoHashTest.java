package ch.hsr.geohash;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

public class GeoHashTest {
	private GeoHash hash;
	private Random rand;

	@Before
	public void setUp() {
		hash = new GeoHash();
		rand = new Random();
	}

	@Test
	public void testAddingOnes() {
		hash.addOnBitToEnd();
		assertEquals(0x1l, hash.bits);
		assertEquals(1, hash.significantBits());
		hash.addOnBitToEnd();
		hash.addOnBitToEnd();
		hash.addOnBitToEnd();
		assertEquals(0xfl, hash.bits);
		assertEquals(4, hash.significantBits());
	}

	@Test
	public void testAddingZeroes() {
		hash.addOnBitToEnd();
		assertEquals(0x1l, hash.bits);

		hash.addOffBitToEnd();
		hash.addOffBitToEnd();
		hash.addOffBitToEnd();
		hash.addOffBitToEnd();
		assertEquals(0x10l, hash.bits);
		assertEquals(5, hash.significantBits());
	}

	@Test
	public void testToBase32() {
		hash.bits = 0x6ff0414000000000l;
		hash.significantBits = 25;

		String base32 = hash.toBase32();
		assertEquals("ezs42", base32);
	}

	@Test
	public void testDecode() {
		// for all lat/lon pairs check decoded point is in the same bbox as the
		// geohash formed by encoder
		// TODO could possibly be less brute-force here and be more scientific
		// about possible failure points
		for (double lat = -90; lat <= 90; lat += rand.nextDouble() + 0.5) {
			for (double lon = -180; lon <= 180; lon += rand.nextDouble() + 0.5) {
				for (int precisionChars = 2; precisionChars <= 12; precisionChars++) {
					GeoHash gh = GeoHash.withCharacterPrecision(lat, lon, precisionChars);
					WGS84Point[] bbox = gh.getBoundingBoxPoints();
					GeoHash decodedHash = GeoHash.fromGeohashString(gh.toBase32());
					WGS84Point decodedCenter = decodedHash.getBoundingBoxCenterPoint();
					assertTrue("Decoded position should be within bounds of original",
							(decodedCenter.latitude >= bbox[0].latitude)
									&& (decodedCenter.longitude >= bbox[0].longitude)
									&& (decodedCenter.latitude <= bbox[1].latitude)
									&& (decodedCenter.longitude <= bbox[1].longitude));

					// they should now actually have the same bounding box.
					WGS84Point[] decodedBoundingBox = decodedHash.getBoundingBoxPoints();
					assertEquals(bbox[0], decodedBoundingBox[0]);
					assertEquals(bbox[1], decodedBoundingBox[1]);

					// the two hashes should also be equal
					assertEquals(gh, decodedHash);
					assertEquals(gh.toBase32(), decodedHash.toBase32());
				}
			}
		}
	}

	@Test
	public void testWithin() {
		hash.bits = 0x6ff0414000000000l;
		hash.significantBits = 25;
		System.out.println(hash.toBase32());
		assertEquals("ezs42", hash.toBase32());

		GeoHash bbox = new GeoHash();
		bbox.bits = 0x6ff0000000000000l;
		bbox.significantBits = 12;

		assertTrue(hash.toBase32() + " should be within " + bbox.toBase32(), hash.within(bbox));
	}

	@Test
	public void testNotWithin() {
		hash.bits = 0x6ff0414000000000l;
		hash.significantBits = 25;
		assertEquals("ezs42", hash.toBase32());

		GeoHash bbox = new GeoHash();
		bbox.bits = 0x6fc0000000000000l;
		bbox.significantBits = 12;

		assertFalse(hash.toBase32() + " should NOT be within " + bbox.toBase32(), hash.within(bbox));
	}

	@Test
	public void testConstructorWithBitPrecision() {
		GeoHash hash1 = GeoHash.withBitPrecision(45, 120, 20);
		assertEquals(hash1.significantBits, 20);
		System.out.println(hash1);
		System.out.println(hash1.toBase32());

		GeoHash hash2 = GeoHash.withBitPrecision(45, 120, 55);
		assertEquals(hash2.significantBits, 55);
		System.out.println(hash2);
		System.out.println(hash2.toBase32());

		assertTrue(hash2.within(hash1));

		// this should match Dave Troys Codebase. This is also his maximum
		// accuracy (12 5-nibbles).
		GeoHash hash3 = GeoHash.withBitPrecision(20, 31, 60);
		assertEquals("sew1c2vs2q5r", hash3.toBase32());
	}

	@Test
	public void testLatLonBoundingBoxes() {
		hash = GeoHash.withBitPrecision(40, 120, 10);
		System.out.println(hash.toBase32());
		printBoundingBox(hash);
	}

	@Test
	public void testByCharacterPrecision() {
		hash = GeoHash.withCharacterPrecision(20, 31, 12);
		assertEquals("sew1c2vs2q5r", hash.toBase32());

		hash = GeoHash.withCharacterPrecision(-20, 31, 12);
		assertEquals("ksqn1rje83g2", hash.toBase32());

		hash = GeoHash.withCharacterPrecision(-20.783236276, 31.9867127312312, 12);
		assertEquals("ksq9zbs0b7vw", hash.toBase32());

		hash = GeoHash.withCharacterPrecision(-76.5110040642321, 39.0247389581054, 12);
		assertEquals("hf7u8p8gn747", hash.toBase32());

		hash = GeoHash.withCharacterPrecision(-76.5110040642321, 39.0247389581054, 8);
		assertEquals("hf7u8p8g", hash.toBase32());

		hash = GeoHash.withCharacterPrecision(-76.5110040642321, 39.0247389581054, 4);
		assertEquals("hf7u", hash.toBase32());

		hash = GeoHash.withCharacterPrecision(39.0247389581054, -76.5110040642321, 12);
		assertEquals("dqcw4bnrs6s7", hash.toBase32());
	}

	@Test
	public void testGetLatitudeBits() {
		hash = GeoHash.withBitPrecision(30, 30, 16);
		long[] latitudeBits = hash.getRightAlignedLatitudeBits();
		assertEquals(0xaal, latitudeBits[0]);
		assertEquals(8, latitudeBits[1]);
	}

	@Test
	public void testGetLongitudeBits() {
		hash = GeoHash.withBitPrecision(30, 30, 16);
		long[] longitudeBits = hash.getRightAlignedLongitudeBits();
		assertEquals(0x95l, longitudeBits[0]);
		assertEquals(8, longitudeBits[1]);
	}

	@Test
	public void testNeighbourLocationCode() {
		// set up corner case
		hash.bits = 0xc400000000000000l;
		hash.significantBits = 7;

		long[] lonBits = hash.getRightAlignedLongitudeBits();
		assertEquals(0x8, lonBits[0]);
		assertEquals(4, lonBits[1]);

		long[] latBits = hash.getRightAlignedLatitudeBits();
		assertEquals(0x5, latBits[0]);
		assertEquals(3, latBits[1]);

		GeoHash north = hash.getNorthernNeighbour();
		assertEquals(0xc000000000000000l, north.bits);
		assertEquals(7, north.significantBits);

		GeoHash south = hash.getSouthernNeighbour();
		assertEquals(0xd000000000000000l, south.bits);
		assertEquals(7, south.significantBits());

		GeoHash east = hash.getEasternNeighbour();
		assertEquals(0xc600000000000000l, east.bits);

		// NOTE: this is actually a corner case!
		GeoHash west = hash.getWesternNeighbour();
		assertEquals(0x6e00000000000000l, west.bits);

		// NOTE: and now, for the most extreme corner case in 7-bit geohash-land
		hash.bits = 0xfe00000000000000l;

		east = hash.getEasternNeighbour();
		assertEquals(0x5400000000000000l, east.bits);

		// and then from there, just a little south of sanity...
		south = east.getSouthernNeighbour();
		assertEquals(0x0l, south.bits);
	}

	@Test
	public void testEqualsAndHashCode() {
		GeoHash hash1 = GeoHash.withBitPrecision(30, 30, 24);
		GeoHash hash2 = GeoHash.withBitPrecision(30, 30, 24);
		GeoHash hash3 = GeoHash.withBitPrecision(30, 30, 10);

		assertTrue(hash1.equals(hash2) && hash2.equals(hash1));
		assertFalse(hash1.equals(hash3) && hash3.equals(hash1));

		assertEquals(hash1.hashCode(), hash2.hashCode());
		assertFalse(hash1.hashCode() == hash3.hashCode());
	}

	@Test
	public void testAdjacentHashes() {
		GeoHash[] adjacent = GeoHash.fromGeohashString("dqcw4").getAdjacent();
		assertEquals(8, adjacent.length);
	}

	@Test
	public void testMovingInCircle() {
		// moving around hashes in a circle should be possible
		checkMovingInCircle(34.2, -45.123);
		// this should also work at the "back" of the earth
		checkMovingInCircle(45, 180);
		checkMovingInCircle(90, 180);
		checkMovingInCircle(0, -180);
	}

	private void checkMovingInCircle(double latitude, double longitude) {
		GeoHash start;
		GeoHash end;
		start = GeoHash.withCharacterPrecision(latitude, longitude, 12);
		end = start.getEasternNeighbour();
		end = end.getSouthernNeighbour();
		end = end.getWesternNeighbour();
		end = end.getNorthernNeighbour();
		assertEquals(start, end);
		assertArrayEquals(start.getBoundingBoxPoints(), end.getBoundingBoxPoints());
	}

	@Test
	public void testMovingAroundWorldOnHashStrips() throws Exception {
		String[] directions = { "Northern", "Eastern", "Southern", "Western" };
		for (String direction : directions) {
			checkMoveAroundStrip(direction);
		}
	}

	private void checkMoveAroundStrip(String direction) throws Exception {
		for (int bits = 2; bits < 16; bits++) {
			double randomLatitude = (rand.nextDouble() - 0.5) * 180;
			double randomLongitude = (rand.nextDouble() - 0.5) * 360;

			// this divides the range by 2^bits
			GeoHash hash = GeoHash.withBitPrecision(randomLatitude, randomLongitude, bits);
			Method method = hash.getClass().getDeclaredMethod("get" + direction + "Neighbour");
			GeoHash result = hash;

			// moving this direction 2^bits times should yield the same hash
			// again
			for (int i = 0; i < Math.pow(2, bits); i++) {
				result = (GeoHash) method.invoke(result);
			}
			assertEquals(hash, result);
		}
	}

	@Test
	public void testIssue1() {
		double lat = 40.390943;
		double lon = -75.9375;
		GeoHash hash = GeoHash.withCharacterPrecision(lat, lon, 12);

		String base32 = "dr4jb0bn2180";
		GeoHash fromRef = GeoHash.fromGeohashString(base32);
		assertEquals(hash, fromRef);
		assertEquals(base32, hash.toBase32());
		assertEquals(base32, fromRef.toBase32());

		hash = GeoHash.withCharacterPrecision(lat, lon, 10);
		assertEquals("dr4jb0bn21", hash.toBase32());
	}

	private void printBoundingBox(GeoHash hash) {
		System.out.println("Bounding Box: \ncenter =" + hash.getBoundingBoxCenterPoint());
		System.out.print("corners=");
		System.out.println(String.format("%s,%s", hash.getBoundingBoxPoints()[0], hash.getBoundingBoxPoints()[1]));
	}
}
