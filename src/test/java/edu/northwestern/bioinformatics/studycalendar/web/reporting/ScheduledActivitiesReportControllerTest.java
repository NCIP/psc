package edu.northwestern.bioinformatics.studycalendar.web.reporting;

import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRowDao;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import org.apache.commons.lang.StringUtils;
import static org.easymock.EasyMock.expect;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import static java.util.Collections.EMPTY_LIST;
import java.util.Map;

/**
 * @author John Dzak
 */
public class ScheduledActivitiesReportControllerTest extends ControllerTestCase {
    private ScheduledActivitiesReportController controller;
    private ScheduledActivitiesReportRowDao dao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        dao = registerDaoMockFor(ScheduledActivitiesReportRowDao.class);

        controller = new ScheduledActivitiesReportController();
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

    ////// Helper Methods
    private ModelAndView handleRequest() throws Exception {
        replayMocks();
        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();
        return mv;
    }

    private void expectDaoSearch() {
        expect(dao.search()).andReturn(EMPTY_LIST);
    }
}
