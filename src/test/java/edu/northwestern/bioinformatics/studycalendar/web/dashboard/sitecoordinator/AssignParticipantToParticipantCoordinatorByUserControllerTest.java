package edu.northwestern.bioinformatics.studycalendar.web.dashboard.sitecoordinator;

import static edu.northwestern.bioinformatics.studycalendar.domain.UserRole.findByRole;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.StudySiteService;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import static org.easymock.EasyMock.expect;

import static java.util.Arrays.asList;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.ArrayList;

public class AssignParticipantToParticipantCoordinatorByUserControllerTest extends ControllerTestCase {
    private AssignParticipantToParticipantCoordinatorByUserController controller;
    private UserDao userDao;
    private StudySiteService studySiteService;
    private User participantCoord0, participantCoord1, participantCoord2;
    private List<StudySite> studySites;
    private List<Site> sites;
    private Site site0;
    private Site site1;
    private Study study0;
    private Study study1;
    private Participant participant0;
    private Participant participant1;
    private Participant participant2;
    private Participant participant3;
    private StudySite studySite0;
    private StudySite studySite1;
    private StudySite studySite2;
    private Site site2;
    private StudySite studySite3;

    protected void setUp() throws Exception {
        super.setUp();

        userDao = registerDaoMockFor(UserDao.class);
        studySiteService = registerMockFor(StudySiteService.class);

        controller = new AssignParticipantToParticipantCoordinatorByUserController();
        controller.setUserDao(userDao);
        controller.setStudySiteService(studySiteService);

        study0 = createNamedInstance("Study A", Study.class);
        study1 = createNamedInstance("Study B", Study.class);

        site0 = createNamedInstance("Northwestern", Site.class);
        site1 = createNamedInstance("Mayo", Site.class);
        site2 = createNamedInstance("Empty Site", Site.class);
        sites = asList(site0, site1, site2);

        studySite0 = createStudySite(study0, site0);
        studySite1 = createStudySite(study0, site1);
        studySite2 = createStudySite(study1, site0);
        studySite3 = createStudySite(study0, site2);
        studySites = asList(studySite0, studySite1, studySite2, studySite3);

        participantCoord0 = setId(0, createNamedInstance("Participant Coordinator 0", User.class));
        participantCoord1 = setId(1, createNamedInstance("Participant Coordinator 1", User.class));
        participantCoord2 = setId(2, createNamedInstance("Participant Coordinator 2", User.class));

        UserRole role0 = createUserRole(participantCoord0, Role.PARTICIPANT_COORDINATOR, site0, site1);
        UserRole role1 = createUserRole(participantCoord1, Role.PARTICIPANT_COORDINATOR, site0, site1);
        UserRole role2 = createUserRole(participantCoord2, Role.PARTICIPANT_COORDINATOR, site1);

        assignStudySite(studySite0, role0);
        assignStudySite(studySite1, role0);
        assignStudySite(studySite2, role0);
        assignStudySite(studySite0, role1);
        assignStudySite(studySite1, role1);
        assignStudySite(studySite1, role2);
        assignStudySite(studySite3, role0);

        participant0 = createParticipant("John" , "Smith");
        participant1 = createParticipant("Steve", "Smith");
        participant2 = createParticipant("Bob"  , "Smith");
        participant3 = createParticipant("Fred" , "Smith");

        createStudyParticipantAssignment(participant0, studySite0);
        createStudyParticipantAssignment(participant1, studySite1);
        createStudyParticipantAssignment(participant2, studySite2);
        createStudyParticipantAssignment(participant3, studySite2);
    }

