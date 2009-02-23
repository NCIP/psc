package edu.northwestern.bioinformatics.studycalendar.tools;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Rhett Sutphin
 */
public class ExpandingMapTest extends TestCase {
    private SortedMap<String, String> internal = new TreeMap<String, String>();
    private ExpandingMap<String, String> map = new ExpandingMap<String, String>(new TestFiller(), internal);

    protected void setUp() throws Exception {
        super.setUp();
        internal.put("one", "1");
        internal.put("eleven", "11");
    }

    public void testNoFillForGetExisting() throws Exception {
        Assert.assertEquals("1", map.get("one"));
        Assert.assertEquals(2, map.size());
        Assert.assertEquals(2, internal.size());
    }

    public void testGetNonExistent() throws Exception {
        String nonExistentKey = "34";
        Assert.assertNull(internal.get(nonExistentKey));
        Assert.assertFalse(internal.containsKey(nonExistentKey));
        Assert.assertEquals(fillValue(nonExistentKey), map.get(nonExistentKey));
        Assert.assertEquals(3, internal.size());
        Assert.assertEquals(fillValue(nonExistentKey), internal.get(nonExistentKey));
    }

    public void testContainsKeyForNonExistentDoesNothing() throws Exception {
        Assert.assertFalse(internal.containsKey("foo"));
        Assert.assertFalse(map.containsKey("foo"));
        Assert.assertFalse(internal.containsKey("foo"));
    }

    public void testDefaultFillIsNull() throws Exception {
        Map<Integer, Integer> def = new ExpandingMap<Integer, Integer>();
        Assert.assertEquals(0, def.size());
        Assert.assertEquals(null, def.get(15));
        Assert.assertTrue(def.containsKey(15));
        Assert.assertEquals(1, def.size());
    }

    public void testConstructorFiller() throws Exception {
        Map<Integer, List> cons = new ExpandingMap<Integer, List>(new ExpandingMap.ConstructorFiller<List>(ArrayList.class));
        Assert.assertNotNull(cons.get(8));
        Assert.assertEquals(0, cons.get(17).size());
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
