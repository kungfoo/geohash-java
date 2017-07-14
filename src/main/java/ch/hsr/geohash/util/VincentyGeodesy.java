/*
 * Copyright 2010, Silvio Heuberger @ IFS www.ifs.hsr.ch
 *
 * This code is release under the Apache License 2.0.
 * You should have received a copy of the license
 * in the LICENSE file. If you have not, see
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package ch.hsr.geohash.util;

import ch.hsr.geohash.WGS84Point;

/**
 * Ecapsulates Vincety's geodesy algorithm .
 */
public class VincentyGeodesy {
	static final double equatorRadius = 6378137, poleRadius = 6356752.3142, f = 1 / 298.257223563;
	public static final double degToRad = 0.0174532925199433;
	static final double equatorRadiusSquared = equatorRadius * equatorRadius, poleRadiusSquared = poleRadius
			* poleRadius;
	public static final double EPSILON = 1e-12;

	/**
	 * returns the {@link WGS84Point} that is in the given direction at the
	 * following distance of the given point.<br>
	 * Uses Vincenty's formula and the WGS84 ellipsoid.
	 * 
	 * @param bearingInDegrees
	 *            : must be within 0 and 360
	 * @param point : where to start
	 * @param distanceInMeters: How far to move in the given direction
	 */
	public static WGS84Point moveInDirection(WGS84Point point, double bearingInDegrees, double distanceInMeters) {

		if (bearingInDegrees < 0 || bearingInDegrees > 360) {
			throw new IllegalArgumentException("direction must be in (0,360)");
		}

		double a = 6378137, b = 6356752.3142, f = 1 / 298.257223563; // WGS-84
		// ellipsiod
		double alpha1 = bearingInDegrees * degToRad;
		double sinAlpha1 = Math.sin(alpha1), cosAlpha1 = Math.cos(alpha1);

		double tanU1 = (1 - f) * Math.tan(point.getLatitude() * degToRad);
		double cosU1 = 1 / Math.sqrt((1 + tanU1 * tanU1)), sinU1 = tanU1 * cosU1;
		double sigma1 = Math.atan2(tanU1, cosAlpha1);
		double sinAlpha = cosU1 * sinAlpha1;
		double cosSqAlpha = 1 - sinAlpha * sinAlpha;
		double uSq = cosSqAlpha * (a * a - b * b) / (b * b);
		double A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
		double B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));

		double sinSigma = 0, cosSigma = 0, cos2SigmaM = 0;
		double sigma = distanceInMeters / (b * A), sigmaP = 2 * Math.PI;
		while (Math.abs(sigma - sigmaP) > 1e-12) {
			cos2SigmaM = Math.cos(2 * sigma1 + sigma);
			sinSigma = Math.sin(sigma);
			cosSigma = Math.cos(sigma);
			double deltaSigma = B
					* sinSigma
					* (cos2SigmaM + B
							/ 4
							* (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) - B / 6 * cos2SigmaM
									* (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM * cos2SigmaM)));
			sigmaP = sigma;
			sigma = distanceInMeters / (b * A) + deltaSigma;
		}

		double tmp = sinU1 * sinSigma - cosU1 * cosSigma * cosAlpha1;
		double lat2 = Math.atan2(sinU1 * cosSigma + cosU1 * sinSigma * cosAlpha1, (1 - f)
				* Math.sqrt(sinAlpha * sinAlpha + tmp * tmp));
		double lambda = Math.atan2(sinSigma * sinAlpha1, cosU1 * cosSigma - sinU1 * sinSigma * cosAlpha1);
		double C = f / 16 * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha));
		double L = lambda - (1 - C) * f * sinAlpha
				* (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));

		double newLat = lat2 / degToRad;
		double newLon = point.getLongitude() + L / degToRad;

		newLon = (newLon > 180.0 ? newLon - 360 : newLon);
		newLon = (newLon < -180.0 ? 360.0 + newLon : newLon);

		return new WGS84Point(newLat, newLon);
	}

	public static double distanceInMeters(WGS84Point foo, WGS84Point bar) {
		double a = 6378137, b = 6356752.3142, f = 1 / 298.257223563; // WGS-84
		// ellipsiod
		double L = (bar.getLongitude() - foo.getLongitude()) * degToRad;
		double U1 = Math.atan((1 - f) * Math.tan(foo.getLatitude() * degToRad));
		double U2 = Math.atan((1 - f) * Math.tan(bar.getLatitude() * degToRad));
		double sinU1 = Math.sin(U1), cosU1 = Math.cos(U1);
		double sinU2 = Math.sin(U2), cosU2 = Math.cos(U2);

		double cosSqAlpha, sinSigma, cos2SigmaM, cosSigma, sigma;

		double lambda = L, lambdaP, iterLimit = 20;
		do {
			double sinLambda = Math.sin(lambda), cosLambda = Math.cos(lambda);
			sinSigma = Math.sqrt((cosU2 * sinLambda) * (cosU2 * sinLambda)
					+ (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda) * (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda));
			if (sinSigma == 0) {
				return 0; // co-incident points
			}
			cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
			sigma = Math.atan2(sinSigma, cosSigma);
			double sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
			cosSqAlpha = 1 - sinAlpha * sinAlpha;
			cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha;
			if (Double.isNaN(cos2SigmaM)) {
				cos2SigmaM = 0; // equatorial line: cosSqAlpha=0
			}
			double C = f / 16 * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha));
			lambdaP = lambda;
			lambda = L + (1 - C) * f * sinAlpha
					* (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));
		} while (Math.abs(lambda - lambdaP) > EPSILON && --iterLimit > 0);

		if (iterLimit == 0) {
			return Double.NaN;
		}
		double uSquared = cosSqAlpha * (a * a - b * b) / (b * b);
		double A = 1 + uSquared / 16384 * (4096 + uSquared * (-768 + uSquared * (320 - 175 * uSquared)));
		double B = uSquared / 1024 * (256 + uSquared * (-128 + uSquared * (74 - 47 * uSquared)));
		double deltaSigma = B
				* sinSigma
				* (cos2SigmaM + B
						/ 4
						* (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) - B / 6 * cos2SigmaM
								* (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM * cos2SigmaM)));
		double s = b * A * (sigma - deltaSigma);

		return s;
	}

}
