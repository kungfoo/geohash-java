/*
 * Copyright 2010, Silvio Heuberger @ IFS www.ifs.hsr.ch
 *
 * This code is release under the LGPL license.
 * You should have received a copy of the license
 * in the LICENSE file. If you have not, see
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 */
package ch.hsr.geohash;

import static org.junit.Assert.*;
import org.junit.Test;

public class GeoHashBoundingBoxSearchTest {

	@Test
	public void testSeveralBoundingBoxes() {
		checkSearchYieldsCorrectNumberOfHashes(40.2, -22.5, 40.3, -22.4);
		checkSearchYieldsCorrectNumberOfHashes(40.2, -22.5, 40.3, -22.4);
		checkSearchYieldsCorrectNumberOfHashes(40.2090980098, -22.523432424324, 40.21982983232432, -22.494234232442);
		checkSearchYieldsCorrectNumberOfHashes(40.09872762, 30.0113312322, 41.23452234, 31.23432);
		checkSearchYieldsCorrectHashes(47.447907, 8.471276, 47.300200, 8.760941, "u0qj");
		checkSearchYieldsCorrectHashes(47.157502, 8.562244, 47.329727, 8.859215, "u0qj", "u0qm", "u0qh", "u0qk");
	}

	private void checkSearchYieldsCorrectNumberOfHashes(double minLat, double minLon, double maxLat, double maxLon) {
		GeoHashBoundingBoxSearch search = new GeoHashBoundingBoxSearch(new BoundingBox(minLat, minLon, maxLat, maxLon));
		assertRightNumberOfSearchHashes(search);
	}

	private void checkSearchYieldsCorrectHashes(double minLat, double minLon, double maxLat, double maxLon,
			String... hashes) {
		GeoHashBoundingBoxSearch search = new GeoHashBoundingBoxSearch(new BoundingBox(minLat, minLon, maxLat, maxLon));
		assertEquals(hashes.length, search.getSearchHashes().size());
		for (String expectedHash : hashes) {
			assertTrue("search hashes should contain " + expectedHash + " is: " + search, search.getSearchHashes().contains(
					GeoHash.fromGeohashString(expectedHash)));
		}
	}

	private void assertRightNumberOfSearchHashes(GeoHashBoundingBoxSearch search) {
		int size = search.getSearchHashes().size();
		assertTrue(size == 1 || size == 2 || size == 4);
	}
}
