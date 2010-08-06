package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.AuthorizationScopeMappings;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.PscUserBuilder;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;
import gov.nih.nci.cabig.ctms.suite.authorization.ProvisioningSession;
import gov.nih.nci.cabig.ctms.suite.authorization.ProvisioningSessionFactory;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.security.AuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.User;
import org.json.JSONObject;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.anyLong;
import static org.easymock.classextension.EasyMock.expectLastCall;

/**
 * @author Rhett Sutphin
 */
public class ProvisionUserCommandTest extends WebTestCase {
    private ProvisionUserCommand command;

    private User csmUser;
    private PscUser pscUser;
    private Site austin, sanAntonio;

    private ProvisioningSession pSession;
    private AuthorizationManager authorizationManager;
    private ProvisioningSessionFactory psFactory;
    private AuthenticationSystem authenticationSystem;
    private SiteDao siteDao;

    private Errors errors;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        csmUser = new User();
        csmUser.setUserId(15L);
        csmUser.setLoginName("jo");
        csmUser.setUpdateDate(new Date()); // or CSM pukes

        pscUser = new PscUser(csmUser, Collections.<SuiteRole, SuiteRoleMembership>emptyMap());

        authorizationManager = registerMockFor(AuthorizationManager.class);
        authorizationManager.modifyUser(csmUser);
        expectLastCall().asStub();
        expect(authorizationManager.getUser(csmUser.getLoginName())).andStubReturn(null); // for validation

        psFactory = registerMockFor(ProvisioningSessionFactory.class);
        pSession = registerMockFor(ProvisioningSession.class);
        expect(psFactory.createSession(anyLong())).andStubReturn(pSession);

        authenticationSystem = registerMockFor(AuthenticationSystem.class);
        expect(authenticationSystem.usesLocalPasswords()).andStubReturn(true);

        errors = new MapBindingResult(new HashMap(), "?");

        austin = Fixtures.createSite("A", "i-a");
        sanAntonio = Fixtures.createSite("SA", "i-sa");
        siteDao = new SiteDao() {
            @Override
            public List<Site> getAll() {
                return Arrays.asList(austin, sanAntonio);
            }
        };

