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
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ch.hsr.geohash.queries.GeoHashBoundingBoxQuery;
import ch.hsr.geohash.queries.GeoHashQuery;

public class GeoHashBoundingBoxSearchTest {

	@Test
	public void testSeveralBoundingBoxes() {
		checkSearchYieldsCorrectNumberOfHashes(40.2090980098, 40.21982983232432, -22.523432424324, -22.494234232442);
		checkSearchYieldsCorrectNumberOfHashes(40.09872762, 41.23452234, 30.0113312322, 31.23432);

		checkSearchYieldsCorrectHashes(47.300200, 47.447907, 8.471276, 8.760941, "u0qj");
		checkSearchYieldsCorrectHashes(47.157502, 47.329727, 8.562244, 8.859215, "u0qj", "u0qm", "u0qh", "u0qk");

		// Testing bounding box over 180-Meridian
		checkSearchYieldsCorrectNumberOfHashes(40.2090980098, 40.21982983232432, 170.523432424324, -170.494234232442);
		checkSearchYieldsCorrectNumberOfHashes(40.2090980098, 40.21982983232432, 170.523432424324, 160.494234232442);

		checkSearchYieldsCorrectHashes(40.2090980098, 40.21982983232432, 170.523432424324, -170.494234232442, "xz", "8p");
		checkSearchYieldsCorrectBinaryHashes(47.157502, 47.329727, 179.062244, -179.859215, "1111101010101111", "010100000000010100000", "010100000000010100010");

		// Check duplicate handling
		checkSearchYieldsCorrectBinaryHashes(47.157502, 47.329727, 179.062244, 160, "");
		checkSearchYieldsCorrectBinaryHashes(47.157502, 47.329727, 179.062244, -1, "01", "1111101010101111");
	}

	private void checkSearchYieldsCorrectNumberOfHashes(double southLat, double northLat, double westLon, double eastLon) {
		GeoHashQuery search = new GeoHashBoundingBoxQuery(new BoundingBox(southLat, northLat, westLon, eastLon));
		assertRightNumberOfSearchHashes(search);
	}

	private void checkSearchYieldsCorrectHashes(double southLat, double northLat, double westLon, double eastLon, String... hashes) {
		GeoHashQuery search = new GeoHashBoundingBoxQuery(new BoundingBox(southLat, northLat, westLon, eastLon));
		assertEquals(hashes.length, search.getSearchHashes().size());
		for (String expectedHash : hashes) {
			assertTrue("search hashes should contain '" + expectedHash + "':'" + GeoHash.fromGeohashString(expectedHash) + "'. Saved hashes:\n " + search, search.getSearchHashes().contains(GeoHash.fromGeohashString(expectedHash)));
		}
	}

	private void checkSearchYieldsCorrectBinaryHashes(double southLat, double northLat, double westLon, double eastLon, String... hashes) {
		GeoHashQuery search = new GeoHashBoundingBoxQuery(new BoundingBox(southLat, northLat, westLon, eastLon));
		assertEquals(hashes.length, search.getSearchHashes().size());
		for (String expectedHash : hashes) {
			assertTrue("search hashes should contain '" + expectedHash + "':'" + GeoHash.fromBinaryString(expectedHash) + "'. Saved hashes:\n " + search, search.getSearchHashes().contains(GeoHash.fromBinaryString(expectedHash)));
		}
	}

	private void assertRightNumberOfSearchHashes(GeoHashQuery search) {
		int size = search.getSearchHashes().size();
		assertTrue(size <= 8 && size > 0);
	}
}
