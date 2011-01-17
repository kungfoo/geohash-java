package ch.hsr.geohash.util;

import ch.hsr.geohash.GeoHash;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created by IntelliJ IDEA.
 * User: kevin
 * Date: Jan 6, 2011
 * Time: 11:10:03 AM
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
        return current.compareTo(boundingBox.getTopRight()) <= 0;
    }

    @Override
    public GeoHash next() {
        GeoHash rv = current;
        if (!hasNext()) throw new NoSuchElementException();
        current = rv.next();
        while (hasNext() && !boundingBox.getBoundingBox().contains(current.getPoint())) {
            current = current.next();
        }
        return rv;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
