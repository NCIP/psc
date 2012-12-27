/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.Person;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.DefaultPerson;

import java.util.*;

/**
 * @author Jalpa Patel
 */
@SuppressWarnings({"unchecked"})
public class EncapsulatedListIteratorTest extends EncapsulatedCollectionTestCase {
    private ListIterator<Person> iterator;
    private ListIterator farIterator;
    private List farList;

    public void setUp() throws Exception {
        super.setUp();
        farList = new LinkedList(Arrays.asList(
            farPerson("Polo"), farPerson("Alexander"), farPerson("Polo")
        ));
        farIterator = farList.listIterator();
        iterator = new EncapsulatedListIterator<Person>(farIterator, membrane, defaultClassLoader(), loaderA);
    }

    public void testHasPreviousWhenHas() throws Exception {
        farIterator.next();
        assertTrue(iterator.hasPrevious());
    }

    public void testHasPreviousWhenDoesNot() throws Exception {
       assertFalse(iterator.hasPrevious());
    }

    public void testNextIndexTracksFarIterator() throws Exception {
       assertEquals(0, iterator.nextIndex());
       farIterator.next();
       assertEquals(1, iterator.nextIndex());
    }

    public void testNextIndexIsSizeAtEnd() throws Exception {
        moveToEnd();
        assertEquals(3, iterator.nextIndex());
    }

    public void testPreviousIndexTracksFarIterator() throws Exception {
        farIterator.next();
        assertEquals(0, iterator.previousIndex());
        farIterator.next();
        assertEquals(1, iterator.previousIndex());
    }
    
    public void testPreviousIndexIsNegOneAtStart() throws Exception {
        assertEquals(-1, iterator.previousIndex());
    }

    public void testPreviousIsSameAsLastNext() throws Exception {
        Person next = iterator.next();
        assertSame(next, iterator.previous());
    }

    public void testPreviousAtBeginningThrowsNoSuchElement() throws Exception {
       try {
           iterator.previous();
           fail("Exception not thrown");
       } catch (NoSuchElementException nsee) {
           // good
       }
    }

    public void testSetWrapsValue() throws Exception {
        DefaultPerson newNearOne = new DefaultPerson("Vespucci", "traveler");
        farIterator.next();
        iterator.set(newNearOne);

        Object newFarObj = farList.get(0);
        assertTrue("New item not proxied", newFarObj.getClass().getName().contains("Enhancer"));
        assertEquals("New item not recoverable from membrane", newNearOne.getName(),
                ((Person) membrane.farToNear(newFarObj)).getName());
        assertEquals("New item not recoverable from membrane", newNearOne, membrane.farToNear(newFarObj));
    }

    public void testAddWrapsValue() throws Exception {
        DefaultPerson newNearOne = new DefaultPerson("Vespucci", "traveler");
        farIterator.next();
        iterator.add(newNearOne);

        Object newFarObj = farList.get(1);
        assertTrue("New item not proxied", newFarObj.getClass().getName().contains("Enhancer"));
        assertEquals("New item not recoverable from membrane", newNearOne.getName(),
                ((Person) membrane.farToNear(newFarObj)).getName());
        assertEquals("New item not recoverable from membrane", newNearOne, membrane.farToNear(newFarObj));
        assertEquals("List not extended", 4, farList.size());
    }

    private void moveToEnd() {
        farIterator.next();
        farIterator.next();
        farIterator.next();
    }
}