    public void testBuildDisplayMap() throws Exception {
        expect(userDao.getById(participantCoord0.getId())).andReturn(participantCoord0);
        expect(studySiteService.getAllStudySitesForParticipantCoordinator(participantCoord0)).andReturn(getStudySitesForUser(participantCoord0));
        replayMocks();

        Map<Site, Map<Study, List<Participant>>> actualDisplayMap = controller.buildDisplayMap(participantCoord0.getId());
        verifyMocks();

        assertEquals("Wrong number of sites", asList(site0, site1).size(), actualDisplayMap.keySet().size());

        Iterator<Site> actualSitesIter = actualDisplayMap.keySet().iterator();
        assertEquals("Wrong site", site1.getName(), actualSitesIter.next().getName());
        assertEquals("Wrong site", site0.getName(), actualSitesIter.next().getName());

        assertEquals("Wrong number of studies", 2, actualDisplayMap.get(site0).keySet().size());
        assertEquals("Wrong number of studies", 1, actualDisplayMap.get(site1).keySet().size());

        Iterator<Study> actualStudyIter0 = actualDisplayMap.get(site0).keySet().iterator();
        assertEquals("Wrong Study", study0.getName(), actualStudyIter0.next().getName());
        assertEquals("Wrong Study", study1.getName(), actualStudyIter0.next().getName());

        Iterator<Study> actualStudyIter1 = actualDisplayMap.get(site1).keySet().iterator();
        assertEquals("Wrong Study", study0.getName(), actualStudyIter1.next().getName());

        assertNull("Should be null", actualDisplayMap.get(site1).get(study1));
        assertEquals("Wrong number of participants", 1, actualDisplayMap.get(site0).get(study0).size());
        assertEquals("Wrong number of participants", 2, actualDisplayMap.get(site0).get(study1).size());
        assertEquals("Wrong number of participants", 1, actualDisplayMap.get(site1).get(study0).size());

        assertEquals("Wrong participant", participant0.getFullName(), actualDisplayMap.get(site0).get(study0).get(0).getFullName());
        assertEquals("Wrong participant", participant1.getFullName(), actualDisplayMap.get(site1).get(study0).get(0).getFullName());
        assertEquals("Wrong participant", participant2.getFullName(), actualDisplayMap.get(site0).get(study1).get(0).getFullName());
        assertEquals("Wrong participant", participant3.getFullName(), actualDisplayMap.get(site0).get(study1).get(1).getFullName());
    }

    public void testBuildDisplayMapParticipantNotFound() throws Exception {
        Integer NON_EXISTENT_ID = -1;
        
        expect(userDao.getById(NON_EXISTENT_ID)).andReturn(null);
        replayMocks();

        Map<Site, Map<Study, List<Participant>>> actualDisplayMap = controller.buildDisplayMap(NON_EXISTENT_ID);
        verifyMocks();

        assertEquals("Wrong number of sites", 0, actualDisplayMap.keySet().size());
    }

    public void testBuildStudySiteParticipantCoordinatorMap() throws Exception {
        expect(userDao.getById(participantCoord0.getId())).andReturn(participantCoord0);
        expect(studySiteService.getAllStudySitesForParticipantCoordinator(participantCoord0)).andReturn(getStudySitesForUser(participantCoord0));
        replayMocks();

        Map<Study, Map<Site, List<User>>> map = controller.buildStudySiteParticipantCoordinatorMap(participantCoord0.getId());
        verifyMocks();

        assertEquals("Wrong number of participant coordinators", 1, map.get(studySite0.getStudy()).get(studySite0.getSite()).size());
        assertEquals("Wrong number of participant coordinators", 2, map.get(studySite1.getStudy()).get(studySite1.getSite()).size());

        assertEquals("Wrong participant coordinator", participantCoord1.getName(), map.get(studySite0.getStudy()).get(studySite0.getSite()).get(0).getName());
                                                                       
        assertEquals("Wrong participant coordinator", participantCoord1.getName(), map.get(studySite1.getStudy()).get(studySite1.getSite()).get(0).getName());
        assertEquals("Wrong participant coordinator", participantCoord2.getName(), map.get(studySite1.getStudy()).get(studySite1.getSite()).get(1).getName());
    }

    private List<StudySite> getStudySitesForUser(User user) {
        return findByRole(user.getUserRoles(), Role.PARTICIPANT_COORDINATOR).getStudySites();
    }

    private StudyParticipantAssignment createStudyParticipantAssignment(Participant participant, StudySite studySite) {
        StudyParticipantAssignment assignment = new StudyParticipantAssignment();
        assignment.setParticipant(participant);
        participant.addAssignment(assignment);
        assignment.setStudySite(studySite);
        if (studySite.getStudyParticipantAssignments() == null) {
            studySite.setStudyParticipantAssignments(new ArrayList());
        }
        studySite.getStudyParticipantAssignments().add(assignment);
        return assignment;
    }

    private void assignStudySite(StudySite studySite, UserRole role) {
        role.addStudySite(studySite);

        List<UserRole> userRoles = studySite.getUserRoles() != null ? studySite.getUserRoles() : new ArrayList();
        userRoles.add(role);

        studySite.setUserRoles(userRoles);
    }
}
