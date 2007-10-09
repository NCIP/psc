package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.service.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.SecurityContextHolderTestHelper;

import javax.servlet.http.HttpServletRequest;

import static org.easymock.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

public class ScheduleControllerTest extends ControllerTestCase {

	private TemplateService templateService;

    private ScheduledEventDao scheduledEventDao;
	private StudyDao studyDao;
    private UserDao userDao;
    private User user;

    private ScheduleController controller;
    private String userName;

    List<Study> ownedStudies = new ArrayList<Study>();
    List<Site> ownedSites;
    List<Study> studies = new ArrayList<Study>();
    List<StudyParticipantAssignment> studyParticipantAssignments = new ArrayList<StudyParticipantAssignment>();
    private ScheduleCommand command;

    private ParticipantDao participantDao;
    private ParticipantService service;
    private ParticipantCoordinatorDashboardService paService;
    StudyParticipantAssignment actualAssignment;

    Study study;

    protected void setUp() throws Exception {
        super.setUp();
        user = new User();
        userName = "USER NAME";
        user.setName(userName);
        SecurityContextHolderTestHelper.setSecurityContext(userName , "pass");
        participantDao = registerMockFor(ParticipantDao.class);
        service = new ParticipantService();
        service.setParticipantDao(participantDao);

        paService = new ParticipantCoordinatorDashboardService();

        study = setId(100, Fixtures.createBasicTemplate());
        studies.add(study);
        ownedStudies.add(study);

        studyDao = registerDaoMockFor(StudyDao.class);
        userDao = registerDaoMockFor(UserDao.class);
        scheduledEventDao = registerDaoMockFor(ScheduledEventDao.class);

        templateService = registerMockFor(TemplateService.class);
        command = registerMockFor(ScheduleCommand.class);

        controller = new ScheduleController(){
            protected Object formBackingObject(HttpServletRequest request) throws Exception {
                return command;
            }
        };
        controller.setStudyDao(studyDao);
        controller.setUserDao(userDao);
        controller.setScheduledEventDao(scheduledEventDao);
        controller.setTemplateService(templateService);
        controller.setParticipantCoordinatorDashboardService(paService);

        request.setMethod("GET"); // To simplify the binding tests
        request.addParameter("id", "15");

        expect(userDao.getByName(userName)).andReturn(user).anyTimes();
        expect(userDao.getAssignments(user)).andReturn(studyParticipantAssignments).anyTimes();
    }

    public void testReferenceData() throws Exception {
        expect(studyDao.getAll()).andReturn(studies);
        expect(templateService.checkOwnership(userName, studies)).andReturn(ownedStudies);
        replayMocks();
            Map<String, Object> refdata = controller.referenceData(request);
        verifyMocks();
        assertSame(user.getName(), refdata.get("userName"));
        Object mapOfUserAndCalendar = refdata.get("mapOfUserAndCalendar");
        Object ownedStudies = refdata.get("ownedStudies");
        Object pastDueActivities = refdata.get("pastDueActivities");
        assertNotNull("MapOfUserAndCalendar is Null", mapOfUserAndCalendar);
        assertNotNull("ownedStudies is Null", ownedStudies);
        assertNotNull("pastDueActivities is Null", pastDueActivities);

    }

    public void testFormBackingObject() throws Exception {
        replayMocks();
        Object object = controller.formBackingObject(request);
        verifyMocks();
        assertEquals("Not the correct command", command, object);
    }


    public void testOnSubmit() throws Exception {
        command.setUser(user);
        command.setUserDao(userDao);
        command.setScheduledEventDao(scheduledEventDao);
        Map<String, Object> model = null;
        expect(command.execute(paService)).andReturn(model);
        replayMocks();

        ModelAndView mv = controller.onSubmit(request, response, command, null);
        verifyMocks();
        assertEquals("Key from Model and View is wrong ", "template/ajax/listOfParticipantsAndEvents", mv.getViewName());
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        ApplicationSecurityManager.removeUserSession();
    }
}

