package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyParticipantAssignmentDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import static org.easymock.classextension.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class DisplayScheduleControllerTest extends ControllerTestCase {
    private DisplayScheduleController controller;
    private StudyParticipantAssignmentDao studyParticipantAssignmentDao;

    protected void setUp() throws Exception {
        super.setUp();
        studyParticipantAssignmentDao = registerDaoMockFor(StudyParticipantAssignmentDao.class);

        controller = new DisplayScheduleController();
        controller.setStudyParticipantAssignmentDao(studyParticipantAssignmentDao);
    }

    public void testResponse() throws Exception {
        StudyParticipantAssignment expectedAssignment = setId(17, new StudyParticipantAssignment());
        ScheduledCalendar expectedCalendar = new ScheduledCalendar();
        PlannedCalendar expectedPlannedCalendar = new PlannedCalendar();
        {
            StudySite studySite = createStudySite(createNamedInstance("A", Study.class), createNamedInstance("NU", Site.class));
            expectedPlannedCalendar.setStudy(studySite.getStudy());
            expectedCalendar.addArm(new ScheduledArm());

            expectedAssignment.setParticipant(createParticipant("Preston", "Sturges"));
            expectedAssignment.setScheduledCalendar(expectedCalendar);
            expectedAssignment.setStudySite(studySite);
        }

        expect(studyParticipantAssignmentDao.getById(17)).andReturn(expectedAssignment);
        replayMocks();

        request.setParameter("assignment", expectedAssignment.getId().toString());
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertEquals("schedule/display", mv.getViewName());

        Map<String, Object> actualModel = mv.getModel();

        assertSame(expectedPlannedCalendar, actualModel.get("plannedCalendar"));
        assertSame(expectedCalendar, actualModel.get("calendar"));
        assertSame(expectedAssignment.getParticipant(), actualModel.get("participant"));
        assertSame(expectedCalendar.getScheduledArms().get(0), actualModel.get("arm"));
        assertSame(expectedAssignment, actualModel.get("assignment"));
    }
}
