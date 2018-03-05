package ch.hsr.geohash;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

public class GeoHashMatchers {

    public static Matcher<GeoHash> hashThatContains(final WGS84Point point) {
        return new TypeSafeMatcher<GeoHash>() {
            @Override
            protected boolean matchesSafely(GeoHash hash) {
                return hash.contains(point);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("A hash that contains: ").appendValue(point);
            }

            @Override
            protected void describeMismatchSafely(GeoHash item, Description mismatchDescription) {
                mismatchDescription.appendText("Hash ").appendValue(item).appendText(" did not contain ").appendValue(point);
            }
        };
    }

    public static Matcher<GeoHash> hashThatContains(final double latitude, final double longitude) {
        return hashThatContains(new WGS84Point(latitude, longitude));
    }
}
