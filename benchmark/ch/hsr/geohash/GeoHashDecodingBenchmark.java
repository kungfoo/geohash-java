package ch.hsr.geohash;

import ch.mollusca.benchmarking.Before;
import ch.mollusca.benchmarking.Benchmark;

public class GeoHashDecodingBenchmark {
	private static final char[] base32 = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'b', 'c', 'd', 'e', 'f',
		'g', 'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
	
	@Before
	public void setup(){
		
	}
	
	@Benchmark
	public void benchmarkRandomEncoding(){
		
	}
}
