package ch.hsr.geohash;

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
		Assert.assertEquals(0x1l, hash.bits);
		Assert.assertEquals(1, hash.significantBits());
		hash.addOnBitToEnd();
		hash.addOnBitToEnd();
		hash.addOnBitToEnd();
		Assert.assertEquals(0xfl, hash.bits);
		Assert.assertEquals(4, hash.significantBits());
	}

	@Test
	public void testAddingZeroes() {
		hash.addOnBitToEnd();
		Assert.assertEquals(0x1l, hash.bits);

		hash.addOffBitToEnd();
		hash.addOffBitToEnd();
		hash.addOffBitToEnd();
		hash.addOffBitToEnd();
		Assert.assertEquals(0x10l, hash.bits);
		Assert.assertEquals(5, hash.significantBits());
	}

	@Test
	public void testLongToBitString() {
		hash.bits = 0x5555555555555555l;
		Assert
				.assertEquals(
						"0101010101010101010101010101010101010101010101010101010101010101",
						GeoHash.longToBitString(hash.bits));
	}

	@Test
	public void testToBase32() {
		hash.bits = 0x6ff0414000000000l;
		hash.significantBits = 25;

		String base32 = hash.toBase32();
		Assert.assertEquals("ezs42", base32);
	}

	@Test
	public void testWithin() {
		hash.bits = 0x6ff0414000000000l;
		hash.significantBits = 25;
		System.out.println(hash);
		System.out.println(hash.toBase32());
		Assert.assertEquals("ezs42", hash.toBase32());

		GeoHash bbox = new GeoHash();
		bbox.bits = 0x6ff0000000000000l;
		bbox.significantBits = 12;

		Assert.assertTrue(hash.toBase32() + " should be within "
				+ bbox.toBase32(), hash.within(bbox));
	}

	@Test
	public void testNotWithin() {
		hash.bits = 0x6ff0414000000000l;
		hash.significantBits = 25;
		Assert.assertEquals("ezs42", hash.toBase32());

		GeoHash bbox = new GeoHash();
		bbox.bits = 0x6fc0000000000000l;
		bbox.significantBits = 12;

		Assert.assertFalse(hash.toBase32() + " should NOT be within "
				+ bbox.toBase32(), hash.within(bbox));
	}

	@Test
	public void testConstructorWithBitPrecision() {
		GeoHash hash1 = GeoHash.withBitPrecision(45, 120, 20);
		Assert.assertEquals(hash1.significantBits, 20);
		System.out.println(hash1);
		System.out.println(hash1.toBase32());

		GeoHash hash2 = GeoHash.withBitPrecision(45, 120, 55);
		Assert.assertEquals(hash2.significantBits, 55);
		System.out.println(hash2);
		System.out.println(hash2.toBase32());

		Assert.assertTrue(hash2.within(hash1));

		// this should match Dave Troys Codebase. This is also his maximum
		// accuracy (12 5-nibbles).
		GeoHash hash3 = GeoHash.withBitPrecision(20, 31, 60);
		Assert.assertEquals("sew1c2vs2q5r", hash3.toBase32());
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
		Assert.assertEquals("sew1c2vs2q5r", hash.toBase32());

		hash = GeoHash.withCharacterPrecision(-20, 31, 12);
		Assert.assertEquals("ksqn1rje83g2", hash.toBase32());

		hash = GeoHash.withCharacterPrecision(-20.783236276, 31.9867127312312,
				12);
		Assert.assertEquals("ksq9zbs0b7vw", hash.toBase32());

		hash = GeoHash.withCharacterPrecision(-76.5110040642321,
				39.0247389581054, 12);
		Assert.assertEquals("hf7u8p8gn747", hash.toBase32());

		hash = GeoHash.withCharacterPrecision(-76.5110040642321,
				39.0247389581054, 8);
		Assert.assertEquals("hf7u8p8g", hash.toBase32());

		hash = GeoHash.withCharacterPrecision(-76.5110040642321,
				39.0247389581054, 4);
		Assert.assertEquals("hf7u", hash.toBase32());
	}

	@Test
	public void testGetLatitudeBits() {
		hash = GeoHash.withBitPrecision(30, 30, 16);
		long[] latitudeBits = hash.getRightAlignedLatitudeBits();
		Assert.assertEquals(0xaal, latitudeBits[0]);
		Assert.assertEquals(8, latitudeBits[1]);
	}

	@Test
	public void testGetLongitudeBits() {
		hash = GeoHash.withBitPrecision(30, 30, 16);
		long[] longitudeBits = hash.getRightAlignedLongitudeBits();
		Assert.assertEquals(0x95l, longitudeBits[0]);
		Assert.assertEquals(8, longitudeBits[1]);
	}

	@Test
	public void testNeighbourLocationCode() {
		// set up corner case
		hash.bits = 0xc400000000000000l;
		hash.significantBits = 7;
		System.out.println(hash);

		long[] lonBits = hash.getRightAlignedLongitudeBits();
		Assert.assertEquals(0x8, lonBits[0]);
		Assert.assertEquals(4, lonBits[1]);

		long[] latBits = hash.getRightAlignedLatitudeBits();
		Assert.assertEquals(0x5, latBits[0]);
		Assert.assertEquals(3, latBits[1]);

		GeoHash north = hash.getNorthernNeighbour();
		Assert.assertEquals(0xc000000000000000l, north.bits);
		Assert.assertEquals(7, north.significantBits);

		GeoHash south = hash.getSouthernNeighbour();
		Assert.assertEquals(0xd000000000000000l, south.bits);
		Assert.assertEquals(7, south.significantBits());

		GeoHash east = hash.getEasternNeighbour();
		Assert.assertEquals(0xc600000000000000l, east.bits);

		// NOTE: this is actually a corner case!
		GeoHash west = hash.getWesternNeighbour();
		Assert.assertEquals(0x6e00000000000000l, west.bits);

		// NOTE: and now, for the most extreme corner case in 7-bit geohash-land
		hash.bits = 0xfe00000000000000l;

		east = hash.getEasternNeighbour();
		Assert.assertEquals(0x5400000000000000l, east.bits);
		
		// and then from there, just a little south of sanity...
		south = east.getSouthernNeighbour();
		Assert.assertEquals(0x0l, south.bits);
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
