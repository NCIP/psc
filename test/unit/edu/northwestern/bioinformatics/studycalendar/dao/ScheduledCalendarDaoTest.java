package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEventState;

import java.util.Calendar;

/**
 * @author Rhett Sutphin
 */
public class ScheduledCalendarDaoTest extends DaoTestCase {
    private ScheduledCalendarDao dao
        = (ScheduledCalendarDao) getApplicationContext().getBean("scheduledCalendarDao");

    public void testGetById() throws Exception {
        ScheduledCalendar cal = dao.getById(-20);

        assertEquals("Wrong assignment", -1, (int) cal.getAssignment().getId());

        assertEquals("Wrong number of arms", 2, cal.getArms().size());
        assertEquals("Wrong arm 0", -4, (int) cal.getArms().get(0).getId());
        assertEquals("Wrong arm 1", -3, (int) cal.getArms().get(1).getId());

        assertEquals("Wrong number of events", 1, cal.getEvents().size());
        ScheduledEvent event = cal.getEvents().get(0);
        assertEquals("Wrong event", -10, (int) event.getId());
        assertEquals("Wrong base event for event", -6, (int) event.getPlannedEvent().getId());
        StudyCalendarTestCase.assertDayOfDate("Wrong ideal date", 2006, Calendar.OCTOBER, 31, event.getIdealDate());
        StudyCalendarTestCase.assertDayOfDate("Wrong actual date", 2006, Calendar.OCTOBER, 25, event.getActualDate());
        assertEquals("Wrong notes", "Boo!", event.getNotes());
        assertEquals("Wrong event state", ScheduledEventState.OCCURRED, event.getState());
    }
}
