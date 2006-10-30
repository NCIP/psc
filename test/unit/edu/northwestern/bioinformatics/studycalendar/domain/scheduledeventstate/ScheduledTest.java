package edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate;

import edu.nwu.bioinformatics.commons.DateUtils;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

import java.util.Calendar;

/**
 * @author Rhett Sutphin
 */
public class ScheduledTest extends StudyCalendarTestCase {
    public void testSummary() throws Exception {
        assertEquals("Scheduled for 9/22/2004 - Reason",
            new Scheduled("Reason", DateUtils.createDate(2004, Calendar.SEPTEMBER, 22)).getTextSummary());
    }

    public void testSummaryWithNoReason() throws Exception {
        assertEquals("Scheduled for 11/2/2004",
            new Scheduled(null, DateUtils.createDate(2004, Calendar.NOVEMBER, 2)).getTextSummary());
    }
}
