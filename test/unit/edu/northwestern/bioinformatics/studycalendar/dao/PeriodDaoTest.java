package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.Duration;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;

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
        assertEquals("Wrong arm", new Integer(-200), loaded.getArm().getId());
        assertEquals("Wrong start day", new Integer(8), loaded.getStartDay());
        assertEquals("Wrong name", "Treatment", loaded.getName());
        assertEquals("Wrong duration quantity", new Integer(6), loaded.getDuration().getQuantity());
        assertEquals("Wrong duration type", Duration.Unit.week, loaded.getDuration().getUnit());
    }

    public void testEventLoad() throws Exception {
        Period loaded = getDao().getById(-100);

        assertNotNull("Test period not found", loaded);
        List<PlannedEvent> loadedEvents = loaded.getPlannedEvents();
        assertEquals("Wrong number of planned events", 3, loadedEvents.size());
        Iterator<PlannedEvent> iterator = loadedEvents.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("Wrong first event", new Integer(-2002), iterator.next().getId());
        assertTrue(iterator.hasNext());
        assertEquals("Wrong second event", new Integer(-2001), iterator.next().getId());
        assertTrue(iterator.hasNext());
        assertEquals("Wrong third event", new Integer(-2003), iterator.next().getId());
    }

    public void testAddEvent() throws Exception {
        Integer newEventId;
        {
            Period loaded = getDao().getById(-100);
            assertNotNull("Test period not found", loaded);

            PlannedEvent newEvent = new PlannedEvent();
            newEvent.setDay(4);
            newEvent.setActivity(activityDao.getById(-1001));
            loaded.addPlannedEvent(newEvent);

            getDao().save(loaded);

            interruptSession();

            newEventId = newEvent.getId();
            assertNotNull("New event did not get ID on save", newEventId);
        }

        {
            Period reloaded = getDao().getById(-100);
            assertEquals("Wrong number of events", 4, reloaded.getPlannedEvents().size());

            Iterator<PlannedEvent> iterator = reloaded.getPlannedEvents().iterator();
            assertTrue(iterator.hasNext());
            assertEquals("Wrong first event", new Integer(-2002), iterator.next().getId());
            assertTrue(iterator.hasNext());
            assertEquals("Wrong second event", new Integer(-2001), iterator.next().getId());
            assertTrue(iterator.hasNext());
            assertEquals("Wrong (new) third event", newEventId, iterator.next().getId());
            assertTrue(iterator.hasNext());
            assertEquals("Wrong fourth event", new Integer(-2003), iterator.next().getId());
        }
    }
}
