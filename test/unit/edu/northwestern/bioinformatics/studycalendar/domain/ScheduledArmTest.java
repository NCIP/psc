package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.nwu.bioinformatics.commons.DateUtils;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Canceled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Occurred;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Scheduled;

import java.util.List;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Iterator;

/**
 * @author Rhett Sutphin
 */
public class ScheduledArmTest extends StudyCalendarTestCase {
    private ScheduledCalendar scheduledCalendar = new ScheduledCalendar();
    private ScheduledArm scheduledArm = new ScheduledArm();

    protected void setUp() throws Exception {
        super.setUp();
        scheduledCalendar.addArm(scheduledArm);
    }

    public void testNameWithMultiArmEpoch() throws Exception {
        Epoch multi = Epoch.create("Treatment", "A", "B", "C");
        scheduledArm.setArm(multi.getArms().get(1));
        assertName("Treatment: B");
    }

    public void testNameWithZeroArmEpoch() throws Exception {
        Epoch single = Epoch.create("Screening");
        scheduledArm.setArm(single.getArms().get(0));
        assertName("Screening");
    }

    public void testNameWhenRepeated() throws Exception {
        Epoch epoch = Epoch.create("Treatment", "A", "B", "C");
        scheduledCalendar.getScheduledArms().clear();
        scheduledCalendar.addArm(createScheduledArm(epoch.getArms().get(1)));
        scheduledCalendar.addArm(createScheduledArm(epoch.getArms().get(0)));
        scheduledCalendar.addArm(scheduledArm);
        scheduledArm.setArm(epoch.getArms().get(1));

        List<ScheduledArm> arms = scheduledCalendar.getScheduledArms();
        assertName("Treatment: B (1)", arms.get(0));
        assertName("Treatment: A", arms.get(1));
        assertName("Treatment: B (2)", arms.get(2));
    }

    private void assertName(String expectedName) {
        assertName(expectedName, this.scheduledArm);
    }

    private static void assertName(String expectedName, ScheduledArm scheduledArm) {
        assertEquals("Wrong name", expectedName, scheduledArm.getName());
    }

    public void testEventsByDay() throws Exception {
        scheduledArm.addEvent(createScheduledEvent("One", 2006, Calendar.SEPTEMBER, 20));
        scheduledArm.addEvent(createScheduledEvent("Two", 2006, Calendar.SEPTEMBER, 20));
        scheduledArm.addEvent(createScheduledEvent("Three", 2006, Calendar.SEPTEMBER, 18));
        scheduledArm.addEvent(createScheduledEvent("Two", 2006, Calendar.SEPTEMBER, 24));

        Map<Date, List<ScheduledEvent>> byDate = scheduledArm.getEventsByDate();
        assertEquals(3, byDate.size());
        Iterator<Map.Entry<Date, List<ScheduledEvent>>> entries = byDate.entrySet().iterator();

        assertTrue(entries.hasNext());
        assertEventDayRecord(entries.next(), 2006, Calendar.SEPTEMBER, 18, "Three");
        assertTrue(entries.hasNext());
        assertEventDayRecord(entries.next(), 2006, Calendar.SEPTEMBER, 20, "One", "Two");
        assertTrue(entries.hasNext());
        assertEventDayRecord(entries.next(), 2006, Calendar.SEPTEMBER, 24, "Two");
        assertFalse(entries.hasNext());
    }

    private void assertEventDayRecord(
        Map.Entry<Date, List<ScheduledEvent>> actual, int year, int month, int day, String... expectedActivities
    ) {
        assertDayOfDate("Wrong key", year, month, day, actual.getKey());
        List<ScheduledEvent> actualEvents = actual.getValue();
        assertEquals("Wrong number of activities", expectedActivities.length, actualEvents.size());
        for (int i = 0; i < actualEvents.size(); i++) {
            ScheduledEvent actualEvent = actualEvents.get(i);
            assertEquals("Event mismatch at " + i, expectedActivities[i], actualEvent.getActivity().getName());
        }
    }

    public void testGetStartDateWithNoEvents() throws Exception {
        assertNull(scheduledArm.getStartDate());
    }

