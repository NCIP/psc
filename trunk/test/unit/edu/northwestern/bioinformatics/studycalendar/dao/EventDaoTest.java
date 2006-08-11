package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;

/**
 * @author Rhett Sutphin
 */
public class EventDaoTest extends ContextDaoTestCase<EventDao> {
    public void testGetById() throws Exception {
        PlannedEvent loaded = getDao().getPlannedEventById(-12);

        assertEquals("Wrong id", -12, (int) loaded.getId());
        assertEquals("Wrong day number", new Integer(4), loaded.getDay());
        assertNotNull("Period not loaded", loaded.getPeriod());
        assertEquals("Wrong period", -300L, (long) loaded.getPeriod().getId());
        assertNotNull("Activity not loaded", loaded.getActivity());
        assertEquals("Wrong activity", -200L, (long) loaded.getActivity().getId());
    }

    public void testPeriodBidirectional() throws Exception {
        PlannedEvent loaded = getDao().getPlannedEventById(-12);
        assertTrue(loaded.getPeriod().getPlannedEvents().contains(loaded));
    }
}
