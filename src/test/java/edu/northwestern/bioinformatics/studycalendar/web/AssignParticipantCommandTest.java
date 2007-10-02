package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.ParticipantDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import edu.northwestern.bioinformatics.studycalendar.service.ParticipantService;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static org.easymock.classextension.EasyMock.expect;

import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class AssignParticipantCommandTest extends StudyCalendarTestCase {
    private AssignParticipantCommand command;
    private ParticipantService participantService;

    protected void setUp() throws Exception {
        super.setUp();

        participantService = registerMockFor(ParticipantService.class);

        command = new AssignParticipantCommand();
        command.setParticipantService(participantService);
    }

    public void testAssignParticipant() throws Exception {
        Participant participant = setId(11, createParticipant("Fred", "Jones"));
        StudySite studySite = setId(14, createStudySite(null, null));
        StudyParticipantAssignment assignment = new StudyParticipantAssignment();

        command.setParticipant(participant);
        command.setStudySite(studySite);
        command.setStartDate(new Date());
        command.setArm(setId(17, Fixtures.createNamedInstance("Worcestershire", Arm.class)));

        expect(participantService.assignParticipant(participant, studySite, command.getArm(), command.getStartDate(), null)).andReturn(assignment);
        replayMocks();

        assertSame(assignment, command.assignParticipant());
        verifyMocks();
    }
}
