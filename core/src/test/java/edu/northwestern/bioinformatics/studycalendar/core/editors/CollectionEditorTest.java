/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.core.editors;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.PopulationDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createPopulation;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import static org.easymock.EasyMock.expect;

/**
 * @author Jalpa Patel
 */
public class CollectionEditorTest extends StudyCalendarTestCase {
    private CollectionEditor editor;
    private PopulationDao populationDao;
    private Population population;
    private Set populations;

    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        super.setUp();
        populationDao = registerDaoMockFor(PopulationDao.class);
        editor = new CollectionEditor(Set.class, populationDao);
        population = setId(3, createPopulation("T", "Test"));
        populations = new TreeSet<Population>();
        populations.add(population);
    }

    public void testSetAsTextNull() throws Exception {
        editor.setAsText(null);
        assertNull(editor.getValue());
    }

    public void testSetAsTextEmptyString() throws Exception {
        editor.setAsText("");
        assertNull(editor.getValue());
    }

    public void testSetAsTextWithValidSingleId() throws Exception {
        expect(populationDao.getById(3)).andReturn(population);
        replayMocks();
        editor.setAsText("3");
        verifyMocks();
        assertEquals("values are not equal", populations, editor.getValue());
    }

    @SuppressWarnings("unchecked")
    public void testSetAsTextWithValidIds() throws Exception {
        Population population1 = setId(4, createPopulation("T1", "Test1"));
        populations.add(population1);
        expect(populationDao.getById(3)).andReturn(population);
        expect(populationDao.getById(4)).andReturn(population1);
        replayMocks();
        editor.setAsText("3 4");
        verifyMocks();
        assertEquals("values are not equal", populations, editor.getValue());
    }

    public void testSetAsTextWithInValidSingleId() throws Exception {
        Integer invalidId = -3;
        expect(populationDao.getById(invalidId)).andReturn(null);
        replayMocks();
        try {
            editor.setAsText(invalidId.toString());
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            verifyMocks();
            assertEquals("There is no " + populationDao.domainClass().getSimpleName() + " with id = " + invalidId, iae.getMessage());
        }
        verifyMocks();
    }

    public void testSetAsTextWithInValidIdFromIds() throws Exception {
        Integer invalidId = -3;
        expect(populationDao.getById(3)).andReturn(population);
        expect(populationDao.getById(invalidId)).andReturn(null);
        replayMocks();
        try {
            editor.setAsText("3 -3");
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            verifyMocks();
            assertEquals("There is no " + populationDao.domainClass().getSimpleName() + " with id = " + invalidId, iae.getMessage());
        }
        verifyMocks();
    }

    public void testSetAsTextWithNonIdsValues() throws Exception {
        SortedSet<String> values =  new TreeSet<String>();
        values.add("test1");
        values.add("test2 test3");
        editor = new CollectionEditor(Set.class);
        replayMocks();
        editor.setAsText("test1 test2+test3");
        verifyMocks();

        assertEquals("values are not equal", values, editor.getValue());
    }

    public void testSetAsTextWithNonIdSingleValue() throws Exception {
        SortedSet<String> values =  new TreeSet<String>();
        values.add("test1");
        editor = new CollectionEditor(Set.class);
        replayMocks();
        editor.setAsText("test1");
        verifyMocks();

        assertEquals("values are not equal", values, editor.getValue());
    }
}
