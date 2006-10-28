package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.nwu.bioinformatics.commons.DateUtils;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyParticipantAssignmentDao;
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
public class DisplayScheduleControllerTest extends ControllerTestCase {
    private DisplayScheduleController controller;
    private StudyParticipantAssignmentDao studyParticipantAssignmentDao;
    private StudyParticipantAssignment assignment;

    protected void setUp() throws Exception {
        super.setUp();
        studyParticipantAssignmentDao = registerDaoMockFor(StudyParticipantAssignmentDao.class);

        controller = new DisplayScheduleController();
        controller.setStudyParticipantAssignmentDao(studyParticipantAssignmentDao);

        assignment = setId(17, new StudyParticipantAssignment());
        ScheduledCalendar expectedCalendar = new ScheduledCalendar();
        PlannedCalendar expectedPlannedCalendar = new PlannedCalendar();
        {
            StudySite studySite = createStudySite(createNamedInstance("A", Study.class), createNamedInstance("NU", Site.class));
            expectedPlannedCalendar.setStudy(studySite.getStudy());
            expectedCalendar.addArm(new ScheduledArm());

            assignment.setParticipant(createParticipant("Preston", "Sturges"));
            assignment.setScheduledCalendar(expectedCalendar);
            assignment.setStudySite(studySite);
        }

        expect(studyParticipantAssignmentDao.getById(17)).andReturn(assignment);
        request.setParameter("assignment", assignment.getId().toString());
    }

    public void testBaseResponse() throws Exception {
        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertEquals("schedule/display", mv.getViewName());

        Map<String, Object> actualModel = mv.getModel();

        assertSame(assignment.getStudySite().getStudy().getPlannedCalendar(), actualModel.get("plannedCalendar"));
        assertSame(assignment.getScheduledCalendar(), actualModel.get("calendar"));
        assertSame(assignment.getParticipant(), actualModel.get("participant"));
        assertSame(assignment.getScheduledCalendar().getScheduledArms().get(0), actualModel.get("arm"));
        assertSame(assignment, actualModel.get("assignment"));
    }
    
    public void testDefaultDates() throws Exception {
        ScheduledArm scheduledArm = registerMockFor(ScheduledArm.class);
        expect(scheduledArm.getNextArmPerProtocolStartDate())
            .andReturn(DateUtils.createDate(2005, Calendar.MARCH, 11));
        scheduledArm.setScheduledCalendar(assignment.getScheduledCalendar());

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
}
