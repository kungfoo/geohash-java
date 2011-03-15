package ch.hsr.geohash.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class LongUtilTest {
	public void testSameNumbersHave64BitsPrefix() {
		for (long a = 0; a < 120000000; a += 101) {
			long b = a;
			assertEquals(64, LongUtil.commonPrefixLength(a, b));
		}
	}

	@Test
	public void testKnownPrefixLenghts() {
		long a = 0x8f00000000000000l;
		long b = 0x8000000000000000l;
		long c = 0x8800000000000000l;
		assertPrefixLength(4, a, b);
		assertPrefixLength(4, b, c);
		assertPrefixLength(5, a, c);
		assertPrefixLength(0, 0x0, a);
		assertPrefixLength(16, 0x8888300000000000l, 0x8888c00000000000l);
	}

	private void assertPrefixLength(int length, long a, long b) {
		assertEquals(length, LongUtil.commonPrefixLength(a, b));
	}
}
