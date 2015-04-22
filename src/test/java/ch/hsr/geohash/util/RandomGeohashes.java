package ch.hsr.geohash.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import ch.hsr.geohash.GeoHash;

public class RandomGeohashes {

	// TODO: could possibly be less brute-force here and be more scientific
	// about possible failure points
	public static Iterable<GeoHash> fullRange() {
		return new Iterable<GeoHash>() {
			@Override
			public Iterator<GeoHash> iterator() {
				Random rand = new Random();
				List<GeoHash> hashes = new ArrayList<GeoHash>();
				for (double lat = -90; lat <= 90; lat += rand.nextDouble() + 1.45) {
					for (double lon = -180; lon <= 180; lon += rand.nextDouble() + 1.54) {
						for (int precisionChars = 6; precisionChars <= 12; precisionChars++) {
							GeoHash gh = GeoHash.withCharacterPrecision(lat, lon, precisionChars);
							hashes.add(gh);
						}
					}
				}
				return hashes.iterator();
			}
		};
	}

	/**
	 * Fixes seed to make things reproducible.
	 */
	private static final Random rand = new Random(9817298371L);

	/**
	 * @return a completely random {@link GeoHash} with a random number of bits.
	 *         precision will be between [5,64] bits.
	 */
	public static GeoHash create() {
		return GeoHash.withBitPrecision(randomLatitude(), randomLongitude(), randomPrecision());
	}

	/**
	 * @return a completely random geohash with a precision that is a multiple
	 *         of 5 and in [5,60] bits.
	 */
	public static GeoHash createWith5BitsPrecision() {
		return GeoHash.withCharacterPrecision(randomLatitude(), randomLongitude(), randomCharacterPrecision());
	}

	/**
	 * @param precision
	 *            what precision to use.
	 * @return a completely random geohash with the given number of bits
	 *         precision.
	 */
	public static GeoHash createWithPrecision(int precision) {
		return GeoHash.withBitPrecision(randomLatitude(), randomLongitude(), precision);
	}

	private static double randomLatitude() {
		return (rand.nextDouble() - 0.5) * 180;
	}

	private static double randomLongitude() {
		return (rand.nextDouble() - 0.5) * 360;
	}

	private static int randomPrecision() {
		return rand.nextInt(60) + 5;
	}

	private static int randomCharacterPrecision() {
		return rand.nextInt(12) + 1;
	}

}
