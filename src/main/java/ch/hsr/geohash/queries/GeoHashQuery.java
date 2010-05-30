package ch.hsr.geohash.queries;

import java.util.List;

import ch.hsr.geohash.GeoHash;

public interface GeoHashQuery {

	/**
	 * check wether a geohash is within the hashes that make up this query.
	 */
	public abstract boolean contains(GeoHash hash);

	/**
	 * should return the hashes that re required to perform this search.
	 */
	public abstract List<GeoHash> getSearchHashes();

}