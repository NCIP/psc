package edu.northwestern.bioinformatics.studycalendar.utils;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Rhett Sutphin
 */
public class ExpandingMapTest extends StudyCalendarTestCase {
    private SortedMap<String, String> internal = new TreeMap<String, String>();
    private ExpandingMap<String, String> map = new ExpandingMap<String, String>(new TestFiller(), internal);

    protected void setUp() throws Exception {
        super.setUp();
        internal.put("one", "1");
        internal.put("eleven", "11");
    }

    public void testNoFillForGetExisting() throws Exception {
        assertEquals("1", map.get("one"));
        assertEquals(2, map.size());
        assertEquals(2, internal.size());
    }

    public void testGetNonExistent() throws Exception {
        String nonExistentKey = "34";
        assertNull(internal.get(nonExistentKey));
        assertFalse(internal.containsKey(nonExistentKey));
        assertEquals(fillValue(nonExistentKey), map.get(nonExistentKey));
        assertEquals(3, internal.size());
        assertEquals(fillValue(nonExistentKey), internal.get(nonExistentKey));
    }

    public void testContainsKeyForNonExistentDoesNothing() throws Exception {
        assertFalse(internal.containsKey("foo"));
        assertFalse(map.containsKey("foo"));
        assertFalse(internal.containsKey("foo"));
    }

    public void testDefaultFillIsNull() throws Exception {
        Map<Integer, Integer> def = new ExpandingMap<Integer, Integer>();
        assertEquals(0, def.size());
        assertEquals(null, def.get(15));
        assertTrue(def.containsKey(15));
        assertEquals(1, def.size());
    }

    private static class TestFiller implements ExpandingMap.Filler<String> {
        public String createNew(Object key) {
            return fillValue(key);
        }
    }

    private static String fillValue(Object key) {
        return "fill " + key;
    }
}
