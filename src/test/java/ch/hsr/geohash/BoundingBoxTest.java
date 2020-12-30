/*
 * Copyright 2010, Silvio Heuberger @ IFS www.ifs.hsr.ch
 *
 * This code is release under the Apache License 2.0.
 * You should have received a copy of the license
 * in the LICENSE file. If you have not, see
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package ch.hsr.geohash;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class BoundingBoxTest {

	private static final double DELTA = 1e-12;
	private BoundingBox a;
	private BoundingBox b;
	private BoundingBox c;
	private BoundingBox d;
	private BoundingBox e;

	@Before
	public void setUp() {
		a = new BoundingBox(new WGS84Point(21, 20), new WGS84Point(30, 31));
		b = new BoundingBox(a);
		c = new BoundingBox(new WGS84Point(-45, -170), new WGS84Point(45, 170));
		d = new BoundingBox(new WGS84Point(-45, 170), new WGS84Point(-45, -170));
		e = new BoundingBox(d);
	}

	@Test
	public void testHashCode() {
		assertEquals(a.hashCode(), b.hashCode());
		assertFalse(a.hashCode() == c.hashCode());
	}

	@Test
	public void testEqualsObject() {
		assertEquals(a, b);
		assertEquals(b, a);
		assertFalse(a.equals(c));
		assertEquals(d, e);
		assertEquals(e, d);
		assertFalse(c.equals(d));
		assertFalse(c.equals(a));
	}

	@Test
	public void testContains() {
		BoundingBox bbox = new BoundingBox(45, 46, 120, 121);
		assertContains(bbox, new WGS84Point(45.5, 120.5));
		assertNotContains(bbox, new WGS84Point(90, 90));

		// Testing bounding box over 180-Meridian
		bbox = new BoundingBox(45, 46, 170, -170);
		assertContains(bbox, new WGS84Point(45.5, 175));
		assertContains(bbox, new WGS84Point(45.5, -175));
		assertNotContains(bbox, new WGS84Point(45.5, -165));
		assertNotContains(bbox, new WGS84Point(45.5, 165));
	}

	@Test
	public void testSize() {
		BoundingBox bbox = new BoundingBox(45, 90, 0, 30);
		assertHeightIs(bbox, 45);
		assertWidthIs(bbox, 30);
		bbox = new BoundingBox(-45, 45, -22.5, 30);
		assertHeightIs(bbox, 90);
		assertWidthIs(bbox, 52.5);
		bbox = new BoundingBox(-46.1, -44, -128, -127.2);
		assertHeightIs(bbox, 2.1);
		assertWidthIs(bbox, 0.8);

		// Testing bounding box over 180-Meridian
		bbox = new BoundingBox(45, 90, 170, -170);
		assertHeightIs(bbox, 45);
		assertWidthIs(bbox, 20);
	}

	private void assertWidthIs(BoundingBox bbox, double width) {
		assertEquals(width, bbox.getLongitudeSize(), DELTA);
	}

	private void assertHeightIs(BoundingBox bbox, double height) {
		assertEquals(height, bbox.getLatitudeSize(), DELTA);
	}

	@Test
	public void testIntersects() {
		BoundingBox bbox = new BoundingBox(-10, 10, 40, 41);
		assertIntersects(bbox, new BoundingBox(-15, 5, 40.5, 43));
		assertDoesNotIntersect(bbox, new BoundingBox(-15, 5, 42, 43));

		// Testing bounding box over 180-Meridian
		bbox = new BoundingBox(45, 90, 170, -170);
		assertIntersects(bbox, new BoundingBox(50, 55, 175, 176));
		assertIntersects(bbox, new BoundingBox(50, 55, 160, 176));
		assertIntersects(bbox, new BoundingBox(50, 55, -175, -176));
		assertIntersects(bbox, new BoundingBox(50, 55, -160, -176));
		assertIntersects(bbox, new BoundingBox(50, 55, 175, -175));
		assertIntersects(bbox, new BoundingBox(50, 55, -175, 175));

		assertDoesNotIntersect(bbox, new BoundingBox(-15, 5, 42, 43));
		assertDoesNotIntersect(bbox, new BoundingBox(-15, 5, 175, 176));
		assertDoesNotIntersect(bbox, new BoundingBox(-15, 5, 175, -175));
		assertDoesNotIntersect(bbox, new BoundingBox(50, 55, 160, 169));
		assertDoesNotIntersect(bbox, new BoundingBox(50, 55, -169, -160));
	}
	
	@Test
	public void testExpandToIncludePoint() {		
		// expand east
		BoundingBox bbox = new BoundingBox(-10, 10, 40, 41);
		WGS84Point point = new WGS84Point(0, 45);
		bbox.expandToInclude(point);
		assertContains(bbox, point);
		assertExpandToInclude(bbox, -10, 10, 40, 45);
		
		bbox = new BoundingBox(-10, 10, 40, 41);
		point = new WGS84Point(0, -140);
		bbox.expandToInclude(point);
		assertContains(bbox, point);
		assertExpandToInclude(bbox, -10, 10, 40, -140);
		assertEquals(true, bbox.intersects180Meridian());

		// expand west
		bbox = new BoundingBox(-10, 10, 40, 41);
		point = new WGS84Point(0, 35);
		bbox.expandToInclude(point);
		assertContains(bbox, point);
		assertExpandToInclude(bbox, -10, 10, 35, 41);
		
		bbox = new BoundingBox(-10, 10, 40, 41);
		point = new WGS84Point(0, -139);
		bbox.expandToInclude(point);
		assertContains(bbox, point);
		assertExpandToInclude(bbox, -10, 10, -139, 41);
		assertEquals(false, bbox.intersects180Meridian());

		// expand south
		bbox = new BoundingBox(-10, 10, 40, 41);
		point = new WGS84Point(-20, 40);
		bbox.expandToInclude(point);
		assertContains(bbox, point);
		assertExpandToInclude(bbox, -20, 10, 40, 41);
		
		// expand north
		bbox = new BoundingBox(-10, 10, 40, 41);
		point = new WGS84Point(20, 40);
		bbox.expandToInclude(point);
		assertContains(bbox, point);
		assertExpandToInclude(bbox, -10, 20, 40, 41);
	}
	
	private void assertExpandToInclude(BoundingBox bbox, double southLatitude, double northLatitude, double westLongitude, double eastLongitude) {
		assertEquals(southLatitude, bbox.getSouthLatitude(), DELTA);
		assertEquals(northLatitude, bbox.getNorthLatitude(), DELTA);
		assertEquals(westLongitude, bbox.getWestLongitude(), DELTA);
		assertEquals(eastLongitude, bbox.getEastLongitude(), DELTA);
	}

	private void assertDoesNotIntersect(BoundingBox bbox, BoundingBox boundingBox) {
		assertFalse(bbox + " should NOT intersect " + boundingBox, bbox.intersects(boundingBox));
		assertFalse(boundingBox + " should NOT intersect " + bbox, boundingBox.intersects(bbox));
	}

	private void assertIntersects(BoundingBox bbox, BoundingBox boundingBox) {
		assertTrue(bbox + " should intersect " + boundingBox, bbox.intersects(boundingBox));
		assertTrue(boundingBox + " should intersect " + bbox, boundingBox.intersects(bbox));
	}

	private void assertContains(BoundingBox box, WGS84Point p) {
		assertTrue(p + " should be in " + box, box.contains(p));
	}

	private void assertNotContains(BoundingBox box, WGS84Point p) {
		assertFalse(p + " should NOT be in " + box, box.contains(p));
	}
}
