package edu.northwestern.bioinformatics.studycalendar.web.reporting;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.service.AuthorizationService;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.setId;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
import static org.easymock.EasyMock.expect;

/**
 * @author John Dzak
 */
public class ScheduledActivitiesReportControllerTest extends ControllerTestCase {
    private ScheduledActivitiesReportController controller;
    private ActivityTypeDao activityTypeDao;
    private List<ActivityType> activityTypes = new ArrayList<ActivityType>();
    private AuthorizationService authorizationService;
    private User user;
    private List<Study> studies = new ArrayList<Study>();
    private List<Study> ownedStudies = new ArrayList<Study>();
    private List<User> users = new ArrayList<User>();

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        UserDao userDao = registerDaoMockFor(UserDao.class);
        activityTypeDao = registerDaoMockFor(ActivityTypeDao.class);

        String userName = "USER NAME";
        user = Fixtures.createUser(userName, Role.SUBJECT_COORDINATOR);
        users.add(user);
        SecurityContextHolderTestHelper.setSecurityContext(user, "pass");

        Study study = setId(100, Fixtures.createBasicTemplate());
        studies.add(study);
        ownedStudies.add(study);

        StudyDao studyDao = registerDaoMockFor(StudyDao.class);
        authorizationService = registerMockFor(AuthorizationService.class);

        controller = new ScheduledActivitiesReportController();
        controller.setAuthorizationService(authorizationService);
        controller.setStudyDao(studyDao);
        controller.setControllerTools(controllerTools);
        controller.setUserDao(userDao);
        controller.setActivityTypeDao(activityTypeDao);
        controller.setApplicationSecurityManager(applicationSecurityManager);

        applicationSecurityManager.setUserService(registerMockFor(UserService.class));
        expect(applicationSecurityManager.getFreshUser()).andReturn(user).anyTimes();
        expect(studyDao.getAll()).andReturn(studies);
        expect(userDao.getAllSubjectCoordinators()).andReturn(users);
        expect(authorizationService.filterStudiesForVisibility(studies, user.getUserRole(Role.SUBJECT_COORDINATOR))).andReturn(ownedStudies).anyTimes();
    }

    public void testAuthorizedRoles() {
        Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, null);
        assertRolesAllowed(actualAuthorizations,
            DATA_READER,
            STUDY_SUBJECT_CALENDAR_MANAGER,
            STUDY_TEAM_ADMINISTRATOR);
    }

    @SuppressWarnings({"unchecked"})
    public void testCreateModel() {
        expectActivityTypeDaoCall();
        replayMocks();
        Map<String,Object> model = controller.createModel(request);
        verifyMocks();
        assertNotNull("Model should contain modes", model.get("modes"));
    }

    @SuppressWarnings({"unchecked"})
    public void testHandle() throws Exception {
        expectActivityTypeDaoCall();

        ModelAndView mv = handleRequest();
        assertEquals("Wrong view", "reporting/scheduledActivitiesReport", mv.getViewName());
    }

    public void testGetMapOfColleagueUsers() throws Exception {
        String user2Name = "USER2";
        User user2 = Fixtures.createUser(user2Name, Role.SUBJECT_COORDINATOR);
        users.add(user2);
        SecurityContextHolderTestHelper.setSecurityContext(user2, "pass");

        expect(authorizationService.filterStudiesForVisibility(studies, user2.getUserRole(Role.SUBJECT_COORDINATOR))).andReturn(ownedStudies).anyTimes();

        replayMocks();
        List<User> listOfUsers = controller.getListOfColleagueUsers();
        verifyMocks();
        assertEquals("Wrong number of users ", 2, listOfUsers.size());
        assertEquals("Wrong user one", user, listOfUsers.get(0));
        assertEquals("Wrong user two", user2, listOfUsers.get(1));
    }

    ////// Helper Methods

    private ModelAndView handleRequest() throws Exception {
        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();
        return mv;
    }

    @SuppressWarnings({"unchecked"})
    private void expectActivityTypeDaoCall() {
        expect(activityTypeDao.getAll()).andReturn(activityTypes);
    }
}
