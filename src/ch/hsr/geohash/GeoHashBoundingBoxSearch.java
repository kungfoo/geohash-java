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

	/* there's not going to be more than 4 hashes. */
	private List<GeoHash> searchHashes = new ArrayList<GeoHash>(4);

	/**
	 * return the hash(es) that approximate this bounding box.
	 */
	public GeoHashBoundingBoxSearch(BoundingBox bbox) {
		int fittingBits = GeoHashSizeTable.numberOfBitsForOverlappingGeoHash(bbox);
		WGS84Point center = bbox.getCenterPoint();
		GeoHash centerHash = GeoHash.withBitPrecision(center.getLatitude(), center.getLongitude(), fittingBits);

		if (hashFits(centerHash, bbox)) {
			System.out.println("yay, centered hash fits.");
			searchHashes.add(centerHash);
		} else {
			expandSearch(centerHash, bbox);
		}
	}

	private void expandSearch(GeoHash centerHash, BoundingBox bbox) {
		assert centerHash.getBoundingBox().intersects(bbox) : "center hash must at least intersect the bounding box!";
		searchHashes.add(centerHash);

		for (GeoHash adjacent : centerHash.getAdjacent()) {
			BoundingBox adjacentBox = adjacent.getBoundingBox();
			if (adjacentBox.intersects(bbox) && !searchHashes.contains(adjacent)) {
				searchHashes.add(adjacent);
			}
		}
	}

	private boolean hashFits(GeoHash hash, BoundingBox bbox) {
		return hash.contains(bbox.getUpperLeft()) && hash.contains(bbox.getLowerRight());
	}

	public List<GeoHash> getSearchHashes() {
		return searchHashes;
	}

	@Override
	public String toString() {
		StringBuilder bui = new StringBuilder();
		for (GeoHash hash : searchHashes) {
			bui.append(hash).append("\n");
		}
		return bui.toString();
	}
}
