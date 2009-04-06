package edu.northwestern.bioinformatics.studycalendar.tools;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Rhett Sutphin
 */
public class MapBuilderTest extends TestCase {
    public void testUsesOrderPreservingMapByDefault() throws Exception {
        Map<String, String> actual = new MapBuilder<String, String>().
            put("bar", "z").
            put("foo", "1").
            put("baz", "j").
            toMap();
        assertEquals(3, actual.size());
        List<String> keys = new ArrayList<String>(actual.keySet());
        assertEquals("Wrong first key",  "bar", keys.get(0));
        assertEquals("Wrong second key", "foo", keys.get(1));
        assertEquals("Wrong third key",  "baz", keys.get(2));
    }
    
    public void testUsesProvidedMapIfProvided() throws Exception {
        Map<String, String> expected = new TreeMap<String, String>();

        Map<String, String> actual = new MapBuilder<String, String>(expected).
            put("bar", "z").
            put("foo", "1").
            put("baz", "j").
            toMap();

        assertSame(expected, actual);
        assertEquals(3, expected.size());
    }
}
