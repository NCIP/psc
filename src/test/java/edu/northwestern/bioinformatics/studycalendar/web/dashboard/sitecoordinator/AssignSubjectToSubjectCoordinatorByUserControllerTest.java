package edu.northwestern.bioinformatics.studycalendar.web.dashboard.sitecoordinator;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.StudySiteService;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import static org.easymock.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;

import static java.util.Arrays.asList;
import java.util.*;

public class AssignSubjectToSubjectCoordinatorByUserControllerTest extends ControllerTestCase {
    private AssignSubjectToSubjectCoordinatorByUserController controller;
    private UserDao userDao;
    private StudySiteService studySiteService;
    private User subjectCoord0, subjectCoord1, subjectCoord2;
    private List<StudySite> studySites;
    private List<Site> sites;
    private Site site0;
    private Site site1;
    private Study study0;
    private Study study1;
    private Subject subject0;
    private Subject subject1;
    private Subject subject2;
    private Subject subject3;
    private StudySite studySite0;
    private StudySite studySite1;
    private StudySite studySite2;
    private Site site2;
    private StudySite studySite3;
    private StudyDao studyDao;
    private SiteDao siteDao;
    private UserService userService;
    private SubjectDao subjectDao;

    protected void setUp() throws Exception {
        super.setUp();

        userDao = registerDaoMockFor(UserDao.class);
        studySiteService = registerMockFor(StudySiteService.class);
        userService = registerMockFor(UserService.class);
        studyDao = registerMockFor(StudyDao.class);
        siteDao = registerMockFor(SiteDao.class);
        subjectDao = registerDaoMockFor(SubjectDao.class);

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
        sites = asList(site0, site1, site2);

        studySite0 = createStudySite(study0, site0);
        studySite1 = createStudySite(study0, site1);
        studySite2 = createStudySite(study1, site0);
        studySite3 = createStudySite(study0, site2);
        studySites = asList(studySite0, studySite1, studySite2, studySite3);

        subjectCoord0 = setId(0, createNamedInstance("Subject Coordinator 0", User.class));
        subjectCoord1 = setId(1, createNamedInstance("Subject Coordinator 1", User.class));
        subjectCoord2 = setId(2, createNamedInstance("Subject Coordinator 2", User.class));

        UserRole role0 = createUserRole(subjectCoord0, Role.SUBJECT_COORDINATOR, site0, site1);
        UserRole role1 = createUserRole(subjectCoord1, Role.SUBJECT_COORDINATOR, site0, site1);
        UserRole role2 = createUserRole(subjectCoord2, Role.SUBJECT_COORDINATOR, site1);

        /* assign subjectCoord0 to studySite0, studySite1, studySite2 */
        assignStudySite(studySite0, role0);
        assignStudySite(studySite1, role0);
        assignStudySite(studySite2, role0);

        /* assign subjectCoord1 to studySite0, studySite1 */
        assignStudySite(studySite0, role1);
        assignStudySite(studySite1, role1);

        /* assign subjectCoord2 to studySite2, studySite3 */
        assignStudySite(studySite1, role2);
        assignStudySite(studySite3, role0);

        subject0 = createSubject("John" , "Smith");
        subject1 = createSubject("Steve", "Smith");
        subject2 = createSubject("Bob"  , "Smith");
        subject3 = createSubject("Fred" , "Smith");

        createStudySubjectAssignment(subject0, studySite0, subjectCoord0);
        createStudySubjectAssignment(subject1, studySite1, subjectCoord0);
        createStudySubjectAssignment(subject2, studySite2, subjectCoord0);
        createStudySubjectAssignment(subject3, studySite2, subjectCoord0);
    }

    public void testBuildDisplayMap() throws Exception {
        expect(studySiteService.getAllStudySitesForSubjectCoordinator(subjectCoord0)).andReturn(getStudySitesForUser(subjectCoord0));
        replayMocks();

        Map<Site, Map<Study, List<Subject>>> actualDisplayMap = controller.buildDisplayMap(subjectCoord0);
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

    public void testBuildDisplayMapfWithNoSubjectsAssigned() throws Exception {
        expect(studySiteService.getAllStudySitesForSubjectCoordinator(subjectCoord1)).andReturn(getStudySitesForUser(subjectCoord1));
        replayMocks();

        Map<Site, Map<Study, List<Subject>>> actualDisplayMap = controller.buildDisplayMap(subjectCoord1);
        verifyMocks();

        assertEquals("Wrong number of sites", 0, actualDisplayMap.keySet().size());
    }

    public void testBuildDisplayMapSubjectNotFound() throws Exception {
        replayMocks();

        Map<Site, Map<Study, List<Subject>>> actualDisplayMap = controller.buildDisplayMap(null);
        verifyMocks();

        assertEquals("Wrong number of sites", 0, actualDisplayMap.keySet().size());
    }

    public void testBuildStudySiteSubjectCoordinatorMap() throws Exception {
        expect(studySiteService.getAllStudySitesForSubjectCoordinator(subjectCoord0)).andReturn(getStudySitesForUser(subjectCoord0));
        replayMocks();

        Map<Study, Map<Site, List<User>>> map = controller.buildStudySiteSubjectCoordinatorMap(subjectCoord0);
        verifyMocks();

        assertEquals("Wrong number of subject coordinators", 1, map.get(studySite0.getStudy()).get(studySite0.getSite()).size());
        assertEquals("Wrong number of subject coordinators", 2, map.get(studySite1.getStudy()).get(studySite1.getSite()).size());

        assertEquals("Wrong subject coordinator", subjectCoord1.getName(), map.get(studySite0.getStudy()).get(studySite0.getSite()).get(0).getName());
                                                                       
        assertEquals("Wrong subject coordinator", subjectCoord1.getName(), map.get(studySite1.getStudy()).get(studySite1.getSite()).get(0).getName());
        assertEquals("Wrong subject coordinator", subjectCoord2.getName(), map.get(studySite1.getStudy()).get(studySite1.getSite()).get(1).getName());
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
        expect(userDao.getById(5)).andReturn(subjectCoord0);
        replayMocks();

        tibController.handleRequest(request, response);

        verifyMocks();
    }

    private List<StudySite> getStudySitesForUser(User user) {
        return user.getUserRole(Role.SUBJECT_COORDINATOR).getStudySites();
    }

    private StudySubjectAssignment createStudySubjectAssignment(Subject subject, StudySite studySite, User subjectCoordinator) {
        StudySubjectAssignment assignment = new StudySubjectAssignment();
        assignment.setSubject(subject);
        assignment.setSubjectCoordinator(subjectCoordinator);
        subject.addAssignment(assignment);
        assignment.setStudySite(studySite);
        if (studySite.getStudySubjectAssignments() == null) {
            studySite.setStudySubjectAssignments(new ArrayList());
        }
        studySite.getStudySubjectAssignments().add(assignment);
        return assignment;
    }

    private void assignStudySite(StudySite studySite, UserRole role) {
        role.addStudySite(studySite);

        List<UserRole> userRoles = studySite.getUserRoles() != null ? studySite.getUserRoles() : new ArrayList();
        userRoles.add(role);

        studySite.setUserRoles(userRoles);
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

       protected ModelAndView onSubmit(Object o) throws Exception {
           return new ModelAndView();
       }
   }
}
