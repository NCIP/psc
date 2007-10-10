package edu.northwestern.bioinformatics.studycalendar.web;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import static org.easymock.EasyMock.expect;

import java.util.*;

public class CreateUserCommandTest extends StudyCalendarTestCase {
    private UserService service;
    private SiteDao siteDao;
    List<Site> sites;

    protected void setUp() throws Exception {
        super.setUp();

        service = registerMockFor(UserService.class);
        siteDao = registerDaoMockFor(SiteDao.class);

        sites = Arrays.asList(
                createNamedInstance("Mayo Clinic", Site.class),
                createNamedInstance("Northwestern Clinic", Site.class)
        );
    }

    // TODO: write intrepetRolesGrid first
   /* public void testApplyForNewUser() throws Exception {
        User expectedUser = createUser(null, "Joe", null, true, "pass",Role.STUDY_COORDINATOR);
        CreateUserCommand command = createCommand(new User());
        command.setName(expectedUser.getName());
        command.setUserRoles(expectedUser.getUserRoles());
        command.setActiveFlag(expectedUser.getActiveFlag());
        command.setPassword(expectedUser.getPlainTextPassword());

        expect(siteDao.getAll()).andReturn(sites);
        expect(service.saveUser(expectedUser)).andReturn(expectedUser);
        replayMocks();

        User actualUser = command.apply();
        verifyMocks();

        assertEquals("Different user names", expectedUser.getName(), actualUser.getName());
    }*/

    public void testBuildRolesGrid() throws Exception {
        User expectedUser = createUser(-1, "Joe", -1L, true, "pass", Role.STUDY_ADMIN);
        expectedUser.addUserRole(createUserRole(Role.RESEARCH_ASSOCIATE, sites.get(0)));

        expect(siteDao.getAll()).andReturn(sites);
        replayMocks();
        
        CreateUserCommand command = createCommand(expectedUser);
        verifyMocks();

        Map<Site, Map<Role, CreateUserCommand.RoleCell>> rolesGrid = command.getRolesGrid();
        assertEquals("Roles grid has wrong number of sites", 2 , rolesGrid.size());
        assertEquals("Wrong number checked for site specific Role", Role.values().length, rolesGrid.get(sites.get(0)).size());
        assertTrue("Role should be true for all sites", rolesGrid.get(sites.get(0)).get(Role.STUDY_ADMIN).isSelected());
        assertTrue("Role should be true for all sites", rolesGrid.get(sites.get(1)).get(Role.STUDY_ADMIN).isSelected());
        assertFalse("Role should not be true for this site", rolesGrid.get(sites.get(0)).get(Role.PARTICIPANT_COORDINATOR).isSelected());
        assertTrue("Role should not be true for this site",  rolesGrid.get(sites.get(0)).get(Role.RESEARCH_ASSOCIATE).isSelected());

    }

    public void testInterpretRolesGrid() throws Exception {
        List expectedUserRoles = Arrays.asList(
                createUserRole(Role.STUDY_ADMIN),
                createUserRole(Role.RESEARCH_ASSOCIATE, sites.get(0)),
                createUserRole(Role.PARTICIPANT_COORDINATOR, sites.get(1))
        );
        User expectedUser = createUser(-1, "Joe", -1L, true, "pass");
        expectedUser.setUserRoles(new HashSet(expectedUserRoles));

        expect(siteDao.getAll()).andReturn(sites);
        expect(service.saveUser(expectedUser)).andReturn(expectedUser);
        replayMocks();

        CreateUserCommand command = createCommand(expectedUser);
        command.apply();
        verifyMocks();

        Set<UserRole> actualUserRoles = command.getUserRoles();

        assertEquals("Wrong number of roles", 3, actualUserRoles.size());

        assertTrue("User Role missing", actualUserRoles.contains(expectedUserRoles.get(0)));
        assertTrue("User Role missing", actualUserRoles.contains(expectedUserRoles.get(1)));
        assertTrue("User Role missing", actualUserRoles.contains(expectedUserRoles.get(2)));

        List<UserRole> userRoleList = new ArrayList(actualUserRoles);
        UserRole userRole0 = userRoleList.get(userRoleList.indexOf(expectedUserRoles.get(0)));
        UserRole userRole1 = userRoleList.get(userRoleList.indexOf(expectedUserRoles.get(1)));
        UserRole userRole2 = userRoleList.get(userRoleList.indexOf(expectedUserRoles.get(2)));

        assertEquals("Wrong Role", Role.STUDY_ADMIN, userRole0.getRole());
        assertEquals("Wrong Role", Role.RESEARCH_ASSOCIATE, userRole1.getRole());
        assertEquals("Wrong Role", Role.PARTICIPANT_COORDINATOR, userRole2.getRole());

        assertEquals("Wrong number of sites", 1, userRole1.getSites().size());
        assertEquals("Wrong number of sites", 1, userRole2.getSites().size());

        assertEquals("Wrong Site", sites.get(0), userRole1.getSites().iterator().next());
        assertEquals("Wrong Site", sites.get(1), userRole2.getSites().iterator().next());

    }
    
    public CreateUserCommand createCommand(User user) {
        CreateUserCommand command = new CreateUserCommand(user, siteDao);
        command.setUserService(service);
        command.setSiteDao(siteDao);
        return command;
    }


}
