package edu.northwestern.bioinformatics.studycalendar.web.subject;

import edu.northwestern.bioinformatics.studycalendar.test.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import gov.nih.nci.cabig.ctms.lang.DateTools;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class ScheduleDayTest extends StudyCalendarTestCase {
    public void testEmptyWhenNoActivities() throws Exception {
        assertTrue(new ScheduleDay(new Date()).isEmpty());
    }

    public void testNotEmptyWhenHasAnActivity() throws Exception {
        ScheduleDay day = new ScheduleDay(new Date());
        day.getActivities().add(Fixtures.createScheduledActivity("A", 2008, Calendar.JANUARY, 4));
        assertFalse(day.isEmpty());
    }
    
    public void testNaturalOrderIsByDate() throws Exception {
        ScheduleDay day1 = new ScheduleDay(DateTools.createDate(2008, Calendar.SEPTEMBER, 1));
        ScheduleDay day2 = new ScheduleDay(DateTools.createDate(2008, Calendar.SEPTEMBER, 2));

        assertNegative(day1.compareTo(day2));
        assertPositive(day2.compareTo(day1));
    }
}
