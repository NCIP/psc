package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import junit.framework.TestCase;

import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.ListIterator;

import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.Person;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.DefaultPerson;

/**
 * @author Jalpa Patel
 */
public class EncapsulatedListTest extends EncapsulatedCollectionTestCase {
    private List<Object> farList;
    private EncapsulatedList<Person> encapsulated;
    private DefaultPerson nearPolo, nearAlexander, nearVespucci;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        farList = new LinkedList<Object>(Arrays.asList(
            farPerson("Polo"),
            farPerson("Alexander")
        ));
        encapsulated = new EncapsulatedList<Person>(farList, membrane);
        nearPolo = new DefaultPerson("Polo", "traveler");
        nearAlexander = new DefaultPerson("Alexander", "traveler");
        nearVespucci = new DefaultPerson("Vespucci", "traveler");
    }

    public void testAddAllAtIndex() throws Exception {
        boolean result = encapsulated.addAll(1, Arrays.asList(nearVespucci, nearPolo));
        assertTrue("Change not flagged in return value", result);
        assertEquals(4, encapsulated.size());

        Object newFarObj = farList.get(1);
        assertTrue("New item not proxied", newFarObj.getClass().getName().contains("Enhancer"));
        assertEquals("New item in wrong CL", loaderA, newFarObj.getClass().getClassLoader());
        assertSame("First new item not recoverable", nearVespucci, encapsulated.get(1));
        assertSame("Second new item not recoverable", nearPolo, encapsulated.get(2));

        assertSame("New item not recoverable from membrane", nearVespucci, membrane.farToNear(newFarObj));
        assertSame("New item not recoverable from membrane", newFarObj, membrane.traverse(nearVespucci, loaderA));

        assertTrue("Collection does not contain new item", encapsulated.contains(nearVespucci));
    }

    public void testAdd() throws Exception {
        encapsulated.add(1,nearVespucci);
        assertEquals(3, encapsulated.size());
        Object newFarObj = farList.get(1);
        assertTrue("New item not proxied", newFarObj.getClass().getName().contains("Enhancer"));
        assertEquals("New item in wrong CL", loaderA, newFarObj.getClass().getClassLoader());
        assertSame("New item not recoverable", nearVespucci, encapsulated.get(1));

        assertSame("New item not recoverable from membrane", nearVespucci, membrane.farToNear(newFarObj));
        assertSame("New item not recoverable from membrane", newFarObj, membrane.traverse(nearVespucci, loaderA));

        assertTrue("Collection does not contain new item", encapsulated.contains(nearVespucci));
    }

    public void testGet() throws Exception {
        Object actual = encapsulated.get(1);
        assertTrue("Actual not a Person: " + actual.getClass().getName(), actual instanceof Person);
        assertEquals("Item not found", "Alexander", ((Person)actual).getName());
    }

    public void testRemove() throws Exception {
        Person actual = encapsulated.remove(1);
        assertEquals("Remove Item is not expected Item", "Alexander", actual.getName());
        assertEquals("Item can not be removed", 1, farList.size());
        assertEquals("Item not found", "Polo", encapsulated.get(0).getName());

    }

    public void testIndexOfPresentItem() throws Exception {
        assertEquals("Item Index is not expected", 1, encapsulated.indexOf(nearAlexander));
    }

    public void testIndexOfNonPresentItem() throws Exception {
        assertEquals("Item found", -1, encapsulated.indexOf(nearVespucci));
    }

    public void testLastIndexOfPresentItem() throws Exception {
        encapsulated.add(encapsulated.get(0));
        encapsulated.add(encapsulated.get(1));
        assertEquals("Last Index of item is not expected", 3, encapsulated.lastIndexOf(nearAlexander));
    }

    public void testLastIndexOfNonPresentItem() throws Exception {
        assertEquals("Item found", -1, encapsulated.lastIndexOf(nearVespucci));
    }

    public void testSet() throws Exception {
        Person value = encapsulated.set(1,nearVespucci);
        assertEquals(2, encapsulated.size());
        Object newFarObj = farList.get(1);

        assertEquals("Not expected", "Alexander", value.getName());
        assertTrue("New item not proxied", newFarObj.getClass().getName().contains("Enhancer"));
        assertEquals("New item in wrong CL", loaderA, newFarObj.getClass().getClassLoader());
        assertSame("New item not recoverable", nearVespucci, encapsulated.get(1));

        assertSame("New item not recoverable from membrane", nearVespucci, membrane.farToNear(newFarObj));
        assertSame("New item not recoverable from membrane", newFarObj, membrane.traverse(nearVespucci, loaderA));

        assertTrue("Collection does not contain new item", encapsulated.contains(nearVespucci));
    }

    public void testGetSubList() throws Exception {
        encapsulated.add(encapsulated.get(0));
        encapsulated.add(encapsulated.get(1));
        encapsulated.add(encapsulated.get(0));

        List<Person> personList = encapsulated.subList(2,5);
        assertEquals("Size expected",3,personList.size());
        assertEquals("Item is not expected", "Polo", personList.get(0).getName());
        assertEquals("Item is not expected", "Alexander", personList.get(1).getName());
        assertEquals("Item is not expected", "Polo", personList.get(2).getName());
    }

    public void testGetListIterator() throws Exception {
        ListIterator<Person> listIterator = encapsulated.listIterator();
        assertEquals("Item is not expected", "Polo", listIterator.next().getName());
        listIterator.next();
        assertEquals("Item is not expected", "Alexander", listIterator.previous().getName());
    }

    public void testSetListIterator() throws Exception {
        ListIterator<Person> listIterator = encapsulated.listIterator();
        listIterator.next();
        listIterator.set(nearVespucci);
        assertEquals("New far object is in wrong CL", loaderA, farList.get(0).getClass().getClassLoader());
    }
    
    public void testIndexLitIterator() throws Exception {
        ListIterator<Person> listIterator = encapsulated.listIterator(1);
        assertEquals("Item not expected", 0, listIterator.previousIndex());
        assertEquals("Item not expected", 1, listIterator.nextIndex());
    }

}
