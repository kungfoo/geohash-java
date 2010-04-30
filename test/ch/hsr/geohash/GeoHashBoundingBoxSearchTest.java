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
	}

	private void checkSearchYieldsCorrectNumberOfHashes(double minLat, double minLon, double maxLat, double maxLon) {
		WGS84Point upperLeft = new WGS84Point(minLat, minLon);
		WGS84Point lowerRight = new WGS84Point(maxLat, maxLon);
		GeoHashBoundingBoxSearch search = new GeoHashBoundingBoxSearch(new BoundingBox(upperLeft, lowerRight));
		assertRightNumberOfSearchHashes(search);
	}

	private void assertRightNumberOfSearchHashes(GeoHashBoundingBoxSearch search) {
		assertTrue(search.getSearchHashes().size() == 2 || search.getSearchHashes().size() == 4);
	}
}
