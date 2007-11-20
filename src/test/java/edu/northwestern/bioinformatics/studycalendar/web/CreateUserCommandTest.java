package edu.northwestern.bioinformatics.studycalendar.web;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import edu.northwestern.bioinformatics.studycalendar.service.UserRoleService;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import static org.easymock.EasyMock.expect;

import java.util.*;

public class CreateUserCommandTest extends StudyCalendarTestCase {
    private UserService userService;
    private SiteDao siteDao;
    List<Site> sites;
    Study study;
    private UserRoleService userRoleService;

    protected void setUp() throws Exception {
        super.setUp();

        siteDao         = registerDaoMockFor(SiteDao.class);
        userService     = registerMockFor(UserService.class);
        userRoleService = registerMockFor(UserRoleService.class);

        sites = Arrays.asList(
                createNamedInstance("Mayo Clinic", Site.class),
                createNamedInstance("Northwestern Clinic", Site.class)
        );

        study = createNamedInstance("Study A", Study.class);
    }

    public void testBuildRolesGrid() throws Exception {
        User expectedUser = createUser(-1, "Joe", -1L, true, Role.STUDY_ADMIN);
        expectedUser.addUserRole(createUserRole(expectedUser, Role.RESEARCH_ASSOCIATE, sites.get(0), sites.get(1)));

        expect(siteDao.getAll()).andReturn(sites);
        replayMocks();
        
        CreateUserCommand command = createCommand(expectedUser);
        verifyMocks();

        Map<Site, Map<Role, CreateUserCommand.RoleCell>> rolesGrid = command.getRolesGrid();
        assertEquals("Roles grid has wrong number of sites", 2 , rolesGrid.size());
        assertEquals("Wrong number checked for site specific Role", Role.values().length, rolesGrid.get(sites.get(0)).size());
        assertTrue("Role should be true for all sites", rolesGrid.get(sites.get(0)).get(Role.STUDY_ADMIN).isSelected());
        assertTrue("Role should be true for all sites", rolesGrid.get(sites.get(1)).get(Role.STUDY_ADMIN).isSelected());
        assertFalse("Role should not be true for this site", rolesGrid.get(sites.get(0)).get(Role.SUBJECT_COORDINATOR).isSelected());
        assertTrue("Role should not be true for this site",  rolesGrid.get(sites.get(0)).get(Role.RESEARCH_ASSOCIATE).isSelected());
        assertTrue("Role should not be true for this site",  rolesGrid.get(sites.get(1)).get(Role.RESEARCH_ASSOCIATE).isSelected());

    }

    public void testInterpretRolesGrid() throws Exception {
        User expectedUser = createUser(-1, "Joe", -1L, true);

        List<UserRole> expectedUserRoles = Arrays.asList(
                createUserRole(expectedUser, Role.STUDY_ADMIN),
                createUserRole(expectedUser, Role.SUBJECT_COORDINATOR, sites.get(0), sites.get(1))
        );
        expectedUser.setUserRoles(new HashSet<UserRole>(expectedUserRoles));

        expect(siteDao.getAll()).andReturn(sites);

        userRoleService.assignUserRole(expectedUser, Role.SUBJECT_COORDINATOR, sites.get(0));
        userRoleService.assignUserRole(expectedUser, Role.SUBJECT_COORDINATOR, sites.get(1));

        userRoleService.removeUserRoleAssignment(expectedUser, Role.SITE_COORDINATOR, sites.get(0));
        userRoleService.removeUserRoleAssignment(expectedUser, Role.SITE_COORDINATOR, sites.get(1));

        userRoleService.removeUserRoleAssignment(expectedUser, Role.RESEARCH_ASSOCIATE, sites.get(0));
        userRoleService.removeUserRoleAssignment(expectedUser, Role.RESEARCH_ASSOCIATE, sites.get(1));

        userRoleService.assignUserRole(expectedUser, Role.STUDY_ADMIN);
        userRoleService.removeUserRoleAssignment(expectedUser, Role.STUDY_COORDINATOR);
        userRoleService.removeUserRoleAssignment(expectedUser, Role.SYSTEM_ADMINISTRATOR);

        replayMocks();

        CreateUserCommand command = createCommand(expectedUser);
        expectedUser.setUserRoles(Collections.<UserRole>emptySet());        // empty user roles so we can test the assign method
        command.assignUserRolesFromRolesGrid(command.getRolesGrid());
        verifyMocks();
    }

