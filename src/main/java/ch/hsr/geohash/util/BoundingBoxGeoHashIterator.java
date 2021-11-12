package ch.hsr.geohash.util;

import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.util.TwoGeoHashBoundingBox;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterate over all of the values within a bounding box at a particular
 * resolution
 */
public class BoundingBoxGeoHashIterator implements Iterator<GeoHash> {
	private TwoGeoHashBoundingBox boundingBox;
	private GeoHash current;

	public BoundingBoxGeoHashIterator(TwoGeoHashBoundingBox bbox) {
		boundingBox = bbox;
		current = bbox.getSouthWestCorner();
	}

	public TwoGeoHashBoundingBox getBoundingBox() {
		return boundingBox;
	}

	@Override
	public boolean hasNext() {
		return current != null;
	}

	@Override
	public GeoHash next() {
		if (!hasNext()) {
			throw new NoSuchElementException();
		}

		GeoHash rv = current;
		if (rv.equals(boundingBox.getNorthEastCorner())) {
		    current = null;
        } else {
			current = rv.next();
            while (hasNext() && !boundingBox.getBoundingBox().contains(current.getOriginatingPoint())) {
                current = current.next();
            }
        }

		return rv;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
