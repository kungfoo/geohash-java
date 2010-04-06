/*
 * Copyright 2010, Silvio Heuberger @ IFS www.ifs.hsr.ch
 *
 * This code is release under the LGPL license.
 * You should have received a copy of the license
 * in the LICENSE file. If you have not, see
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 */
package ch.hsr.geohash;

public class GeoHashBoundingBoxSearch {
	
	private BoundingBox boundingBox;
	private int precision;

	/**
	 * @param upperLeft
	 *            : upper left corner of the bounding box to find hashes within.
	 * @param lowerRight
	 *            : lower right corner.
	 */
	public GeoHashBoundingBoxSearch(BoundingBox bbox) {
		this(bbox, 16);
	}

	/**
	 * search for hashes with the given precision. The search will be faster
	 * when less precise, but it my contain more false positives.
	 * 
	 * @param precision
	 *            : bit precision to search with. Defaults to 16 bits.
	 */
	public GeoHashBoundingBoxSearch(BoundingBox bbox, int precision) {
		this.boundingBox = bbox;
		this.precision = Math.min(precision, 64);
		
		
	}
}
