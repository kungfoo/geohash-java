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

	@Before
	public void setUp() {
		a = new BoundingBox(new WGS84Point(30, 20), new WGS84Point(21, 31));
		b = new BoundingBox(a);
		c = new BoundingBox(new WGS84Point(45, -170), new WGS84Point(-45, 170));
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
	}

	@Test
	public void testContains() {
		BoundingBox bbox = new BoundingBox(45, 46, 121, 120);
		assertContains(bbox, new WGS84Point(45.5, 120.5));
		assertNotContains(bbox, new WGS84Point(90, 90));
	}

	@Test
	public void testSize() {
		BoundingBox bbox = new BoundingBox(45, 90, 0, 30);
		assertHeightIs(bbox, 45);
		assertWidthIs(bbox, 30);
		bbox = new BoundingBox(-45, 45, -22.5, 30);
		assertHeightIs(bbox, 90);
		assertWidthIs(bbox, 52.5);
		bbox = new BoundingBox(-44, -46.1, -127.2, -128);
		assertHeightIs(bbox, 2.1);
		assertWidthIs(bbox, 0.8);
	}

	private void assertWidthIs(BoundingBox bbox, double width) {
		assertEquals(width, bbox.getLongitudeSize(), DELTA);
	}

	private void assertHeightIs(BoundingBox bbox, double height) {
		assertEquals(height, bbox.getLatitudeSize(), DELTA);
	}

	@Test
	public void testIntersects() {
		BoundingBox bbox = new BoundingBox(10, -10, 41, 40);
		assertIntersects(bbox, new BoundingBox(5, -15, 40.5, 43));
		assertDoesNotIntersect(bbox, new BoundingBox(5, -15, 42, 43));
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
