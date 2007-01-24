package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.nwu.bioinformatics.commons.DateUtils;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyParticipantAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledArmDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import static org.easymock.classextension.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings("unchecked")
public class DisplayScheduleControllerTest extends ControllerTestCase {
    private DisplayScheduleController controller;
    private StudyParticipantAssignmentDao studyParticipantAssignmentDao;
    private ScheduledCalendarDao scheduledCalendarDao;
    private ScheduledArmDao scheduledArmDao;
    private StudyParticipantAssignment assignment;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        studyParticipantAssignmentDao = registerDaoMockFor(StudyParticipantAssignmentDao.class);
        scheduledCalendarDao = registerDaoMockFor(ScheduledCalendarDao.class);
        scheduledArmDao = registerDaoMockFor(ScheduledArmDao.class);

        controller = new DisplayScheduleController();
        controller.setStudyParticipantAssignmentDao(studyParticipantAssignmentDao);
        controller.setScheduledCalendarDao(scheduledCalendarDao);
        controller.setScheduledArmDao(scheduledArmDao);

        assignment = setId(17, new StudyParticipantAssignment());
        ScheduledCalendar expectedCalendar = new ScheduledCalendar();
        PlannedCalendar expectedPlannedCalendar = new PlannedCalendar();
        {
            StudySite studySite = createStudySite(createNamedInstance("A", Study.class), createNamedInstance("NU", Site.class));
            expectedPlannedCalendar.setStudy(studySite.getStudy());
            expectedCalendar.addArm(new ScheduledArm());
            expectedCalendar.addArm(new ScheduledArm());

            assignment.setParticipant(createParticipant("Preston", "Sturges"));
            assignment.setScheduledCalendar(expectedCalendar);
            assignment.setStudySite(studySite);
        }
    }

    public void testBaseResponse() throws Exception {
        expect(studyParticipantAssignmentDao.getById(17)).andReturn(assignment);
        request.setParameter("assignment", assignment.getId().toString());

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertEquals("schedule/display", mv.getViewName());

        Map<String, Object> actualModel = mv.getModel();

        assertSame(assignment.getStudySite().getStudy().getPlannedCalendar(), actualModel.get("plannedCalendar"));
        assertSame(assignment.getScheduledCalendar(), actualModel.get("calendar"));
        assertSame(assignment.getParticipant(), actualModel.get("participant"));
        assertSame(assignment.getScheduledCalendar().getCurrentArm(), actualModel.get("arm"));
        assertSame(assignment, actualModel.get("assignment"));
    }
    
    public void testAssignmentMayBeBigId() throws Exception {
        String bigId = "LE-BIG-ID";
        expect(studyParticipantAssignmentDao.getByBigId(bigId)).andReturn(assignment);
        request.setParameter("assignment", bigId);

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertEquals("schedule/display", mv.getViewName());

        Map<String, Object> actualModel = mv.getModel();

        assertSame(assignment.getStudySite().getStudy().getPlannedCalendar(), actualModel.get("plannedCalendar"));
        assertSame(assignment.getScheduledCalendar(), actualModel.get("calendar"));
        assertSame(assignment.getParticipant(), actualModel.get("participant"));
        assertSame(assignment.getScheduledCalendar().getCurrentArm(), actualModel.get("arm"));
        assertSame(assignment, actualModel.get("assignment"));
    }

    public void testCalendarParameterUsedWhenNoAssignment() throws Exception {
        assignment.getScheduledCalendar().setId(54);
        request.addParameter("calendar", "54");
        expect(scheduledCalendarDao.getById(54)).andReturn(assignment.getScheduledCalendar());

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertEquals("schedule/display", mv.getViewName());

        Map<String, Object> actualModel = mv.getModel();

        assertSame(assignment.getStudySite().getStudy().getPlannedCalendar(), actualModel.get("plannedCalendar"));
        assertSame(assignment.getScheduledCalendar(), actualModel.get("calendar"));
        assertSame(assignment.getParticipant(), actualModel.get("participant"));
        assertSame(assignment.getScheduledCalendar().getCurrentArm(), actualModel.get("arm"));
        assertSame(assignment, actualModel.get("assignment"));
    }
    
    public void testDefaultDates() throws Exception {
        expect(studyParticipantAssignmentDao.getById(17)).andReturn(assignment);
        request.setParameter("assignment", assignment.getId().toString());

        ScheduledArm scheduledArm = registerMockFor(ScheduledArm.class);
        expect(scheduledArm.getNextArmPerProtocolStartDate())
            .andReturn(DateUtils.createDate(2005, Calendar.MARCH, 11));
        scheduledArm.setScheduledCalendar(assignment.getScheduledCalendar());
        expect(scheduledArm.isComplete()).andReturn(false);

        replayMocks();
        assignment.getScheduledCalendar().addArm(scheduledArm);
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        Map<String, Object> actualModel = mv.getModel();
        Map<String, Date> actualDates = (Map<String, Date>) actualModel.get("dates");
        assertNotNull("Dates missing", actualDates);

        assertDayOfDate(2005, Calendar.MARCH, 11, actualDates.get("PER_PROTOCOL"));
        assertDatesClose(new Date(), actualDates.get("IMMEDIATE"), 1000);
    }

    public void testArmWhenArmIdSpecified() throws Exception {
        expect(studyParticipantAssignmentDao.getById(17)).andReturn(assignment);
        request.setParameter("assignment", assignment.getId().toString());

        ScheduledArm arm = new ScheduledArm();
        arm.setId(14);
        request.addParameter("arm", "14");
        expect(scheduledArmDao.getById(14)).andReturn(arm);

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertSame(arm, mv.getModel().get("arm"));
    }
}
