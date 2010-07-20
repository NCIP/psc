package edu.northwestern.bioinformatics.studycalendar.web.dashboard.subjectcoordinator;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.setId;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.NotificationDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Role.SUBJECT_COORDINATOR;
import edu.northwestern.bioinformatics.studycalendar.service.AuthorizationService;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectCoordinatorDashboardService;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.configuration.MockConfiguration;
import edu.northwestern.bioinformatics.studycalendar.configuration.Configuration;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import edu.nwu.bioinformatics.commons.DateUtils;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_SUBJECT_CALENDAR_MANAGER;
import static org.easymock.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

public class ScheduleControllerTest extends ControllerTestCase {
    private ScheduleController controller;

    private AuthorizationService authorizationService;

    private ScheduledActivityDao scheduledActivityDao;
    private StudyDao studyDao;
    private UserDao userDao;
    private NotificationDao notificationDao;
    private ActivityTypeDao activityTypeDao;

    private User user;

    private String userName;

    private List<Study> ownedStudies = new ArrayList<Study>();
    private List<Study> studies = new ArrayList<Study>();
    private List<StudySubjectAssignment> studySubjectAssignments = new ArrayList<StudySubjectAssignment>();
    private ScheduleCommand command;

    private SubjectDao subjectDao;
    private SubjectService service;
    private SubjectCoordinatorDashboardService paService;
    private List<User> users = new ArrayList<User>();
    private List<ActivityType> activityTypes = new ArrayList<ActivityType>();
    private Study study;
    private Site site;
    private StudySite studySite;
    private List<StudySite> studySites = new ArrayList<StudySite>();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        userName = "USER NAME";
        user = Fixtures.createUser(userName, Role.SUBJECT_COORDINATOR);
        SecurityContextHolderTestHelper.setSecurityContext(user, "pass");
        subjectDao = registerDaoMockFor(SubjectDao.class);
        activityTypeDao = registerDaoMockFor(ActivityTypeDao.class);

        service = new SubjectService();
        service.setSubjectDao(subjectDao);

        paService = new SubjectCoordinatorDashboardService();

        study = setId(100, Fixtures.createBasicTemplate());
        site = Fixtures.createSite("Site");
        studySite = Fixtures.createStudySite(study, site);
        studies.add(study);
        ownedStudies.add(study);
        studySites.add(studySite);

        studyDao = registerDaoMockFor(StudyDao.class);
        userDao = registerDaoMockFor(UserDao.class);
        scheduledActivityDao = registerDaoMockFor(ScheduledActivityDao.class);
        notificationDao = registerDaoMockFor(NotificationDao.class);

        authorizationService = registerMockFor(AuthorizationService.class);
        command = registerMockFor(ScheduleCommand.class);

        controller = new ScheduleController(){
            @Override
            protected Object formBackingObject(HttpServletRequest request) throws Exception {
                return command;
            }
        };
        controller.setStudyDao(studyDao);
        controller.setUserDao(userDao);
        controller.setScheduledActivityDao(scheduledActivityDao);
        controller.setNotificationDao(notificationDao);
        controller.setActivityTypeDao(activityTypeDao);
        controller.setAuthorizationService(authorizationService);
        controller.setSubjectCoordinatorDashboardService(paService);
        controller.setApplicationSecurityManager(applicationSecurityManager);

        request.setMethod("GET"); // To simplify the binding tests
        request.addParameter("id", "15");

        expect(userDao.getAssignments(user)).andReturn(studySubjectAssignments).anyTimes();
    }

    public void testAuthorizedRoles() {
        Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, null);
        assertRolesAllowed(actualAuthorizations, STUDY_SUBJECT_CALENDAR_MANAGER);
    }

    public void testReferenceData() throws Exception {
        applicationSecurityManager.setUserService(registerMockFor(UserService.class));
        expect(applicationSecurityManager.getFreshUser()).andReturn(user).anyTimes();
        expect(studyDao.getAll()).andReturn(studies);
        expect(activityTypeDao.getAll()).andReturn(activityTypes).anyTimes();
        expect(authorizationService.filterStudiesForVisibility(studies, user.getUserRole(Role.SUBJECT_COORDINATOR))).andReturn(ownedStudies);
        expect(authorizationService.filterStudySitesForVisibilityFromStudiesList(ownedStudies, user.getUserRole(Role.SUBJECT_COORDINATOR))).andReturn(studySites);
        expect(authorizationService.filterStudySubjectAssignmentsByStudySite(studySites, studySubjectAssignments)).andReturn(studySubjectAssignments);
        expect(userDao.getAllSubjectCoordinators()).andReturn(users);

        replayMocks();
            Map<String, Object> refdata = controller.referenceData(request);
        verifyMocks();
        assertSame(user, refdata.get("userName"));
        assertNotNull("MapOfUserAndCalendar is Null", refdata.get("mapOfUserAndCalendar"));
        assertNotNull("ownedStudies is Null", refdata.get("ownedStudies"));
        assertNotNull("pastDueActivities is Null", refdata.get("pastDueActivities"));
    }

    public void testNotifications() throws Exception {
        AdverseEvent event = new AdverseEvent();
        event.setDescription("Big bad");
        event.setDetectionDate(DateUtils.createDate(2006, Calendar.APRIL, 5));

        Notification notification = new Notification(event);
        Integer notificationId = 29;
        notification.setId(notificationId);

        expect(command.getNotificationId()).andReturn(notificationId).anyTimes();

        expect(notificationDao.getById(notificationId)).andReturn(notification);
        notificationDao.save(notification);
        replayMocks();

        ModelAndView mv = controller.onSubmit(request, response, command, null);
        verifyMocks();

        assertEquals("Key for Model and View is wrong ", "template/ajax/notificationList", mv.getViewName());
        assertEquals("Model doesn't contain notifications ", true , mv.getModel().containsKey("notifications"));
    }

    public void testOnSubmit() throws Exception {
        command.setUser(user);
        expect(command.getNotificationId()).andReturn(null).anyTimes();
        command.setUserDao(userDao);
        command.setScheduledActivityDao(scheduledActivityDao);
        Map<String, Object> model = null;
        expect(command.execute(paService)).andReturn(model);
        replayMocks();

        ModelAndView mv = controller.onSubmit(request, response, command, null);
        verifyMocks();
        assertEquals("Key from Model and View is wrong ", "template/ajax/listOfSubjectsAndEvents", mv.getViewName());
    }
}

