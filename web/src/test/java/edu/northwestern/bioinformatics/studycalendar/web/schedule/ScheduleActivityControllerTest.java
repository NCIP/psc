package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.PscUserBuilder;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserActionDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.domain.UserAction;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.ScheduleService;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.nwu.bioinformatics.commons.DateUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.Map;
import java.util.TreeMap;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_SUBJECT_CALENDAR_MANAGER;
import static org.easymock.classextension.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class ScheduleActivityControllerTest extends ControllerTestCase {
    private ScheduleActivityController controller;
    private ScheduledCalendarDao scheduledCalendarDao;
    private ScheduledActivityDao scheduledActivityDao;
    private ScheduleService scheduleService;
    private ScheduledActivity event;
    private ScheduleActivityCommand command;
    private ApplicationSecurityManager mockApplicationSecurityManager;
    private String applicationPath = "psc/application";
    private UserActionDao userActionDao;
    private PscUser pscUser =   AuthorizationObjectFactory.createPscUser("user", 12L);

    protected void setUp() throws Exception {
        super.setUp();
        scheduledCalendarDao = registerDaoMockFor(ScheduledCalendarDao.class);
        scheduledActivityDao = registerDaoMockFor(ScheduledActivityDao.class);
        userActionDao = registerDaoMockFor(UserActionDao.class);
        scheduleService = registerMockFor(ScheduleService.class);
        mockApplicationSecurityManager = registerMockFor(ApplicationSecurityManager.class);
        command = registerMockFor(ScheduleActivityCommand.class,
            ScheduleActivityCommand.class.getMethod("apply"));

        controller = new ScheduleActivityController() {
            protected Object formBackingObject(HttpServletRequest request) throws Exception {
                return command;
            }
        };
        controller.setScheduleService(scheduleService);
        controller.setScheduledCalendarDao(scheduledCalendarDao);
        controller.setScheduledActivityDao(scheduledActivityDao);
        controller.setControllerTools(controllerTools);
        controller.setApplicationSecurityManager(mockApplicationSecurityManager);
        controller.setApplicationPath(applicationPath);
        controller.setUserActionDao(userActionDao);
        request.setMethod("GET");
    }
    
    public void testBindEvent() throws Exception {
        expectShowFormWithNoErrors();

        assertSame(event, command.getEvent());
    }

    public void testBindMode() throws Exception {
        request.addParameter("newMode", "2");

        expectShowFormWithNoErrors();

        assertSame(ScheduledActivityMode.OCCURRED, command.getNewMode());
    }

    public void testBindNoMode() throws Exception {
        request.addParameter("newMode", "");

        expectShowFormWithNoErrors();

        assertNull(command.getNewMode());
    }

    public void testBindDate() throws Exception {
        request.addParameter("newDate", "11/02/2003");

        expectShowFormWithNoErrors();

        assertDayOfDate(2003, Calendar.NOVEMBER, 2, command.getNewDate());
    }
    
    public void testBindReason() throws Exception {
        request.addParameter("newReason", "Insisted");

        expectShowFormWithNoErrors();

        assertEquals("Insisted", command.getNewReason());
    }

    public void testBindReasonBlankIsNull() throws Exception {
        request.addParameter("newReason", "");

        expectShowFormWithNoErrors();

        assertNull(command.getNewReason());
    }

    public void testBindNotes() throws Exception {
        request.addParameter("newNotes", "Insisted");

        expectShowFormWithNoErrors();

        assertEquals("Insisted", command.getNewNotes());
    }

    public void testBindNotesBlankIsNull() throws Exception {
        request.addParameter("newNotes", "");

        expectShowFormWithNoErrors();

        assertNull(command.getNewNotes());
    }

    public void testChangeStateOnSubmit() throws Exception {
        Subject subject = edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createSubject("Perry", "Duglas");
        subject.setGridId("1111");
        StudySite studySite = new StudySite();
        Study study = new Study();
        study.setAssignedIdentifier("test-study");
        studySite.setStudy(study);

        command.setEvent(createScheduledActivity("testActivity", 2003, 03, 14,
            ScheduledActivityMode.SCHEDULED.createStateInstance(DateUtils.createDate(2003, Calendar.MARCH, 14), "testing")));
        command.setNewMode(ScheduledActivityMode.OCCURRED);
        command.setNewDate(DateUtils.createDate(2003, Calendar.MARCH, 15));
        command.getEvent().setScheduledStudySegment(new ScheduledStudySegment());
        command.getEvent().getScheduledStudySegment().setScheduledCalendar(new ScheduledCalendar());
        command.getEvent().getScheduledStudySegment().getScheduledCalendar().setAssignment(Fixtures.createAssignment(studySite, subject));
        request.setMethod("POST");

        UserAction userAction = new UserAction();
        String context = applicationPath.concat("/api/v1/subjects/1111/schedules");
        userAction.setContext(context);
        userAction.setActionType("activity update");
        userAction.setUser(pscUser.getCsmUser());
        String des = "testActivity is updated for Perry Duglas for test-study";
        userAction.setDescription(des);
        expect(mockApplicationSecurityManager.getUser()).andReturn(pscUser);
        userActionDao.save(userAction);
        command.apply();

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();
    }

    public void testAddNotesOnSubmit() throws Exception {
        Subject subject = edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createSubject("Perry", "Duglas");
        subject.setGridId("1111");
        StudySite studySite = new StudySite();
        Study study = new Study();
        study.setAssignedIdentifier("test-study");
        studySite.setStudy(study);

        command.setEvent(createScheduledActivity("testActivity", 2003, 03, 14,
            ScheduledActivityMode.SCHEDULED.createStateInstance
                    (DateUtils.createDate(2003, Calendar.MARCH, 14), "testing")));
        command.setNewNotes("New notes");
        command.getEvent().setScheduledStudySegment(new ScheduledStudySegment());
        command.getEvent().getScheduledStudySegment().setScheduledCalendar(new ScheduledCalendar());
        StudySubjectAssignment ssa =Fixtures.createAssignment(studySite, subject);
        command.getEvent().getScheduledStudySegment().getScheduledCalendar().setAssignment(ssa);
        request.setMethod("POST");

        UserAction userAction = new UserAction();
        String context = applicationPath.concat("/api/v1/subjects/1111/schedules");
        userAction.setContext(context);
        userAction.setActionType("activity update");
        userAction.setUser(pscUser.getCsmUser());
        String des = "testActivity is updated for Perry Duglas for test-study";
        userAction.setDescription(des);
        expect(mockApplicationSecurityManager.getUser()).andReturn(pscUser);
        userActionDao.save(userAction);
        command.apply();

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();
    }

    private void expectShowFormWithNoErrors() throws Exception {
        event = setId(16, createScheduledActivity("SBC", 2002, Calendar.MAY, 3));

        command.setEvent(event);
        Subject subject = edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createSubject("Perry", "Duglas");
        subject.setGridId("1111");
        StudySite studySite = new StudySite();
        Study study = new Study();
        study.setAssignedIdentifier("test-study");
        studySite.setStudy(study);
        StudySubjectAssignment ssa =Fixtures.createAssignment(studySite, subject);

        command.getEvent().setScheduledStudySegment(new ScheduledStudySegment());
        command.getEvent().getScheduledStudySegment().setScheduledCalendar(new ScheduledCalendar());
        command.getEvent().getScheduledStudySegment().getScheduledCalendar().setAssignment(ssa);

        expect(scheduledActivityDao.getById(16)).andReturn(event);
        Map<String,String> uriListMap = new TreeMap<String, String>();
        expect(scheduleService.generateActivityTemplateUri(event)).andReturn(uriListMap);
        request.setParameter("event", "16");
        PscUserBuilder builder = new PscUserBuilder();
        expect(mockApplicationSecurityManager.getUser()).andReturn(builder.add(STUDY_SUBJECT_CALENDAR_MANAGER).forAllSites().forAllStudies().toUser());

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        BindingResult errors = (BindingResult) mv.getModel().get("org.springframework.validation.BindingResult.command");
        assertFalse("Binding errors: " + errors.getAllErrors(), errors.hasErrors());
    }
}
