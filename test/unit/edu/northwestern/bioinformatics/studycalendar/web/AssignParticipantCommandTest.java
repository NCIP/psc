package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.ParticipantDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.service.ParticipantService;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static org.easymock.classextension.EasyMock.expect;

import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class AssignParticipantCommandTest extends StudyCalendarTestCase {
    private AssignParticipantCommand command;
    private ParticipantDao participantDao;
    private ParticipantService participantService;
    private StudySiteDao studySiteDao;

    protected void setUp() throws Exception {
        super.setUp();

        participantDao = registerMockFor(ParticipantDao.class);
        studySiteDao = registerMockFor(StudySiteDao.class);
        participantService = registerMockFor(ParticipantService.class);

        command = new AssignParticipantCommand();
        command.setParticipantDao(participantDao);
        command.setParticipantService(participantService);
        command.setStudySiteDao(studySiteDao);
    }

    public void testAssignParticipant() throws Exception {
        int participantId = 11;
        int studySiteId = 14;

        command.setParticipantId(participantId);
        command.setStudySiteId(studySiteId);
        command.setStartDateEpoch(new Date());

        Participant participant = setId(participantId, createParticipant("Fred", "Jones"));
        StudySite studySite = setId(studySiteId, createStudySite(null, null));

        expect(participantDao.getById(participantId)).andReturn(participant);
        expect(studySiteDao.getById(studySiteId)).andReturn(studySite);
        participantService.assignParticipant(participant, studySite, command.getStartDateEpoch());
        replayMocks();

        command.assignParticipant();
        verifyMocks();
    }
}
