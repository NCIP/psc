package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static org.easymock.EasyMock.expect;

import static java.util.Arrays.asList;
import java.util.List;

public class SiteCoordinatorDashboardCommandTest extends StudyCalendarTestCase {
    private UserDao userDao;
    private StudyDao studyDao;

    protected void setUp() throws Exception {
        super.setUp();

        userDao  = registerDaoMockFor(UserDao.class);
        studyDao = registerDaoMockFor(StudyDao.class);
    }

    public void testBuildStudyAssignmentGrid() throws Exception {
        List<Study> studies = asList( createNamedInstance("Study A", Study.class) );
        List<User> users    = asList( createNamedInstance("John", User.class)     );

        expect(userDao.getAll()).andReturn(users);
        expect(studyDao.getAll()).andReturn(studies);

        replayMocks();
        SiteCoordinatorDashboardCommand newCommand = createCommand();
        verifyMocks();

        assertTrue("Wrong User" , newCommand.getStudyAssignmentGrid().containsKey(users.get(0)));
        assertTrue("Wrong Study", newCommand.getStudyAssignmentGrid().get(users.get(0)).containsKey(studies.get(0)));
    }

    private SiteCoordinatorDashboardCommand createCommand() {
        return new SiteCoordinatorDashboardCommand(userDao, studyDao);
    }
}
