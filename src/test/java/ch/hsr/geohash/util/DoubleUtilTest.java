package ch.hsr.geohash.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DoubleUtilTest {

	@Test
	public void testPositiveValue() {
		assertEquals(58.1541,DoubleUtil.remainderWithFix(58.1541, 360), 0.00001);
		assertEquals(93.1541,DoubleUtil.remainderWithFix(453.1541, 360), 0.00001);
	}
	
	@Test
	public void testNegativeValue() {
		assertEquals(301.8459,DoubleUtil.remainderWithFix(-58.1541, 360), 0.00001);
		assertEquals(266.8459,DoubleUtil.remainderWithFix(-453.1541, 360), 0.00001);
	}
}
