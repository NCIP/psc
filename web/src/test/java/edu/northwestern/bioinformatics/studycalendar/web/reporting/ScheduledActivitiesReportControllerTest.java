package edu.northwestern.bioinformatics.studycalendar.web.reporting;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportFilters;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.service.AuthorizationService;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import edu.nwu.bioinformatics.commons.DateUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
import static org.easymock.EasyMock.*;

/**
 * @author John Dzak
 */
public class ScheduledActivitiesReportControllerTest extends ControllerTestCase {
    private ScheduledActivitiesReportController controller;
    private ScheduledActivitiesReportCommand command;
    private ScheduledActivitiesReportFilters filters;
    private UserDao userDao;
    private ActivityTypeDao activityTypeDao;
    private List<ActivityType> activityTypes = new ArrayList<ActivityType>();
    private AuthorizationService authorizationService;
    private StudyDao studyDao;
    private String userName;
    private User user;
    private Study study;
    private Site site;
    private StudySite studySite;
    private List<Study> studies = new ArrayList<Study>();
    private List<Study> ownedStudies = new ArrayList<Study>();
    private List<StudySite> studySites= new ArrayList<StudySite>();
    private List<User> users = new ArrayList<User>();

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        userDao = registerDaoMockFor(UserDao.class);
        activityTypeDao = registerDaoMockFor(ActivityTypeDao.class);

        filters = new ScheduledActivitiesReportFilters();
        command = new ScheduledActivitiesReportCommand(filters);

        controller = new ScheduledActivitiesReportController() {
            @Override
            protected Object getCommand(HttpServletRequest request) throws Exception {
                return command;
            }
        };


        userName = "USER NAME";
        user = Fixtures.createUser(userName, Role.SUBJECT_COORDINATOR);
        users.add(user);
        SecurityContextHolderTestHelper.setSecurityContext(user, "pass");

        study = setId(100, Fixtures.createBasicTemplate());
        site = Fixtures.createSite("Site");
        studySite = Fixtures.createStudySite(study, site);
        studies.add(study);
        ownedStudies.add(study);
        studySites.add(studySite);

        studyDao = registerDaoMockFor(StudyDao.class);
        authorizationService = registerMockFor(AuthorizationService.class);
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
            Map<String,Object> model = controller.createModel(new BindException(this, StringUtils.EMPTY));
        verifyMocks();
        assertNotNull("Model should contain modes", model.get("modes"));
    }

    @SuppressWarnings({"unchecked"})
    public void testHandle() throws Exception {
        expectActivityTypeDaoCall();

        ModelAndView mv = handleRequest();
        assertEquals("Wrong view", "reporting/scheduledActivitiesReport", mv.getViewName());
    }

    public void testBindCurrentStateMode() throws Exception {
        expectActivityTypeDaoCall();
        request.setParameter("filters.currentStateMode", "1");
        ScheduledActivitiesReportCommand command = postAndReturnCommand("command.filters.currentStateMode");
        assertEquals("Wrong state", ScheduledActivityMode.SCHEDULED, command.getFilters().getCurrentStateMode());
    }

    public void testBindActualActivityStartDate() throws Exception {
        expectActivityTypeDaoCall();
        request.setParameter("filters.actualActivityDate.start", "10/25/2006");
        ScheduledActivitiesReportCommand command = postAndReturnCommand("command.filters.actualActivityDate.start");
        assertEquals("Wrong date", DateUtils.createDate(2006, Calendar.OCTOBER, 25, 0, 0, 0), command.getFilters().getActualActivityDate().getStart());
    }

    public void testBindActualActivityStopDate() throws Exception {
        expectActivityTypeDaoCall();
        request.setParameter("filters.actualActivityDate.stop", "10/25/2006");
        ScheduledActivitiesReportCommand command = postAndReturnCommand("command.filters.actualActivityDate.stop");
        assertEquals("Wrong date", DateUtils.createDate(2006, Calendar.OCTOBER, 25, 0, 0, 0), command.getFilters().getActualActivityDate().getStop());
    }

    public void testBindActivityType() throws Exception{
        ActivityType activityType = Fixtures.createActivityType("INTERVENTION");
        expect (activityTypeDao.getById(2)).andReturn(activityType).anyTimes();
        expectActivityTypeDaoCall();
        request.addParameter("filters.activityType", "2");

        ScheduledActivitiesReportCommand command = postAndReturnCommand("command.filters.activityType");
        assertEquals("Wrong type", activityType, command.getFilters().getActivityType());
    }

    public void testBindSubjectCoordinator() throws Exception {
        expectActivityTypeDaoCall();
        request.addParameter("filters.subjectCoordinator", "100");
        expectFindUser(100, setId(100, new User()));
        ScheduledActivitiesReportCommand command = postAndReturnCommand("command.filters.subjectCoordinator");
        assertEquals("Wrong user", 100, (int) command.getFilters().getSubjectCoordinator().getId());
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

    @SuppressWarnings({ "unchecked" })
    private ScheduledActivitiesReportCommand postAndReturnCommand(String expectNoErrorsForField) throws Exception {
        Map<String, Object> model = handleRequest().getModel();
        assertNoBindingErrorsFor(expectNoErrorsForField, model);
        return (ScheduledActivitiesReportCommand) model.get("command");
    }

    private void expectFindAllSubjectCoordinators() {
        expect(userDao.getAllSubjectCoordinators()).andReturn(Arrays.asList(new User()));
    }

    private void expectFindUser(int i, User u) {
        expect(userDao.getById(i)).andReturn(u);
    }
}
