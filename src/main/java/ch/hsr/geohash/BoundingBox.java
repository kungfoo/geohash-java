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
import ch.hsr.geohash.util.DoubleUtil;

public class BoundingBox implements Serializable {
	private static final long serialVersionUID = -7145192134410261076L;
	private double southLatitude;
	private double northLatitude;
	private double westLongitude;
	private double eastLongitude;
	private boolean intersects180Meridian;

	/**
	 * create a bounding box defined by two coordinates
	 */
	public BoundingBox(WGS84Point southWestCorner, WGS84Point northEastCorner) {
		this(southWestCorner.getLatitude(), northEastCorner.getLatitude(), southWestCorner.getLongitude(), northEastCorner.getLongitude());
	}

	/**
	 * Create a bounding box with the specified latitudes and longitudes. This constructor takes the order of the points into account.
	 *
	 * @param northLatitude
	 * @param southLatitude
	 * @param westLongitude
	 * @param eastLongitude
	 *
	 * @throws IllegalArgumentException
	 *             When the defined BoundingBox would go over one of the poles. This kind of box is not supported.
	 */
	public BoundingBox(double southLatitude, double northLatitude, double westLongitude, double eastLongitude) {
		if (southLatitude > northLatitude)
			throw new IllegalArgumentException("The southLatitude must not be greater than the northLatitude");

		if (Math.abs(southLatitude) > 90 || Math.abs(northLatitude) > 90 || Math.abs(westLongitude) > 180 || Math.abs(eastLongitude) > 180) {
			throw new IllegalArgumentException("The supplied coordinates are out of range.");
		}

		this.northLatitude = northLatitude;
		this.westLongitude = westLongitude;

		this.southLatitude = southLatitude;
		this.eastLongitude = eastLongitude;

		intersects180Meridian = eastLongitude < westLongitude;
	}

	/**
	 * Clone constructor
	 *
	 * @param that
	 */
	public BoundingBox(BoundingBox that) {
		this(that.southLatitude, that.northLatitude, that.westLongitude, that.eastLongitude);
	}

	/**
	 * Returns the NorthWestCorner of this BoundingBox as a new Point.
	 *
	 * @return
	 */
	public WGS84Point getNorthWestCorner() {
		return new WGS84Point(northLatitude, westLongitude);
	}

	/**
	 * Returns the NorthEastCorner of this BoundingBox as a new Point.
	 *
	 * @return
	 */
	public WGS84Point getNorthEastCorner() {
		return new WGS84Point(northLatitude, eastLongitude);
	}

	/**
	 * Returns the SouthEastCorner of this BoundingBox as a new Point.
	 *
	 * @return
	 */
	public WGS84Point getSouthEastCorner() {
		return new WGS84Point(southLatitude, eastLongitude);
	}

	/**
	 * Returns the SouthWestCorner of this BoundingBox as a new Point.
	 *
	 * @return
	 */
	public WGS84Point getSouthWestCorner() {
		return new WGS84Point(southLatitude, westLongitude);
	}

	/**
	 * Returns the size of the bounding box in degrees of latitude. The value returned will always be positive.
	 *
	 * @return
	 */
	public double getLatitudeSize() {
		return northLatitude - southLatitude;
	}

	/**
	 * Returns the size of the bounding box in degrees of longitude. The value returned will always be positive.
	 *
	 * @return
	 */
	public double getLongitudeSize() {
		if (eastLongitude == 180.0 && westLongitude == -180.0)
			return 360.0;
		double size = (eastLongitude - westLongitude) % 360;

		// Remainder fix for earlier java versions
		if (size < 0)
			size += 360.0;
		return size;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof BoundingBox) {
			BoundingBox that = (BoundingBox) obj;
			return southLatitude == that.southLatitude && westLongitude == that.westLongitude && northLatitude == that.northLatitude && eastLongitude == that.eastLongitude;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 37 * result + hashCode(southLatitude);
		result = 37 * result + hashCode(northLatitude);
		result = 37 * result + hashCode(westLongitude);
		result = 37 * result + hashCode(eastLongitude);
		return result;
	}

	private static int hashCode(double x) {
		long f = Double.doubleToLongBits(x);
		return (int) (f ^ (f >>> 32));
	}

	public boolean contains(WGS84Point point) {
		return containsLatitude(point.getLatitude()) && containsLongitude(point.getLongitude());
	}

	public boolean intersects(BoundingBox other) {
		// Check latitude first cause it's the same for all cases
		if (other.southLatitude > northLatitude || other.northLatitude < southLatitude) {
			return false;
		} else {
			if (!intersects180Meridian && !other.intersects180Meridian) {
				return !(other.eastLongitude < westLongitude || other.westLongitude > eastLongitude);
			} else if (intersects180Meridian && !other.intersects180Meridian) {
				return !(eastLongitude < other.westLongitude && westLongitude > other.eastLongitude);
			} else if (!intersects180Meridian && other.intersects180Meridian) {
				return !(westLongitude > other.eastLongitude && eastLongitude < other.westLongitude);
			} else
				return true;
		}
	}

