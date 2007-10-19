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

/**
 * @author John Dzak
 */
public class SiteCoordinatorDashboardCommandTest extends StudyCalendarTestCase {
    private SiteDao siteDao;
    private UserRoleDao userRoleDao;
    private Study study;
    private List<Site> sites;
    private List<UserRole> userRoles;
    private User user0, user1;
    private Site site0, site1;


    protected void setUp() throws Exception {
        super.setUp();

        siteDao     = registerDaoMockFor(SiteDao.class);
        userRoleDao = registerDaoMockFor(UserRoleDao.class);

        study = createNamedInstance("Study A", Study.class);

        site0  = createNamedInstance("Mayo Clinic" , Site.class);
        site1  = createNamedInstance("Northwestern", Site.class);

        user0  = createNamedInstance("John", User.class);
        user1  = createNamedInstance("Jake", User.class);

        UserRole role0 = createUserRole(user0, Role.PARTICIPANT_COORDINATOR, site0, site1);
        role0.setStudySites(asList(createStudySite(study, site0)));

        UserRole role1 = createUserRole(user1, Role.PARTICIPANT_COORDINATOR, site0);

        user0.addUserRole(role0);
        user1.addUserRole(role1);

        sites     = asList(site0, site1);
        userRoles = asList(role0, role1);
    }

    public void testBuildStudyAssignmentGrid() throws Exception {
        expect(userRoleDao.getAllParticipantCoordinatorUserRoles()).andReturn(userRoles);
        expect(siteDao.getAll()).andReturn(sites);

        replayMocks();
        SiteCoordinatorDashboardCommand newCommand = createCommand();
        verifyMocks();

        Map<User, Map<Site,SiteCoordinatorDashboardCommand.StudyAssignmentCell>> studyAssignmentGrid = newCommand.getStudyAssignmentGrid();

        assertTrue("No User", studyAssignmentGrid.containsKey(user0));
        assertTrue("No User", studyAssignmentGrid.containsKey(user1));

        assertTrue("No Site", studyAssignmentGrid.get(user0).containsKey(site0));
        assertTrue("No Site", studyAssignmentGrid.get(user1).containsKey(site0));

        assertTrue("Should be selected"     , studyAssignmentGrid.get(user0).get(site0).isSelected());
        assertFalse("Should not be selected", studyAssignmentGrid.get(user0).get(site1).isSelected());
        assertFalse("Should not be selected", studyAssignmentGrid.get(user1).get(site0).isSelected());
        assertFalse("Should not be selected", studyAssignmentGrid.get(user1).get(site1).isSelected());

        assertTrue("Site Access should be allowed"     , studyAssignmentGrid.get(user0).get(site0).isSiteAccessAllowed());
        assertTrue("Site Access should be allowed"     , studyAssignmentGrid.get(user0).get(site1).isSiteAccessAllowed());
        assertTrue("Site Access should be allowed"     , studyAssignmentGrid.get(user1).get(site0).isSiteAccessAllowed());
        assertFalse("Site Access should not be allowed", studyAssignmentGrid.get(user1).get(site1).isSiteAccessAllowed());
    }

    private SiteCoordinatorDashboardCommand createCommand() {
        return new SiteCoordinatorDashboardCommand(siteDao, userRoleDao, study);
    }
}
