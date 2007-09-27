package edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate;

import edu.nwu.bioinformatics.commons.DateUtils;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

import java.util.Calendar;

/**
 * @author Rhett Sutphin
 */
public class OccurredTest extends StudyCalendarTestCase {
    public void testSummary() throws Exception {
        assertEquals("Occurred on 9/22/2004 - Reason",
            new Occurred("Reason", DateUtils.createDate(2004, Calendar.SEPTEMBER, 22)).getTextSummary());
    }

    public void testSummaryWithNoReason() throws Exception {
        assertEquals("Occurred on 11/2/2004",
            new Occurred(null, DateUtils.createDate(2004, Calendar.NOVEMBER, 2)).getTextSummary());
    }

    public void testConditionalOccurredAvailableStates() throws Exception {
        Occurred event = new Occurred("Reason",  DateUtils.createDate(2004, Calendar.NOVEMBER, 2));
        assertEquals("Wrong number of available states", 2, event.getAvailableStates(true).size());
    }

   public void testRegularOccurredAvailableStates() throws Exception {
        Occurred event = new Occurred("Reason",  DateUtils.createDate(2004, Calendar.NOVEMBER, 2));
        assertEquals("Wrong number of available states", 2, event.getAvailableStates(false).size());
    }
}
