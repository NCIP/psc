package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserRoleDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static org.easymock.EasyMock.expect;

import static java.util.Arrays.asList;
import java.util.*;

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
    private UserRole role0, role1;


    protected void setUp() throws Exception {
        super.setUp();

        siteDao     = registerDaoMockFor(SiteDao.class);
        userRoleDao = registerDaoMockFor(UserRoleDao.class);

        study = createNamedInstance("Study A", Study.class);

        site0  = createNamedInstance("Mayo Clinic" , Site.class);
        site1  = createNamedInstance("Northwestern", Site.class);

        user0  = createNamedInstance("John", User.class);
        user1  = createNamedInstance("Jake", User.class);

        user0.addUserRole(role0);
        user1.addUserRole(role1);

        role0 = createUserRole(user0, Role.PARTICIPANT_COORDINATOR, site0, site1);
        role0.setStudySites(new ArrayList(asList(createStudySite(study, site0))));

        role1 = createUserRole(user1, Role.PARTICIPANT_COORDINATOR, site0);

        sites     = asList(site0, site1);
        userRoles = asList(role0, role1);
    }

    public void testBuildStudyAssignmentGrid() throws Exception {
        expectBuildStudyAssignmentGrid();

        replayMocks();
        SiteCoordinatorDashboardCommand command = createCommand();
        verifyMocks();

        Map<User, Map<Site,SiteCoordinatorDashboardCommand.StudyAssignmentCell>> studyAssignmentGrid = command.getStudyAssignmentGrid();

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

    public void testApply() throws Exception {
        expectBuildStudyAssignmentGrid();
        expectApply();

        replayMocks();

        SiteCoordinatorDashboardCommand command = createCommand();
        command.apply();
        verifyMocks();
    }

    public void testIsSelectedPos() throws Exception {
        expectBuildStudyAssignmentGrid();
        replayMocks();
        
        boolean result = createCommand().isSiteSelected(role0, study, site0);
        verifyMocks();

        assertTrue("Site should be selected", result);
    }

    public void testIsSelectedNeg() throws Exception {
        expectBuildStudyAssignmentGrid();
        replayMocks();

        boolean result = createCommand().isSiteSelected(role1, study, site0);
        verifyMocks();

        assertFalse("Site should be selected", result);
    }

    public void testIsSiteAccessAllowedPos() throws Exception {
        expectBuildStudyAssignmentGrid();
        replayMocks();

        boolean result = createCommand().isSiteAccessAllowed(role0, site0);
        verifyMocks();

        assertTrue("Site access should be allowed", result);
    }

    public void testIsSiteAccessAllowedNeg() throws Exception {
        expectBuildStudyAssignmentGrid();
        replayMocks();

        boolean result = createCommand().isSiteAccessAllowed(role0, site1);
        verifyMocks();

        assertTrue("Site access should not be allowed", result);
    }

    protected void expectBuildStudyAssignmentGrid() {
        expect(userRoleDao.getAllParticipantCoordinators()).andReturn(userRoles);
        expect(siteDao.getAll()).andReturn(sites);
    }

    private void expectApply() {
        expect(userRoleDao.getByUserAndRole(user0, Role.PARTICIPANT_COORDINATOR)).andReturn(role0);
        expect(userRoleDao.getByUserAndRole(user1, Role.PARTICIPANT_COORDINATOR)).andReturn(role1);
        userRoleDao.save(role0);
        userRoleDao.save(role1);
    }

    private SiteCoordinatorDashboardCommand createCommand() {
        return new SiteCoordinatorDashboardCommand(siteDao, userRoleDao, study);
    }
}
