package ch.hsr.geohash;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class BoundingBoxTest {

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
	
	private void assertContains(BoundingBox box, WGS84Point p){
		assertTrue(p + " should be in " + box, box.contains(p));
	}
}
