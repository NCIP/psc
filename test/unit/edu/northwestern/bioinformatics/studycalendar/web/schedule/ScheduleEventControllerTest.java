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
            ScheduleEventCommand.class.getMethod("changeState"));

        controller = new ScheduleEventController() {
            protected Object formBackingObject(HttpServletRequest request) throws Exception {
                return command;
            }
        };
        controller.setScheduledCalendarDao(scheduledCalendarDao);
        controller.setScheduledEventDao(scheduledEventDao);

        request.setMethod("GET");
    }

    public void testBindEvent() throws Exception {
        ScheduledEvent event = setId(16, createScheduledEvent("SBC", 2002, Calendar.MAY, 3));
        expect(scheduledEventDao.getById(16)).andReturn(event);
        request.setParameter("event", "16");

        replayMocks();
        controller.handleRequest(request, response);
        verifyMocks();

        assertSame(event, command.getEvent());
    }

    public void testBindMode() throws Exception {
        request.addParameter("newMode", "2");

        replayMocks();
        controller.handleRequest(request, response);
        verifyMocks();

        assertSame(ScheduledEventMode.OCCURRED, command.getNewMode());
    }
    
    public void testBindDate() throws Exception {
        request.addParameter("newDate", "11/2/2003");

        replayMocks();
        controller.handleRequest(request, response);
        verifyMocks();

        assertDayOfDate(2003, Calendar.NOVEMBER, 2, command.getNewDate());
    }
    
    public void testBindReason() throws Exception {
        request.addParameter("newReason", "Insisted");

        replayMocks();
        controller.handleRequest(request, response);
        verifyMocks();

        assertEquals("Insisted", command.getNewReason());
    }

    public void testChangeStateOnSubmit() throws Exception {
        command.setEvent(new ScheduledEvent());
        command.getEvent().setScheduledArm(new ScheduledArm());
        command.getEvent().getScheduledArm().setScheduledCalendar(new ScheduledCalendar());
        request.setMethod("POST");
        command.changeState();

        replayMocks();
        controller.handleRequest(request, response);
        verifyMocks();
    }
}
