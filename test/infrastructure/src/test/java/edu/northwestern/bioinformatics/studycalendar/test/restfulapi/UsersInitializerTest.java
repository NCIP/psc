package edu.northwestern.bioinformatics.studycalendar.test.restfulapi;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import edu.northwestern.bioinformatics.studycalendar.test.integrated.RowPreservingInitializer;
import edu.northwestern.bioinformatics.studycalendar.test.integrated.SchemaInitializerTestCase;
import static org.easymock.EasyMock.expect;

import java.util.Arrays;

/**
 * @author Rhett Sutphin
 */
public class UsersInitializerTest extends SchemaInitializerTestCase {
    private UserDao userDao;
    private UserService userService;
    private SiteDao siteDao;
    private Site site;

    public void setUp() throws Exception {
        super.setUp();
        userDao = registerDaoMockFor(UserDao.class);
        siteDao = registerDaoMockFor(SiteDao.class);
        userService = registerMockFor(UserService.class);
        site = Fixtures.createSite("NU", "IL036");
    }

    @SuppressWarnings({ "ConstantConditions" })
    public void testIsRowPreservingInitializerForUserRoleSitesTable() throws Exception {
        UsersInitializer initializer = createInitializer("");
        assertTrue(initializer instanceof RowPreservingInitializer);
        assertEquals("Wrong table", "user_role_sites", initializer.getTableName());
        assertEquals("Wrong PKs",
            Arrays.asList("user_role_id", "site_id"), initializer.getPrimaryKeyNames());
    }

    public void testCreatesCorrectUserForNonSiteSpecificRole() throws Exception {
        expectNoExistingUsers("ben");
        User expected = Fixtures.createUser("ben", Role.STUDY_COORDINATOR);
        expect(userService.saveUser(expected, "ben", "ben@psctest.example.net")).andReturn(expected);

        replayMocks();
        createInitializer("ben:\n  STUDY_COORDINATOR:\n").oneTimeSetup(connectionSource);
        verifyMocks();
    }

    public void testCreatesCorrectUserForSiteSpecificRole() throws Exception {
        expectNoExistingUsers("alex");
        expectGetSite();
        User expected = Fixtures.createUser("alex", Role.SITE_COORDINATOR);
        expected.getUserRole(Role.SITE_COORDINATOR).addSite(site);

        expect(userService.saveUser(expected, "alex", "alex@psctest.example.net")).andReturn(expected);

        replayMocks();
        createInitializer("alex:\n  SITE_COORDINATOR: ['IL036']\n").oneTimeSetup(connectionSource);
        verifyMocks();
    }

    public void testUpdatesUserIfExists() throws Exception {
        User originalUser = Fixtures.createUser("charlie", Role.SYSTEM_ADMINISTRATOR);
        expect(userDao.getByName("charlie")).andReturn(originalUser);

        expectGetSite();
        User updatedUser = Fixtures.createUser("charlie", Role.SYSTEM_ADMINISTRATOR, Role.SITE_COORDINATOR);
        updatedUser.getUserRole(Role.SITE_COORDINATOR).addSite(site);

        expect(userService.saveUser(updatedUser, "charlie", "charlie@psctest.example.net")).andReturn(updatedUser);

        replayMocks();
        createInitializer("charlie:\n  SYSTEM_ADMINISTRATOR:\n  SITE_COORDINATOR: ['IL036']\n").oneTimeSetup(connectionSource);
        verifyMocks();
    }

    private void expectGetSite() {
        expect(siteDao.getByAssignedIdentifier(site.getAssignedIdentifier())).andReturn(site);
    }

    private void expectNoExistingUsers(String... usernames) {
        for (String username : usernames) {
            expect(userDao.getByName(username)).andReturn(null);
        }
    }

    private UsersInitializer createInitializer(String yaml) throws Exception {
        UsersInitializer initializer = new UsersInitializer();
        initializer.setUserDao(userDao);
        initializer.setUserService(userService);
        initializer.setSiteDao(siteDao);
        initializer.setYamlResource(literalYamlResource(yaml));
        initializer.afterPropertiesSet();
        return initializer;
    }
}
