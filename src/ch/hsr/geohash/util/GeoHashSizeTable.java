/*
 * Copyright 2010, Silvio Heuberger @ IFS www.ifs.hsr.ch
 *
 * This code is release under the LGPL license.
 * You should have received a copy of the license
 * in the LICENSE file. If you have not, see
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 */
package ch.hsr.geohash.util;

public class GeoHashSizeTable {
	private static final int NUM_BITS = 64;
	private static final double[] dLat = new double[NUM_BITS];
	private static final double[] dLon = new double[NUM_BITS];

	static {
		for (int i = 0; i < NUM_BITS; i++) {
			dLat[i] = dLat(i);
			dLon[i] = dLon(i);
		}
	}

	protected static final double dLat(int bits) {
		return 180d / Math.pow(2, bits / 2);
	}

	protected static final double dLon(int bits) {
		return 360d / Math.pow(2, (bits + 1) / 2);
	}
}
