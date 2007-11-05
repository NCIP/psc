package edu.northwestern.bioinformatics.studycalendar.web.dashboard.sitecoordinator;

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
    private User user;
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

    protected void setUp() throws Exception {
        super.setUp();

        userDao = registerDaoMockFor(UserDao.class);
        studySiteService = registerMockFor(StudySiteService.class);

        controller = new AssignParticipantToParticipantCoordinatorByUserController();
        controller.setUserDao(userDao);
        controller.setStudySiteService(studySiteService);

        user = setId(1, createNamedInstance("Participant Coordinator", User.class));

        study0 = createNamedInstance("Study A", Study.class);
        study1 = createNamedInstance("Study B", Study.class);

        site0 = createNamedInstance("Northwestern", Site.class);
        site1 = createNamedInstance("Mayo", Site.class);
        sites = asList(site0, site1);

        StudySite studySite0 = createStudySite(study0, site0);
        StudySite studySite1 = createStudySite(study0, site1);
        StudySite studySite2 = createStudySite(study1, site0);
        studySites = asList(studySite0, studySite1, studySite2);

        participant0 = createParticipant("John" , "Smith");
        participant1 = createParticipant("Steve", "Smith");
        participant2 = createParticipant("Bob"  , "Smith");
        participant3 = createParticipant("Fred" , "Smith");

        StudyParticipantAssignment assignment0 = createStudyParticipantAssignment(participant0, studySite0);
        StudyParticipantAssignment assignment1 = createStudyParticipantAssignment(participant1, studySite1);
        StudyParticipantAssignment assignment2 = createStudyParticipantAssignment(participant2, studySite2);
        StudyParticipantAssignment assignment3 = createStudyParticipantAssignment(participant3, studySite2);
    }

    public void testReferenceData() throws Exception {
        request.setMethod("GET");
        request.setParameter("selected", user.getId().toString());

        expect(userDao.getById(user.getId())).andReturn(user);
        expect(studySiteService.getAllStudySitesForParticipantCoordinator(user)).andReturn(studySites);
        replayMocks();

        Map<String, Object> refMap = controller.referenceData(request);
        Map<Site, Map<Study, List<Participant>>> actualDisplayMap = (Map<Site, Map<Study, List<Participant>>>) refMap.get("displayMap");
        verifyMocks();

        assertEquals("Wrong number of sites", sites.size(), actualDisplayMap.keySet().size());

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
}
