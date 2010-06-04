/*
 * Copyright 2010, Silvio Heuberger @ IFS www.ifs.hsr.ch
 *
 * This code is release under the LGPL license.
 * You should have received a copy of the license
 * in the LICENSE file. If you have not, see
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 */
package ch.hsr.geohash;

import java.util.HashMap;
import java.util.Map;

public final class GeoHash {
	private static final int[] BITS = { 16, 8, 4, 2, 1 };
	private static final int BASE32_BITS = 5;
	public static final long FIRST_BIT_FLAGGED = 0x8000000000000000l;
	private static final char[] base32 = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k',
			'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

	private final static Map<Character, Integer> decodeMap = new HashMap<Character, Integer>();
	static {
		int sz = base32.length;
		for (int i = 0; i < sz; i++) {
			decodeMap.put(base32[i], i);
		}
	}

	protected long bits = 0;
	private WGS84Point point;

	private BoundingBox boundingBox;

	protected byte significantBits = 0;

	protected GeoHash() {
	}

	/**
	 * This method uses the given number of characters as the desired precision
	 * value. The hash can only be 64bits long, thus a maximum precision of 12
	 * characters can be achieved.
	 */
	public static GeoHash withCharacterPrecision(double latitude, double longitude, int numberOfCharacters) {
		int desiredPrecision = (numberOfCharacters * 5 <= 60) ? numberOfCharacters * 5 : 60;
		return new GeoHash(latitude, longitude, desiredPrecision);
	}

	/**
	 * create a new {@link GeoHash} with the given number of bits accuracy. This
	 * at the same time defines this hash's bounding box.
	 */
	public static GeoHash withBitPrecision(double latitude, double longitude, int numberOfBits) {
		if (Math.abs(latitude) > 90.0 || Math.abs(longitude) > 180.0)
			throw new IllegalArgumentException("Can't have lat/lon values out of (-90,90)/(-180/180)");
		return new GeoHash(latitude, longitude, numberOfBits);
	}

	/**
	 * build a new {@link GeoHash} from a base32-encoded {@link String}.<br>
	 * This will also set up the hashes bounding box and other values, so it can
	 * also be used with functions like within().
	 */
	public static GeoHash fromGeohashString(String geohash) {
		double[] latitudeRange = { -90.0, 90.0 };
		double[] longitudeRange = { -180.0, 180.0 };

		boolean isEvenBit = true;
		GeoHash hash = new GeoHash();

		for (int i = 0; i < geohash.length(); i++) {
			int cd = decodeMap.get(geohash.charAt(i));
			for (int j = 0; j < BASE32_BITS; j++) {
				int mask = BITS[j];
				if (isEvenBit) {
					divideRangeDecode(hash, longitudeRange, (cd & mask) != 0);
				} else {
					divideRangeDecode(hash, latitudeRange, (cd & mask) != 0);
				}
				isEvenBit = !isEvenBit;
			}
		}

		double latitude = (latitudeRange[0] + latitudeRange[1]) / 2;
		double longitude = (longitudeRange[0] + longitudeRange[1]) / 2;

		hash.point = new WGS84Point(latitude, longitude);
		setBoundingBox(hash, latitudeRange, longitudeRange);
		hash.bits <<= (64 - hash.significantBits);
		return hash;
	}

	private GeoHash(double latitude, double longitude, int desiredPrecision) {
		point = new WGS84Point(latitude, longitude);
		desiredPrecision = Math.min(desiredPrecision, 64);

		boolean isEvenBit = true;
		double[] latitudeRange = { -90, 90 };
		double[] longitudeRange = { -180, 180 };

		while (significantBits < desiredPrecision) {
			if (isEvenBit) {
				divideRangeEncode(longitude, longitudeRange);
			} else {
				divideRangeEncode(latitude, latitudeRange);
			}
			isEvenBit = !isEvenBit;
		}

		setBoundingBox(this, latitudeRange, longitudeRange);
		bits <<= (64 - desiredPrecision);
	}

	private static void setBoundingBox(GeoHash hash, double[] latitudeRange, double[] longitudeRange) {
		hash.boundingBox = new BoundingBox(new WGS84Point(latitudeRange[0], longitudeRange[0]), new WGS84Point(latitudeRange[1],
				longitudeRange[1]));
	}

