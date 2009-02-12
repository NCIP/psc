package edu.northwestern.bioinformatics.studycalendar.service;

import static java.util.Arrays.asList;

import static org.easymock.EasyMock.expect;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createStudySite;

import static java.util.Collections.singleton;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createUserRole;
import edu.northwestern.bioinformatics.studycalendar.dao.UserRoleDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createNamedInstance;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarAuthorizationManager;

import java.util.Collections;

public class UserRoleServiceTest extends StudyCalendarTestCase {
    private SiteService siteService;
    private UserRoleService userRoleService;
    private UserRoleDao userRoleDao;
    private User user2, user0, user1;
    private Site site0, site1;
    private UserRole userRole0, userRole1;
    private UserDao userDao;
    private StudyCalendarAuthorizationManager authorizatioManager;
    private StudySite studySite0;
    private StudySiteService studySiteService;

    protected void setUp() throws Exception {
        super.setUp();

        userDao     = registerDaoMockFor(UserDao.class);
        siteService = registerMockFor(SiteService.class);
        userRoleDao = registerDaoMockFor(UserRoleDao.class);
        studySiteService = registerMockFor(StudySiteService.class);
        authorizatioManager = registerMockFor(StudyCalendarAuthorizationManager.class);

        userRoleService = new UserRoleService();

        userRoleService.setStudyCalendarAuthorizationManager(authorizatioManager);
        userRoleService.setStudySiteService(studySiteService);
        userRoleService.setSiteService(siteService);
        userRoleService.setUserRoleDao(userRoleDao);
        userRoleService.setUserDao(userDao);

        user0 = createNamedInstance("Bob", User.class);
        user1 = createNamedInstance("Steve", User.class);
        user2 = createNamedInstance("John", User.class);

        site0 = createNamedInstance("Northwestern", Site.class);
        site1 = createNamedInstance("Mayo Clinic", Site.class);

        userRole0 = createUserRole(user0, Role.SUBJECT_COORDINATOR, site0, site1);
        userRole1 = createUserRole(user1, Role.STUDY_ADMIN);

        user0.addUserRole(userRole0);
        user1.addUserRole(userRole1);

        studySite0 = createStudySite(createNamedInstance("Study A", Study.class), site0);
        userRole0.addStudySite(studySite0);
    }

    public void testAssignUserRoleNotSiteSpecific() throws Exception {
        Role role = Role.STUDY_ADMIN;

        UserRole userRole = new UserRole(user2, role, site0);

        userRoleDao.save(userRole);
        authorizatioManager.assignCsmGroups(user2,  singleton(userRole));
        replayMocks();

        userRoleService.assignUserRole(user2, role, site0);
        verifyMocks();

        assertEquals("Wrong user role size", 1, user2.getUserRoles().size());

        UserRole actualUserRole = user2.getUserRole(role);
        assertNotNull("User role does not exist", actualUserRole);
    }

    public void testAssignUserRoleSiteSpecific() throws Exception {
        Role role = Role.SUBJECT_COORDINATOR;
        UserRole userRole = new UserRole(user2, role, site0);

        userRoleDao.save(userRole);
        siteService.assignProtectionGroup(site0, user2, role);
        authorizatioManager.assignCsmGroups(user2, singleton(userRole));
        replayMocks();

        userRoleService.assignUserRole(user2, role, site0);
        verifyMocks();

        assertEquals("Wrong user role size", 1, user2.getUserRoles().size());

        UserRole actualUserRole = user2.getUserRole(role);
        assertEquals("Wrong user role site size", 1, actualUserRole.getSites().size());
    }

    public void testRemoveUserRoleAssignmentSiteSpecific() throws Exception {
        Role role = Role.SUBJECT_COORDINATOR;

        expect(studySiteService.getStudySitesForSubjectCoordinator(user0, site0)).andReturn(asList(studySite0));
        userRoleDao.save(userRole0);
        siteService.removeProtectionGroup(site0, user0);
        replayMocks();

        userRoleService.removeUserRoleAssignment(user0, role, site0);
        verifyMocks();

        assertEquals("Wrong user role size", 1, user0.getUserRoles().size());

        UserRole actualUserRole = user0.getUserRole(role);
        assertEquals("Wrong user role site size", 1, actualUserRole.getSites().size());
        assertEquals("Study Sites should be empty", 0, actualUserRole.getStudySites().size());
    }

    public void testRemoveUserRoleAssignmentNotSiteSpecific() throws Exception {
        Role role = Role.STUDY_ADMIN;

        userRoleDao.save(userRole1);
        userDao.save(user1);
        authorizatioManager.assignCsmGroups(user1, Collections.<UserRole>emptySet());
        replayMocks();

        userRoleService.removeUserRoleAssignment(user1, role, site0);
        verifyMocks();

        assertEquals("Wrong user role size", 0, user1.getUserRoles().size());

        UserRole actualUserRole = user1.getUserRole(role);
        assertNull("User role still exists", actualUserRole);
    }
}
