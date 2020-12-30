package ch.hsr.geohash.util;

public class DoubleUtil {

	/**
	 * Utility function to consistently compute the remainder over all java versions
	 * @param value
	 * @param remainder
	 * @return
	 */
	public static double remainderWithFix(double value, int remainder) {
		double res = value % remainder;
		
		// Fix for lower java versions, since the remainder-operator (%) changed in one version, idk which one
		return res < 0 ? res += remainder : res;
	}
}
