package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.DefaultPerson;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.Person;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class EncapsulatedCollectionTest extends EncapsulatedCollectionTestCase {
    private List<Object> farCollection;
    private EncapsulatedCollection<Person> encapsulated;
    private DefaultPerson nearPolo, nearAlexander, nearVespucci;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        farCollection = new LinkedList<Object>(Arrays.asList(
            farPerson("Polo"),
            farPerson("Alexander")
        ));
        encapsulated = new EncapsulatedCollection<Person>(farCollection, membrane, defaultClassLoader());
        nearPolo = new DefaultPerson("Polo", "traveler");
        nearAlexander = new DefaultPerson("Alexander", "traveler");
        nearVespucci = new DefaultPerson("Vespucci", "traveler");
    }

    public void testSizeIsCorrect() throws Exception {
        assertEquals(2, encapsulated.size());
    }

    public void testIsNotEmpty() throws Exception {
        assertFalse(encapsulated.isEmpty());
    }
    
    public void testContainsEquivalentPerson() throws Exception {
        assertTrue(encapsulated.contains(nearPolo));
    }

    public void testDoesNotContainUnknownPerson() throws Exception {
        assertFalse(encapsulated.contains(nearVespucci));
    }
    
    public void testCanIterate() throws Exception {
        Iterator<Person> nearIterator = encapsulated.iterator();
        assertTrue(nearIterator.hasNext());
        assertEquals("Polo", nearIterator.next().getName());
        assertTrue(nearIterator.hasNext());
        assertEquals("Alexander", nearIterator.next().getName());
        assertFalse(nearIterator.hasNext());
    }

    public void testToObjectArray() throws Exception {
        Object[] array = encapsulated.toArray();
        assertEquals("Wrong number of elements", farCollection.size(), array.length);
        assertTrue("First element is wrong type", array[0] instanceof Person);
        assertEquals("First element is wrong person", "Polo", ((Person) array[0]).getName());
        assertTrue("Second element is wrong type", array[1] instanceof Person);
        assertEquals("Second element is wrong person", "Alexander", ((Person) array[1]).getName());
    }

    @SuppressWarnings({ "ToArrayCallWithZeroLengthArrayArgument" })
    public void testToTypedEmptyArray() throws Exception {
        Person[] array = encapsulated.toArray(new Person[0]);
        assertEquals("Wrong number of elements", farCollection.size(), array.length);
        assertEquals("First element is wrong person", "Polo", array[0].getName());
        assertEquals("Second element is wrong person", "Alexander", array[1].getName());
    }

    public void testToTypedSizedArray() throws Exception {
        Person[] expected = new Person[2];
        Person[] actual = encapsulated.toArray(expected);
        assertSame("Input array not reused", expected, actual);
        assertEquals("First element is wrong person", "Polo", actual[0].getName());
        assertEquals("Second element is wrong person", "Alexander", actual[1].getName());
    }

    public void testAdd() throws Exception {
        encapsulated.add(nearVespucci);
        assertEquals(3, encapsulated.size());
        Object newFarObj = farCollection.get(2);
        assertTrue("New item not proxied", newFarObj.getClass().getName().contains("Enhancer"));
        assertEquals("New item in wrong CL", loaderA, newFarObj.getClass().getClassLoader().getParent());
        assertSame("New item not recoverable", nearVespucci, getEncapsulatedItem(2));

        assertSame("New item not recoverable from membrane", nearVespucci, membrane.farToNear(newFarObj));
        assertSame("New item not recoverable from membrane", newFarObj, membrane.traverse(nearVespucci, loaderA));

        assertTrue("Collection does not contain new item", encapsulated.contains(nearVespucci));
    }
    
    public void testRemoveWhenExists() throws Exception {
        assertTrue(encapsulated.remove(nearPolo));
        assertEquals(1, encapsulated.size());
        assertEquals("Alexander", encapsulated.iterator().next().getName());
    }

    public void testRemoveWhenDoesNotExist() throws Exception {
        assertFalse(encapsulated.remove(nearVespucci));
    }

    public void testContainsAllWhenDoes() throws Exception {
        assertTrue(encapsulated.containsAll(Arrays.asList(nearAlexander, nearPolo)));
    }

    public void testContainsAllWhenDoesNot() throws Exception {
        assertFalse(encapsulated.containsAll(Arrays.asList(nearAlexander, nearVespucci)));
    }

    public void testAddAll() throws Exception {
        assertTrue(encapsulated.addAll(Arrays.asList(nearVespucci)));
        assertEquals(3, encapsulated.size());
        assertTrue(encapsulated.contains(nearVespucci));
    }

    public void testRemoveAllWithNoOverlap() throws Exception {
        assertFalse(encapsulated.removeAll(Arrays.asList(nearVespucci)));
        assertEquals(2, encapsulated.size());
    }

    public void testRemoveAllWithOverlap() throws Exception {
        assertTrue(encapsulated.removeAll(Arrays.asList(nearVespucci, nearPolo)));
        assertEquals(1, encapsulated.size());
        assertTrue(encapsulated.contains(nearAlexander));
    }

    public void testRetainAllWithNoOverlap() throws Exception {
        assertTrue(encapsulated.retainAll(Arrays.asList(nearVespucci)));
        assertTrue(encapsulated.isEmpty());
    }

    public void testRetainAllWithOverlap() throws Exception {
        assertTrue(encapsulated.retainAll(Arrays.asList(nearVespucci, nearPolo)));
        assertEquals(1, encapsulated.size());
        assertTrue(encapsulated.contains(nearPolo));
    }

    private Person getEncapsulatedItem(int index) {
        Iterator<Person> it = encapsulated.iterator();
        while (index > 0) {
            assertTrue("Insufficient elements in collection", it.hasNext());
            it.next();
            index--;
        }
        return it.next();
    }
}
