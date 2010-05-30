package ch.hsr.geohash.util;

public class LongUtil {
	public static final long FIRST_BIT = 0x8000000000000000l;

	public static final int commonPrefixLength(long a, long b) {
		int result = 0;
		while (result < 64 && (a & FIRST_BIT) == (b & FIRST_BIT)) {
			result++;
			a <<= 1;
			b <<= 1;
		}
		return result;
	}
}
