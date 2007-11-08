package edu.northwestern.bioinformatics.studycalendar.web.dashboard.sitecoordinator;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createAssignment;

import static java.util.Arrays.asList;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createParticipant;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.dao.ParticipantDao;

import java.util.List;

public class AssignParticipantToParticipantCoordinatorByUserCommandTest extends StudyCalendarTestCase {
    private AssignParticipantToParticipantCoordinatorByUserCommand command;
    private Participant participant0, participant1;
    private User pcNew;
    private ParticipantDao participantDao;
    private List<Participant> participants;
    private Study study;
    private Site site;
    private User pcOld;

    protected void setUp() throws Exception {
        super.setUp();

        participantDao = registerDaoMockFor(ParticipantDao.class);

        command = new AssignParticipantToParticipantCoordinatorByUserCommand();
        command.setParticipantDao(participantDao);

        pcOld = createNamedInstance("Old Participant Coordinator", User.class);
        pcNew = createNamedInstance("New Participant Coordinator", User.class);

        participant0 = createParticipant("John", "Smith");
        participant1 = createParticipant("Jake", "Smith");
        participants = asList(participant0, participant1);

        study = createNamedInstance("Study A", Study.class);
        site = createNamedInstance("Northwestern", Site.class);

        StudyParticipantAssignment assignment0 = createAssignment(study, site, participant0);
        StudyParticipantAssignment assignment1 = createAssignment(study, site, participant1);

        assignment0.setParticipantCoordinator(pcOld);
        assignment1.setParticipantCoordinator(pcOld);

        participant0.addAssignment(assignment0);
        participant1.addAssignment(assignment1);


    }

    public void testAssignParticipantsToParticipantCoordinator() {
        command.setParticipants(participants);
        command.setParticipantCoordinator(pcNew);
        command.setStudy(study);
        command.setSite(site);

        participantDao.save(participant0);
        participantDao.save(participant1);
        replayMocks();
        
        command.assignParticipantsToParticipantCoordinator();
        verifyMocks();

        assertEquals("Wrong number of assignments", 1, participant0.getAssignments().size());
        assertEquals("Wrong number of assignments", 1, participant1.getAssignments().size());

        assertEquals("Wrong participant coordinator assigned", pcNew.getName(), participant0.getAssignments().get(0).getParticipantCoordinator().getName());
        assertEquals("Wrong participant coordinator assigned", pcNew.getName(), participant1.getAssignments().get(0).getParticipantCoordinator().getName());
    }
}
