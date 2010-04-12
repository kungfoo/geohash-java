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
		GeoHashBoundingBoxSearch search = new GeoHashBoundingBoxSearch(new BoundingBox(new WGS84Point(40.09872762,
				30.0113312322), new WGS84Point(41.23452234, 31.23432)));
		
	}
}
