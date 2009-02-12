package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.nwu.bioinformatics.commons.DateUtils;

import edu.northwestern.bioinformatics.studycalendar.dao.StudySegmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.core.ServicedFixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.NextStudySegmentMode;
import static org.easymock.classextension.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindingResult;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class ScheduleNextStudySegmentControllerTest extends ControllerTestCase {
    private static final Date NEXT_PROTOCOL_DATE = DateUtils.createDate(2003, Calendar.AUGUST, 14);

    private StudySegmentDao studySegmentDao;
    private ScheduledCalendarDao scheduledCalendarDao;

    private ScheduleNextStudySegmentCommand command;
    private ScheduleNextStudySegmentController controller;
    private ScheduledStudySegment scheduledStudySegment;

    protected void setUp() throws Exception {
        super.setUp();
        studySegmentDao = registerDaoMockFor(StudySegmentDao.class);
        scheduledCalendarDao = registerDaoMockFor(ScheduledCalendarDao.class);

        command = registerMockFor(ScheduleNextStudySegmentCommand.class, ScheduleNextStudySegmentCommand.class.getMethod("schedule"));

        controller = new ScheduleNextStudySegmentController() {
            @Override protected Object getCommand(HttpServletRequest request) throws Exception {
                return command;
            }
        };
        controller.setStudySegmentDao(studySegmentDao);
        controller.setScheduledCalendarDao(scheduledCalendarDao);
        controller.setControllerTools(controllerTools);

        scheduledStudySegment = registerMockFor(ScheduledStudySegment.class);
        expect(scheduledStudySegment.getNextStudySegmentPerProtocolStartDate()).andReturn(NEXT_PROTOCOL_DATE);
    }
    
    public void testBindStudySegment() throws Exception {
        int id = 44;
        StudySegment expectedStudySegment = ServicedFixtures.setId(id, ServicedFixtures.createNamedInstance("Baker", StudySegment.class));
        expect(studySegmentDao.getById(id)).andReturn(expectedStudySegment);
        request.addParameter("studySegment", Integer.toString(id));

        executeRequest();

        assertSame(expectedStudySegment, command.getStudySegment());
    }

    public void testBindCalendar() throws Exception {
        int id = 17;
        ScheduledCalendar expectedCalendar = ServicedFixtures.setId(id, new ScheduledCalendar());
        expect(scheduledCalendarDao.getById(id)).andReturn(expectedCalendar);
        request.addParameter("calendar", Integer.toString(id));

        executeRequest();

        assertSame(expectedCalendar, command.getCalendar());
    }

    public void testBindDate() throws Exception {
        request.addParameter("startDate", "08/05/2003");

        executeRequest();

        assertDayOfDate(2003, Calendar.AUGUST, 5, command.getStartDate());
    }

    public void testBindMode() throws Exception {
        request.addParameter("mode", "PER_PROTOCOL");

        executeRequest();

        assertSame(NextStudySegmentMode.PER_PROTOCOL, command.getMode());
    }

    public void testScheduledStudySegmentInModel() throws Exception {
        ModelAndView actual = executeRequest();
        assertTrue("Missing scheduledStudySegment", actual.getModel().containsKey("scheduledStudySegment"));
        assertSame(scheduledStudySegment, actual.getModel().get("scheduledStudySegment"));
        assertDatesClose("Missing nextPerProtocolDate",
            NEXT_PROTOCOL_DATE, (Date) actual.getModel().get("nextPerProtocolDate"), 2);
    }

    private ModelAndView executeRequest() throws Exception {
        expect(command.schedule()).andReturn(scheduledStudySegment);
        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        BindingResult result = (BindingResult) mv.getModel().get(BindingResult.MODEL_KEY_PREFIX + "command");
        assertEquals("There were errors in the request: " + result.getAllErrors(), 0, result.getErrorCount());
        return mv;
    }
}
