package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.dao.ArmDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.service.NextArmMode;
import static org.easymock.classextension.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindingResult;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;

/**
 * @author Rhett Sutphin
 */
public class ScheduleNextArmControllerTest extends ControllerTestCase {
    private ArmDao armDao;
    private ScheduledCalendarDao scheduledCalendarDao;

    private ScheduleNextArmCommand command;
    private ScheduleNextArmController controller;
    private static final ScheduledArm SCHEDULED_ARM = new ScheduledArm();

    protected void setUp() throws Exception {
        super.setUp();
        armDao = registerDaoMockFor(ArmDao.class);
        scheduledCalendarDao = registerDaoMockFor(ScheduledCalendarDao.class);

        command = registerMockFor(ScheduleNextArmCommand.class, ScheduleNextArmCommand.class.getMethod("schedule"));

        controller = new ScheduleNextArmController() {
            @Override protected Object getCommand(HttpServletRequest request) throws Exception {
                return command;
            }
        };
        controller.setArmDao(armDao);
        controller.setScheduledCalendarDao(scheduledCalendarDao);
    }
    
    public void testBindArm() throws Exception {
        int id = 44;
        Arm expectedArm = Fixtures.setId(id, Fixtures.createNamedInstance("Baker", Arm.class));
        expect(armDao.getById(id)).andReturn(expectedArm);
        request.addParameter("arm", Integer.toString(id));

        executeRequest();

        assertSame(expectedArm, command.getArm());
    }

    public void testBindCalendar() throws Exception {
        int id = 17;
        ScheduledCalendar expectedCalendar = Fixtures.setId(id, new ScheduledCalendar());
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

        assertSame(NextArmMode.PER_PROTOCOL, command.getMode());
    }

    public void testScheduledArmInModel() throws Exception {
        ModelAndView actual = executeRequest();
        assertTrue("Missing scheduledArm", actual.getModel().containsKey("scheduledArm"));
        assertSame(SCHEDULED_ARM, actual.getModel().get("scheduledArm"));
    }

    private ModelAndView executeRequest() throws Exception {
        expect(command.schedule()).andReturn(SCHEDULED_ARM);
        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        BindingResult result = (BindingResult) mv.getModel().get(BindingResult.MODEL_KEY_PREFIX + "command");
        assertEquals("There were errors in the request: " + result.getAllErrors(), 0, result.getErrorCount());
        return mv;
    }
}
