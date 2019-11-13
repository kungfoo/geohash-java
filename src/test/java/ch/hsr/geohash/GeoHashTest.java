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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

import ch.hsr.geohash.util.BoundingBoxGeoHashIterator;
import ch.hsr.geohash.util.RandomGeohashes;
import ch.hsr.geohash.util.TwoGeoHashBoundingBox;

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
	public void testToBase32() {
		hash.bits = 0x6ff0414000000000l;
		hash.significantBits = 25;

		String base32 = hash.toBase32();
		assertEquals("ezs42", base32);
	}

	@Test(expected = IllegalStateException.class)
	public void toBase32ShouldThrowWhenPrecisionIsNotAMultipleOf5() {
		hash.bits = 0x6ff0413000000000l;
		hash.significantBits = 24;
		hash.toBase32();
	}

	@Test
	public void testDecode() {
		// for all lat/lon pairs check decoded point is in the same bbox as the
		// geohash formed by encoder
		for (GeoHash gh : RandomGeohashes.fullRange()) {
			BoundingBox bbox = gh.getBoundingBox();
			GeoHash decodedHash = GeoHash.fromGeohashString(gh.toBase32());
			WGS84Point decodedCenter = decodedHash.getBoundingBoxCenterPoint();

			assertTrue("bbox " + bbox + " should contain the decoded center value " + decodedCenter, bbox
					.contains(decodedCenter));
			BoundingBox decodedBoundingBox = decodedHash.getBoundingBox();
			assertEquals(bbox, decodedBoundingBox);
			assertEquals(gh, decodedHash);
			assertEquals(gh.toBase32(), decodedHash.toBase32());
		}
	}

	@Test
	public void testToAndFromBinaryString() {
		for (GeoHash gh : RandomGeohashes.fullRange()) {
			String binaryString = gh.toBinaryString();
			GeoHash readBack = GeoHash.fromBinaryString(binaryString);
			assertEquals(gh, readBack);
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

		assertWithin(hash, bbox);
	}

	private void assertWithin(GeoHash hash, GeoHash bbox) {
		assertTrue(hash + " should be within " + bbox, hash.within(bbox));
	}

	@Test
	public void itShouldCreateAHashWithMaximumPrecisionOf64Bits() {
		GeoHash.withBitPrecision(10.0, 10.0, 64);
	}

	@Test(expected = IllegalArgumentException.class)
	public void itShouldThrowWhenTheBitPrecisionIsMoreThan64Bits() {
		GeoHash.withBitPrecision(46.0, 8.0, 70);
	}

	@Test(expected = IllegalArgumentException.class)
	public void itShouldThrowWhenTheCharacterPrecisionIsTooBig() {
		GeoHash.withCharacterPrecision(10.0, 120.0, 14);
	}

	@Test
	public void testNotWithin() {
		hash.bits = 0x6ff0414000000000l;
		hash.significantBits = 25;
		assertEquals("ezs42", hash.toBase32());

		GeoHash bbox = new GeoHash();
		bbox.bits = 0x6fc0000000000000l;
		bbox.significantBits = 12;

		assertFalse(hash + " should NOT be within " + bbox, hash.within(bbox));
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
		assertEncodingWithCharacterPrecision(new WGS84Point(20, 31), 12, "sew1c2vs2q5r");
		assertEncodingWithCharacterPrecision(new WGS84Point(-20, 31), 12, "ksqn1rje83g2");
		assertEncodingWithCharacterPrecision(new WGS84Point(-20.783236276, 31.9867127312312), 12, "ksq9zbs0b7vw");

		WGS84Point point = new WGS84Point(-76.5110040642321, 39.0247389581054);
		String fullStringValue = "hf7u8p8gn747";
		for (int characters = 12; characters > 1; characters--) {
			assertEncodingWithCharacterPrecision(point, characters, fullStringValue.substring(0, characters));
		}

		assertEncodingWithCharacterPrecision(new WGS84Point(39.0247389581054, -76.5110040642321), 12, "dqcw4bnrs6s7");

		String geoHashString = GeoHash.geoHashStringWithCharacterPrecision(point.getLatitude(), point.getLongitude(), 12);
		assertEquals(fullStringValue, geoHashString);
	}

	private void assertEncodingWithCharacterPrecision(WGS84Point point, int numberOfCharacters, String stringValue) {
		GeoHash hash = GeoHash.withCharacterPrecision(point.getLatitude(), point.getLongitude(), numberOfCharacters);
		assertEquals(stringValue, hash.toBase32());
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
		assertEquals(0xd000000000000000l, north.bits);
		assertEquals(7, north.significantBits);

		GeoHash south = hash.getSouthernNeighbour();
		assertEquals(0xc000000000000000l, south.bits);
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
		assertEquals(start.getBoundingBox(), end.getBoundingBox());
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

			GeoHash hash = RandomGeohashes.createWithPrecision(bits);
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
	public void testKnownNeighbouringHashes() {
		GeoHash h1 = GeoHash.fromGeohashString("u1pb");
		assertEquals("u0zz", h1.getSouthernNeighbour().toBase32());
		assertEquals("u1pc", h1.getNorthernNeighbour().toBase32());
		assertEquals("u300", h1.getEasternNeighbour().toBase32());
		assertEquals("u302", h1.getEasternNeighbour().getEasternNeighbour().toBase32());
		assertEquals("u1p8", h1.getWesternNeighbour().toBase32());

		assertEquals("sp2j", GeoHash.withCharacterPrecision(41.7, 0.08, 4).toBase32());
	}

	@Test
	public void testKnownAdjacentNeighbours() {
		String center = "dqcjqc";
		String[] adjacent = new String[] { "dqcjqf", "dqcjqb", "dqcjr1", "dqcjq9", "dqcjqd", "dqcjr4", "dqcjr0",
				"dqcjq8" };
		assertAdjacentHashesAre(center, adjacent);

		center = "u1x0dfg";
		adjacent = new String[] { "u1x0dg4", "u1x0dg5", "u1x0dgh", "u1x0dfu", "u1x0dfs", "u1x0dfe", "u1x0dfd",
				"u1x0dff" };
		assertAdjacentHashesAre(center, adjacent);

		center = "sp2j";
		adjacent = new String[] { "ezry", "sp2n", "sp2q", "sp2m", "sp2k", "sp2h", "ezru", "ezrv" };
		assertAdjacentHashesAre(center, adjacent);
	}

	@Test
	public void testThatAdjacentHashesHavePointInitialized() {
		String center = "dqcjqc";
		GeoHash geohash = GeoHash.fromGeohashString(center);
		GeoHash[] adjacentHashes = geohash.getAdjacent();
		for (GeoHash adjacentHash : adjacentHashes) {
			assertNotNull(adjacentHash.getBoundingBox());
			assertNotNull(adjacentHash.getBoundingBoxCenterPoint());
			assertNotNull(adjacentHash.getPoint());
		}
	}

	private void assertAdjacentHashesAre(String centerString, String[] adjacentStrings) {
		GeoHash center = GeoHash.fromGeohashString(centerString);
		GeoHash[] adjacent = center.getAdjacent();
		for (String check : adjacentStrings) {
			assertArrayContainsGeoHash(check, adjacent);
		}
	}

	private void assertArrayContainsGeoHash(String check, GeoHash[] hashes) {
		boolean found = false;
		for (GeoHash hash : hashes) {
			if (hash.toBase32().equals(check)) {
				found = true;
				break;
			}
		}
		assertTrue("Array should contain " + check, found);
	}

	@Test
	public void testNeibouringHashesNearMeridian() {
		GeoHash hash = GeoHash.fromGeohashString("sp2j");
		GeoHash west = hash.getWesternNeighbour();
		assertEquals("ezrv", west.toBase32());
		west = west.getWesternNeighbour();
		assertEquals("ezrt", west.toBase32());
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

	@Test
	public void testSimpleWithin() {
		GeoHash hash = GeoHash.withBitPrecision(70, -120, 8);
		GeoHash inside = GeoHash.withBitPrecision(74, -130, 64);
		assertWithin(inside, hash);
	}

	private void printBoundingBox(GeoHash hash) {
		System.out.println("Bounding Box: \ncenter =" + hash.getBoundingBoxCenterPoint());
		System.out.print("corners=");
		System.out.println(hash.getBoundingBox());
	}

	@Test
	public void testToLongAndBack() {
		double lat = 40.390943;
		double lon = -75.9375;
		GeoHash hash = GeoHash.withCharacterPrecision(lat, lon, 10);
		long lv = hash.longValue();
		assertEquals(lv + (1 << (64 - hash.significantBits())), hash.next().longValue());
		GeoHash hashFromLong = GeoHash.fromLongValue(lv, hash.significantBits());
		assertEquals("dr4jb0bn21", hashFromLong.toBase32());
		assertEquals(hash, hashFromLong);
	}

	@Test
	public void testNext() {
		double lat = 37.7;
		double lon = -122.52;
		GeoHash hash = GeoHash.withBitPrecision(lat, lon, 10);
		GeoHash next = hash.next();
		assertTrue(hash.compareTo(next) < 0);
	}

	@Test
	public void testNextPrev() {
		double lat = 37.7;
		double lon = -122.52;
		GeoHash hash = GeoHash.withBitPrecision(lat, lon, 35);
		GeoHash next = hash.next(2);
		assertTrue(hash.compareTo(next) < 0);
		GeoHash prev1 = next.prev();
		GeoHash prev2 = prev1.next(-1);
		assertTrue(prev1.compareTo(next) < 0);
		System.out.println("hash: " + hash.toBase32());
		System.out.println("next: " + next.toBase32());
		System.out.println("prev1: " + prev1.toBase32());
		System.out.println("prev2: " + prev2.toBase32());

		assertTrue(prev2.compareTo(prev1) < 0);
		assertTrue(prev2.compareTo(hash) == 0);
	}

	@Test(expected = IllegalStateException.class)
	public void testGetCharacterPrecisionThrows() throws Exception {
		GeoHash hash = GeoHash.withBitPrecision(37.7, -122.52, 32);
		hash.getCharacterPrecision();
	}

	@Test
	public void testGetCharacterPrecisionWorksWhenPrecisionIsMultipleOfFive() throws Exception {
		GeoHash hash = GeoHash.withBitPrecision(37.7, -122.52, 60);
		int precision = hash.getCharacterPrecision();
		assertEquals(precision, 12);
	}

	@Test
	public void testStepsBetween() {
		GeoHash bl = GeoHash.withBitPrecision(37.7, -122.52, 35);
		GeoHash ur = GeoHash.withBitPrecision(37.84, -122.35, 35);

		long steps = GeoHash.stepsBetween(bl, bl);
		assertEquals(steps, 0);

		steps = GeoHash.stepsBetween(bl, bl.next(4));
		assertEquals(steps, 4);

		BoundingBoxGeoHashIterator iter = new BoundingBoxGeoHashIterator(new TwoGeoHashBoundingBox(bl, ur));
		int count = 0;
		while (iter.hasNext()) {
			iter.next();
			count++;
		}
		assertEquals(12875, count);

		int allHashes = 0;
		int inBbox = 1;
		int latMore = 0;
		int lonMore = 0;
		int bothMore = 0;
		int latLess = 0;
		int lonLess = 0;
		int bothLess = 0;
		int latLessLonMore = 0;
		int latMoreLonLess = 0;
		GeoHash idx = bl;
		BoundingBox iterBbox = iter.getBoundingBox().getBoundingBox();
		while (idx.compareTo(ur) < 0) {
			idx = idx.next();
			allHashes++;
			if (iterBbox.contains(idx.getPoint())) {
				inBbox++;
			}
			boolean latIsMore = false;
			boolean latIsLess = false;
			if (idx.getPoint().getLatitude() > iterBbox.getNorthLatitude()) {
				latIsMore = true;
				latMore++;
			} else if (idx.getPoint().getLatitude() < iterBbox.getSouthLatitude()) {
				latIsLess = true;
				latLess++;
			}
			if (idx.getPoint().getLongitude() > iterBbox.getEastLongitude()) {
				lonMore++;
				if (latIsMore) {
					bothMore++;
				}
				if (latIsLess) {
					latLessLonMore++;
				}
			} else if (idx.getPoint().getLongitude() < iterBbox.getWestLongitude()) {
				lonLess++;
				if (latIsLess) {
					bothLess++;
				}
				if (latIsMore) {
					latMoreLonLess++;
				}
			}
		}

		// Just trying to understand where these GeoHashes are with regard to
		// their bounding box.
		steps = GeoHash.stepsBetween(bl, ur);
		assertEquals(48472, steps);
		assertEquals(steps, allHashes);
		assertEquals(count, inBbox);
		assertEquals(14938, latMore);
		assertEquals(640, lonMore);
		assertEquals(0, bothMore);
		assertEquals(7680, latLess);
		assertEquals(24391, lonLess);
		assertEquals(0, bothLess);
		assertEquals(240, latLessLonMore);
		assertEquals(11811, latMoreLonLess);
		assertEquals(steps, lonLess + latLess + latMore + lonMore + inBbox - latLessLonMore - latMoreLonLess - 1);

	}

	@Test
	public void testCompareTo() {
		GeoHash prevHash = null;
		for (int i = 0; i < 10000; i++) {
			GeoHash hash = RandomGeohashes.createWith5BitsPrecision();
			if (i >= 1) {
				String prevHashBase32 = prevHash.toBase32();
				String hashBase32 = hash.toBase32();
				String errorMessage = String.format("prev: %s, cur: %s", prevHashBase32, hashBase32);
				if (prevHashBase32.compareTo(hashBase32) < 0) {
					assertTrue(errorMessage, prevHash.compareTo(hash) < 0);
				} else if (prevHashBase32.compareTo(hashBase32) > 0) {
					assertTrue(errorMessage, prevHash.compareTo(hash) > 0);
				} else {
					assertTrue(errorMessage, prevHash.compareTo(hash) == 0);
				}
			}
			prevHash = hash;
		}
	}

	@Test
	public void testOrdIsPositive() {
		double lat = 40.390943;
		double lon = 75.9375;
		GeoHash hash = GeoHash.withCharacterPrecision(lat, lon, 12);
		assertEquals(0xcf6915015410500l, hash.ord());
		assertTrue(hash.ord() >= 0);
	}

	@Test
	public void testSecondCaseWhereOrdMustBePositive() {
		GeoHash hash = GeoHash.withCharacterPrecision(-36.919550434870125,174.71024582237604,7);
		assertTrue(hash.ord() > 0);
	}
}
