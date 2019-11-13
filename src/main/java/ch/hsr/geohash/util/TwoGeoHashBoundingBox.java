package ch.hsr.geohash.util;

import ch.hsr.geohash.BoundingBox;
import ch.hsr.geohash.GeoHash;

/**
 * Created by IntelliJ IDEA. User: kevin Date: Jan 17, 2011 Time: 12:03:47 PM
 */
public class TwoGeoHashBoundingBox {
    private BoundingBox boundingBox;
    private GeoHash southWest;
    private GeoHash northEast;

    public static TwoGeoHashBoundingBox withCharacterPrecision(BoundingBox bbox, int numberOfCharacters) {
        GeoHash southWest = GeoHash.withCharacterPrecision(bbox.getSouthLatitude(), bbox.getWestLongitude(), numberOfCharacters);
        GeoHash northEast = GeoHash.withCharacterPrecision(bbox.getNorthLatitude(), bbox.getEastLongitude(), numberOfCharacters);
        return new TwoGeoHashBoundingBox(southWest, northEast);
    }

    public static TwoGeoHashBoundingBox withBitPrecision(BoundingBox bbox, int numberOfBits) {
        GeoHash southWest = GeoHash.withBitPrecision(bbox.getSouthLatitude(), bbox.getWestLongitude(), numberOfBits);
        GeoHash northEast = GeoHash.withBitPrecision(bbox.getNorthLatitude(), bbox.getEastLongitude(), numberOfBits);
        return new TwoGeoHashBoundingBox(southWest, northEast);
    }

    public TwoGeoHashBoundingBox(GeoHash southWest, GeoHash northEast) {
        if (southWest.significantBits() != northEast.significantBits()) {
            throw new IllegalArgumentException("Does it make sense to iterate between hashes that have different precisions?");
        }
        this.southWest = GeoHash.fromLongValue(southWest.longValue(), southWest.significantBits());
        this.northEast = GeoHash.fromLongValue(northEast.longValue(), northEast.significantBits());
        boundingBox = new BoundingBox(southWest.getBoundingBox().getSouthLatitude(), northEast.getBoundingBox().getNorthLatitude(), southWest.getBoundingBox().getWestLongitude(), northEast.getBoundingBox().getEastLongitude());
    }

    public BoundingBox getBoundingBox() {
        return boundingBox;
    }

    public GeoHash getSouthWest() {
        return southWest;
    }

    public GeoHash getNorthEast() {
        return northEast;
    }

    public String toBase32() {
        return southWest.toBase32() + northEast.toBase32();
    }
}
