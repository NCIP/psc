package edu.northwestern.bioinformatics.studycalendar.web.dashboard.sitecoordinator;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.StudySiteService;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import gov.nih.nci.security.AuthorizationManager;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.AuthorizationScopeMappings.createSuiteRoleMembership;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory.createPscUser;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_TEAM_ADMINISTRATOR;
import static java.util.Arrays.asList;
import static org.easymock.EasyMock.expect;

public class AssignSubjectToSubjectCoordinatorByUserControllerTest extends ControllerTestCase {
    private AssignSubjectToSubjectCoordinatorByUserController controller;
    private UserDao userDao;
    private StudySiteService studySiteService;
    private PscUser subjectCoord0, subjectCoord1, subjectCoord2, siteCoord0;
    private Site site0, site1;
    private Study study0, study1;
    private Subject subject0, subject1, subject2, subject3, subjectUnassigned;
    private StudySite studySite00, studySite01, studySite10, studySite02;
    private Site site2;
    private StudyDao studyDao;
    private SiteDao siteDao;
    private UserService userService;
    private SubjectDao subjectDao;
    private AuthorizationManager authorizationManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        userDao = registerDaoMockFor(UserDao.class);
        studySiteService = registerMockFor(StudySiteService.class);
        userService = registerMockFor(UserService.class);
        studyDao = registerMockFor(StudyDao.class);
        siteDao = registerMockFor(SiteDao.class);
        subjectDao = registerDaoMockFor(SubjectDao.class);
        authorizationManager = registerMockFor(AuthorizationManager.class);

        controller = new AssignSubjectToSubjectCoordinatorByUserController();
        controller.setUserDao(userDao);
        controller.setStudySiteService(studySiteService);
        controller.setUserService(userService);
        controller.setStudyDao(studyDao);
        controller.setSiteDao(siteDao);
        controller.setSubjectDao(subjectDao);

        study0 = createNamedInstance("Study A", Study.class);
        study1 = createNamedInstance("Study B", Study.class);

        site0 = createNamedInstance("Northwestern", Site.class);
        site1 = createNamedInstance("Mayo", Site.class);
        site2 = createNamedInstance("Empty Site", Site.class);

        studySite00 = createStudySite(study0, site0);
        studySite01 = createStudySite(study0, site1);
        studySite10 = createStudySite(study1, site0);
        studySite02 = createStudySite(study0, site2);

        subjectCoord0 = createPscUser("Subject Coordinator 0", 0L,
            createSuiteRoleMembership(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).
                forSites(site0, site1).forStudies(study0, study1));
        subjectCoord1 = createPscUser("Subject Coordinator 1", 1L,
            createSuiteRoleMembership(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).
                forSites(site0, site1).forStudies(study0));
        subjectCoord2 = createPscUser("Subject Coordinator 2", 2L,
            createSuiteRoleMembership(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).forSites(site1));

        siteCoord0 = createPscUser("Site Coordinator 0", 2L,
            createSuiteRoleMembership(PscRole.STUDY_TEAM_ADMINISTRATOR).forSites(site0, site1),
            createSuiteRoleMembership(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER).
                forSites(site0, site1, site2).forStudies(study0));

        subject0 = createSubject("John" , "Smith");
        subject1 = createSubject("Steve", "Smith");
        subject2 = createSubject("Bob"  , "Smith");
        subject3 = createSubject("Fred" , "Smith");
        subjectUnassigned = createSubject("Unique", "Smith");

