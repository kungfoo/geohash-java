package ch.hsr.geohash.util;

import ch.hsr.geohash.BoundingBox;
import ch.hsr.geohash.GeoHash;

/**
 * Created by IntelliJ IDEA. User: kevin Date: Jan 17, 2011 Time: 12:03:47 PM
 */
public class TwoGeoHashBoundingBox {
	private BoundingBox boundingBox;
	private GeoHash southEast;
	private GeoHash northWest;

	public static TwoGeoHashBoundingBox withCharacterPrecision(BoundingBox bbox, int numberOfCharacters) {
		GeoHash bottomLeft = GeoHash.withCharacterPrecision(bbox.getSouthLatitude(), bbox.getWestLongitude(), numberOfCharacters);
		GeoHash topRight = GeoHash.withCharacterPrecision(bbox.getNorthLatitude(), bbox.getEastLongitude(), numberOfCharacters);
		return new TwoGeoHashBoundingBox(bottomLeft, topRight);
	}

	public static TwoGeoHashBoundingBox withBitPrecision(BoundingBox bbox, int numberOfBits) {
		GeoHash bottomLeft = GeoHash.withBitPrecision(bbox.getSouthLatitude(), bbox.getWestLongitude(), numberOfBits);
		GeoHash topRight = GeoHash.withBitPrecision(bbox.getNorthLatitude(), bbox.getEastLongitude(), numberOfBits);
		return new TwoGeoHashBoundingBox(bottomLeft, topRight);
	}

	public static TwoGeoHashBoundingBox fromBase32(String base32) {
		String southWestBase32 = base32.substring(0, 7);
		String northEastBase32 = base32.substring(7);
		return new TwoGeoHashBoundingBox(GeoHash.fromGeohashString(southWestBase32), GeoHash.fromGeohashString(northEastBase32));
	}

	public TwoGeoHashBoundingBox(GeoHash southEast, GeoHash northWest) {
		if (southEast.significantBits() != northWest.significantBits()) {
			throw new IllegalArgumentException("Does it make sense to iterate between hashes that have different precisions?");
		}
		this.southEast = GeoHash.fromLongValue(southEast.longValue(), southEast.significantBits());
		this.northWest = GeoHash.fromLongValue(northWest.longValue(), northWest.significantBits());
		boundingBox = new BoundingBox(southEast.getBoundingBox().getSouthLatitude(), northWest.getBoundingBox().getNorthLatitude(), northWest.getBoundingBox().getWestLongitude(), southEast.getBoundingBox().getEastLongitude());
	}

	public BoundingBox getBoundingBox() {
		return boundingBox;
	}

	public GeoHash getSouthEast() {
		return southEast;
	}

	public GeoHash getNorthWest() {
		return northWest;
	}

	public String toBase32() {
		return southEast.toBase32() + northWest.toBase32();
	}
}
