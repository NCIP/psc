package edu.northwestern.bioinformatics.studycalendar.web;

import junit.framework.TestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.web.NewParticipantCommand;

import java.util.Date;
import java.util.List;
import java.util.Arrays;

/**
 * @author Padmaja Vedula
 */
public class NewParticipantCommandTest extends TestCase {
    private NewParticipantCommand command = new NewParticipantCommand();

    public void testCreateParticipant() throws Exception {
        String expectedLastName = "Scott";
        command.setFirstName("Tiger");
        command.setLastName(expectedLastName);
        command.setGender("Male");
        command.setDateOfBirth(new Date());
        command.setPersonId("123-45-5678");

        Participant participant = command.createParticipant();
        assertEquals(expectedLastName, participant.getLastName());
        assertEquals("should not have any assigments", 0, participant.getStudyParticipantAssignments().size());
    }
}
