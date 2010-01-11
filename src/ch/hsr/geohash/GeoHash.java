package ch.hsr.geohash;

import java.util.HashMap;
import java.util.Map;

public final class GeoHash {
	private static final int[] BITS = { 16, 8, 4, 2, 1 };
	private static final int BASE32_BITS = 5;
	private static final long FIRST_BIT_FLAGGED = 0x8000000000000000l;
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

	// bounding box corners.
	private WGS84Point upperLeft;
	private WGS84Point lowerRight;

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
		hash.upperLeft = new WGS84Point(latitudeRange[0], longitudeRange[0]);
		hash.lowerRight = new WGS84Point(latitudeRange[1], longitudeRange[1]);
		hash.bits <<= (64 - hash.significantBits);
		return hash;
	}

	private GeoHash(double latitude, double longitude, int desiredPrecision) {
		point = new WGS84Point(latitude, longitude);
		highCapDesiredPrecision(desiredPrecision);

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

		upperLeft = new WGS84Point(latitudeRange[0], longitudeRange[0]);
		lowerRight = new WGS84Point(latitudeRange[1], longitudeRange[1]);
		bits <<= (64 - desiredPrecision);
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
	 * get the base32 string for this {@link GeoHash}.
	 */
	public String toBase32() {
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
		double centerLatitude = (upperLeft.latitude + lowerRight.latitude) / 2;
		double centerLongitude = (upperLeft.longitude + lowerRight.longitude) / 2;
		return new WGS84Point(centerLatitude, centerLongitude);
	}

	/**
	 * @return an array containing the two points: upper left, lower right of
	 *         the bounding box.
	 */
	public WGS84Point[] getBoundingBoxPoints() {
		return new WGS84Point[] { upperLeft, lowerRight };
	}

	/**
	 * @return an array containing all four corners of the bounding box.<br>
	 *         upper left, upper right, lower left, lower right.
	 */
	public WGS84Point[] getFourBoundingBoxPoints() {
		WGS84Point upperRight = new WGS84Point(upperLeft.latitude, lowerRight.longitude);
		WGS84Point lowerLeft = new WGS84Point(lowerRight.latitude, upperLeft.longitude);
		return new WGS84Point[] { upperLeft, upperRight, lowerLeft, lowerRight };
	}

	public boolean enclosesCircleAroundPoint(WGS84Point point, double radius) {
		return false;
	}

	@Override
	public String toString() {
		return String.format("%s -> %s,%s", longToBitString(bits), upperLeft, lowerRight);
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

	protected GeoHash getNorthernNeighbour() {
		long[] latitudeBits = getRightAlignedLatitudeBits();
		long[] longitudeBits = getRightAlignedLongitudeBits();
		latitudeBits[0] -= 1;
		latitudeBits[0] = maskLastNBits(latitudeBits[0], latitudeBits[1]);
		return recombineLatLonBitsToHash(latitudeBits, longitudeBits);
	}

	protected GeoHash getSouthernNeighbour() {
		long[] latBits = getRightAlignedLatitudeBits();
		long[] lonBits = getRightAlignedLongitudeBits();
		latBits[0] += 1;
		latBits[0] = maskLastNBits(latBits[0], latBits[1]);
		return recombineLatLonBitsToHash(latBits, lonBits);
	}

	protected GeoHash getEasternNeighbour() {
		long[] latBits = getRightAlignedLatitudeBits();
		long[] lonBits = getRightAlignedLongitudeBits();
		lonBits[0] += 1;
		lonBits[0] = maskLastNBits(lonBits[0], lonBits[1]);
		return recombineLatLonBitsToHash(latBits, lonBits);
	}

	protected GeoHash getWesternNeighbour() {
		long[] latBits = getRightAlignedLatitudeBits();
		long[] lonBits = getRightAlignedLongitudeBits();
		lonBits[0] -= 1;
		lonBits[0] = maskLastNBits(lonBits[0], lonBits[1]);
		return recombineLatLonBitsToHash(latBits, lonBits);
	}

	protected GeoHash recombineLatLonBitsToHash(long[] latBits, long[] lonBits) {
		GeoHash hash = new GeoHash();
		boolean isEvenBit = false;
		latBits[0] <<= (64 - latBits[1]);
		lonBits[0] <<= (64 - lonBits[1]);
		for (int i = 0; i < latBits[1] + lonBits[1]; i++) {
			if (isEvenBit) {
				if ((latBits[0] & FIRST_BIT_FLAGGED) == FIRST_BIT_FLAGGED) {
					hash.addOnBitToEnd();
				} else {
					hash.addOffBitToEnd();
				}
				latBits[0] <<= 1;
			} else {
				if ((lonBits[0] & FIRST_BIT_FLAGGED) == FIRST_BIT_FLAGGED) {
					hash.addOnBitToEnd();
				} else {
					hash.addOffBitToEnd();
				}
				lonBits[0] <<= 1;
			}
			isEvenBit = !isEvenBit;
		}
		hash.bits <<= (64 - hash.significantBits);
		return hash;
	}

	protected long[] getRightAlignedLatitudeBits() {
		long value = 0;
		long copyOfBits = bits;
		copyOfBits <<= 1;
		int numberOfBits = getNumberOfLatLonBits()[0];
		for (int i = 0; i < numberOfBits; i++) {
			if ((copyOfBits & FIRST_BIT_FLAGGED) == FIRST_BIT_FLAGGED) {
				value |= 0x1;
			}
			value <<= 1;
			copyOfBits <<= 2;
		}
		value >>>= 1;
		return new long[] { value, numberOfBits };
	}

	protected long[] getRightAlignedLongitudeBits() {
		long value = 0;
		long copyOfBits = bits;
		int numberOfBits = getNumberOfLatLonBits()[1];
		for (int i = 0; i < numberOfBits; i++) {
			if ((copyOfBits & FIRST_BIT_FLAGGED) == FIRST_BIT_FLAGGED) {
				value |= 0x1;
			}
			value <<= 1;
			copyOfBits <<= 2;
		}
		value >>>= 1;
		return new long[] { value, numberOfBits };
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

	protected String longToBitString(long value) {
		StringBuilder buf = new StringBuilder();
		for (int i = significantBits; i > 0; i--) {
			long bit = value & FIRST_BIT_FLAGGED;
			if (bit == FIRST_BIT_FLAGGED) {
				buf.append('1');
			} else {
				buf.append('0');
			}
			value <<= 1;
		}
		return buf.toString();
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

	private long maskLastNBits(long value, long n) {
		long mask = 0xffffffffffffffffl;
		mask >>>= (64 - n);
		return value & mask;
	}

	private void highCapDesiredPrecision(int desiredPrecision) {
		if (desiredPrecision > 64)
			desiredPrecision = 64;
	}
}