	private void divideRangeEncode(double value, double[] range) {
		double mid = (range[0] + range[1]) / 2;
		if (value >= mid) {
			addOnBitToEnd();
			range[0] = mid;
		} else {
			addOffBitToEnd();
			range[1] = mid;
		}
	}

	private static void divideRangeDecode(GeoHash hash, double[] range, boolean b) {
		double mid = (range[0] + range[1]) / 2;
		if (b) {
			hash.addOnBitToEnd();
			range[0] = mid;
		} else {
			hash.addOffBitToEnd();
			range[1] = mid;
		}
	}

	/**
	 * returns the 8 adjacent hashes for this one. They are in the following
	 * order:<br>
	 * N, NE, E, SE, S, SW, W, NW
	 */
	public GeoHash[] getAdjacent() {
		GeoHash northern = getNorthernNeighbour();
		GeoHash eastern = getEasternNeighbour();
		GeoHash southern = getSouthernNeighbour();
		GeoHash western = getWesternNeighbour();
		return new GeoHash[] { northern, northern.getEasternNeighbour(), eastern, southern.getEasternNeighbour(), southern,
				southern.getWesternNeighbour(), western, northern.getWesternNeighbour() };
	}

	/**
	 * how many significant bits are there in this {@link GeoHash}?
	 */
	public int significantBits() {
		return (int) significantBits;
	}

	/**
	 * get the base32 string for this {@link GeoHash}.<br>
	 * this method only makes sense, if this hash has a multiple of 5
	 * significant bits.
	 */
	public String toBase32() {
		if(significantBits % 5 != 0){
			return "";
		}
		StringBuilder buf = new StringBuilder();

		long firstFiveBitsMask = 0xf800000000000000l;
		long bitsCopy = bits;
		int partialChunks = (int) Math.ceil(((double) significantBits / 5));

		for (int i = 0; i < partialChunks; i++) {
			int pointer = (int) ((bitsCopy & firstFiveBitsMask) >>> 59);
			buf.append(base32[pointer]);
			bitsCopy <<= 5;
		}
		return buf.toString();
	}

	/**
	 * returns true iff this is within the given geohash bounding box.
	 */
	public boolean within(GeoHash boundingBox) {
		return (bits & boundingBox.mask()) == boundingBox.bits;
	}

	/**
	 * find out if the given point lies within this hashes bounding box.<br>
	 * <i>Note: this operation checks the bounding boxes coordinates, i.e. does
	 * not use the {@link GeoHash}s special abilities.s</i>
	 */
	public boolean contains(WGS84Point point) {
		return boundingBox.contains(point);
	}

	/**
	 * returns the {@link WGS84Point} that was originally used to set up this.<br>
	 * If it was built from a base32-{@link String}, this is the center point of
	 * the bounding box.
	 */
	public WGS84Point getPoint() {
		return point;
	}

	/**
	 * return the center of this {@link GeoHash}s bounding box. this is rarely
	 * the same point that was used to build the hash.
	 */
	// TODO: make sure this method works as intented for corner cases!
	public WGS84Point getBoundingBoxCenterPoint() {
		return boundingBox.getCenterPoint();
	}

	public BoundingBox getBoundingBox() {
		return boundingBox;
	}

	public boolean enclosesCircleAroundPoint(WGS84Point point, double radius) {
		return false;
	}

	protected GeoHash recombineLatLonBitsToHash(long[] latBits, long[] lonBits) {
		GeoHash hash = new GeoHash();
		boolean isEvenBit = false;
		latBits[0] <<= (64 - latBits[1]);
		lonBits[0] <<= (64 - lonBits[1]);
		double[] latitudeRange = { -90.0, 90.0 };
		double[] longitudeRange = { -180.0, 180.0 };

		for (int i = 0; i < latBits[1] + lonBits[1]; i++) {
			if (isEvenBit) {
				divideRangeDecode(hash, latitudeRange, (latBits[0] & FIRST_BIT_FLAGGED) == FIRST_BIT_FLAGGED);
				latBits[0] <<= 1;
			} else {
				divideRangeDecode(hash, longitudeRange, (lonBits[0] & FIRST_BIT_FLAGGED) == FIRST_BIT_FLAGGED);
				lonBits[0] <<= 1;
			}
			isEvenBit = !isEvenBit;
		}
		hash.bits <<= (64 - hash.significantBits);
		setBoundingBox(hash, latitudeRange, longitudeRange);
		return hash;
	}

