package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import static org.easymock.EasyMock.expect;
import org.springframework.web.servlet.ModelAndView;

import static java.util.Arrays.asList;
import java.util.List;
import java.util.Map;

/**
 * @author John Dzak
 */

public class SiteCoordinatorDashboardControllerTest extends ControllerTestCase {
    SiteCoordinatorDashboardController controller;
    private StudyDao studyDao;
    private UserDao userDao;
    List<User> users;
    private SiteDao siteDao;
    private List<Site> sites;
    private Study study;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        siteDao  = registerDaoMockFor(SiteDao.class);
        userDao  = registerDaoMockFor(UserDao.class);
        studyDao = registerDaoMockFor(StudyDao.class);

        controller = new SiteCoordinatorDashboardController();
        controller.setSiteDao(siteDao);
        controller.setUserDao(userDao);
        controller.setStudyDao(studyDao);

        users    = asList(createNamedInstance("John", User.class));
        sites    = asList(createNamedInstance("Mayo Clinic", Site.class));
        study    = createNamedInstance("Study A", Study.class);
    }

    public void testFormBackingObject() throws Exception {
        request.setMethod("GET");

        expectCommandBuildGrid();
        expectRefData();

        replayMocks();

        Map<String, Object> model = controller.handleRequest(request, response).getModel();
        SiteCoordinatorDashboardCommand command = (SiteCoordinatorDashboardCommand) model.get("command");
        verifyMocks();
        assertNotNull("Command is null", command);
    }

    public void testGetView() throws Exception {
        request.setMethod("GET");

        expectCommandBuildGrid();
        expectRefData();

        replayMocks();

        ModelAndView mv = controller.handleRequest(request, response);
        verifyMocks();
        
        assertEquals("Wrong View Name", "siteCoordinatorDashboard", mv.getViewName());
    }

    private void expectCommandBuildGrid() {
        expect(userDao.getAllParticipantCoordinators()).andReturn(users);
        expect(siteDao.getAll()).andReturn(sites);
        expect(studyDao.getById(1)).andReturn(study);
    }

    public void expectRefData() {
        expect(siteDao.getAll()).andReturn(sites);
    }
}
