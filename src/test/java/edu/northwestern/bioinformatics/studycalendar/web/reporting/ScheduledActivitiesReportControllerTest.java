package edu.northwestern.bioinformatics.studycalendar.web.reporting;

import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportFilters;
import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportRowDao;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import org.apache.commons.lang.StringUtils;
import static org.easymock.EasyMock.expect;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import static java.util.Collections.EMPTY_LIST;
import java.util.Map;

/**
 * @author John Dzak
 */
public class ScheduledActivitiesReportControllerTest extends ControllerTestCase {
    private ScheduledActivitiesReportController controller;
    private ScheduledActivitiesReportRowDao dao;
    private ScheduledActivitiesReportCommand command;
    private ScheduledActivitiesReportFilters filters;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        dao = registerDaoMockFor(ScheduledActivitiesReportRowDao.class);
        filters = new ScheduledActivitiesReportFilters();
        command = new ScheduledActivitiesReportCommand(filters);

        controller = new ScheduledActivitiesReportController() {
            protected Object getCommand(HttpServletRequest request) throws Exception {
                return command;
            }
        };
        controller.setScheduledActivitiesReportRowDao(dao);
    }

    @SuppressWarnings({"unchecked"})
    public void testCreateModel() {
        Map<String,Object> model = controller.createModel(new BindException(this, StringUtils.EMPTY), EMPTY_LIST);
        assertNotNull("Model should contain modes", model.get("modes"));
    }

    @SuppressWarnings({"unchecked"})
    public void testHandle() throws Exception {
        expectDaoSearch();
        ModelAndView mv = handleRequest();
        assertEquals("Wrong view", "reporting/scheduledActivitiesReport", mv.getViewName());
    }

    public void testBindCurrentStateMode() throws Exception {
        request.setParameter("filters.currentStateMode", "1");
        expectDaoSearch();
        ScheduledActivitiesReportCommand command = postAndReturnCommand("command.filters.currentStateMode");
        assertEquals("Wrong state", ScheduledActivityMode.SCHEDULED, command.getFilters().getCurrentStateMode());
    }

    ////// Helper Methods
    private ModelAndView handleRequest() throws Exception {
        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();
        return mv;
    }

    @SuppressWarnings({"unchecked"})
    private void expectDaoSearch() {
        expect(dao.search(filters)).andReturn(EMPTY_LIST);
    }

    @SuppressWarnings({ "unchecked" })
    private ScheduledActivitiesReportCommand postAndReturnCommand(String expectNoErrorsForField) throws Exception {
        Map<String, Object> model = handleRequest().getModel();
        assertNoBindingErrorsFor(expectNoErrorsForField, model);
        return (ScheduledActivitiesReportCommand) model.get("command");
    }
}
