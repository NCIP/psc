package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;

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
            assertEquals("Event mismatch at " + i, expectedActivities[i], actualEvent.getPlannedEvent().getActivity().getName());
        }
    }
}
