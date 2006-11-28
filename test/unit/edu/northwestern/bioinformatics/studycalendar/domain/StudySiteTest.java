package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

/**
 * @author Rhett Sutphin
 */
public class StudySiteTest extends StudyCalendarTestCase {
    private StudySite studySite;

    protected void setUp() throws Exception {
        super.setUp();
        studySite = new StudySite();
    }
    
    public void testUsedWhenUsed() throws Exception {
        StudyParticipantAssignment assignment = new StudyParticipantAssignment();
        studySite.getStudyParticipantAssignments().add(assignment);
        assertTrue(studySite.isUsed());
    }

    public void testUsedWhenNotUsed() throws Exception {
        assertFalse(studySite.isUsed());
    }
}
