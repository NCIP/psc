package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserRoleDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
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
    private SiteDao siteDao;
    private UserRoleDao userRoleDao;
    private List<Site> sites;
    private List<UserRole> userRoles;
    private List<Study> studies;
    private Study study;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        siteDao     = registerDaoMockFor(SiteDao.class);
        studyDao    = registerDaoMockFor(StudyDao.class);
        userRoleDao = registerDaoMockFor(UserRoleDao.class);

        controller = new SiteCoordinatorDashboardController();
        controller.setSiteDao(siteDao);
        controller.setStudyDao(studyDao);
        controller.setUserRoleDao(userRoleDao);

        User user0     = createNamedInstance("John", User.class);

        study     = setId(1,createNamedInstance("Study A", Study.class));
        userRoles = asList(Fixtures.createUserRole(user0, Role.PARTICIPANT_COORDINATOR));
        sites     = asList(createNamedInstance("Mayo Clinic", Site.class));
        studies   = asList(study);

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
        expect(userRoleDao.getAllParticipantCoordinators()).andReturn(userRoles);
        expect(siteDao.getAll()).andReturn(sites);
        expect(studyDao.getById(study.getId())).andReturn(study);
    }

    public void expectRefData() {
        expect(siteDao.getAll()).andReturn(sites);
        expect(studyDao.getAll()).andReturn(studies);
    }
}
