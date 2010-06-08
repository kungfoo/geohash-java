package ch.hsr.geohash;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import ch.hsr.geohash.queries.GeoHashCircleQuery;
import ch.hsr.geohash.queries.GeoHashQuery;

public class GeoHashCircleQueryTest {
	private WGS84Point center = new WGS84Point(10.00254, 76.30627);;

	@Test
	public void checkJerryDonSample1() {
		checkRadiusSearchHashes(1000, "t9y2bk" );
	}

	@Test
	public void testJerryDonSample2() {
		checkRadiusSearchHashes(1500, "t9y2bh", "t9y2bs", "t9yb2b0", "t9yb2b8");
	}
	
	@Test
	public void testJerryDonSample3() {
		checkRadiusSearchHashes(2500, "t9y2b");
	}

	private void checkRadiusSearchHashes(int radius, String ... string) {
		GeoHashQuery query = new GeoHashCircleQuery(center, radius);
		List<String> expectedHashes = Arrays.asList(string);
		List<GeoHash> searchHashes = query.getSearchHashes();
		assertEquals(expectedHashes.size(), searchHashes.size());
		for(GeoHash hash : searchHashes){
			if(! expectedHashes.contains(hash.toBase32())){
				fail("Hash " + hash + " not found in list of expected hashes");
			}
		}
	}
}
