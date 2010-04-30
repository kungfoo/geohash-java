/*
 * Copyright 2010, Silvio Heuberger @ IFS www.ifs.hsr.ch
 *
 * This code is release under the LGPL license.
 * You should have received a copy of the license
 * in the LICENSE file. If you have not, see
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 */
package ch.hsr.geohash;

public class BoundingBox {
	private WGS84Point upperLeft;
	private WGS84Point lowerRight;

	public BoundingBox(WGS84Point upperLeft, WGS84Point lowerRight) {
		this.upperLeft = upperLeft;
		this.lowerRight = lowerRight;
	}

	public BoundingBox(double minLat, double minLon, double maxLat, double maxLon) {
		this(new WGS84Point(minLat, minLon), new WGS84Point(maxLat, maxLon));
	}

	public BoundingBox(BoundingBox other) {
		upperLeft = new WGS84Point(other.upperLeft);
		lowerRight = new WGS84Point(other.lowerRight);
	}

	public WGS84Point getUpperLeft() {
		return upperLeft;
	}

	public WGS84Point getLowerRight() {
		return lowerRight;
	}

	public double getLatitudeSize() {
		return lowerRight.getLatitude() - upperLeft.getLatitude();
	}

	public double getLongitudeSize() {
		return lowerRight.getLongitude() - upperLeft.getLongitude();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj instanceof BoundingBox) {
			BoundingBox o = (BoundingBox) obj;
			return upperLeft.equals(o.upperLeft) && lowerRight.equals(o.lowerRight);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return 31 * (713 + upperLeft.hashCode()) + lowerRight.hashCode();
	}

	public boolean contains(WGS84Point point) {
		return (point.latitude >= upperLeft.latitude) && (point.longitude >= upperLeft.longitude)
				&& (point.latitude <= lowerRight.latitude) && (point.longitude <= lowerRight.longitude);
	}

	public boolean intersects(BoundingBox other) {
		return (upperLeft.latitude < other.lowerRight.latitude) && (lowerRight.latitude > other.upperLeft.latitude)
				&& (upperLeft.longitude < other.lowerRight.longitude) && (lowerRight.longitude > other.upperLeft.longitude);
	}

	@Override
	public String toString() {
		return upperLeft + " -> " + lowerRight;
	}

	public WGS84Point[] getFourBoundingBoxPoints() {
		WGS84Point upperRight = new WGS84Point(upperLeft.latitude, lowerRight.longitude);
		WGS84Point lowerLeft = new WGS84Point(lowerRight.latitude, upperLeft.longitude);
		return new WGS84Point[] { upperLeft, upperRight, lowerLeft, lowerRight };
	}

	public WGS84Point getCenterPoint() {
		double centerLatitude = (upperLeft.latitude + lowerRight.latitude) / 2;
		double centerLongitude = (upperLeft.longitude + lowerRight.longitude) / 2;
		return new WGS84Point(centerLatitude, centerLongitude);
	}
}
