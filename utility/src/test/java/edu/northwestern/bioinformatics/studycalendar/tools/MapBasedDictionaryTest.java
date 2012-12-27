/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools;

import junit.framework.TestCase;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class MapBasedDictionaryTest extends TestCase {
    private Map<String, Number> map;
    private Dictionary<String, Number> dict;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        map = new LinkedHashMap<String, Number>();
        map.put("answer", 42);
        map.put(    "pi",  3.1416);
        map.put(     "e",  2.718);

        dict = new MapBasedDictionary<String, Number>(map);
    }

    public void testSizeMatches() throws Exception {
        assertEquals(3, dict.size());
    }

    public void testMapIsReferencedDirectly() throws Exception {
        assertEquals(3, dict.size());

        map.put("fine", 1.0 / 137);
        assertEquals("Dictionary does not reflect updates to map", 4, dict.size());
    }

    @SuppressWarnings({ "unchecked" })
    public void testEmptyForEmptyMap() throws Exception {
        assertTrue(new MapBasedDictionary(Collections.emptyMap()).isEmpty());
    }

    public void testNotEmptyForNotEmptyMap() throws Exception {
        assertFalse(dict.isEmpty());
    }

    public void testKeysExposedInSameOrder() throws Exception {
        Iterator<String> mapKeys = map.keySet().iterator();
        Enumeration<String> dictKeys = dict.keys();

        int i = 0;
        while (mapKeys.hasNext() && dictKeys.hasMoreElements()) {
            assertEquals("Mismatch at " + i, mapKeys.next(), dictKeys.nextElement());
            i++;
        }
        assertFalse("More keys in map than in dict", mapKeys.hasNext());
        assertFalse("More keys in dict than in map", dictKeys.hasMoreElements());
    }

    public void testValuesExposedInSameOrder() throws Exception {
        Iterator<Number> mapValues = map.values().iterator();
        Enumeration<Number> dictValues = dict.elements();

        int i = 0;
        while (mapValues.hasNext() && dictValues.hasMoreElements()) {
            assertEquals("Mismatch at " + i, mapValues.next(), dictValues.nextElement());
            i++;
        }
        assertFalse("More values in map than in dict", mapValues.hasNext());
        assertFalse("More values in dict than in map", dictValues.hasMoreElements());
    }

    public void testPutWorks() throws Exception {
        dict.put("c", 3e8);
        assertEquals(3e8, dict.get("c"));
    }
    
    public void testPutReturnsOriginalValue() throws Exception {
        assertEquals(3.1416, dict.put("pi", 3.14159));
    }

    public void testPutReflectedInMap() throws Exception {
        dict.put("c", 3e8);
        assertEquals(3e8, map.get("c"));
    }

    public void testGetWorks() throws Exception {
        assertEquals(42, dict.get("answer"));
    }

    public void testRemoveWhenPresent() throws Exception {
        assertEquals(3.1416, dict.remove("pi"));
    }

    public void testRemoveWhenNotPresent() throws Exception {
        assertNull(dict.remove("fine"));
    }

    public void testRemoveReflectedInMap() throws Exception {
        dict.remove("e");
        assertEquals(2, map.size());
    }
    
    public void testDefaultConstructorCreatesUsableDictionary() throws Exception {
        Dictionary<Integer, String> def = new MapBasedDictionary<Integer, String>();
        def.put(1, "unus, una, unum");
        assertEquals("unus, una, unum", def.get(1));
    }
    
    public void testCopiedDictDoesNotWriteThroughToSource() throws Exception {
        Dictionary<String, Number> copy = MapBasedDictionary.copy(map);
        assertEquals("Original value not in copy", 42, copy.get("answer"));
        copy.put("answer", 43);
        assertEquals("Value not written to dict", 43, copy.get("answer"));
        assertEquals("Value modified in original map", 42, map.get("answer"));
    }

    public void testCopiedDictIncludesNullValues() throws Exception {
        Dictionary<String, Object> copy = MapBasedDictionary.copy(Collections.singletonMap("unknown", null));
        assertEquals(1, copy.size());
        assertNull(copy.get("unknown"));
    }
    
    public void testEqualsWhenMapsEqual() throws Exception {
        Dictionary<String, Double> one = MapBasedDictionary.copy(Collections.singletonMap("fine", 1.0 / 137));
        Dictionary<String, Double> two = MapBasedDictionary.copy(Collections.singletonMap("fine", 1.0 / 137));
        assertEquals(one, two);
    }
}
