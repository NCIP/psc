package edu.northwestern.bioinformatics.studycalendar.web;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createUserRole;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserRoleDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import static org.easymock.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import static java.util.Arrays.asList;
import java.util.List;
import java.util.Map;

/**
 * @author John Dzak
 */
public class SiteCoordinatorDashboardControllerTest extends ControllerTestCase {
    SiteCoordinatorDashboardController controller;
    private StudyDao studyDao;
    private SiteDao siteDao;
    private List<Site> sites;

    private List<Study> studies;
    private SiteCoordinatorDashboardCommand command;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        siteDao     = registerDaoMockFor(SiteDao.class);
        studyDao    = registerDaoMockFor(StudyDao.class);

        command     = registerMockFor(SiteCoordinatorDashboardCommand.class);

        controller = new MockableController();
        controller.setSiteDao(siteDao);
        controller.setStudyDao(studyDao);

        sites     = asList(createNamedInstance("Mayo Clinic", Site.class));
        studies   = asList(createNamedInstance("Study A", Study.class));
    }

    public void testGetView() throws Exception {
        request.setMethod("GET");

        expectRefData();

        replayMocks();

        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();
        
        assertEquals("Wrong View Name", "siteCoordinatorDashboard", mv.getViewName());
    }

    public void testOnSubmitView() throws Exception {
        request.setMethod("POST");

        command.apply();
        
        replayMocks();

        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertEquals("Wrong View Name", "siteCoordinatorSchedule", ((RedirectView) mv.getView()).getUrl());
    }

    public void expectRefData() {
        expect(siteDao.getAll()).andReturn(sites);
        expect(studyDao.getAll()).andReturn(studies);
    }

    private class MockableController extends SiteCoordinatorDashboardController {
        public MockableController() {
            setSiteDao(siteDao);
            setStudyDao(studyDao);
        }

        protected Object formBackingObject(HttpServletRequest request) throws Exception {
            return command;
        }
    }
}
