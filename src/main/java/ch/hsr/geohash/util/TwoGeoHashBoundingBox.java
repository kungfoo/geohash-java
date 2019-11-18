package ch.hsr.geohash.util;

import ch.hsr.geohash.BoundingBox;
import ch.hsr.geohash.GeoHash;

/**
 * Created by IntelliJ IDEA. User: kevin Date: Jan 17, 2011 Time: 12:03:47 PM
 */
public class TwoGeoHashBoundingBox {
	private BoundingBox boundingBox;
	private GeoHash southWestCorner;
	private GeoHash northEastCorner;

	public static TwoGeoHashBoundingBox withCharacterPrecision(BoundingBox bbox, int numberOfCharacters) {
		GeoHash southWestCorner = GeoHash.withCharacterPrecision(bbox.getSouthLatitude(), bbox.getWestLongitude(), numberOfCharacters);
		GeoHash northEastCorner = GeoHash.withCharacterPrecision(bbox.getNorthLatitude(), bbox.getEastLongitude(), numberOfCharacters);
		return new TwoGeoHashBoundingBox(southWestCorner, northEastCorner);
	}

	public static TwoGeoHashBoundingBox withBitPrecision(BoundingBox bbox, int numberOfBits) {
		GeoHash southWestCorner = GeoHash.withBitPrecision(bbox.getSouthLatitude(), bbox.getWestLongitude(), numberOfBits);
		GeoHash northEastCorner = GeoHash.withBitPrecision(bbox.getNorthLatitude(), bbox.getEastLongitude(), numberOfBits);
		return new TwoGeoHashBoundingBox(southWestCorner, northEastCorner);
	}

	public TwoGeoHashBoundingBox(GeoHash southWestCorner, GeoHash northEastCorner) {
		if (southWestCorner.significantBits() != northEastCorner.significantBits()) {
			throw new IllegalArgumentException("Does it make sense to iterate between hashes that have different precisions?");
		}
		this.southWestCorner = GeoHash.fromLongValue(southWestCorner.longValue(), southWestCorner.significantBits());
		this.northEastCorner = GeoHash.fromLongValue(northEastCorner.longValue(), northEastCorner.significantBits());
		boundingBox = new BoundingBox(southWestCorner.getBoundingBox().getSouthLatitude(), northEastCorner.getBoundingBox().getNorthLatitude(), southWestCorner.getBoundingBox().getWestLongitude(), northEastCorner.getBoundingBox().getEastLongitude());
	}

	public BoundingBox getBoundingBox() {
		return boundingBox;
	}

	public GeoHash getSouthWestCorner() {
		return southWestCorner;
	}

	public GeoHash getNorthEastCorner() {
		return northEastCorner;
	}

	public String toBase32() {
		return southWestCorner.toBase32() + northEastCorner.toBase32();
	}
}
