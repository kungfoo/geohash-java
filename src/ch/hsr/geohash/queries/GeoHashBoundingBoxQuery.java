/*
 * Copyright 2010, Silvio Heuberger @ IFS www.ifs.hsr.ch
 *
 * This code is release under the LGPL license.
 * You should have received a copy of the license
 * in the LICENSE file. If you have not, see
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 */
package ch.hsr.geohash.queries;

import java.util.ArrayList;
import java.util.List;

import ch.hsr.geohash.BoundingBox;
import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;
import ch.hsr.geohash.util.GeoHashSizeTable;

/**
 * This class returns the hashes covering a certain bounding box. There are
 * either 1,2 or 4 susch hashes, depending on the position of the bounding box
 * on the geohash grid.
 */
public class GeoHashBoundingBoxQuery implements GeoHashQuery {

	/* there's not going to be more than 4 hashes. */
	private List<GeoHash> searchHashes = new ArrayList<GeoHash>(4);
	
	public GeoHashBoundingBoxQuery(BoundingBox bbox) {
		int fittingBits = GeoHashSizeTable.numberOfBitsForOverlappingGeoHash(bbox);
		WGS84Point center = bbox.getCenterPoint();
		GeoHash centerHash = GeoHash.withBitPrecision(center.getLatitude(), center.getLongitude(), fittingBits);

		if (hashFits(centerHash, bbox)) {
			searchHashes.add(centerHash);
		} else {
			expandSearch(centerHash, bbox);
		}
	}

	private void expandSearch(GeoHash centerHash, BoundingBox bbox) {
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

	public boolean contains(GeoHash hash) {
		for (GeoHash searchHash : searchHashes) {
			if (hash.within(searchHash))
				return true;
		}
		return false;
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