        command = actual(pscUser);
    }

    private ProvisionUserCommand actual(PscUser existingUser) {
        return actual(existingUser, new PscUserBuilder("sam").
            add(PscRole.USER_ADMINISTRATOR).forAllSites().
            toUser());
    }

    private ProvisionUserCommand actual(PscUser existingUser, PscUser provisioner) {
        return ProvisionUserCommand.create(existingUser,
            psFactory, authorizationManager, authenticationSystem, siteDao,
            provisioner
        );
    }

    ////// create

    public void testCreateWithoutUserSetsBlankUserInfo() throws Exception {
        ProvisionUserCommand actual = actual(null);

        assertNotNull("User not created", actual.getUser());
        assertNotNull("Current should be set", actual.getCurrentRoles());
        assertTrue("Current should be set empty", actual.getCurrentRoles().isEmpty());
    }

    public void testCreateForSysAdminProvisioner() throws Exception {
        PscUser provisioner = new PscUserBuilder("jo").
            add(PscRole.SYSTEM_ADMINISTRATOR).
            toUser();
        ProvisionUserCommand actual = actual(pscUser, provisioner);

        assertEquals("Wrong number of provisionable roles: " + actual.getProvisionableRoles(),
            2, actual.getProvisionableRoles().size());
        assertMayProvision(SuiteRole.USER_ADMINISTRATOR, actual);
        assertMayProvision(SuiteRole.SYSTEM_ADMINISTRATOR, actual);

        assertEquals("Should not be able to provision for any specific sites",
            0, actual.getProvisionableSites().size());
        assertTrue("Should be able to provision for \"all sites\"",
            actual.getCanProvisionAllSites());

        /* TODO
        assertEquals("Should not be able to provision for any specific studies",
            0, actual.getProvisionableStudies().size());
        assertFalse("Should be able to provision for \"all sites\"",
            actual.getCanProvisionAllStudies());
         */
    }

    private void assertMayProvision(SuiteRole expectedRole, ProvisionUserCommand actual) {
        assertTrue("Should be able to provision " + expectedRole,
            actual.getProvisionableRoles().contains(new ProvisioningRole(expectedRole)));
    }

    public void testCreateForUnlimitedUserAdministrator() throws Exception {
        PscUser provisioner = new PscUserBuilder("jo").
            add(PscRole.USER_ADMINISTRATOR).forAllSites().
            toUser();
        ProvisionUserCommand actual = actual(pscUser, provisioner);

        assertEquals("Wrong number of provisionable roles: " + actual.getProvisionableRoles(),
            SuiteRole.values().length, actual.getProvisionableRoles().size());

        assertEquals("Should be able to provision for any specific site",
            2, actual.getProvisionableSites().size());
        assertTrue("Should be able to provision for austin",
            actual.getProvisionableSites().contains(austin));
        assertTrue("Should be able to provision for san antonio",
            actual.getProvisionableSites().contains(sanAntonio));
        assertTrue("Should be able to provision for \"all sites\"",
            actual.getCanProvisionAllSites());

        /* TODO
        assertEquals("Should not be able to provision for all specific studies",
            0, actual.getProvisionableStudies().size());
        assertTrue("Should be able to provision for \"all studies\"",
            actual.getCanProvisionAllStudies());
         */
    }

    public void testCreateForSiteLimitedUserAdministrator() throws Exception {
        PscUser provisioner = new PscUserBuilder("jo").
            add(PscRole.USER_ADMINISTRATOR).forSites(sanAntonio).
            toUser();
        ProvisionUserCommand actual = actual(pscUser, provisioner);

        assertEquals("Wrong number of provisionable roles: " + actual.getProvisionableRoles(),
            SuiteRole.values().length, actual.getProvisionableRoles().size());

        assertEquals("Should be able to provision for the specific site",
            1, actual.getProvisionableSites().size());
        assertTrue("Should be able to provision for san antonio",
            actual.getProvisionableSites().contains(sanAntonio));
        assertFalse("Should not be able to provision for \"all sites\"",
            actual.getCanProvisionAllSites());

        /* TODO
        assertEquals("Should not be able to provision for all specific studies",
            0, actual.getProvisionableStudies().size());
        assertTrue("Should be able to provision for \"all studies\"",
            actual.getCanProvisionAllStudies());
         */
    }

    public void testCreateForRandomProvisioner() throws Exception {
        PscUser provisioner = new PscUserBuilder("jo").
            add(PscRole.SUBJECT_MANAGER).forSites(sanAntonio).
            toUser();
        ProvisionUserCommand actual = actual(pscUser, provisioner);

        assertEquals("Wrong number of provisionable roles: " + actual.getProvisionableRoles(),
            0, actual.getProvisionableRoles().size());
    }

    public void testCreateForNoProvisionerIsBlank() throws Exception {
        ProvisionUserCommand actual = actual(pscUser, null);

        assertEquals("Wrong number of provisionable roles: " + actual.getProvisionableRoles(),
            0, actual.getProvisionableRoles().size());
    }

    ////// apply

    public void testApplyUpdatesUserIfAlreadySaved() throws Exception {
        /* expect */ authorizationManager.modifyUser(csmUser);
        replayMocks();

        command.apply();
        verifyMocks();
    }

    public void testApplyCreatesUserIfNotSaved() throws Exception {
        csmUser.setUserId(null);
        /* expect */ authorizationManager.createUser(csmUser);
        replayMocks();

        command.apply();
        verifyMocks();
    }

    public void testApplyCreatesUserIfLookupFailsWhenSoConfigured() throws Exception {
        csmUser.setUserId(null);
        command.setLookUpBoundUser(true);
        
        expect(authorizationManager.getUser(csmUser.getLoginName())).andReturn(null);
        /* expect */ authorizationManager.createUser(csmUser);
        replayMocks();

        command.apply();
        verifyMocks();
    }

    public void testApplyLooksForExistingUserByUsernameIfConfiguredAndProvidedUserNotAlreadySaved() throws Exception {
        command.setLookUpBoundUser(true);

        csmUser.setUserId(null);
        csmUser.setEmailId("foo@nihil.it");

        User savedJo = AuthorizationObjectFactory.createCsmUser("jo");
        savedJo.setUserId(13L);
        savedJo.setUpdateDate(new Date());
        savedJo.setFirstName("Josephine");
        expect(authorizationManager.getUser(csmUser.getLoginName())).andReturn(savedJo);

        /* expect */ authorizationManager.modifyUser(savedJo);
        replayMocks();

        command.apply();
        verifyMocks();

        assertSame("User not replaced with loaded one", savedJo, command.getUser());
        assertEquals("Bound properties not copied to loaded user",
            "foo@nihil.it", command.getUser().getEmailId());
        assertEquals("Not-bound properties blanked on loaded user",
            "Josephine", command.getUser().getFirstName());
    }

    public void testApplyAppliesAddAllScope() throws Exception {
        expectRoleChange("data_reader", "add", "site", "__ALL__");

        SuiteRoleMembership srm = expectGetAndReplaceMembership(SuiteRole.DATA_READER);
        replayMocks();

        command.apply();
        verifyMocks();
        assertTrue("Membership not made for all sites", srm.isAllSites());
    }

    public void testApplyAppliesAddSingleScope() throws Exception {
        expectRoleChange("data_reader", "add", "site", sanAntonio.getAssignedIdentifier());

        SuiteRoleMembership srm = expectGetAndReplaceMembership(SuiteRole.DATA_READER);
        replayMocks();

        command.apply();
        verifyMocks();
        assertTrue("Membership not made for specified site", srm.getSiteIdentifiers().contains(sanAntonio.getAssignedIdentifier()));
    }

    public void testApplyAppliesAddGroupOnly() throws Exception {
        expectRoleChange("business_administrator", "add");

        expectGetAndReplaceMembership(SuiteRole.BUSINESS_ADMINISTRATOR);
        replayMocks();

        command.apply();
        verifyMocks();
    }

    public void testApplyRemoveGroupDeletesRole() throws Exception {
        expectRoleChange("system_administrator", "remove");

        /* expect */ pSession.deleteRole(SuiteRole.SYSTEM_ADMINISTRATOR);
        replayMocks();

        command.apply();
        verifyMocks();
    }

    public void testApplyAppliesRemoveSingleScope() throws Exception {
        expectRoleChange("data_reader", "remove", "site", sanAntonio.getAssignedIdentifier());

        SuiteRoleMembership srm = expectGetAndReplaceMembership(SuiteRole.DATA_READER).
            forSites(sanAntonio, austin);
        replayMocks();

        command.apply();
        verifyMocks();
        assertEquals("Removed site not removed", 1, srm.getSiteIdentifiers().size());
        assertEquals("Wrong site removed",
            austin.getAssignedIdentifier(), srm.getSiteIdentifiers().get(0));
    }

    public void testApplySetsPasswordToRequestedValueIfUsingLocalPasswordsAndThePasswordIsSet() throws Exception {
        command.setPassword("hullaballo");
        expect(authenticationSystem.usesLocalPasswords()).andReturn(true);
        replayMocks();

        command.apply();
        verifyMocks();
        assertEquals("Password not set", "hullaballo", csmUser.getPassword());
    }

    // TODO: make this test deterministic via a non-random source of randomness
    public void testApplySetsPasswordToRandomValueIfUsingNotLocalPasswordsAndTheUserIsNew() throws Exception {
        command.getUser().setUserId(null);
        expect(authenticationSystem.usesLocalPasswords()).andReturn(false);
        /* expect */ authorizationManager.createUser(csmUser);
        replayMocks();

        command.apply();
        verifyMocks();
        assertNotNull("Password not set", csmUser.getPassword());
        assertTrue("Password not of expected length: " + csmUser.getPassword().length(),
            16 <= csmUser.getPassword().length() && csmUser.getPassword().length() <= 32);
        String candidate = csmUser.getPassword();
        for (int i = 0; i < candidate.length(); i++) {
            assertTrue("Character at " + i + " ('" + candidate.charAt(i) + "' 0x" + Integer.toHexString(candidate.charAt(i)) + ") out of range",
                ' ' <= candidate.charAt(i) && candidate.charAt(i) <= '~');
        }
    }

    public void testApplyDoesNotSetPasswordToRandomValueIfNotLocalPasswordsAndTheUserExists() throws Exception {
        expect(authenticationSystem.usesLocalPasswords()).andReturn(false);
        replayMocks();

        command.apply();
        verifyMocks();
        assertNull("Password should not be set", csmUser.getPassword());
    }

    public void testApplyDoesNotSetPasswordIfBlank() throws Exception {
        command.setPassword("\t");
        replayMocks();

        command.apply();
        verifyMocks();
        assertNull("Password should not be set", csmUser.getPassword());
    }

    private SuiteRoleMembership expectGetAndReplaceMembership(SuiteRole expectedRole) {
        SuiteRoleMembership srm =
            AuthorizationScopeMappings.createSuiteRoleMembership(PscRole.valueOf(expectedRole));
        expect(pSession.getProvisionableRoleMembership(expectedRole)).andReturn(srm);
        /* expect */ pSession.replaceRole(srm);
        return srm;
    }

    ////// authorization

    public void testRoleChangesForUnallowedRolesIgnored() throws Exception {
        command.setProvisionableRoles(SuiteRole.USER_ADMINISTRATOR);

        expectRoleChange(command, "system_administrator", "add", null, null);
        replayMocks(); // nothing expected

        command.apply();
        verifyMocks();
    }

    public void testSiteChangeWhenDisallowedIgnored() throws Exception {
        command.setProvisionableSites(Arrays.asList(sanAntonio));

        expectRoleChange(command, "data_reader", "add", "site", austin.getAssignedIdentifier());
        replayMocks(); // nothing expected

        command.apply();
        verifyMocks();
    }

    public void testAllSiteChangesWhenDisallowedIgnored() throws Exception {
        command.setCanProvisionAllSites(false);

        expectRoleChange(command, "data_reader", "add", "site", "__ALL__");
        replayMocks(); // nothing expected

        command.apply();
        verifyMocks();
    }

    ////// validation

    public void testInvalidWhenBlankUsername() throws Exception {
        command.getUser().setLoginName("\t");
        doValidate();
        assertFieldErrorCount("user.loginName", 1);
    }

    public void testInvalidWhenNewInstanceWithDuplicateUsernameAndLookupNotConfigured() throws Exception {
        csmUser.setUserId(null);
        command.setLookUpBoundUser(false);
        command.getUser().setLoginName("zap");

        User expectedMatch = new User();
        expectedMatch.setUserId(14L);
        expect(authorizationManager.getUser("zap")).andReturn(expectedMatch);

        doValidate();

        assertFieldErrorCount("user.loginName", 1);
    }

    public void testValidWhenNewInstanceWithDuplicateUsernameAndLookupIsConfigured() throws Exception {
        csmUser.setUserId(null);
        command.setLookUpBoundUser(true);
        command.getUser().setLoginName("zap");

        User expectedMatch = new User();
        expectedMatch.setUserId(14L);
        expect(authorizationManager.getUser("zap")).andReturn(expectedMatch);

        doValidate();

        assertFieldErrorCount("user.loginName", 0);
    }

    public void testValidWhenDuplicateUsernameAndEditingSavedInstance() throws Exception {
        command.getUser().setLoginName("zap");
        expect(authorizationManager.getUser("zap")).andReturn(csmUser);

        doValidate();

        assertFieldErrorCount("user.loginName", 0);
    }

    public void testInvalidWhenDuplicateUsernameAndEditingDifferentInstance() throws Exception {
        command.getUser().setLoginName("zap");

        User expectedMatch = new User();
        expectedMatch.setUserId(14L);
        expect(authorizationManager.getUser("zap")).andReturn(expectedMatch);

        doValidate();

        assertFieldErrorCount("user.loginName", 1);
    }

    public void testInvalidWithBlankEmailAddress() throws Exception {
        command.getUser().setEmailId("\n\t");

        doValidate();
        assertFieldErrorCount("user.emailId", 1);
    }

    public void testInvalidWithNonAddressEmailAddress() throws Exception {
        command.getUser().setEmailId("hello");

        doValidate();
        assertFieldErrorCount("user.emailId", 1);
    }

    public void testValidWithReasonableEmailAddress() throws Exception {
        command.getUser().setEmailId("someguy@nemo.it");

        doValidate();
        assertFieldErrorCount("user.emailId", 0);
    }

    public void testPasswordRequiredForNewUserIfLocalPasswordsUsed() throws Exception {
        expect(authenticationSystem.usesLocalPasswords()).andReturn(true);
        csmUser.setUserId(null);
        command.setPassword(null);

        doValidate();
        assertFieldErrorCount("password", 1);
    }

    public void testPasswordNotRequiredForNewUserIfLocalPasswordsNotUsed() throws Exception {
        expect(authenticationSystem.usesLocalPasswords()).andReturn(false);
        csmUser.setUserId(null);
        command.setPassword(null);

        doValidate();
        assertFieldErrorCount("password", 0);
    }

    public void testPasswordNotRequiredForExistingUser() throws Exception {
        command.setPassword(null);

        doValidate();
        assertFieldErrorCount("password", 0);
    }

    public void testMismatchedPasswordsAreInvalid() throws Exception {
        command.setPassword("abc");
        command.setRePassword("123");

        doValidate();
        assertFieldErrorCount("rePassword", 1);
    }

    public void testMatchingPasswordsAreValid() throws Exception {
        command.setPassword("abc");
        command.setRePassword("abc");

        doValidate();
        assertFieldErrorCount("password", 0);
        assertFieldErrorCount("rePassword", 0);
    }

    public void testBlankFirstNameIsInvalid() throws Exception {
        command.getUser().setFirstName(" ");
        
        doValidate();
        assertFieldErrorCount("user.firstName", 1);
    }
    
    public void testSetFirstNameIsValid() throws Exception {
        command.getUser().setFirstName("Jo");
        
        doValidate();
        assertFieldErrorCount("user.firstName", 0);
    }
    
    public void testBlankLastNameIsInvalid() throws Exception {
        command.getUser().setLastName(" ");
        
        doValidate();
        assertFieldErrorCount("user.lastName", 1);
    }
    
    public void testSetLastNameIsValid() throws Exception {
        command.getUser().setLastName("Jo");
        
        doValidate();
        assertFieldErrorCount("user.lastName", 0);
    }
    
    private void assertFieldErrorCount(String field, int expectedCount) {
        assertEquals("Wrong number of errors for " + field + ": " + errors.getFieldErrors(field),
            expectedCount, errors.getFieldErrorCount(field));
    }

    private void doValidate() {
        replayMocks();
        command.validate(errors);
        verifyMocks();
    }

    ////// javascript user init
    
    public void testJavascriptUserForEmptyUserIsCorrect() throws Exception {
        assertEquals("new psc.admin.ProvisionableUser('jo', {})", command.getJavaScriptProvisionableUser());
    }

    public void testJavascriptUserWithOneGroupOnlyRole() throws Exception {
        ProvisionUserCommand actual = actual(new PscUserBuilder("jo").add(PscRole.SYSTEM_ADMINISTRATOR).toUser());
        assertEquals("new psc.admin.ProvisionableUser('jo', {\"system_administrator\": {}})",
            actual.getJavaScriptProvisionableUser());
    }

    public void testJavascriptUserWithOneSiteScopedRole() throws Exception {
        ProvisionUserCommand actual = actual(
            new PscUserBuilder("jo").add(PscRole.USER_ADMINISTRATOR).forSites(austin).toUser());
        assertEquals("new psc.admin.ProvisionableUser('jo', {\"user_administrator\": {\"sites\": [\"i-a\"]}})",
            actual.getJavaScriptProvisionableUser());
    }

    public void testJavascriptUserWithOneSitePlusStudyScopedRole() throws Exception {
        ProvisionUserCommand actual = actual(
            new PscUserBuilder("jo").add(PscRole.DATA_READER).forSites(sanAntonio).forAllStudies().toUser());
        assertEquals("new psc.admin.ProvisionableUser('jo', {\"data_reader\": {\n    \"sites\": [\"i-sa\"],\n    \"studies\": [\"__ALL__\"]\n}})",
            actual.getJavaScriptProvisionableUser());
    }

    public void testJavascriptUserWithMultipleRoles() throws Exception {
        ProvisionUserCommand actual = actual(
            new PscUserBuilder("jo").
                add(PscRole.USER_ADMINISTRATOR).forSites(austin).
                add(PscRole.DATA_READER).forAllSites().
                    forStudies(Fixtures.createBasicTemplate("T"), Fixtures.createBasicTemplate("Q")).
                toUser());
        assertEquals("new psc.admin.ProvisionableUser('jo', {\n    \"data_reader\": {\n        \"sites\": [\"__ALL__\"],\n        \"studies\": [\n            \"T\",\n            \"Q\"\n        ]\n    },\n    \"user_administrator\": {\"sites\": [\"i-a\"]}\n})",
            actual.getJavaScriptProvisionableUser());
    }

    private void expectRoleChange(String roleKey, String changeKind) {
        expectRoleChange(roleKey, changeKind, null, null);
    }

    private void expectRoleChange(String roleKey, String changeKind, String scopeType, String scopeIdent) {
        expectRoleChange(this.command, roleKey, changeKind, scopeType, scopeIdent);
    }

    private void expectRoleChange(
        ProvisionUserCommand command,
        String roleKey, String changeKind, String scopeType, String scopeIdent
    ) {
        MapBuilder<String, String> mb = new MapBuilder<String, String>().
            put("role", roleKey).put("kind", changeKind);
        if (scopeType != null) {
            mb.put("scopeType", scopeType).put("scopeIdentifier", scopeIdent);
        }

        command.getRoleChanges().put(new JSONObject(mb.toMap()));
    }
}
