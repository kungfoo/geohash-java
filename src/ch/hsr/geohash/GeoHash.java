package ch.hsr.geohash;

public final class GeoHash {
	private static final long FIRST_BIT_FLAGGED = 0x8000000000000000l;
	private static final char[] base32 = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm',
			'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

	protected long bits = 0;
	private WGS84Point point;
	private WGS84Point[] boundingBox;

	protected byte significantBits = 0;

	protected GeoHash() {
	}

	/**
	 * This method uses the given number of characters as the desired precision
	 * value. The hash can only be 64bits long, thus a maximum precision of 12
	 * characters can be achieved.
	 */
	public static GeoHash withCharacterPrecision(double latitude,
			double longitude, int numberOfCharacters) {
		int desiredPrecision = (numberOfCharacters * 5 <= 60) ? numberOfCharacters * 5
				: 60;
		return new GeoHash(latitude, longitude, desiredPrecision);
	}

	/**
	 * create a new {@link GeoHash} with the given number of bits accuracy. This
	 * at the same time defines this hash's bounding box.
	 */
	public static GeoHash withBitPrecision(double latitude, double longitude,
			int numberOfBits) {
		return new GeoHash(latitude, longitude, numberOfBits);
	}

	public static GeoHash fromGeohashString(String geohash) {
		return null;
	}

	public GeoHash getNeighbour(int direction, int length) {
		return null;
	}

	/**
	 * how many singificant bits are there in this hash?
	 */
	public int significantBits() {
		return (int) significantBits;
	}

	/**
	 * get the base32 string for this geohash.
	 */
	public String toBase32() {
		StringBuffer buf = new StringBuffer();

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
	 * returns the {@link WGS84Point} that was originally used to set up this
	 * {@link GeoHash}
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
		double centerLatitude = (boundingBox[0].latitude + boundingBox[1].latitude) / 2;
		double centerLongitude = (boundingBox[0].longitude + boundingBox[1].longitude) / 2;
		return new WGS84Point(centerLatitude, centerLongitude);
	}

	/**
	 * @return an array containing the two points: upper left, lower right of
	 *         the bounding box.
	 */
	public WGS84Point[] getBoundingBoxPoints() {
		return boundingBox;
	}

	@Override
	public String toString() {
		return String.format("%s -> (%d,%d)", longToBitString(bits), 0, 0);
	}

	/**
	 * return a long mask for this hashes significant bits.
	 */
	public final long mask() {
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
	
	protected GeoHash getSouthernNeighbour(){
		long[] latBits = getRightAlignedLatitudeBits();
		long[] lonBits = getRightAlignedLongitudeBits();
		latBits[0] += 1;
		latBits[0] = maskLastNBits(latBits[0], latBits[1]);
		return recombineLatLonBitsToHash(latBits, lonBits);
	}
	
	protected GeoHash getEasternNeighbour(){
		long[] latBits = getRightAlignedLatitudeBits();
		long[] lonBits = getRightAlignedLongitudeBits();
		lonBits[0] += 1;
		lonBits[0] = maskLastNBits(lonBits[0], lonBits[1]);
		return recombineLatLonBitsToHash(latBits, lonBits);
	}
	
	protected GeoHash getWesternNeighbour(){
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

	private long maskLastNBits(long value, long n) {
		long mask = 0xffffffffffffffffl;
		mask >>>= (64 - n);
		return value & mask;
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

	protected static String longToBitString(long value) {
		StringBuffer buf = new StringBuffer();
		for (int i = 64; i > 0; i--) {
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

	private GeoHash(double latitude, double longitude, int desiredPrecision) {
		point = new WGS84Point(latitude, longitude);
		highCapDesiredPrecision(desiredPrecision);

		boolean isEvenBit = true;
		double[] latitudeRange = { -90, 90 };
		double[] longitudeRange = { -180, 180 };
		double mid;

		while (significantBits < desiredPrecision) {
			if (isEvenBit) {
				mid = (longitudeRange[0] + longitudeRange[1]) / 2;
				if (longitude > mid) {
					addOnBitToEnd();
					longitudeRange[0] = mid;
				} else {
					addOffBitToEnd();
					longitudeRange[1] = mid;
				}
			} else {
				mid = (latitudeRange[0] + latitudeRange[1]) / 2;
				if (latitude > mid) {
					addOnBitToEnd();
					latitudeRange[0] = mid;
				} else {
					addOffBitToEnd();
					latitudeRange[1] = mid;
				}
			}
			isEvenBit = !isEvenBit;
		}

		boundingBox = new WGS84Point[] {
				new WGS84Point(latitudeRange[0], longitudeRange[0]),
				new WGS84Point(latitudeRange[1], longitudeRange[1]) };

		bits <<= (64 - desiredPrecision);
	}

	/**
	 * a hash cannot have more than 64 bits right now, so we cap the desired
	 * number of bits there.
	 */
	private void highCapDesiredPrecision(int desiredPrecision) {
		if (desiredPrecision > 64)
			desiredPrecision = 64;
	}
}