	public GeoHash getNorthernNeighbour() {
		long[] latitudeBits = getRightAlignedLatitudeBits();
		long[] longitudeBits = getRightAlignedLongitudeBits();
		latitudeBits[0] += 1;
		latitudeBits[0] = maskLastNBits(latitudeBits[0], latitudeBits[1]);
		return recombineLatLonBitsToHash(latitudeBits, longitudeBits);
	}

	public GeoHash getSouthernNeighbour() {
		long[] latitudeBits = getRightAlignedLatitudeBits();
		long[] longitudeBits = getRightAlignedLongitudeBits();
		latitudeBits[0] -= 1;
		latitudeBits[0] = maskLastNBits(latitudeBits[0], latitudeBits[1]);
		return recombineLatLonBitsToHash(latitudeBits, longitudeBits);
	}

	public GeoHash getEasternNeighbour() {
		long[] latitudeBits = getRightAlignedLatitudeBits();
		long[] longitudeBits = getRightAlignedLongitudeBits();
		longitudeBits[0] += 1;
		longitudeBits[0] = maskLastNBits(longitudeBits[0], longitudeBits[1]);
		return recombineLatLonBitsToHash(latitudeBits, longitudeBits);
	}

	public GeoHash getWesternNeighbour() {
		long[] latitudeBits = getRightAlignedLatitudeBits();
		long[] longitudeBits = getRightAlignedLongitudeBits();
		longitudeBits[0] -= 1;
		longitudeBits[0] = maskLastNBits(longitudeBits[0], longitudeBits[1]);
		return recombineLatLonBitsToHash(latitudeBits, longitudeBits);
	}

	protected long[] getRightAlignedLatitudeBits() {
		long copyOfBits = bits << 1;
		long value = extractEverySecondBit(copyOfBits, getNumberOfLatLonBits()[0]);
		return new long[] { value, getNumberOfLatLonBits()[0] };
	}

	protected long[] getRightAlignedLongitudeBits() {
		long copyOfBits = bits;
		long value = extractEverySecondBit(copyOfBits, getNumberOfLatLonBits()[1]);
		return new long[] { value, getNumberOfLatLonBits()[1] };
	}

	private long extractEverySecondBit(long copyOfBits, int numberOfBits) {
		long value = 0;
		for (int i = 0; i < numberOfBits; i++) {
			if ((copyOfBits & FIRST_BIT_FLAGGED) == FIRST_BIT_FLAGGED) {
				value |= 0x1;
			}
			value <<= 1;
			copyOfBits <<= 2;
		}
		value >>>= 1;
		return value;
	}

	protected int[] getNumberOfLatLonBits() {
		if (significantBits % 2 == 0) {
			return new int[] { significantBits / 2, significantBits / 2 };
		} else {
			return new int[] { significantBits / 2, significantBits / 2 + 1 };
		}
	}

	protected final void addOnBitToEnd() {
		significantBits++;
		bits <<= 1;
		bits = bits | 0x1;
	}

	protected final void addOffBitToEnd() {
		significantBits++;
		bits <<= 1;
	}

	@Override
	public String toString() {
		if (significantBits % 5 == 0) {
			return String.format("%s -> %s -> %s", Long.toBinaryString(bits), boundingBox, toBase32());
		} else {
			return String.format("%s -> %s, bits: %d", Long.toBinaryString(bits), boundingBox, significantBits);
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof GeoHash) {
			GeoHash other = (GeoHash) obj;
			if (other.significantBits == significantBits && other.bits == bits) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		int f = 17;
		f = 31 * f + (int) (bits ^ (bits >>> 32));
		f = 31 * f + significantBits;
		return f;
	}

	/**
	 * return a long mask for this hashes significant bits.
	 */
	private final long mask() {
		if (significantBits == 0) {
			return 0;
		} else {
			long value = FIRST_BIT_FLAGGED;
			value >>= (significantBits - 1);
			return value;
		}
	}

	private long maskLastNBits(long value, long n) {
		long mask = 0xffffffffffffffffl;
		mask >>>= (64 - n);
		return value & mask;
	}
}
