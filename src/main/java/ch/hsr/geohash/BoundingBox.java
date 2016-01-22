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

public class BoundingBox implements Serializable {
	private static final long serialVersionUID = -7145192134410261076L;
	private double minLat;
	private double maxLat;
	private double minLon;
	private double maxLon;

	/**
	 * create a bounding box defined by two coordinates
	 */
	public BoundingBox(WGS84Point p1, WGS84Point p2) {
		this(p1.getLatitude(), p2.getLatitude(), p1.getLongitude(), p2.getLongitude());
	}

	public BoundingBox(double y1, double y2, double x1, double x2) {
		minLon = Math.min(x1, x2);
		maxLon = Math.max(x1, x2);
		minLat = Math.min(y1, y2);
		maxLat = Math.max(y1, y2);
	}

	public BoundingBox(BoundingBox that) {
		this(that.minLat, that.maxLat, that.minLon, that.maxLon);
	}

	public WGS84Point getUpperLeft() {
		return new WGS84Point(maxLat, minLon);
	}

	public WGS84Point getLowerRight() {
		return new WGS84Point(minLat, maxLon);
	}

	public double getLatitudeSize() {
		return maxLat - minLat;
	}

	public double getLongitudeSize() {
		return maxLon - minLon;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof BoundingBox) {
			BoundingBox that = (BoundingBox) obj;
			return minLat == that.minLat && minLon == that.minLon && maxLat == that.maxLat && maxLon == that.maxLon;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 37 * result + hashCode(minLat);
		result = 37 * result + hashCode(maxLat);
		result = 37 * result + hashCode(minLon);
		result = 37 * result + hashCode(maxLon);
		return result;
	}

	private static int hashCode(double x) {
		long f = Double.doubleToLongBits(x);
		return (int) (f ^ (f >>> 32));
	}

	public boolean contains(WGS84Point point) {
		return (point.getLatitude() >= minLat) && (point.getLongitude() >= minLon) && (point.getLatitude() <= maxLat)
				&& (point.getLongitude() <= maxLon);
	}

	public boolean intersects(BoundingBox other) {
		return !(other.minLon > maxLon || other.maxLon < minLon || other.minLat > maxLat || other.maxLat < minLat);
	}

	@Override
	public String toString() {
		return getUpperLeft() + " -> " + getLowerRight();
	}

	public WGS84Point getCenterPoint() {
		double centerLatitude = (minLat + maxLat) / 2;
		double centerLongitude = (minLon + maxLon) / 2;
		return new WGS84Point(centerLatitude, centerLongitude);
	}

	public void expandToInclude(BoundingBox other) {
		if (other.minLon < minLon) {
			minLon = other.minLon;
		}
		if (other.maxLon > maxLon) {
			maxLon = other.maxLon;
		}
		if (other.minLat < minLat) {
			minLat = other.minLat;
		}
		if (other.maxLat > maxLat) {
			maxLat = other.maxLat;
		}
	}

	public double getMinLon() {
		return minLon;
	}

	public double getMinLat() {
		return minLat;
	}

	public double getMaxLat() {
		return maxLat;
	}

	public double getMaxLon() {
		return maxLon;
	}
}
