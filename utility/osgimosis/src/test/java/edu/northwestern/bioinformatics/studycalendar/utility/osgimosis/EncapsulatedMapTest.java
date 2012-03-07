package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.DefaultHat;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.DefaultPerson;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.Hat;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.Person;

import java.awt.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class EncapsulatedMapTest extends EncapsulatedCollectionTestCase {
    private Map<Object, Object> farMap;

    private Person nearPolo, nearAlexander, nearVespucci;
    private Hat nearBlackHat, nearGreenHat, nearRedHat;
    private EncapsulatedMap<Person, Hat> encapsulated;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        farMap = new HashMap<Object, Object>();
        farMap.put(farPerson("Polo"), farHat(Color.black));
        farMap.put(farPerson("Alexander"), farHat(Color.green));

        encapsulated = new EncapsulatedMap<Person, Hat>(
            farMap, membrane, Thread.currentThread().getContextClassLoader());
        nearPolo = new DefaultPerson("Polo", "traveler");
        nearAlexander = new DefaultPerson("Alexander", "traveler");
        nearVespucci = new DefaultPerson("Vespucci", "traveler");

        nearBlackHat = new DefaultHat(Color.black);
        nearGreenHat = new DefaultHat(Color.green);
        nearRedHat = new DefaultHat(Color.red);
    }

    public void testSizeIsDelegated() throws Exception {
        assertEquals(2, encapsulated.size());
    }

    public void testIsEmptyWhenNotEmpty() throws Exception {
        assertFalse(encapsulated.isEmpty());
    }

    public void testIsEmptyWhenEmpty() throws Exception {
        farMap.clear();
        assertTrue(encapsulated.isEmpty());
    }

    public void testClearIsDelegated() throws Exception {
        encapsulated.clear();
        assertTrue(farMap.isEmpty());
    }

    public void testContainsKeyWhenContains() throws Exception {
        assertTrue(encapsulated.containsKey(nearPolo));
    }

    public void testContainsKeyWhenDoesNotContain() throws Exception {
        assertFalse(encapsulated.containsKey(nearVespucci));
    }

    public void testContainsValueWhenContains() throws Exception {
        assertTrue(encapsulated.containsValue(nearBlackHat));
    }

    public void testContainsValueWhenNotContains() throws Exception {
        assertFalse(encapsulated.containsValue(nearRedHat));
    }

    public void testGetWhenPresent() throws Exception {
        assertEquals(nearGreenHat, encapsulated.get(nearAlexander));
    }

    public void testGetWhenAbsent() throws Exception {
        assertNull(encapsulated.get(nearVespucci));
    }

    public void testPutWhenNewKey() throws Exception {
        Hat oldValue = encapsulated.put(nearVespucci, nearRedHat);
        assertEquals(3, farMap.size());
        assertNull("Wrong old value", oldValue);
        assertEquals("Wrong new value", nearRedHat, encapsulated.get(nearVespucci));
        assertNotSame("Stored value not encapsulated",
            DefaultHat.class, farMap.get(farPerson("Vespucci")).getClass());
    }

    public void testPutWhenReplacingKey() throws Exception {
        Hat oldValue = encapsulated.put(nearAlexander, nearBlackHat);
        assertEquals(2, farMap.size());
        assertEquals("Wrong old value", nearGreenHat, oldValue);
        assertEquals("Wrong new value", nearBlackHat, encapsulated.get(nearAlexander));
        assertNotSame("Stored value not encapsulated",
            DefaultHat.class, farMap.get(farPerson("Alexander")).getClass());
    }

    public void testPutAllWhenReplacing() throws Exception {
        encapsulated.putAll(Collections.singletonMap(nearPolo, nearRedHat));
        assertEquals(2, farMap.size());
        assertEquals("Wrong new value", nearRedHat, encapsulated.get(nearPolo));
        assertNotSame("Stored value not encapsulated",
            DefaultHat.class, farMap.get(farPerson("Polo")).getClass());
    }

    public void testPutAllWhenNewKey() throws Exception {
        encapsulated.putAll(Collections.singletonMap(nearVespucci, nearRedHat));
        assertEquals(3, farMap.size());
        assertEquals("Wrong new value", nearRedHat, encapsulated.get(nearVespucci));
        assertNotSame("Stored value not encapsulated",
            DefaultHat.class, farMap.get(farPerson("Vespucci")).getClass());
    }

    public void testRemoveExistingKey() throws Exception {
        Hat removed = encapsulated.remove(nearPolo);
        assertEquals(Color.black, removed.getColor());
    }

    public void testRemoveNonExistentKey() throws Exception {
        assertNull(encapsulated.remove(nearVespucci));
    }

    public void testKeySet() throws Exception {
        assertEquals(2, encapsulated.keySet().size());
        assertTrue(encapsulated.keySet().contains(nearPolo));
        assertFalse(encapsulated.keySet().contains(nearVespucci));
    }

    public void testValues() throws Exception {
        assertEquals(2, encapsulated.values().size());
        assertTrue(encapsulated.values().contains(nearBlackHat));
    }

    public void testEntrySetSize() throws Exception {
        assertEquals(2, encapsulated.entrySet().size());
    }

    public void testEntrySetContents() throws Exception {
        encapsulated.remove(nearPolo);
        Map.Entry<Person, Hat> entry = encapsulated.entrySet().iterator().next();
        assertEquals(nearAlexander, entry.getKey());
        assertEquals(nearGreenHat, entry.getValue());
    }

    public void testEntrySetRemovalsReflectedInMap() throws Exception {
        assertEquals(2, farMap.size());

        Iterator<Map.Entry<Person,Hat>> entryIterator = encapsulated.entrySet().iterator();
        entryIterator.next();
        entryIterator.remove();

        assertEquals(1, farMap.size());
    }

    public void testEntrySetChangesReflectedInMap() throws Exception {
        encapsulated.remove(nearPolo);

        Iterator<Map.Entry<Person,Hat>> entryIterator = encapsulated.entrySet().iterator();
        Map.Entry<Person, Hat> entry = entryIterator.next();
        Hat oldValue = entry.setValue(nearRedHat);

        assertEquals("Wrong old value", oldValue, nearGreenHat);
        assertEquals("Wrong new value", nearRedHat, encapsulated.get(nearAlexander));
    }
}
