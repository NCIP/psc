package edu.northwestern.bioinformatics.studycalendar.web.dashboard.subjectcoordinator;

import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectCoordinatorDashboardService;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.SecurityContextHolderTestHelper;
import edu.nwu.bioinformatics.commons.DateUtils;

import javax.servlet.http.HttpServletRequest;

import static org.easymock.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

public class ScheduleControllerTest extends ControllerTestCase {
    private ScheduleController controller;

    private TemplateService templateService;

    private ScheduledActivityDao scheduledActivityDao;
    private StudyDao studyDao;
    private UserDao userDao;
    private NotificationDao notificationDao;

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
    private Study study;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        userName = "USER NAME";
        user = Fixtures.createUser(userName, Role.SUBJECT_COORDINATOR);
        SecurityContextHolderTestHelper.setSecurityContext(userName , "pass");
        subjectDao = registerDaoMockFor(SubjectDao.class);
        service = new SubjectService();
        service.setSubjectDao(subjectDao);

        paService = new SubjectCoordinatorDashboardService();

        study = setId(100, Fixtures.createBasicTemplate());
        studies.add(study);
        ownedStudies.add(study);

        studyDao = registerDaoMockFor(StudyDao.class);
        userDao = registerDaoMockFor(UserDao.class);
        scheduledActivityDao = registerDaoMockFor(ScheduledActivityDao.class);
        notificationDao = registerDaoMockFor(NotificationDao.class);

        templateService = registerMockFor(TemplateService.class);
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
        controller.setTemplateService(templateService);
        controller.setSubjectCoordinatorDashboardService(paService);

        request.setMethod("GET"); // To simplify the binding tests
        request.addParameter("id", "15");

        expect(userDao.getByName(userName)).andReturn(user).anyTimes();
        expect(userDao.getAssignments(user)).andReturn(studySubjectAssignments).anyTimes();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        ApplicationSecurityManager.removeUserSession();
    }

    public void testReferenceData() throws Exception {
        expect(studyDao.getAll()).andReturn(studies);
        expect(templateService.filterForVisibility(studies, user.getUserRole(Role.SUBJECT_COORDINATOR))).andReturn(ownedStudies);
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

