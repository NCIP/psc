package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledActivityDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import static org.easymock.classextension.EasyMock.expect;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;

/**
 * @author Rhett Sutphin
 */
public class BatchRescheduleControllerTest extends ControllerTestCase {
    private ScheduledCalendarDao scheduledCalendarDao;
    private ScheduledActivityDao scheduledActivityDao;
    private BatchRescheduleController controller;
    private BatchRescheduleCommand command;

    protected void setUp() throws Exception {
        super.setUp();
        scheduledCalendarDao = registerDaoMockFor(ScheduledCalendarDao.class);
        scheduledActivityDao = registerDaoMockFor(ScheduledActivityDao.class);
        command = registerMockFor(BatchRescheduleCommand.class, BatchRescheduleCommand.class.getMethod("apply"));
        controller = new BatchRescheduleController() {
            @Override
            public Object getCommand(HttpServletRequest request) {
                return command;
            }
        };
        
        controller.setScheduledCalendarDao(scheduledCalendarDao);
        controller.setScheduledActivityDao(scheduledActivityDao);
        controller.setControllerTools(controllerTools);
    }

    public void testBindMode() throws Exception {
        
        request.addParameter("newMode", "1");
        doHandle();
        assertEquals(ScheduledActivityMode.SCHEDULED, command.getNewMode());
    }

    public void testBindReason() throws Exception {
        request.addParameter("newReason", "Here's why");
        doHandle();
        assertEquals("Here's why", command.getNewReason());
    }

    public void testBindBlankReasonIsNull() throws Exception {
        request.addParameter("newReason", " ");
        doHandle();
        assertNull(command.getNewReason());
    }

    public void testBindScheduledCalendar() throws Exception {
        ScheduledCalendar expectedCalendar = Fixtures.setId(7, new ScheduledCalendar());
        request.addParameter("scheduledCalendar", "7");
        expect(scheduledCalendarDao.getById(7)).andReturn(expectedCalendar);
        doHandle();
        assertSame(expectedCalendar, command.getScheduledCalendar());
    }
    
    public void testBindDateOffset() throws Exception {
        request.addParameter("dateOffset", "5");
        doHandle();
        assertEquals(5, (int) command.getDateOffset());
    }

    public void testBindScheduledActivities() throws Exception {
        ScheduledActivity expectedEvent = Fixtures.setId(7, new ScheduledActivity());
        ScheduledActivity expectedEvent2 = Fixtures.setId(8, new ScheduledActivity());
        request.addParameter("events", "7");
        request.addParameter("events", "8");
        expect(scheduledActivityDao.getById(7)).andReturn(expectedEvent);
        expect(scheduledActivityDao.getById(8)).andReturn(expectedEvent2);
        doHandle();
        Iterator actualEvents = command.getEvents().iterator();
        assertSame(expectedEvent, actualEvents.next());
        assertSame(expectedEvent2, actualEvents.next());
    }

    private void doHandle() throws Exception {
        command.apply();
        replayMocks();
        controller.handleRequest(request, response);
        verifyMocks();
    }

}
