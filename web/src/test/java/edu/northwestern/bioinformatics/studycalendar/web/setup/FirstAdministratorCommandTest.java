package edu.northwestern.bioinformatics.studycalendar.web.setup;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;
import gov.nih.nci.cabig.ctms.suite.authorization.ProvisioningSession;
import gov.nih.nci.cabig.ctms.suite.authorization.ProvisioningSessionFactory;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.security.AuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.User;

import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class FirstAdministratorCommandTest extends WebTestCase {
    private FirstAdministratorCommand command;

    private ProvisioningSession pSession;
    private AuthorizationManager csmAuthorizationManager;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        pSession = registerMockFor(ProvisioningSession.class);
        ProvisioningSessionFactory psFactory = registerMockFor(ProvisioningSessionFactory.class);
        expect(psFactory.createSession(anyLong())).andStubReturn(pSession);

        AuthenticationSystem authenticationSystem = registerMockFor(AuthenticationSystem.class);
        expect(authenticationSystem.usesLocalPasswords()).andStubReturn(true);

        csmAuthorizationManager = registerMockFor(AuthorizationManager.class);

        command = new FirstAdministratorCommand(
            psFactory, csmAuthorizationManager, authenticationSystem);
    }

    public void testUserIsSetToBlank() throws Exception {
        assertNotNull("No user", command.getUser());
        assertNotNull("No user memberships", command.getUser().getMemberships());
        assertTrue("Memberships not empty", command.getUser().getMemberships().isEmpty());
    }

    public void testMayProvisionSysAdmins() throws Exception {
        assertEquals("Wrong number of provisionable roles", 1,
            command.getProvisionableRoles().size());
        assertEquals("Wrong provisionable role",
            "system_administrator", command.getProvisionableRoles().get(0).getKey());
    }

    public void testAutomaticallyProvisionsAsSystemAdmin() throws Exception {
        command.getUser().getCsmUser().setLoginName("newguy");

        User found = AuthorizationObjectFactory.createCsmUser(63L, "newguy");
        expect(csmAuthorizationManager.getUser("newguy")).andReturn(found);
        /* expect */ csmAuthorizationManager.modifyUser(found);

        SuiteRoleMembership srm = new SuiteRoleMembership(SuiteRole.SYSTEM_ADMINISTRATOR, null, null);
        expect(pSession.getProvisionableRoleMembership(SuiteRole.SYSTEM_ADMINISTRATOR)).andReturn(srm);
        /* expect */ pSession.replaceRole(srm);

        replayMocks();
        command.apply();
        verifyMocks();
    }
}
