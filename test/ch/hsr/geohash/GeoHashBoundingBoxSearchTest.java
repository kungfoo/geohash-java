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
	public void testBoundingBoxSearch() {
		GeoHashBoundingBoxSearch search = createSearch(40.09872762, 30.0113312322, 41.23452234, 31.23432);
		
	}

	@Test
	public void testSeveralBoundingBoxes() {
		GeoHashBoundingBoxSearch search = createSearch(40.2, -22.5, 40.3, -22.4);
		createSearch(40.2, -22.5, 40.3, -22.4);
		createSearch(40.2090980098, -22.523432424324, 40.21982983232432, -22.494234232442);
	}

	private GeoHashBoundingBoxSearch createSearch(double minx, double miny, double maxx, double maxy) {
		return new GeoHashBoundingBoxSearch(new BoundingBox(new WGS84Point(minx, miny), new WGS84Point(maxx, maxy)));
	}
}
