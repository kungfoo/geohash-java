/*
 * Copyright 2010, Silvio Heuberger @ IFS www.ifs.hsr.ch
 *
 * This code is release under the LGPL license.
 * You should have received a copy of the license
 * in the LICENSE file. If you have not, see
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 */
package ch.hsr.geohash;

import java.util.Random;

import ch.hsr.geohash.util.RandomGeohashes;
import ch.mollusca.benchmarking.Before;
import ch.mollusca.benchmarking.Benchmark;

public class GeoHashEncodingBenchmark {
	private static final int NUMBER_OF_HASHES = 1000000;
	private GeoHash[] hashes;

	@Before
	public void setupBenchmark() {
		hashes = new GeoHash[NUMBER_OF_HASHES];
	}

	@Benchmark(times = 10)
	public void benchmarkGeoHashEncoding() {
		for (int i = 0; i < NUMBER_OF_HASHES; i++) {
			hashes[i] = RandomGeohashes.createWithPrecision(60);
		}
	}
}
