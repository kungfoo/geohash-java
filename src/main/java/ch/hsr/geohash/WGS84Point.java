/*
 * Copyright 2010, Silvio Heuberger @ IFS www.ifs.hsr.ch
 *
 * This code is release under the Apache License 2.0.
 * You should have received a copy of the license
 * in the LICENSE file. If you have not, see
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package ch.hsr.geohash;

import java.io.Serializable;

/**
 * {@link WGS84Point} encapsulates coordinates on the earths surface.<br>
 * Coordinate projections might end up using this class...
 */
public class WGS84Point implements Serializable {
	private static final long serialVersionUID = 7457963026513014856L;
	private final double longitude;
	private final double latitude;

	public WGS84Point(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
		if (Math.abs(latitude) > 90 || Math.abs(longitude) > 180) {
			throw new IllegalArgumentException("The supplied coordinates " + this + " are out of range.");
		}
	}

	public WGS84Point(WGS84Point other) {
		this(other.latitude, other.longitude);
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	@Override
	public String toString() {
		return String.format("(" + latitude + "," + longitude + ")");
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof WGS84Point) {
			WGS84Point other = (WGS84Point) obj;
			return latitude == other.latitude && longitude == other.longitude;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int result = 42;
		long latBits = Double.doubleToLongBits(latitude);
		long lonBits = Double.doubleToLongBits(longitude);
		result = 31 * result + (int) (latBits ^ (latBits >>> 32));
		result = 31 * result + (int) (lonBits ^ (lonBits >>> 32));
		return result;
	}
}
