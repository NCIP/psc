package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

/**
 * @author Rhett Sutphin
 */
public class StudyParticipantAssignmentTest extends StudyCalendarTestCase {
    private StudyParticipantAssignment assignment = new StudyParticipantAssignment();
    
    public void testSetCalendarMaintainsBidrectionality() throws Exception {
        ScheduledCalendar calendar = new ScheduledCalendar();
        assignment.setScheduledCalendar(calendar);
        assertSame(assignment, calendar.getAssignment());
    }
}
