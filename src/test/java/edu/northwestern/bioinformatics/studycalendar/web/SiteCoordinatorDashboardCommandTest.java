package edu.northwestern.bioinformatics.studycalendar.web;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createUserRole;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static org.easymock.EasyMock.expect;

import static java.util.Arrays.asList;
import java.util.List;

public class SiteCoordinatorDashboardCommandTest extends StudyCalendarTestCase {
    private UserDao userDao;
    private StudyDao studyDao;
    private List<Study> studies;
    private List<User> users;
    private List<Site> sites;
    private SiteDao siteDao;

    protected void setUp() throws Exception {
        super.setUp();

        userDao  = registerDaoMockFor(UserDao.class);
        studyDao = registerDaoMockFor(StudyDao.class);
        siteDao  = registerDaoMockFor(SiteDao.class);

        Study study0 = createNamedInstance("Study A", Study.class);
        Site  site0  = createNamedInstance("Mayo Clinic", Site.class);
        User  user0  = createNamedInstance("John", User.class);
        User  user1  = createNamedInstance("John", User.class);

        user0.addUserRole(createUserRole(user0, Role.PARTICIPANT_COORDINATOR, site0));
        user1.addUserRole(createUserRole(user1, Role.PARTICIPANT_COORDINATOR, site0));

        studies = asList(study0);
        sites   = asList(site0);
        users   = asList(user0, user1);
    }

    public void testBuildStudyAssignmentGrid() throws Exception {
        expect(userDao.getAllParticipantCoordinators()).andReturn(users);
        expect(siteDao.getAll()).andReturn(sites);

        replayMocks();
        SiteCoordinatorDashboardCommand newCommand = createCommand();
        verifyMocks();

        assertTrue("Wrong User" , newCommand.getStudyAssignmentGrid().containsKey(users.get(0)));
        assertTrue("Wrong Site", newCommand.getStudyAssignmentGrid().get(users.get(0)).containsKey(sites.get(0)));
    }

    private SiteCoordinatorDashboardCommand createCommand() {
        return new SiteCoordinatorDashboardCommand(userDao, studyDao, siteDao);
    }
}
