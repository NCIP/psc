package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

/**
 * @author Rhett Sutphin
 */
public class ParticipantTest extends StudyCalendarTestCase {
    private Participant participant = new Participant();

    protected void setUp() throws Exception {
        super.setUp();
        participant.setFirstName("Ana");
        participant.setLastName("Ng");
    }

    public void testLastFirst() throws Exception {
        assertEquals("Ng, Ana", participant.getLastFirst());
    }

    public void testLastFirstNoFirst() throws Exception {
        participant.setFirstName(null);
        assertEquals("Ng", participant.getLastFirst());
    }

    public void testLastFirstNoLast() throws Exception {
        participant.setLastName(null);
        assertEquals("Ana", participant.getLastFirst());
    }

    public void testFullName() throws Exception {
        assertEquals("Ana Ng", participant.getFullName());
    }

    public void testFullNameNoLast() throws Exception {
        participant.setLastName(null);
        assertEquals("Ana", participant.getFullName());
    }

    public void testFullNameNoFirst() throws Exception {
        participant.setFirstName(null);
        assertEquals("Ng", participant.getFullName());
    }
}
