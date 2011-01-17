package ch.hsr.geohash.util;

import ch.hsr.geohash.GeoHash;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

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

}
