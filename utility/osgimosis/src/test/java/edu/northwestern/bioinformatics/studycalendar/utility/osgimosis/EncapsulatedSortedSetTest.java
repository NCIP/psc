/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.DefaultPerson;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.PeopleByName;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.Person;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "RawUseOfParameterizedType" })
public class EncapsulatedSortedSetTest extends EncapsulatedCollectionTestCase {
    @SuppressWarnings({ "RawUseOfParameterizedType" })
    private SortedSet farSet;
    private EncapsulatedSortedSet<Person> encapsulated;
    private DefaultPerson nearPolo, nearAlexander, nearVespucci;

    @Override
    @SuppressWarnings({ "unchecked", "RawUseOfParameterizedType" })
    public void setUp() throws Exception {
        super.setUp();
        Comparator farComparator = (Comparator) classFromLoader(PeopleByName.class, loaderA).newInstance();
        farSet = new TreeSet(farComparator);
        farSet.add(farPerson("Polo"));
        farSet.add(farPerson("Alexander"));
        farSet.add(farPerson("Vespucci"));
        encapsulated = new EncapsulatedSortedSet<Person>(farSet, membrane, defaultClassLoader());
        nearPolo = new DefaultPerson("Polo", "traveler");
        nearAlexander = new DefaultPerson("Alexander", "traveler");
        nearVespucci = new DefaultPerson("Vespucci", "traveler");
    }

    public void testComparatorIsAccessible() throws Exception {
        Object actual = encapsulated.comparator();
        assertTrue(actual instanceof Comparator);
    }

    public void testSubSet() throws Exception {
        SortedSet<Person> actual = encapsulated.subSet(nearPolo, nearVespucci);
        assertEquals("Wrong number of elements", 1, actual.size());
        assertEquals("Wrong first element", "Polo", actual.iterator().next().getName());
    }

    public void testHeadSet() throws Exception {
        SortedSet<Person> actual = encapsulated.headSet(nearPolo);
        assertEquals("Wrong number of elements", 1, actual.size());
        assertEquals("Wrong first element", "Alexander", actual.iterator().next().getName());
    }

    public void testTailSet() throws Exception {
        SortedSet<Person> actual = encapsulated.tailSet(nearPolo);
        assertEquals("Wrong number of elements", 2, actual.size());
        Iterator<Person> people = actual.iterator();
        assertEquals("Wrong first element", "Polo", people.next().getName());
        assertEquals("Wrong second element", "Vespucci", people.next().getName());
    }

    public void testFirst() throws Exception {
        Person actual = encapsulated.first();
        assertEquals("Wrong first", "Alexander", actual.getName());
    }

    public void testList() throws Exception {
        Person actual = encapsulated.last();
        assertEquals("Wrong first", "Vespucci", actual.getName());
    }

    public void testReverseFirst() throws Exception {
        SortedSet<Person> nearSet = new TreeSet<Person>(Arrays.asList(nearPolo));
        EncapsulatedSortedSet reverse = new EncapsulatedSortedSet(nearSet, membrane, loaderA);
        Object far0 = reverse.first();
        assertEquals("Far object is in wrong CL", loaderA, far0.getClass().getClassLoader().getParent());
    }
}
