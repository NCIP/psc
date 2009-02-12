package edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.nwu.bioinformatics.commons.DateUtils;

import java.util.Calendar;


public class NotApplicableTest extends StudyCalendarTestCase {
    public void testSummary() throws Exception {
        assertEquals("NA on 8/22/2004 - Not Available Reason", new NotApplicable("Not Available Reason", DateUtils.createDate(2004, Calendar.AUGUST, 22)).getTextSummary());
    }

    public void testSummaryWithoutReason() throws Exception {
        assertEquals("NA on 8/22/2004", new NotApplicable(null, DateUtils.createDate(2004, Calendar.AUGUST, 22)).getTextSummary());
    }

    public void testAvailableStates() throws Exception {
        NotApplicable event = new NotApplicable("Reason",DateUtils.createDate(2004, Calendar.AUGUST, 22));
        assertEquals("Wrong number of available states", 3, event.getAvailableStates(true).size());
    }
}