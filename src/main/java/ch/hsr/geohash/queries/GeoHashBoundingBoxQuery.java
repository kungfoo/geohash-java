/*
 * Copyright 2010, Silvio Heuberger @ IFS www.ifs.hsr.ch
 *
 * This code is release under the Apache License 2.0.
 * You should have received a copy of the license
 * in the LICENSE file. If you have not, see
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package ch.hsr.geohash.queries;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ch.hsr.geohash.BoundingBox;
import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;
import ch.hsr.geohash.util.GeoHashSizeTable;

/**
 * This class returns the hashes covering a certain bounding box. There are
 * either 1,2 or 4 such hashes, depending on the position of the bounding box
 * on the geohash grid.
 */
public class GeoHashBoundingBoxQuery implements GeoHashQuery, Serializable {
	private static final long serialVersionUID = 9223256928940522683L;
	/* there's not going to be more than 4 hashes. */
	private List<GeoHash> searchHashes = new ArrayList<>(4);
	/* the combined bounding box of those hashes. */
	private BoundingBox boundingBox;

	public GeoHashBoundingBoxQuery(BoundingBox bbox) {
		int fittingBits = GeoHashSizeTable.numberOfBitsForOverlappingGeoHash(bbox);
		WGS84Point center = bbox.getCenterPoint();
		GeoHash centerHash = GeoHash.withBitPrecision(center.getLatitude(), center.getLongitude(), fittingBits);

		if (hashFits(centerHash, bbox)) {
			addSearchHash(centerHash);
		} else {
			expandSearch(centerHash, bbox);
		}
	}

	private void addSearchHash(GeoHash hash) {
		if (boundingBox == null) {
			boundingBox = new BoundingBox(hash.getBoundingBox());
		} else {
			boundingBox.expandToInclude(hash.getBoundingBox());
		}
		searchHashes.add(hash);
	}

	private void expandSearch(GeoHash centerHash, BoundingBox bbox) {
		addSearchHash(centerHash);

		for (GeoHash adjacent : centerHash.getAdjacent()) {
			BoundingBox adjacentBox = adjacent.getBoundingBox();
			if (adjacentBox.intersects(bbox) && !searchHashes.contains(adjacent)) {
				addSearchHash(adjacent);
			}
		}
	}

	private boolean hashFits(GeoHash hash, BoundingBox bbox) {
		return hash.contains(bbox.getUpperLeft()) && hash.contains(bbox.getLowerRight());
	}

	@Override
	public boolean contains(GeoHash hash) {
		for (GeoHash searchHash : searchHashes) {
			if (hash.within(searchHash)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean contains(WGS84Point point) {
		return contains(GeoHash.withBitPrecision(point.getLatitude(), point.getLongitude(), 64));
	}

	@Override
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

	@Override
	public String getWktBox() {
		return "BOX(" + boundingBox.getMinLon() + " " + boundingBox.getMinLat() + "," + boundingBox.getMaxLon() + " "
				+ boundingBox.getMaxLat() + ")";
	}
}
