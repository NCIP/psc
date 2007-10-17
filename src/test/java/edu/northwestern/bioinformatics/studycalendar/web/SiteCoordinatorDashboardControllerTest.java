package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import static org.easymock.EasyMock.expect;

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

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        studyDao = registerDaoMockFor(StudyDao.class);
        userDao  = registerDaoMockFor(UserDao.class);

        controller = new SiteCoordinatorDashboardController();
        controller.setStudyDao(studyDao);
        controller.setUserDao(userDao);
    }

    public void testFormBackingObject() throws Exception {
        List<Study> studies = asList( createNamedInstance("Study A", Study.class));
        List<User> users = asList(createNamedInstance("John", User.class));

        request.setMethod("GET");

        expect(userDao.getAll()).andReturn(users);
        expect(studyDao.getAll()).andReturn(studies);

        replayMocks();

        Map<String, Object> model = controller.handleRequest(request, response).getModel();
        SiteCoordinatorDashboardCommand command = (SiteCoordinatorDashboardCommand) model.get("command");
        verifyMocks();
        assertNotNull("Command is null", command);
    }
}
