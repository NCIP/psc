package edu.northwestern.bioinformatics.studycalendar.web;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createUser;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import static org.easymock.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import static java.util.Arrays.asList;
import java.util.List;

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
    private User user;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        siteDao         = registerDaoMockFor(SiteDao.class);
        studyDao        = registerDaoMockFor(StudyDao.class);
        templateService = registerMockFor(TemplateService.class);

        command     = registerMockFor(SiteCoordinatorDashboardCommand.class);

        controller = new MockableController();
        controller.setSiteDao(siteDao);
        controller.setStudyDao(studyDao);
        controller.setTemplateService(templateService);

        user      =  createUser(1, "john", 1L, true, "pass");

        sites     = asList(createNamedInstance("Mayo Clinic", Site.class));
        studies   = asList(createNamedInstance("Study A", Study.class));

        SecurityContextHolderTestHelper.setSecurityContext(user.getName(), user.getPassword());
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
        Study study = setId(1, createNamedInstance("Study A", Study.class));
        request.setMethod("POST");

        command.apply();
        expect(command.getStudy()).andReturn(study);
        replayMocks();

        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();

        assertEquals("Wrong View Name", "siteCoordinatorSchedule", ((RedirectView) mv.getView()).getUrl());
        assertEquals("Wrong View Name", study.getId(), (((RedirectView) mv.getView()).getStaticAttributes().get("study")));
    }

    public void expectRefData() throws Exception{
        expect(siteDao.getAll()).andReturn(sites);
        expect(studyDao.getAll()).andReturn(studies);
        expect(templateService.checkOwnership(user.getName(), studies)).andReturn(studies);
        expect(command.getStudy()).andReturn(studies.get(0));
    }


    protected void tearDown() throws Exception {
        super.tearDown();
        ApplicationSecurityManager.removeUserSession();
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
