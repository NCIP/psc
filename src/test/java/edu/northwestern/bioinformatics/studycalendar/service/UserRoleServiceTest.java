package edu.northwestern.bioinformatics.studycalendar.service;

import static java.util.Collections.singleton;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createUserRole;
import edu.northwestern.bioinformatics.studycalendar.dao.UserRoleDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;

import java.util.Collections;
import java.util.HashSet;
import java.util.Arrays;

public class UserRoleServiceTest extends StudyCalendarTestCase {
    private SiteService siteService;
    private UserRoleService userRoleService;
    private UserRoleDao userRoleDao;
    private User user0, user1, user2;
    private Site site0, site1;
    private UserRole userRole0, userRole1;
    private UserDao userDao;
    private StudyCalendarAuthorizationManager authorizatioManager;

    protected void setUp() throws Exception {
        super.setUp();

        userDao     = registerDaoMockFor(UserDao.class);
        siteService = registerMockFor(SiteService.class);
        userRoleDao = registerDaoMockFor(UserRoleDao.class);
        authorizatioManager = registerMockFor(StudyCalendarAuthorizationManager.class);

        userRoleService = new UserRoleService();

        userRoleService.setStudyCalendarAuthorizationManager(authorizatioManager);
        userRoleService.setSiteService(siteService);
        userRoleService.setUserRoleDao(userRoleDao);
        userRoleService.setUserDao(userDao);

        user0 = createNamedInstance("John", User.class);
        user1 = createNamedInstance("Bob", User.class);
        user2 = createNamedInstance("Steve", User.class);

        site0 = createNamedInstance("Northwestern", Site.class);
        site1 = createNamedInstance("Mayo Clinic", Site.class);

        userRole0 = createUserRole(user1, Role.PARTICIPANT_COORDINATOR, site0, site1);
        userRole1 = createUserRole(user2, Role.STUDY_ADMIN);

        user1.addUserRole(userRole0);
        user2.addUserRole(userRole1);
    }

    public void testAssignUserRoleNotSiteSpecific() throws Exception {
        Role role = Role.STUDY_ADMIN;

        UserRole userRole = new UserRole(user0, role, site0);

        userRoleDao.save(userRole);
        authorizatioManager.assignCsmGroups(user0,  singleton(userRole));
        replayMocks();

        userRoleService.assignUserRole(user0, role, site0);
        verifyMocks();

        assertEquals("Wrong user role size", 1, user0.getUserRoles().size());

        UserRole actualUserRole = UserRole.findByRole(user0.getUserRoles(), role);
        assertNotNull("User role does not exist", actualUserRole);
    }

    public void testAssignUserRoleSiteSpecific() throws Exception {
        Role role = Role.PARTICIPANT_COORDINATOR;
        UserRole userRole = new UserRole(user0, role, site0);

        userRoleDao.save(userRole);
        siteService.assignProtectionGroup(site0, user0, role);
        authorizatioManager.assignCsmGroups(user0, singleton(userRole));
        replayMocks();

        userRoleService.assignUserRole(user0, role, site0);
        verifyMocks();

        assertEquals("Wrong user role size", 1, user0.getUserRoles().size());

        UserRole actualUserRole = UserRole.findByRole(user0.getUserRoles(), role);
        assertEquals("Wrong user role site size", 1, actualUserRole.getSites().size());
    }

    public void testRemoveUserRoleAssignmentSiteSpecific() throws Exception {
        Role role = Role.PARTICIPANT_COORDINATOR;

        userRoleDao.save(userRole0);
        siteService.removeProtectionGroup(site0, user1);
        replayMocks();

        userRoleService.removeUserRoleAssignment(user1, role, site0);
        verifyMocks();

        assertEquals("Wrong user role size", 1, user1.getUserRoles().size());

        UserRole actualUserRole = UserRole.findByRole(user1.getUserRoles(), role);
        assertEquals("Wrong user role site size", 1, actualUserRole.getSites().size());
    }

    public void testRemoveUserRoleAssignmentNotSiteSpecific() throws Exception {
        Role role = Role.STUDY_ADMIN;

        userRoleDao.save(userRole1);
        userDao.save(user2);
        authorizatioManager.assignCsmGroups(user2, Collections.<UserRole>emptySet());
        replayMocks();

        userRoleService.removeUserRoleAssignment(user2, role, site0);
        verifyMocks();

        assertEquals("Wrong user role size", 0, user2.getUserRoles().size());

        UserRole actualUserRole = UserRole.findByRole(user2.getUserRoles(), role);
        assertNull("User role still exists", actualUserRole);
    }
}
