package edu.northwestern.bioinformatics.studycalendar.dao;

import static edu.nwu.bioinformatics.commons.testing.CoreTestCase.*;
import edu.nwu.bioinformatics.commons.DateUtils;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEventMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class ScheduledEventDaoTest extends ContextDaoTestCase<ScheduledEventDao> {
    private PlannedActivityDao plannedActivityDao;
    private ScheduledCalendarDao scheduledCalendarDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        scheduledCalendarDao
            = (ScheduledCalendarDao) getApplicationContext().getBean("scheduledCalendarDao");
        plannedActivityDao
            = (PlannedActivityDao) getApplicationContext().getBean("plannedActivityDao");
    }

    @Override
    protected String getTestDataFileName() {
        return "testdata/ScheduledCalendarDaoTest.xml";
    }
    
    public void testGetById() throws Exception {
        ScheduledEvent loaded = getDao().getById(-10);

        assertEquals("Wrong planned event", -6, (int) loaded.getPlannedActivity().getId());
        assertEquals("Wrong scheduled arm", -22, (int) loaded.getScheduledArm().getId());
        assertEquals("Wrong activity id", -100, (int) loaded.getActivity().getId());
        assertDayOfDate("Wrong ideal date", 2006, Calendar.OCTOBER, 31, loaded.getIdealDate());
        assertEquals("Wrong notes", "Boo!", loaded.getNotes());

        assertEquals("Wrong current reason", "Success", loaded.getCurrentState().getReason());
        assertEquals("Wrong current mode", ScheduledEventMode.OCCURRED, loaded.getCurrentState().getMode());

        assertEquals("Wrong number of previous states", 3, loaded.getPreviousStates().size());

        assertEquals("Wrong details", "Nice Details!!", loaded.getDetails());
    }
    
    public void testGetScheduledEventsFromPlannedEvent() throws Exception {
        Collection<ScheduledEvent> matches = getDao().getEventsFromPlannedEvent(
            plannedActivityDao.getById(-6), scheduledCalendarDao.getById(-21));
        assertEquals("Wrong number of matches", 2, matches.size());
        Collection<Integer> actualIds = DomainObjectTools.collectIds(matches);
        assertContains(actualIds, -30);
        assertContains(actualIds, -31);
    }

    public void testLoadedAndQueriedScheduledEventsFromSameSessionAreSame() throws Exception {
        ScheduledEvent loaded = getDao().getById(-30);
        loaded.setDetails("F9");
        ScheduledEvent queried = null;

        for (ScheduledEvent e : getDao().getEventsFromPlannedEvent(plannedActivityDao.getById(-6), scheduledCalendarDao.getById(-21))) {
            if (e.getId() == -30) {
                queried = e;
                break;
            }
        }

        assertNotNull(queried);
        assertSame(loaded, queried);
    }

    public void testGetByRangeFinite() throws Exception {
        assertEventsByDate(
            DateUtils.createDate(2006, Calendar.OCTOBER, 28),
            DateUtils.createDate(2006, Calendar.OCTOBER, 31),
            -15, -16);
    }

    public void testGetByRangeInfiniteHigh() throws Exception {
        assertEventsByDate(
            DateUtils.createDate(2006, Calendar.OCTOBER, 28),
            null,
            -15, -16, -18);
    }

    public void testGetByRangeInfiniteLow() throws Exception {
        assertEventsByDate(
            null,
            DateUtils.createDate(2006, Calendar.OCTOBER, 30),
            -10, -15, -16);
    }

    private void assertEventsByDate(Date lo, Date hi, int... expectedIds) {
        ScheduledCalendar calendar = scheduledCalendarDao.getById(-20);
        Collection<Integer> actualIds
            = DomainObjectTools.collectIds(getDao().getEventsByDate(calendar, lo, hi));
        assertEquals("Wrong number of matched events", expectedIds.length, actualIds.size());
        for (int expectedId : expectedIds) {
            assertContains("Missing event", actualIds, expectedId);
        }
    }
}
