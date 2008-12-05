package edu.northwestern.bioinformatics.studycalendar.web.subject;

import edu.northwestern.bioinformatics.studycalendar.test.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import static gov.nih.nci.cabig.ctms.lang.DateTools.createDate;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class ScheduleDayTest extends StudyCalendarTestCase {
    private static final Date TODAY = new Date();

    private ScheduleDay day = new ScheduleDay(TODAY, TODAY);

    public void testEmptyWhenNoActivitiesAtAll() throws Exception {
        assertTrue(day.isEmpty());
    }

    public void testNotEmptyWhenHasAnActivity() throws Exception {
        day.getActivities().add(Fixtures.createScheduledActivity("A", 2008, Calendar.JANUARY, 4));
        assertFalse(day.isEmpty());
    }

    public void testNotEmptyWhenHasHiddenActivities() throws Exception {
        day.setHasHiddenActivities(true);
        assertFalse(day.isEmpty());
    }
    
    public void testNaturalOrderIsByDate() throws Exception {
        ScheduleDay day1 = createDay(2008, Calendar.SEPTEMBER, 1);
        ScheduleDay day2 = createDay(2008, Calendar.SEPTEMBER, 2);

        assertNegative(day1.compareTo(day2));
        assertPositive(day2.compareTo(day1));
    }

    public void testDetailTimelineClassesAlwaysIncludesDay() throws Exception {
        assertContains(day.getDetailTimelineClasses(), "day");
    }

    public void testDetailTimelineClassesIncludesDateClass() throws Exception {
        assertContains(createDay(2005, Calendar.MARCH, 30).getDetailTimelineClasses(), "date-2005-03-30");
    }

    public void testDetailClassesIncludesMonthStartWhenAtStartOfMonth() throws Exception {
        assertContains(createDay(2008, Calendar.SEPTEMBER, 1).getDetailTimelineClasses(), "month-start");
    }

    public void testDetailClassesDoesNotIncludeMonthStartWhenNotAtStartOfMonth() throws Exception {
        assertNotContains(createDay(2008, Calendar.SEPTEMBER, 2).getDetailTimelineClasses(), "month-start");
    }

    public void testDetailClassesIncludesYearStartWhenAtStartOfYear() throws Exception {
        assertContains(createDay(2008, Calendar.JANUARY, 1).getDetailTimelineClasses(), "year-start");
    }

    public void testDetailClassesDoesNotIncludeYearStartWhenNotAtStartOfYear() throws Exception {
        assertNotContains(createDay(2008, Calendar.DECEMBER, 31).getDetailTimelineClasses(), "year-start");
    }

    public void testDetailClassesIncludesTodayWhenToday() throws Exception {
        assertContains(day.getDetailTimelineClasses(), "today");
    }

    public void testDetailClassesDoesNotIncludeTodayWhenNotToday() throws Exception {
        assertNotContains(createDay(2007, Calendar.DECEMBER, 31).getDetailTimelineClasses(), "today");
    }

    public void testDetailClassesIncludesHasActivitiesWhenHas() throws Exception {
        day.getActivities().add(new ScheduledActivity());
        assertContains(day.getDetailTimelineClasses(), "has-activities");
    }

    public void testDetailClassesDoesNotIncludeHasActivitiesWhenDoesNotHave() throws Exception {
        assertNotContains(day.getDetailTimelineClasses(), "has-activities");
    }

    public void testIsTodayWhenItIs() throws Exception {
        assertTrue(day.isToday());
    }

    public void testIsTodayWhenItIsWithTimestamp() throws Exception {
        Calendar laterToday = Calendar.getInstance();
        laterToday.setTime(TODAY);
        laterToday.add(Calendar.MINUTE, 13);
        assertTrue(new ScheduleDay(laterToday.getTime(), TODAY).isToday());
    }

    public void testIsTodayWhenItIsNot() throws Exception {
        assertFalse(createDay(2006, Calendar.MARCH, 6).isToday());
    }

    private static ScheduleDay createDay(int year, int month, int day) {
        return new ScheduleDay(createDate(year, month, day), TODAY);
    }
}
