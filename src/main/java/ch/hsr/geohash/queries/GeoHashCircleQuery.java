/*
 * Copyright 2010, Silvio Heuberger @ IFS www.ifs.hsr.ch
 *
 * This code is release under the LGPL license.
 * You should have received a copy of the license
 * in the LICENSE file. If you have not, see
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 */
package ch.hsr.geohash.queries;

import ch.hsr.geohash.BoundingBox;
import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;
import ch.hsr.geohash.util.VincentyGeodesy;

import java.io.Serializable;
import java.util.List;

/**
 * represents a radius search around a specific point via geohashes.
 * Approximates the circle with a square!
 */
public class GeoHashCircleQuery implements GeoHashQuery, Serializable {
    private static final long serialVersionUID = 1263295371663796291L;
    private double radius;
    private GeoHashBoundingBoxQuery query;
    private WGS84Point center;

    /**
     * create a {@link GeoHashCircleQuery} with the given center point and a
     * radius in meters.
     */
    public GeoHashCircleQuery(WGS84Point center, double radius) {
        this.radius = radius;
        this.center = center;
        WGS84Point northEast = VincentyGeodesy.moveInDirection(VincentyGeodesy.moveInDirection(center, 0, radius), 90, radius);
        WGS84Point southWest = VincentyGeodesy.moveInDirection(VincentyGeodesy.moveInDirection(center, 180, radius), 270, radius);
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
        return "Cicle Query [center=" + center + ", radius=" + getRadiusString() + "]";
    }

    private String getRadiusString() {
        if (radius > 1000) {
            return radius / 1000 + "km";
        } else {
            return radius + "m";
        }
    }

    @Override
    public boolean contains(WGS84Point point) {
        return query.contains(point);
    }
}