        createStudySubjectAssignment(subject0, studySite00, subjectCoord0);
        createStudySubjectAssignment(subject1, studySite01, subjectCoord0);
        createStudySubjectAssignment(subject2, studySite10, subjectCoord0);
        createStudySubjectAssignment(subject3, studySite10, subjectCoord0);
        createStudySubjectAssignment(subjectUnassigned, studySite02, null);
    }
    
    public void testAuthorizedRoles() {
        Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, null);
        assertRolesAllowed(actualAuthorizations, STUDY_TEAM_ADMINISTRATOR);
    }

    public void testBuildDisplayMap() throws Exception {
        replayMocks();

        Map<Site, Map<Study, List<Subject>>> actualDisplayMap = controller.buildDisplayMap(subjectCoord0, false);
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
        assertEquals("Wrong number of subjects", 1, actualDisplayMap.get(site0).get(study0).size());
        assertEquals("Wrong number of subjects", 2, actualDisplayMap.get(site0).get(study1).size());
        assertEquals("Wrong number of subjects", 1, actualDisplayMap.get(site1).get(study0).size());

        assertEquals("Wrong subject", subject0.getFullName(), actualDisplayMap.get(site0).get(study0).get(0).getFullName());
        assertEquals("Wrong subject", subject1.getFullName(), actualDisplayMap.get(site1).get(study0).get(0).getFullName());
        assertEquals("Wrong subject", subject2.getFullName(), actualDisplayMap.get(site0).get(study1).get(0).getFullName());
        assertEquals("Wrong subject", subject3.getFullName(), actualDisplayMap.get(site0).get(study1).get(1).getFullName());
    }

    public void testBuildDisplayMapForUnAssigned() throws Exception {
        replayMocks();
        Map<Site, Map<Study, List<Subject>>> actualDisplayMap = controller.buildDisplayMap(siteCoord0, true);
        verifyMocks();

        assertEquals("Wrong number of sites", asList(site2).size(), actualDisplayMap.keySet().size());

        Iterator<Site> actualSitesIter = actualDisplayMap.keySet().iterator();
        assertEquals("Wrong site", site2.getName(), actualSitesIter.next().getName());
        assertEquals("Wrong number of studies", 1, actualDisplayMap.get(site2).keySet().size());

        Iterator<Study> actualStudyIter0 = actualDisplayMap.get(site2).keySet().iterator();
        assertEquals("Wrong Study", study0.getName(), actualStudyIter0.next().getName());
        assertNull("Should be null", actualDisplayMap.get(site2).get(study1));
        assertEquals("Wrong number of subjects", 1, actualDisplayMap.get(site2).get(study0).size());
        assertEquals("Wrong subject", subjectUnassigned.getFullName(), actualDisplayMap.get(site2).get(study0).get(0).getFullName());
    }

    public void testBuildSubjectsForUnassigned() throws Exception {
        List<Subject> subjects = controller.buildSubjects(studySite02, siteCoord0, true);
        assertEquals("Wrong number of subjects", 1, subjects.size());
        assertEquals("Wrong subject", subjectUnassigned.getFullName(), subjects.get(0).getFullName());
    }

    public void testBuildSubjectsForAssigned() throws Exception {
        List<Subject> subjects = controller.buildSubjects(studySite10, subjectCoord0, false);
        assertEquals("Wrong number of subjects", 2, subjects.size());
        assertEquals("Wrong subject", subject2.getFullName(), subjects.get(0).getFullName());
        assertEquals("Wrong subject", subject3.getFullName(), subjects.get(1).getFullName());
    }

    public void testBuildDisplayMapfWithNoSubjectsAssigned() throws Exception {
        replayMocks();
        Map<Site, Map<Study, List<Subject>>> actualDisplayMap = controller.buildDisplayMap(subjectCoord1, true);
        verifyMocks();
        assertEquals("Wrong number of sites", 0, actualDisplayMap.keySet().size());
    }

    public void testBuildDisplayMapSubjectNotFound() throws Exception {
        replayMocks();
        Map<Site, Map<Study, List<Subject>>> actualDisplayMap = controller.buildDisplayMap(null, false);
        verifyMocks();
        assertEquals("Wrong number of sites", 0, actualDisplayMap.keySet().size());
    }

    public void testBuildStudySiteSubjectCoordinatorMap() throws Exception {
        replayMocks();

        Map<Study, Map<Site, List<User>>> map = controller.buildStudySiteSubjectCoordinatorMap(subjectCoord0);
        verifyMocks();

        assertEquals("Wrong number of subject coordinators", 1, map.get(studySite00.getStudy()).get(studySite00.getSite()).size());
        assertEquals("Wrong number of subject coordinators", 2, map.get(studySite01.getStudy()).get(studySite01.getSite()).size());

        assertEquals("Wrong subject coordinator", subjectCoord1.getName(), map.get(studySite00.getStudy()).get(studySite00.getSite()).get(0).getName());

        assertEquals("Wrong subject coordinator", subjectCoord1.getName(), map.get(studySite01.getStudy()).get(studySite01.getSite()).get(0).getName());
        assertEquals("Wrong subject coordinator", subjectCoord2.getName(), map.get(studySite01.getStudy()).get(studySite01.getSite()).get(1).getName());
    }

    public void testStudySiteSubjectCoordinatorMapForUnassgined() throws Exception {
        throw new UnsupportedOperationException("TODO");
        /*
        List<PscUser> usersAvailable = new ArrayList<PscUser>();
        usersAvailable.add(subjectCoord0);
        usersAvailable.add(subjectCoord1);
        usersAvailable.add(subjectCoord2);
        usersAvailable.add(siteCoord0);

        expect(userService.getSiteCoordinatorsAssignableUsers(siteCoord0)).andReturn(usersAvailable).anyTimes();
        expect(studySiteService.getAllStudySitesForSubjectCoordinator(siteCoord0)).andReturn(getStudySitesForUser(siteCoord0));
        expect(studySiteService.getAllStudySitesForSubjectCoordinator(subjectCoord0)).andReturn(getStudySitesForUser(subjectCoord0));
        expect(studySiteService.getAllStudySitesForSubjectCoordinator(subjectCoord1)).andReturn(getStudySitesForUser(subjectCoord1));
        expect(studySiteService.getAllStudySitesForSubjectCoordinator(subjectCoord2)).andReturn(getStudySitesForUser(subjectCoord2));
        replayMocks();
        Map<Study, Map<Site, List<User>>> mapOfUsersToMonitorStudySite = controller.buildStudySiteSubjectCoordinatorMapForUnassigned(siteCoord0);
        verifyMocks();

        assertEquals("Wrong number of studies", 1, mapOfUsersToMonitorStudySite.keySet().size());
        assertEquals("Wrong study name", study0.getName(), mapOfUsersToMonitorStudySite.keySet().iterator().next().getName());
        assertEquals("Wrong number of sites", 1, mapOfUsersToMonitorStudySite.get(study0).keySet().size());
        assertEquals("Wrong site name", site2.getName(), mapOfUsersToMonitorStudySite.get(study0).keySet().iterator().next().getName());


        assertEquals("Wrong number of coordinators", 1, mapOfUsersToMonitorStudySite.get(study0).get(site2).size());
        assertEquals("Wrong assigned coordinator", siteCoord0.getName(), mapOfUsersToMonitorStudySite.get(study0).get(site2).get(0).getName());
        */
    }

    public void testStudySiteSubjectCoordinatorMap() throws Exception {
        replayMocks();
        Map<Study, Map<Site, List<User>>> mapOfUsersToMonitorStudySite = controller.buildStudySiteSubjectCoordinatorMap(subjectCoord1);
        verifyMocks();

        assertEquals("Wrong number of studies", 1, mapOfUsersToMonitorStudySite.keySet().size());
        assertEquals("Wrong study name", study0.getName(), mapOfUsersToMonitorStudySite.keySet().iterator().next().getName());
        assertEquals("Wrong number of sites", 2, mapOfUsersToMonitorStudySite.get(study0).keySet().size());
        Iterator it = mapOfUsersToMonitorStudySite.get(study0).keySet().iterator();
        assertEquals("Wrong first site name", site0.getName(), ((Site)it.next()).getName());
        assertEquals("Wrong second site name", site1.getName(), ((Site)it.next()).getName());

        assertEquals("Wrong number of coordinators for first studySite", 1, mapOfUsersToMonitorStudySite.get(study0).get(site0).size());
        assertEquals("Wrong assigned coordinator for the first studySite", subjectCoord0.getName(), mapOfUsersToMonitorStudySite.get(study0).get(site0).get(0).getName());

        assertEquals("Wrong number of coordinators for the second studySite", 2, mapOfUsersToMonitorStudySite.get(study0).get(site1).size());
        assertEquals("Wrong assigned coordinator for the second studySite", subjectCoord0.getName(), mapOfUsersToMonitorStudySite.get(study0).get(site1).get(0).getName());
        assertEquals("Wrong assigned coordinator for the second studySite", subjectCoord2.getName(), mapOfUsersToMonitorStudySite.get(study0).get(site1).get(1).getName());
    }

    public void testInitBinder() throws Exception {
        TestInitBinderController tibController = new TestInitBinderController();
        request.addParameter("study", "1");
        request.addParameter("site", "2");
        request.addParameter("subjects", "3");
        request.addParameter("subjects", "4");
        request.addParameter("subjectCoordinator", "5");
        request.setMethod("POST");

        expect(studyDao.getById(1)).andReturn(study1);
        expect(siteDao.getById(2)).andReturn(site2);
        expect(subjectDao.getById(3)).andReturn(subject3);
        expect(subjectDao.getById(4)).andReturn(subject0);
        expect(authorizationManager.getUserById("5")).andReturn(subjectCoord0.getCsmUser());
        replayMocks();

        tibController.handleRequest(request, response);

        verifyMocks();
    }

    private List<StudySite> getStudySitesForUser(User user) {
        return user.getUserRole(Role.SUBJECT_COORDINATOR).getStudySites();
    }

    private StudySubjectAssignment createStudySubjectAssignment(Subject subject, StudySite studySite, PscUser manager) {
        StudySubjectAssignment assignment = new StudySubjectAssignment();
        assignment.setSubject(subject);
        assignment.setStudySubjectCalendarManager(manager.getCsmUser());
        subject.addAssignment(assignment);
        assignment.setStudySite(studySite);
        if (studySite.getStudySubjectAssignments() == null) {
            studySite.setStudySubjectAssignments(new ArrayList<StudySubjectAssignment>());
        }
        studySite.getStudySubjectAssignments().add(assignment);
        return assignment;
    }

   public class TestInitBinderController extends AssignSubjectToSubjectCoordinatorByUserController {
       public TestInitBinderController() {
           super();
           this.setUserDao(userDao);
           this.setStudySiteService(studySiteService);
           this.setUserService(userService);
           this.setStudyDao(studyDao);
           this.setSiteDao(siteDao);
           this.setSubjectDao(subjectDao);
       }

       @Override
       protected ModelAndView onSubmit(Object o) throws Exception {
           return new ModelAndView();
       }
   }
}
