package ch.hsr.geohash;

public class GeoHashBoundingBoxSearch {
	
	private WGS84Point upperLeft;
	private WGS84Point lowerRight;
	private int precision;

	/**
	 * @param upperLeft
	 *            : upper left corner of the bounding box to find hashes within.
	 * @param lowerRight
	 *            : lower right corner.
	 */
	public GeoHashBoundingBoxSearch(WGS84Point upperLeft, WGS84Point lowerRight) {
		this(upperLeft, lowerRight, 16);
	}

	/**
	 * search for hashes with the given precision. The search will be faster
	 * when less precise, but it my contain more false positives.
	 * 
	 * @param precision
	 *            : bit precision to search with. Defaults to 16 bits.
	 */
	public GeoHashBoundingBoxSearch(WGS84Point upperLeft, WGS84Point lowerRight, int precision) {
		this.upperLeft = upperLeft;
		this.lowerRight = lowerRight;
		this.precision = Math.min(precision, 64);
		
		
	}
}