package ch.hsr.geohash;

import static org.junit.Assert.*;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class GeoHashTest {
	private GeoHash hash;

	@Before
	public void setUp() {
		hash = new GeoHash();
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
	public void testLongToBitString() {
		hash.bits = 0x5555555555555555l;
		assertEquals(
				"0101010101010101010101010101010101010101010101010101010101010101",
				GeoHash.longToBitString(hash.bits));
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
		//for all lat/lon pairs check decoded point is in the same bbox as the geohash formed by encoder
		//TODO could possibly be less brute-force here and be more scientific about possible failure points
		for (double lat=-90;lat<=90;lat++) {
			for (double lng=-180;lng<=180;lng++) {				
				for (int precisionChars = 2; precisionChars <= 12; precisionChars++){
					GeoHash gh = GeoHash.withCharacterPrecision(lat, lng, precisionChars);
					WGS84Point[] bbox = gh.getBoundingBoxPoints();
					GeoHash decodedGh=GeoHash.fromGeohashString(gh.toBase32());
					WGS84Point decodedCenter = decodedGh.getBoundingBoxCenterPoint();
					Assert.assertTrue("Decoded position should be within bounds of original", 
							(decodedCenter.latitude>=bbox[0].latitude)
							&&
							(decodedCenter.longitude>=bbox[0].longitude)
							&&
							(decodedCenter.latitude<=bbox[1].latitude)
							&&
							(decodedCenter.longitude<=bbox[1].longitude)
					
					);
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

		assertTrue(hash.toBase32() + " should be within " + bbox.toBase32(),
				hash.within(bbox));
	}

	@Test
	public void testNotWithin() {
		hash.bits = 0x6ff0414000000000l;
		hash.significantBits = 25;
		assertEquals("ezs42", hash.toBase32());

		GeoHash bbox = new GeoHash();
		bbox.bits = 0x6fc0000000000000l;
		bbox.significantBits = 12;

		assertFalse(hash.toBase32() + " should NOT be within "
				+ bbox.toBase32(), hash.within(bbox));
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

		hash = GeoHash.withCharacterPrecision(-20.783236276, 31.9867127312312,
				12);
		assertEquals("ksq9zbs0b7vw", hash.toBase32());

		hash = GeoHash.withCharacterPrecision(-76.5110040642321,
				39.0247389581054, 12);
		assertEquals("hf7u8p8gn747", hash.toBase32());

		hash = GeoHash.withCharacterPrecision(-76.5110040642321,
				39.0247389581054, 8);
		assertEquals("hf7u8p8g", hash.toBase32());

		hash = GeoHash.withCharacterPrecision(-76.5110040642321,
				39.0247389581054, 4);
		assertEquals("hf7u", hash.toBase32());
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
	public void testIssue1() {
		double lat = 40.390943;
		double lon = -75.9375;
		
		GeoHash hash = GeoHash.withCharacterPrecision(lat, lon, 12);
		String base32 = hash.toBase32();
		System.out.println(base32);
		assertEquals("dr4jb0bn2180", base32);
	}

	private void printBoundingBox(GeoHash hash) {
		System.out.println("Bounding Box: \ncenter ="
				+ hash.getBoundingBoxCenterPoint());
		System.out.print("corners=");
		System.out
				.println(String.format("%s,%s", hash.getBoundingBoxPoints()[0],
						hash.getBoundingBoxPoints()[1]));
	}
}