    public void testGetStartDate() throws Exception {
        Arm arm = Epoch.create("Screening").getArms().get(0);
        Period period = createPeriod("P1", 4, 7, 3);
        PlannedEvent plannedEvent = createPlannedEvent("ABC", 4);
        period.addPlannedEvent(plannedEvent);
        arm.addPeriod(period);

        scheduledArm.setArm(arm);
        ScheduledEvent event = new ScheduledEvent();
        event.setPlannedEvent(plannedEvent);
        event.setIdealDate(DateUtils.createDate(2004, Calendar.JANUARY, 4));
        scheduledArm.addEvent(event);

        assertDayOfDate(2004, Calendar.JANUARY, 1, scheduledArm.getStartDate());
    }
    
    public void testGetNextArmPerProtocolStartDate() throws Exception {
        Arm arm = Epoch.create("Screening").getArms().get(0);
        Period period = createPeriod("P1", 4, 7, 3);
        PlannedEvent plannedEvent = createPlannedEvent("ABC", 4);
        period.addPlannedEvent(plannedEvent);
        arm.addPeriod(period);

        scheduledArm.setArm(arm);
        ScheduledEvent event = new ScheduledEvent();
        event.setPlannedEvent(plannedEvent);
        event.setIdealDate(DateUtils.createDate(2004, Calendar.JANUARY, 4));
        scheduledArm.addEvent(event);

        assertDayOfDate(2004, Calendar.JANUARY, 22, scheduledArm.getNextArmPerProtocolStartDate());
    }

    public void testIsNotCompleteIfAnyEventInScheduledState() throws Exception {
        scheduledArm.addEvent(createScheduledEvent("ABC", 2005, Calendar.OCTOBER, 4, new Canceled()));
        scheduledArm.addEvent(createScheduledEvent("ABC", 2005, Calendar.OCTOBER, 9, new Occurred()));
        scheduledArm.addEvent(createScheduledEvent("ABC", 2005, Calendar.OCTOBER, 18));

        assertFalse(scheduledArm.isComplete());
    }

    public void testIsCompleteIfAllEventsAreCanceled() throws Exception {
        scheduledArm.addEvent(createScheduledEvent("ABC", 2005, Calendar.OCTOBER, 4, new Canceled()));
        scheduledArm.addEvent(createScheduledEvent("ABC", 2005, Calendar.OCTOBER, 5, new Canceled()));
        scheduledArm.addEvent(createScheduledEvent("ABC", 2005, Calendar.OCTOBER, 6, new Canceled()));

        assertTrue(scheduledArm.isComplete());
    }

    public void testIsCompleteIfAllEventsAreOccurred() throws Exception {
        scheduledArm.addEvent(createScheduledEvent("ABC", 2005, Calendar.OCTOBER, 4, new Occurred()));
        scheduledArm.addEvent(createScheduledEvent("ABC", 2005, Calendar.OCTOBER, 5, new Occurred()));
        scheduledArm.addEvent(createScheduledEvent("ABC", 2005, Calendar.OCTOBER, 6, new Occurred()));

        assertTrue(scheduledArm.isComplete());
    }

    public void testIsCompleteIfNoEventsAreScheduled() throws Exception {
        scheduledArm.addEvent(createScheduledEvent("ABC", 2005, Calendar.OCTOBER, 4, new Occurred()));
        scheduledArm.addEvent(createScheduledEvent("ABC", 2005, Calendar.OCTOBER, 5, new Canceled()));
        scheduledArm.addEvent(createScheduledEvent("ABC", 2005, Calendar.OCTOBER, 6, new Occurred()));

        assertTrue(scheduledArm.isComplete());
    }

    public void testGetNextScheduledDate() {
        Calendar now = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();


        calendar.add(Calendar.MONTH, -1);
        scheduledArm.addEvent(createScheduledEvent("ABC", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 4, new Scheduled()));
        calendar.add(Calendar.MONTH, 2);
        scheduledArm.addEvent(createScheduledEvent("DEF", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 4, new Occurred()));
        calendar.add(Calendar.MONTH, 1);
        scheduledArm.addEvent(createScheduledEvent("GHI", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 4, new Scheduled()));
        calendar.add(Calendar.MONTH, 1);
        scheduledArm.addEvent(createScheduledEvent("JKL", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 4, new Scheduled()));

        Date d = scheduledArm.getNextScheduledEvent(now.getTime()).getActualDate();

        assertTrue(scheduledArm.getEvents().get(2).getActualDate() == d);
    }
}
