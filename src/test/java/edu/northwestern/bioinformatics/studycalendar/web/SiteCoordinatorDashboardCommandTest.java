package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserRoleDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static org.easymock.EasyMock.expect;

import static java.util.Arrays.asList;
import java.util.List;
import java.util.Map;

public class SiteCoordinatorDashboardCommandTest extends StudyCalendarTestCase {
    private SiteDao siteDao;
    private UserRoleDao userRoleDao;
    private Study study;
    private List<User> users;
    private List<Site> sites;
    private List<UserRole> userRoles;


    protected void setUp() throws Exception {
        super.setUp();

        siteDao     = registerDaoMockFor(SiteDao.class);
        userRoleDao = registerDaoMockFor(UserRoleDao.class);

        study = createNamedInstance("Study A", Study.class);

        Site  site0  = createNamedInstance("Mayo Clinic" , Site.class);
        Site  site1  = createNamedInstance("Northwestern", Site.class);

        User  user0  = createNamedInstance("John", User.class);
        User  user1  = createNamedInstance("Jake", User.class);

        UserRole role0 = createUserRole(user0, Role.PARTICIPANT_COORDINATOR, site0, site1);
        role0.setStudySites(asList(createStudySite(study, site0)));

        UserRole role1 = createUserRole(user1, Role.PARTICIPANT_COORDINATOR, site0);

        user0.addUserRole(role0);
        user1.addUserRole(role1);

        sites     = asList(site0, site1);
        users     = asList(user0, user1);
        userRoles = asList(role0, role1);
    }

    public void testBuildStudyAssignmentGrid() throws Exception {
        expect(userRoleDao.getAllParticipantCoordinatorUserRoles()).andReturn(userRoles);
        expect(siteDao.getAll()).andReturn(sites);

        replayMocks();
        SiteCoordinatorDashboardCommand newCommand = createCommand();
        verifyMocks();

        Map<User, Map<Site,SiteCoordinatorDashboardCommand.StudyAssignmentCell>> studyAssignmentGrid = newCommand.getStudyAssignmentGrid();

        assertTrue("No User", studyAssignmentGrid.containsKey(users.get(0)));
        assertTrue("No User", studyAssignmentGrid.containsKey(users.get(1)));

        assertTrue("No Site", studyAssignmentGrid.get(users.get(0)).containsKey(sites.get(0)));
        assertTrue("No Site", studyAssignmentGrid.get(users.get(1)).containsKey(sites.get(0)));

        assertTrue("Should be selected"     , studyAssignmentGrid.get(users.get(0)).get(sites.get(0)).isSelected());
        assertFalse("Should not be selected", studyAssignmentGrid.get(users.get(0)).get(sites.get(1)).isSelected());
        assertFalse("Should not be selected", studyAssignmentGrid.get(users.get(1)).get(sites.get(0)).isSelected());
        assertFalse("Should not be selected", studyAssignmentGrid.get(users.get(1)).get(sites.get(1)).isSelected());

        assertTrue("Site Access should be allowed"     , studyAssignmentGrid.get(users.get(0)).get(sites.get(0)).isSiteAccessAllowed());
        assertTrue("Site Access should be allowed"     , studyAssignmentGrid.get(users.get(0)).get(sites.get(1)).isSiteAccessAllowed());
        assertTrue("Site Access should be allowed"     , studyAssignmentGrid.get(users.get(1)).get(sites.get(0)).isSiteAccessAllowed());
        assertFalse("Site Access should not be allowed", studyAssignmentGrid.get(users.get(1)).get(sites.get(1)).isSiteAccessAllowed());
    }

    private SiteCoordinatorDashboardCommand createCommand() {
        return new SiteCoordinatorDashboardCommand(siteDao, userRoleDao, study);
    }
}
