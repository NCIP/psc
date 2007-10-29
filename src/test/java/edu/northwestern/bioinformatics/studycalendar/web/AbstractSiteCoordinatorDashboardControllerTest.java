package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.utils.NamedComparator;
import static org.easymock.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import static java.util.Arrays.asList;
import java.util.List;
import java.util.Collections;

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
    private Site site0;
    private User user0, user1;
    private Study study0, study1;

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

        controller = new SimpleSiteCoordinatorDashboardController();
        controller.setSiteDao(siteDao);
        controller.setStudyDao(studyDao);
        controller.setTemplateService(templateService);
        controller.setSiteService(siteService);

        command     = registerMockFor(SimpleSiteCoordinatorCommand.class);

        siteCoordinator =  createUser(1, "john", 1L, true, "pass");

        user0 = createNamedInstance("John", User.class);
        user1 = createNamedInstance("Jake", User.class);
        users = asList(user0, user1);

        site0     = createNamedInstance("Mayo Clinic", Site.class);
        sites     = asList(site0);

        study0    = createNamedInstance("Study A", Study.class);
        study1    = createNamedInstance("Study B", Study.class);
        study1.setAmendment(new Amendment());

        studies   = asList(study0, study1);

        SecurityContextHolderTestHelper.setSecurityContext(siteCoordinator.getName(), siteCoordinator.getPassword());
    }

    public void testGetRefData() throws Exception {
        request.setMethod("GET");
        expectRefData();
        replayMocks();
        
        controller.handleRequest(request, response);
        verifyMocks();
    }

    public void testGetSiteCoordinator() throws Exception {
        expect(userDao.getByName(siteCoordinator.getName())).andReturn(siteCoordinator);
        replayMocks();

        User actualSiteCoord = controller.getSiteCoordinator();
        verifyMocks();
        assertEquals("Wrong site coordinator", actualSiteCoord.getName(), siteCoordinator.getName());
    }

    public void testGetAssignableStudies() throws Exception {
        expect(studyDao.getAll()).andReturn(studies);
        expect(templateService.checkOwnership(siteCoordinator.getName(), studies)).andReturn(studies);
        replayMocks();

        List<Study> actualAssignableStudies = controller.getAssignableStudies(siteCoordinator);
        verifyMocks();

        assertEquals("Wrong site", study1.getName(), actualAssignableStudies.get(0).getName());
    }

    public void testGetAssignableSites() {
        expect(siteService.getSitesForSiteCd(siteCoordinator.getName())).andReturn(sites);
        replayMocks();

        List<Site> sites = controller.getAssignableSites(siteCoordinator);
        verifyMocks();

        assertEquals("Wrong site", site0.getName(), sites.get(0).getName());
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
            throw new UnsupportedOperationException();
        }

        public List<Site> getAssignableSites() {
            throw new UnsupportedOperationException();
        }

        public List<User> getAssignableUsers() {
            throw new UnsupportedOperationException();
        }

        public void apply() throws Exception {
            throw new UnsupportedOperationException();
        }
    }
}
