/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.DomainObjectTools;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.nwu.bioinformatics.commons.DateUtils;
import gov.nih.nci.cabig.ctms.lang.DateTools;

import static edu.nwu.bioinformatics.commons.testing.CoreTestCase.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class ScheduledActivityDaoTest extends ContextDaoTestCase<ScheduledActivityDao> {
    private PlannedActivityDao plannedActivityDao;
    private ScheduledCalendarDao scheduledCalendarDao;
    private ScheduledActivityDao scheduledActivityDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        scheduledCalendarDao
            = (ScheduledCalendarDao) getApplicationContext().getBean("scheduledCalendarDao");
        plannedActivityDao
            = (PlannedActivityDao) getApplicationContext().getBean("plannedActivityDao");
        scheduledActivityDao
            = (ScheduledActivityDao) getApplicationContext().getBean("scheduledActivityDao");
    }

    @Override
    protected String getTestDataFileName() {
        return "testdata/ScheduledCalendarDaoTest.xml";
    }
    
    public void testGetById() throws Exception {
        ScheduledActivity loaded = getDao().getById(-10);

        assertEquals("Wrong planned event", -6, (int) loaded.getPlannedActivity().getId());
        assertEquals("Wrong scheduled studySegment", -22, (int) loaded.getScheduledStudySegment().getId());
        assertEquals("Wrong activity id", -100, (int) loaded.getActivity().getId());
        assertDayOfDate("Wrong ideal date", 2006, Calendar.OCTOBER, 31, loaded.getIdealDate());
        assertEquals("Wrong notes", "Boo!", loaded.getNotes());

        assertEquals("Wrong current reason", "Success", loaded.getCurrentState().getReason());
        assertEquals("Wrong current mode", ScheduledActivityMode.OCCURRED, loaded.getCurrentState().getMode());

        assertEquals("Wrong number of previous states", 3, loaded.getPreviousStates().size());

        assertEquals("Wrong details", "Nice Details!!", loaded.getDetails());
        assertEquals("Wrong number of labels", 2, loaded.getLabels().size());
        assertEquals("Wrong first label", "clean-only", loaded.getLabels().first());
        assertEquals("Wrong second label", "soc", loaded.getLabels().last());
    }

    public void testGetEventsByIdealDate() throws Exception {
        String year = "2006";
        String month = "11";
        String day = "01";

        Date date = DateTools.createDate(
        Integer.parseInt(year),
        Integer.parseInt(month) - 1, // The Calendar month constants start with 0
        Integer.parseInt(day));

        ScheduledCalendar calendar = scheduledCalendarDao.getById(-21);
        Collection<ScheduledActivity> activities = scheduledActivityDao.getEventsByDate(calendar, date, date);

        assertEquals("Wrong amount of activities ", 2, activities.size());
    }

    public void testGetScheduledActivitiesFromPlannedActivity() throws Exception {
        Collection<ScheduledActivity> matches = getDao().getEventsFromPlannedActivity(
            plannedActivityDao.getById(-6), scheduledCalendarDao.getById(-21));
        assertEquals("Wrong number of matches", 2, matches.size());
        Collection<Integer> actualIds = DomainObjectTools.collectIds(matches);
        assertContains(actualIds, -30);
        assertContains(actualIds, -31);
    }

    public void testLoadedAndQueriedScheduledActivitiesFromSameSessionAreSame() throws Exception {
        ScheduledActivity loaded = getDao().getById(-30);
        loaded.setDetails("F9");
        ScheduledActivity queried = null;

        for (ScheduledActivity e : getDao().getEventsFromPlannedActivity(plannedActivityDao.getById(-6), scheduledCalendarDao.getById(-21))) {
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
            -15, -16, -17, -18);
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
        assertEquals("Wrong number of matched events: " + actualIds, expectedIds.length, actualIds.size());
        for (int expectedId : expectedIds) {
            assertContains("Missing event", actualIds, expectedId);
        }
    }

    public void testSaveUpdatesLabels() throws Exception {
        {
            ScheduledActivity loaded = getDao().getById(-10);
            loaded.getLabels().remove("soc");
            loaded.getLabels().add("research");
            getDao().save(loaded);
        }

        interruptSession();

        ScheduledActivity reloaded = getDao().getById(-10);
        assertEquals("Wrong number of labels", 2, reloaded.getLabels().size());
        assertEquals("Wrong first label", "clean-only", reloaded.getLabels().first());
        assertEquals("Wrong second label", "research", reloaded.getLabels().last());
    }

    public void testSaveUpdatesCurrentStateModeOnly() throws Exception {
        {
            ScheduledActivity loaded = getDao().getById(-10);
            loaded.getCurrentState().setMode(ScheduledActivityMode.CANCELED);
            getDao().save(loaded);
        }

        interruptSession();

        ScheduledActivity reloaded = getDao().getById(-10);
        assertEquals("Wrong mode value", ScheduledActivityMode.CANCELED, reloaded.getCurrentState().getMode());
    }

    public void testSaveUpdatesCurrentStateModeAndReason() throws Exception {
        {
            ScheduledActivity loaded = getDao().getById(-10);
            loaded.getCurrentState().setMode(ScheduledActivityMode.CANCELED);
            loaded.getCurrentState().setReason("Changing Mode");
            getDao().save(loaded);
        }

        interruptSession();

        ScheduledActivity reloaded = getDao().getById(-10);
        assertEquals("Wrong mode value", ScheduledActivityMode.CANCELED, reloaded.getCurrentState().getMode());
        assertEquals("Wrong reason", "Changing Mode", reloaded.getCurrentState().getReason());
    }
}
