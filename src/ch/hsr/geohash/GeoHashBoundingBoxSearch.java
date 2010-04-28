/*
 * Copyright 2010, Silvio Heuberger @ IFS www.ifs.hsr.ch
 *
 * This code is release under the LGPL license.
 * You should have received a copy of the license
 * in the LICENSE file. If you have not, see
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 */
package ch.hsr.geohash;

import java.util.ArrayList;
import java.util.List;

import ch.hsr.geohash.util.GeoHashSizeTable;

public class GeoHashBoundingBoxSearch {

	private BoundingBox boundingBox;
	private int precision;
	private List<GeoHash> searchHashes;

	/**
	 * return the hash(es) that approximate this bounding box.
	 */
	public GeoHashBoundingBoxSearch(BoundingBox bbox) {
		int fittingBits = GeoHashSizeTable.numberOfBitsForOverlappingGeoHash(bbox);
		GeoHash upperLeftHash = GeoHash
				.withBitPrecision(bbox.getUpperLeft().getLatitude(), bbox.getUpperLeft().getLongitude(), fittingBits);
		GeoHash lowerRightHash = GeoHash.withBitPrecision(bbox.getLowerRight().getLatitude(), bbox.getLowerRight().getLongitude(),
				fittingBits);
		if (upperLeftHash.equals(lowerRightHash)) {
			/* the hashes fit exactly, we're lucky */
			searchHashes = new ArrayList<GeoHash>(1);
			searchHashes.add(upperLeftHash);
		} else {
			// TODO: search for more hashes that cut the bounding box.
		}
	}
}
