package ch.hsr.geohash.util;

import ch.hsr.geohash.GeoHash;

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
		this.boundingBox = bbox;
		this.current = bbox.getBottomLeft();
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
		if (rv.equals(boundingBox.getTopRight())) {
		    current = null;
        } else {
			current = rv.next();
            while (hasNext() && !boundingBox.getBoundingBox().contains(current.getPoint())) {
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
