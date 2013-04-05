/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Duration;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;

import java.util.Iterator;
import java.util.Set;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class PeriodDaoTest extends ContextDaoTestCase<PeriodDao> {
    private ActivityDao activityDao = (ActivityDao) getApplicationContext().getBean("activityDao");

    public void testPropertyLoad() throws Exception {
        Period loaded = getDao().getById(-100);

        assertNotNull("Test period not found", loaded);
        assertEquals("Wrong id", new Integer(-100), loaded.getId());
        assertEquals("Wrong studySegment", new Integer(-200), loaded.getStudySegment().getId());
        assertEquals("Wrong start day", new Integer(8), loaded.getStartDay());
        assertEquals("Wrong name", "Treatment", loaded.getName());
        assertEquals("Wrong duration quantity", new Integer(6), loaded.getDuration().getQuantity());
        assertEquals("Wrong duration type", Duration.Unit.week, loaded.getDuration().getUnit());
    }

    public void testEventLoad() throws Exception {
        Period loaded = getDao().getById(-100);

        assertNotNull("Test period not found", loaded);
        List<PlannedActivity> loadedEvents = loaded.getPlannedActivities();
        assertEquals("Wrong number of planned events", 3, loadedEvents.size());
        Iterator<PlannedActivity> iterator = loadedEvents.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("Wrong first event", new Integer(-2003), iterator.next().getId());
        assertTrue(iterator.hasNext());
        assertEquals("Wrong second event", new Integer(-2002), iterator.next().getId());
        assertTrue(iterator.hasNext());
        assertEquals("Wrong third event", new Integer(-2001), iterator.next().getId());
    }

    public void testAddEvent() throws Exception {
        Integer newEventId;
        {
            Period loaded = getDao().getById(-100);
            assertNotNull("Test period not found", loaded);

            PlannedActivity newEvent = new PlannedActivity();
            newEvent.setDay(4);
            newEvent.setActivity(activityDao.getById(-1001));
            loaded.addPlannedActivity(newEvent);

            getDao().save(loaded);

            interruptSession();

            newEventId = newEvent.getId();
            assertNotNull("New event did not get ID on save", newEventId);
        }

        {
            Period reloaded = getDao().getById(-100);
            assertEquals("Wrong number of events", 4, reloaded.getPlannedActivities().size());

            Iterator<PlannedActivity> iterator = reloaded.getPlannedActivities().iterator();
            assertTrue(iterator.hasNext());
            assertEquals("Wrong first event", new Integer(-2003), iterator.next().getId());
            assertTrue(iterator.hasNext());
            assertEquals("Wrong second event", new Integer(-2002), iterator.next().getId());
            assertTrue(iterator.hasNext());
            assertEquals("Wrong 3rd event", new Integer(-2001), iterator.next().getId());
            assertTrue(iterator.hasNext());
            assertEquals("Wrong (new) 4th event", newEventId, iterator.next().getId());
        }
    }

    public void testSaveDetached() throws Exception {
        Integer id;
        {
            Period period = new Period();
            period.setName("Renaissance");
            getDao().save(period);
            assertNotNull("not saved", period.getId());
            id = period.getId();
        }

        interruptSession();

        Period loaded = getDao().getById(id);
        assertNotNull("Could not reload", loaded);
        assertEquals("Wrong period loaded", "Renaissance", loaded.getName());
    }

    public void testDeleteJustPlainOrphans() throws Exception {
        Period p = getDao().getById(-10);
        assertNotNull(p);
        assertTrue("Period is attached ", p.isDetached());
        assertNull("Period has a parent ", p.getParent());
        getDao().deleteOrphans();
        assertNull(getDao().getById(-10));
    }

    public void testDeletePeriodWithParent() throws Exception {
        Period pa = getDao().getById(-101);
        assertNotNull(pa);
        assertNotNull("Period does not have a parent ", pa.getParent());
        getDao().deleteOrphans();
        assertNotNull(getDao().getById(-101));
    }

    public void testToDeletePeriodWithAddOnly() throws Exception {
        Period p = getDao().getById(-19);
        assertTrue("Period is attached ", p.isDetached());
        assertNull("Period has a parent ", p.getParent());
        getDao().deleteOrphans();
        assertNotNull(getDao().getById(-19));
    }

    public void testToDeletePeriodWithRemoveOnly() throws Exception {
        Period p = getDao().getById(-200);
        assertTrue("Period is attached ", p.isDetached());
        assertNull("Period has a parent ", p.getParent());
        getDao().deleteOrphans();
        assertNotNull(getDao().getById(-200));
    }
}
