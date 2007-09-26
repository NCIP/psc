package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledEventDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEventMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import static org.easymock.classextension.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.validation.BindingResult;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;

/**
 * @author Rhett Sutphin
 */
public class ScheduleEventControllerTest extends ControllerTestCase {
    private ScheduleEventController controller;
    private ScheduledCalendarDao scheduledCalendarDao;
    private ScheduledEventDao scheduledEventDao;

    private ScheduleEventCommand command;

    protected void setUp() throws Exception {
        super.setUp();
        scheduledCalendarDao = registerDaoMockFor(ScheduledCalendarDao.class);
        scheduledEventDao = registerDaoMockFor(ScheduledEventDao.class);
        command = registerMockFor(ScheduleEventCommand.class,
            ScheduleEventCommand.class.getMethod("apply"));

        controller = new ScheduleEventController() {
            protected Object formBackingObject(HttpServletRequest request) throws Exception {
                return command;
            }
        };
        controller.setScheduledCalendarDao(scheduledCalendarDao);
        controller.setScheduledEventDao(scheduledEventDao);
        controller.setControllerTools(controllerTools);

        request.setMethod("GET");
    }

    public void testBindEvent() throws Exception {
        ScheduledEvent event = setId(16, createScheduledEvent("SBC", 2002, Calendar.MAY, 3));
        expect(scheduledEventDao.getById(16)).andReturn(event);
        request.setParameter("event", "16");

        expectShowFormWithNoErrors();

        assertSame(event, command.getEvent());
    }

    public void testBindMode() throws Exception {
        request.addParameter("newMode", "2");

        expectShowFormWithNoErrors();

        assertSame(ScheduledEventMode.OCCURRED, command.getNewMode());
    }

    public void testBindNoMode() throws Exception {
        request.addParameter("newMode", "");

        expectShowFormWithNoErrors();

        assertNull(command.getNewMode());
    }

    public void testBindDate() throws Exception {
        request.addParameter("newDate", "11/2/2003");

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
        command.setEvent(new ScheduledEvent());
        command.getEvent().setScheduledArm(new ScheduledArm());
        command.getEvent().getScheduledArm().setScheduledCalendar(new ScheduledCalendar());
        request.setMethod("POST");
        command.apply();

        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();
    }

    private void expectShowFormWithNoErrors() throws Exception {
        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        BindingResult errors = (BindingResult) mv.getModel().get("org.springframework.validation.BindingResult.command");
        assertFalse("Binding errors: " + errors.getAllErrors(), errors.hasErrors());
    }
}