    public void testInterpretRolesGridRemoveRole() throws Exception {
        User expectedUser = createUser(-1, "Joe", -1L, true);

        List<UserRole> expectedUserRoles = Arrays.asList(
                createUserRole(expectedUser, Role.STUDY_ADMIN),
                createUserRole(expectedUser, Role.SUBJECT_COORDINATOR, sites.get(0), sites.get(1))
        );
        expectedUser.setUserRoles(new HashSet<UserRole>(expectedUserRoles));

        expect(siteDao.getAll()).andReturn(sites);

        userRoleService.assignUserRole(expectedUser, Role.SUBJECT_COORDINATOR, sites.get(0));
        userRoleService.assignUserRole(expectedUser, Role.SUBJECT_COORDINATOR, sites.get(1));

        userRoleService.removeUserRoleAssignment(expectedUser, Role.SITE_COORDINATOR, sites.get(0));
        userRoleService.removeUserRoleAssignment(expectedUser, Role.SITE_COORDINATOR, sites.get(1));

        userRoleService.removeUserRoleAssignment(expectedUser, Role.RESEARCH_ASSOCIATE, sites.get(0));
        userRoleService.removeUserRoleAssignment(expectedUser, Role.RESEARCH_ASSOCIATE, sites.get(1));

        userRoleService.removeUserRoleAssignment(expectedUser, Role.STUDY_ADMIN);
        userRoleService.removeUserRoleAssignment(expectedUser, Role.STUDY_COORDINATOR);
        userRoleService.removeUserRoleAssignment(expectedUser, Role.SYSTEM_ADMINISTRATOR);

        replayMocks();

        CreateUserCommand command = createCommand(expectedUser);
        command.getRolesGrid().get(sites.get(0)).get(Role.STUDY_ADMIN).setSelected(false);
        command.assignUserRolesFromRolesGrid(command.getRolesGrid());
        verifyMocks();
    }

    public void testInterpretRolesGridAddRole() throws Exception {
        User expectedUser = createUser(-1, "Joe", -1L, true);

        List<UserRole> expectedUserRoles = Arrays.asList(
                createUserRole(expectedUser, Role.SUBJECT_COORDINATOR, sites.get(0), sites.get(1))
        );
        expectedUser.setUserRoles(new HashSet<UserRole>(expectedUserRoles));

        expect(siteDao.getAll()).andReturn(sites);

        userRoleService.assignUserRole(expectedUser, Role.SUBJECT_COORDINATOR, sites.get(0));
        userRoleService.assignUserRole(expectedUser, Role.SUBJECT_COORDINATOR, sites.get(1));

        userRoleService.removeUserRoleAssignment(expectedUser, Role.SITE_COORDINATOR, sites.get(0));
        userRoleService.removeUserRoleAssignment(expectedUser, Role.SITE_COORDINATOR, sites.get(1));

        userRoleService.removeUserRoleAssignment(expectedUser, Role.RESEARCH_ASSOCIATE, sites.get(0));
        userRoleService.removeUserRoleAssignment(expectedUser, Role.RESEARCH_ASSOCIATE, sites.get(1));

        userRoleService.assignUserRole(expectedUser, Role.STUDY_ADMIN);
        userRoleService.removeUserRoleAssignment(expectedUser, Role.STUDY_COORDINATOR);
        userRoleService.removeUserRoleAssignment(expectedUser, Role.SYSTEM_ADMINISTRATOR);

        replayMocks();

        CreateUserCommand command = createCommand(expectedUser);
        command.getRolesGrid().get(sites.get(0)).get(Role.STUDY_ADMIN).setSelected(true);
        command.assignUserRolesFromRolesGrid(command.getRolesGrid());
        verifyMocks();
    }

    public CreateUserCommand createCommand(User user) {
        CreateUserCommand command = new CreateUserCommand(user, siteDao, userService, userRoleService);
        return command;
    }

    public void testUserDefaultsToNewWhenNotSet() throws Exception {
        expect(siteDao.getAll()).andReturn(Collections.<Site>emptyList());
        replayMocks();

        CreateUserCommand command = new CreateUserCommand(null, siteDao, userService, userRoleService);
        assertNotNull(command.getUser());
        assertNull(command.getUser().getId());
        assertNull(command.getUser().getName());
    }
}
