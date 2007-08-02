package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.nwu.bioinformatics.commons.DateUtils;

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
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class ScheduleNextArmControllerTest extends ControllerTestCase {
    private static final Date NEXT_PROTOCOL_DATE = DateUtils.createDate(2003, Calendar.AUGUST, 14);

    private ArmDao armDao;
    private ScheduledCalendarDao scheduledCalendarDao;

    private ScheduleNextArmCommand command;
    private ScheduleNextArmController controller;
    private ScheduledArm scheduledArm;

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

        scheduledArm = registerMockFor(ScheduledArm.class);
        expect(scheduledArm.getNextArmPerProtocolStartDate()).andReturn(NEXT_PROTOCOL_DATE);
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
        assertSame(scheduledArm, actual.getModel().get("scheduledArm"));
        assertDatesClose("Missing nextPerProtocolDate",
            NEXT_PROTOCOL_DATE, (Date) actual.getModel().get("nextPerProtocolDate"), 2);
    }

    private ModelAndView executeRequest() throws Exception {
        expect(command.schedule()).andReturn(scheduledArm);
        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        BindingResult result = (BindingResult) mv.getModel().get(BindingResult.MODEL_KEY_PREFIX + "command");
        assertEquals("There were errors in the request: " + result.getAllErrors(), 0, result.getErrorCount());
        return mv;
    }
}
