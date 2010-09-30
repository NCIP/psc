package edu.northwestern.bioinformatics.studycalendar.test.restfulapi;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.test.integrated.RowPreservingInitializer;
import edu.northwestern.bioinformatics.studycalendar.test.integrated.SchemaInitializerTestCase;
import gov.nih.nci.cabig.ctms.suite.authorization.ProvisioningSession;
import gov.nih.nci.cabig.ctms.suite.authorization.ProvisioningSessionFactory;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembershipLoader;
import gov.nih.nci.security.AuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.User;
import gov.nih.nci.security.exceptions.CSTransactionException;
import org.easymock.classextension.EasyMock;

import java.util.Arrays;
import java.util.Collections;

import static gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole.*;
import static java.util.Collections.singletonMap;
import static org.easymock.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class UsersInitializerTest extends SchemaInitializerTestCase {
    private AuthorizationManager csmAuthorizationManager;
    private SuiteRoleMembershipLoader srmLoader;
    private ProvisioningSessionFactory psFactory;
    private ProvisioningSession pSession;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        psFactory = registerMockFor(ProvisioningSessionFactory.class);
        pSession = registerMockFor(ProvisioningSession.class);
        csmAuthorizationManager = registerMockFor(AuthorizationManager.class);
        srmLoader = registerMockFor(SuiteRoleMembershipLoader.class);

        expect(psFactory.createSession(EasyMock.anyLong())).andStubReturn(pSession);
        expect(srmLoader.getProvisioningRoleMemberships(EasyMock.anyLong())).
            andStubReturn(Collections.<SuiteRole, SuiteRoleMembership>emptyMap());
    }

    @SuppressWarnings({ "ConstantConditions" })
    public void testIsRowPreservingInitializerForUserRoleSitesTable() throws Exception {
        UsersInitializer initializer = createInitializer("");
        assertTrue(initializer instanceof RowPreservingInitializer);
        assertEquals("Wrong table", "csm_user", initializer.getTableName());
        assertEquals("Wrong PKs", Arrays.asList("user_id"), initializer.getPrimaryKeyNames());
    }

    public void testCreatesCorrectUserForNonSiteSpecificRole() throws Exception {
        expectNoExistingUsers("ben");
        expectCreateAndReloadUser("ben", 5);
        expect(psFactory.createSession(5)).andReturn(pSession);
        /* expect */ pSession.replaceRole(
            new SuiteRoleMembership(SYSTEM_ADMINISTRATOR, null, null));

        replayMocks();
        createInitializer("ben:\n  roles:\n    system_administrator:\n").
            oneTimeSetup(connectionSource);
        verifyMocks();
    }

    public void testCreatesCorrectUserForSiteSpecificRole() throws Exception {
        expectNoExistingUsers("alex");
        expectCreateAndReloadUser("alex", 9);

        SuiteRoleMembership expected = new SuiteRoleMembership(STUDY_QA_MANAGER, null, null);
        expected.forSites("IL036");
        /* expect */ pSession.replaceRole(expected);

        replayMocks();
        createInitializer("alex:\n  roles:\n    study_qa_manager:\n      sites: ['IL036']\n").
            oneTimeSetup(connectionSource);
        verifyMocks();
    }

    public void testCreatesCorrectUserForSiteAnsStudySpecificRole() throws Exception {
        expectNoExistingUsers("doug");
        expectCreateAndReloadUser("doug", 7);

        SuiteRoleMembership expected =
            new SuiteRoleMembership(STUDY_CALENDAR_TEMPLATE_BUILDER, null, null);
        expected.forSites("IL036");
        expected.forAllStudies();
        /* expect */ pSession.replaceRole(expected);

        replayMocks();
        createInitializer("doug:\n  roles:\n    study_calendar_template_builder:\n      sites: ['IL036']\n      studies: ['__ALL__']\n").
            oneTimeSetup(connectionSource);
        verifyMocks();
    }

    public void testUpdatesUserIfExists() throws Exception {
        User originalUser = AuthorizationObjectFactory.createCsmUser(3, "charlie");
        expect(csmAuthorizationManager.getUser("charlie")).andReturn(originalUser);
        expect(srmLoader.getProvisioningRoleMemberships(3)).andReturn(singletonMap(
            SYSTEM_ADMINISTRATOR, new SuiteRoleMembership(SYSTEM_ADMINISTRATOR, null, null)));
        /* expect */ pSession.deleteRole(SYSTEM_ADMINISTRATOR);

        /* expect */ pSession.replaceRole(
            new SuiteRoleMembership(SYSTEM_ADMINISTRATOR, null, null));
        /* expect */ pSession.replaceRole(
            new SuiteRoleMembership(BUSINESS_ADMINISTRATOR, null, null));

        replayMocks();
        createInitializer("charlie:\n  roles:\n    system_administrator:\n    business_administrator:\n").
            oneTimeSetup(connectionSource);
        verifyMocks();
    }

    public void testBuildUserSetsUsername() throws Exception {
        User built = createInitializer("").buildUser("earl");
        assertEquals("Username wrong", "earl", built.getLoginName());
    }

    public void testBuildUserSetsFirstName() throws Exception {
        User built = createInitializer("").buildUser("earl");
        assertEquals("First name wrong", "Earl", built.getFirstName());
    }

    public void testBuildUserSetsLastName() throws Exception {
        User built = createInitializer("").buildUser("earl");
        assertEquals("Last name wrong", "User", built.getLastName());
    }

    ////// HELPERS

    private void expectNoExistingUsers(String... usernames) {
        for (String username : usernames) {
            expect(csmAuthorizationManager.getUser(username)).andReturn(null);
        }
    }

    private void expectCreateAndReloadUser(String username, int expectedUserId) throws CSTransactionException {
        User expected = AuthorizationObjectFactory.createCsmUser(username);
        /* expect */ csmAuthorizationManager.createUser(expected);
        User expectedReloaded = AuthorizationObjectFactory.createCsmUser(expectedUserId, username);
        expect(csmAuthorizationManager.getUser(username)).andReturn(expectedReloaded);
    }

    private UsersInitializer createInitializer(String yaml) throws Exception {
        UsersInitializer initializer = new UsersInitializer();
        initializer.setProvisioningSessionFactory(psFactory);
        initializer.setCsmAuthorizationManager(csmAuthorizationManager);
        initializer.setSuiteRoleMembershipLoader(srmLoader);
        initializer.setYamlResource(literalYamlResource(yaml));
        initializer.afterPropertiesSet();
        return initializer;
    }
}
