/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Rhett Sutphin
 */
public class MapBuilderTest extends TestCase {
    public void testUsesOrderPreservingMapByDefault() throws Exception {
        Map<String, String> actual = buildTestMap().toMap();
        assertEquals(3, actual.size());
        List<String> keys = new ArrayList<String>(actual.keySet());
        assertEquals("Wrong first key",  "bar", keys.get(0));
        assertEquals("Wrong second key", "foo", keys.get(1));
        assertEquals("Wrong third key",  "baz", keys.get(2));
    }
    
    public void testUsesProvidedMapIfProvided() throws Exception {
        Map<String, String> expected = new TreeMap<String, String>();

        Map<String, String> actual = buildTestMap(new MapBuilder<String, String>(expected)).toMap();

        assertSame(expected, actual);
        assertEquals(3, expected.size());
    }

    public void testToDictionaryPreservesContents() throws Exception {
        Dictionary<String, String> actual = buildTestMap().toDictionary();
        assertEquals("Missing bar", "z", actual.get("bar"));
        assertEquals("Missing foo", "c", actual.get("foo"));
        assertEquals("Missing baz", "l", actual.get("baz"));
    }

    public void testToDictionaryReturnsTheInternalDictionaryIfProvided() throws Exception {
        Hashtable<String, String> expected = new Hashtable<String, String>();
        MapBuilder<String, String> builder = new MapBuilder<String, String>(expected);
        assertSame("Dictionary not the provided one", expected, builder.toDictionary());
    }

    private MapBuilder<String, String> buildTestMap() {
        return buildTestMap(null);
    }

    private MapBuilder<String, String> buildTestMap(MapBuilder<String, String> builder) {
        return (builder == null ? new MapBuilder<String, String>() : builder).
            put("bar", "z").
            put("foo", "c").
            put("baz", "l");
    }
}
