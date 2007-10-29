package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.SecurityContextHolderTestHelper;
import static org.easymock.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import static java.util.Arrays.asList;
import java.util.List;

/**
 * @author John Dzak
 */
public class AbstractSiteCoordinatorDashboardControllerTest extends ControllerTestCase {
    AbstractSiteCoordinatorDashboardController controller;
    private StudyDao studyDao;
    private UserDao userDao;
    private SiteDao siteDao;
    private SiteService siteService;
    private TemplateService templateService;
    private UserService userService;

    private AbstractSiteCoordinatorDashboardCommand command;


    private List<Site> sites;
    private List<Study> studies;
    private User siteCoordinator;
    private User user0, user1;

    private List<User> users;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        siteDao         = registerDaoMockFor(SiteDao.class);
        userDao         = registerDaoMockFor(UserDao.class);
        studyDao        = registerDaoMockFor(StudyDao.class);
        siteService     = registerMockFor(SiteService.class);
        templateService = registerMockFor(TemplateService.class);
        userService     = registerMockFor(UserService.class);

        command     = registerMockFor(SimpleSiteCoordinatorCommand.class);

        controller = new SimpleSiteCoordinatorDashboardController();
        controller.setSiteDao(siteDao);
        controller.setStudyDao(studyDao);
        controller.setTemplateService(templateService);
        controller.setSiteService(siteService);

        siteCoordinator =  createUser(1, "john", 1L, true, "pass");

        user0 = createNamedInstance("John", User.class);
        user1 = createNamedInstance("Jake", User.class);
        users = asList(user0, user1);

        sites     = asList(createNamedInstance("Mayo Clinic", Site.class));
        studies   = asList(createNamedInstance("Study A", Study.class));

        SecurityContextHolderTestHelper.setSecurityContext(siteCoordinator.getName(), siteCoordinator.getPassword());
    }

    public void testGetRefData() throws Exception {
        request.setMethod("GET");
        expectRefData();
        replayMocks();
        
        controller.handleRequest(request, response);
        verifyMocks();
    }

    public void expectRefData() throws Exception{
        expect(command.getAssignableStudies()).andReturn(studies);
        expect(command.getAssignableSites()).andReturn(sites);
        expect(command.getAssignableUsers()).andReturn(users);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        ApplicationSecurityManager.removeUserSession();
    }

    private class SimpleSiteCoordinatorDashboardController extends AbstractSiteCoordinatorDashboardController {
        public SimpleSiteCoordinatorDashboardController() {
            setSiteDao(siteDao);
            setStudyDao(studyDao);
            setUserDao(userDao);
            setUserService(userService);
            setSuccessView("foo");
        }

        protected Object formBackingObject(HttpServletRequest request) throws Exception {
            return command;
        }
    }

    private class SimpleSiteCoordinatorCommand implements AbstractSiteCoordinatorDashboardCommand {

        public List<Study> getAssignableStudies() {
            return null;
        }

        public List<Site> getAssignableSites() {
            return null;
        }

        public List<User> getAssignableUsers() {
            return null;
        }

        public void apply() throws Exception {}
    }
}
