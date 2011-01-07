package ch.hsr.geohash.util;

import ch.hsr.geohash.BoundingBox;
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
    private BoundingBox boundingBox;
    private GeoHash bottomLeft;
    private GeoHash topRight;
    private GeoHash current;

    public static BoundingBoxGeoHashIterator withCharacterPrecision(BoundingBox bbox, int numberOfCharacters) {
        GeoHash bottomLeft = GeoHash.withCharacterPrecision(bbox.getMinLat(), bbox.getMinLon(), numberOfCharacters);
        GeoHash topRight = GeoHash.withCharacterPrecision(bbox.getMaxLat(), bbox.getMaxLon(), numberOfCharacters);
        return new BoundingBoxGeoHashIterator(bottomLeft, topRight);
    }
    /**
     * create a new {@link GeoHash} with the given number of bits accuracy. This
     * at the same time defines this hash's bounding box.
     */
    public static BoundingBoxGeoHashIterator withBitPrecision(BoundingBox bbox, int numberOfBits) {
        GeoHash bottomLeft = GeoHash.withBitPrecision(bbox.getMinLat(), bbox.getMinLon(), numberOfBits);
        GeoHash topRight = GeoHash.withBitPrecision(bbox.getMaxLat(), bbox.getMaxLon(), numberOfBits);
        return new BoundingBoxGeoHashIterator(bottomLeft, topRight);
    }

    public BoundingBoxGeoHashIterator(GeoHash bottomLeft, GeoHash topRight) {
        this.bottomLeft = GeoHash.fromLongValue(bottomLeft.longValue(), bottomLeft.significantBits());;
        this.topRight = GeoHash.fromLongValue(topRight.longValue(), topRight.significantBits());
        this.boundingBox = this.bottomLeft.getBoundingBox();
        this.boundingBox.expandToInclude(this.topRight.getBoundingBox());
        this.current = this.bottomLeft;
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public GeoHash getBottomLeft() {
        return bottomLeft;
    }

    public GeoHash getTopRight() {
        return topRight;
    }

    @Override
    public boolean hasNext() {
        return current.compareTo(topRight) <= 0;
    }

    @Override
    public GeoHash next() {
        GeoHash rv = current;
        if (!hasNext()) throw new NoSuchElementException(); 
        current = rv.next();
        while (hasNext() && !boundingBox.contains(current.getPoint())) {
            current = current.next();
        }
        return rv;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
