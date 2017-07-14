/*
 * Copyright 2010, Silvio Heuberger @ IFS www.ifs.hsr.ch
 *
 * This code is release under the Apache License 2.0.
 * You should have received a copy of the license
 * in the LICENSE file. If you have not, see
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package ch.hsr.geohash.queries;

import java.io.Serializable;
import java.util.List;

import ch.hsr.geohash.BoundingBox;
import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;
import ch.hsr.geohash.util.VincentyGeodesy;

/**
 * represents a radiusInMetres search around a specific point via geohashes.
 * Approximates the circle with a square!
 */
public class GeoHashCircleQuery implements GeoHashQuery, Serializable {
	private static final long serialVersionUID = 1263295371663796291L;
	private double radiusInMetres;
	private GeoHashBoundingBoxQuery query;
	private WGS84Point center;

	/**
	 * create a {@link GeoHashCircleQuery} with the given center point and a
	 * radiusInMetres in meters.
	 */
	public GeoHashCircleQuery(WGS84Point center, double radiusInMetres) {
		this.radiusInMetres = radiusInMetres;
		this.center = center;
		WGS84Point northEast = VincentyGeodesy.moveInDirection(VincentyGeodesy.moveInDirection(center, 0, radiusInMetres), 90,
				radiusInMetres);
		WGS84Point southWest = VincentyGeodesy.moveInDirection(VincentyGeodesy.moveInDirection(center, 180, radiusInMetres),
				270, radiusInMetres);
		BoundingBox bbox = new BoundingBox(northEast, southWest);
		query = new GeoHashBoundingBoxQuery(bbox);
	}

	@Override
	public boolean contains(GeoHash hash) {
		return query.contains(hash);
	}

	@Override
	public String getWktBox() {
		return query.getWktBox();
	}

	@Override
	public List<GeoHash> getSearchHashes() {
		return query.getSearchHashes();
	}

	@Override
	public String toString() {
		return "Cicle Query [center=" + center + ", radiusInMetres=" + getRadiusString() + "]";
	}

	private String getRadiusString() {
		if (radiusInMetres > 1000) {
			return radiusInMetres / 1000 + "km";
		} else {
			return radiusInMetres + "m";
		}
	}

	@Override
	public boolean contains(WGS84Point point) {
		return query.contains(point);
	}
}