	@Override
	public String toString() {
		return getNorthWestCorner() + " -> " + getSouthEastCorner();
	}

	public WGS84Point getCenter() {
		double centerLatitude = (southLatitude + northLatitude) / 2;
		double centerLongitude = (westLongitude + eastLongitude) / 2;

		// This can happen if the bBox crosses the 180-Meridian
		if (centerLongitude > 180)
			centerLongitude -= 360;

		return new WGS84Point(centerLatitude, centerLongitude);
	}
	
	/**
	 * Expands this bounding box to include the provided point. The expansion is done in the direction with the minimal distance. If both distances are the same it'll expand
	 * in east direction. It will not cross poles, but it will cross the 180-Meridian, if thats the shortest distance.<br>
	 *
	 * @param point The point to include
	 */
	public void expandToInclude(WGS84Point point) {
		
		// Expand Latitude
		if(point.getLatitude() < southLatitude)
			southLatitude = point.getLatitude();
		else if(point.getLatitude() > northLatitude)
			northLatitude = point.getLatitude();
		
		// Already done in this case
		if(containsLongitude(point.getLongitude()))
			return;
		
		// If this is not the case compute the distance between the endpoints in east direction
		double distanceEastToPoint = DoubleUtil.remainderWithFix(point.getLongitude() - eastLongitude, 360);
		double distancePointToWest = DoubleUtil.remainderWithFix(westLongitude - point.getLongitude(), 360);

		// The minimal distance needs to be extended
		if(distanceEastToPoint <= distancePointToWest)
			eastLongitude = point.getLongitude();
		else
			westLongitude = point.getLongitude();
		
		intersects180Meridian = eastLongitude < westLongitude;
	}

	/**
	 * Expands this bounding box to include the provided bounding box. The expansion is done in the direction with the minimal distance. If both distances are the same it'll expand
	 * in east direction. It will not cross poles, but it will cross the 180-Meridian, if thats the shortest distance.<br>
	 * If a precise specification of the northEast and southWest points is needed, please create a new bounding box where you can specify the points separately.
	 *
	 * @param other
	 */
	public void expandToInclude(BoundingBox other) {

		// Expand Latitude
		if (other.southLatitude < southLatitude) {
			southLatitude = other.southLatitude;
		}
		if (other.northLatitude > northLatitude) {
			northLatitude = other.northLatitude;
		}

		// Expand Longitude
		// At first check whether the two boxes contain each other or not
		boolean thisContainsOther = containsLongitude(other.eastLongitude) && containsLongitude(other.westLongitude);
		boolean otherContainsThis = other.containsLongitude(eastLongitude) && other.containsLongitude(westLongitude);

		// The new box needs to span the whole globe
		if (thisContainsOther && otherContainsThis) {
			eastLongitude = 180.0;
			westLongitude = -180.0;
			intersects180Meridian = false;
			return;
		}
		// Already done in this case
		if (thisContainsOther)
			return;
		// Expand to match the bigger box
		if (otherContainsThis) {
			eastLongitude = other.eastLongitude;
			westLongitude = other.westLongitude;
			intersects180Meridian = eastLongitude < westLongitude;
			return;
		}

		// If this is not the case compute the distance between the endpoints in east direction
		double distanceEastToOtherEast = DoubleUtil.remainderWithFix(other.eastLongitude - eastLongitude, 360);
		double distanceOtherWestToWest = DoubleUtil.remainderWithFix(westLongitude - other.westLongitude, 360);

		// The minimal distance needs to be extended
		if (distanceEastToOtherEast <= distanceOtherWestToWest) {
			eastLongitude = other.eastLongitude;
		} else {
			westLongitude = other.westLongitude;
		}

		intersects180Meridian = eastLongitude < westLongitude;
	}

	private boolean containsLatitude(double latitude) {
		return latitude >= southLatitude && latitude <= northLatitude;
	}

	private boolean containsLongitude(double longitude) {
		if (intersects180Meridian) {
			return longitude <= eastLongitude || longitude >= westLongitude;
		} else {
			return longitude >= westLongitude && longitude <= eastLongitude;
		}
	}

	public double getEastLongitude() {
		return eastLongitude;
	}

	public double getWestLongitude() {
		return westLongitude;
	}

	public double getNorthLatitude() {
		return northLatitude;
	}

	public double getSouthLatitude() {
		return southLatitude;
	}

	public boolean intersects180Meridian() {
		return intersects180Meridian;
	}
}
