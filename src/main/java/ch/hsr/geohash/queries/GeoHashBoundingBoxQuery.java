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
	/* there can be up to 8 hashes since it can be 2 separate queries */
	private List<GeoHash> searchHashes = new ArrayList<>(8);
	/* the combined bounding box of those hashes. */
	private BoundingBox boundingBox;

	public GeoHashBoundingBoxQuery(BoundingBox bbox) {
		if (!bbox.intersects180Meridian()) {
			// In this case one query is enough
			generateSearchHashes(bbox);
		} else {
			// In this case we need two queries
			BoundingBox eastBox = new BoundingBox(bbox.getSouthLatitude(), bbox.getNorthLatitude(), bbox.getWestLongitude(), 180);
			BoundingBox westBox = new BoundingBox(bbox.getSouthLatitude(), bbox.getNorthLatitude(), -180, bbox.getEastLongitude());

			generateSearchHashes(eastBox);
			generateSearchHashes(westBox);
		}

		// Finally create the combined bounding box
		for (GeoHash hash : searchHashes) {
			if (boundingBox == null)
				boundingBox = new BoundingBox(hash.getBoundingBox());
			else
				boundingBox.expandToInclude(hash.getBoundingBox());
		}

		// Check the search hashes on a query over the full planet
		for (GeoHash hash : searchHashes) {
			if (hash.significantBits() == 0) {
				searchHashes.clear();
				searchHashes.add(hash);
				return;
			}
		}

		// Check the search hashes on possible duplicates
		List<GeoHash> toRemove = new ArrayList<GeoHash>(searchHashes.size() - 1);
		for (GeoHash hash : searchHashes) {
			for (GeoHash hashToCompare : searchHashes) {
				if (hashToCompare.significantBits() < hash.significantBits()) {
					long hashCopy = hash.longValue();
					long hashCompareCopy = hashToCompare.longValue();
					int equalBits = 0;
					while ((hashCompareCopy & GeoHash.FIRST_BIT_FLAGGED) == (hashCopy & GeoHash.FIRST_BIT_FLAGGED)) {
						hashCompareCopy <<= 1;
						hashCopy <<= 1;
						equalBits++;
					}

					if (equalBits == hashToCompare.significantBits()) {
						toRemove.add(hash);
						break;
					}
				}
			}
		}
		for (GeoHash hash : toRemove) {
			searchHashes.remove(hash);
		}
	}

	private void generateSearchHashes(BoundingBox bbox) {
		int fittingBits = GeoHashSizeTable.numberOfBitsForOverlappingGeoHash(bbox);
		WGS84Point center = bbox.getCenter();
		GeoHash centerHash = GeoHash.withBitPrecision(center.getLatitude(), center.getLongitude(), fittingBits);

		if (hashContainsBoundingBox(centerHash, bbox)) {
			// If the centerHash completly fits into the provided bounding box, just add the hash and continue
			searchHashes.add(centerHash);
		} else {
			// Else the search has to be extended to the adjacent geohashes
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

	/**
	 * Checks if the provided hash completely(!) contains the provided bounding box
	 *
	 * @param hash
	 * @param bbox
	 * @return
	 */
	private boolean hashContainsBoundingBox(GeoHash hash, BoundingBox bbox) {
		return hash.contains(bbox.getNorthWestCorner()) && hash.contains(bbox.getSouthEastCorner());
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
		return "BOX(" + boundingBox.getWestLongitude() + " " + boundingBox.getSouthLatitude() + "," + boundingBox.getEastLongitude() + " " + boundingBox.getNorthLatitude() + ")";
	}
}
