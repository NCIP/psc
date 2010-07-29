package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
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
import java.util.LinkedHashMap;

import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class ProvisionUserCommandTest extends WebTestCase {
    private ProvisionUserCommand command;

    private User user;
    private ProvisioningSession pSession;
    private AuthorizationManager authorizationManager;
    private ProvisioningSessionFactory psFactory;
    private AuthenticationSystem authenticationSystem;
    private Errors errors;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        user = new User();
        user.setUserId(15L);
        user.setLoginName("jo");
        user.setUpdateDate(new Date()); // or CSM pukes

        authorizationManager = registerMockFor(AuthorizationManager.class);
        authorizationManager.modifyUser(user);
        expectLastCall().asStub();
        expect(authorizationManager.getUser(user.getLoginName())).andStubReturn(null); // for validation

        psFactory = registerMockFor(ProvisioningSessionFactory.class);
        pSession = registerMockFor(ProvisioningSession.class);
        expect(psFactory.createSession(anyLong())).andStubReturn(pSession);

        authenticationSystem = registerMockFor(AuthenticationSystem.class);
        expect(authenticationSystem.usesLocalPasswords()).andStubReturn(true);

        errors = new MapBindingResult(new HashMap(), "?");

        command = new ProvisionUserCommand(user,
            new LinkedHashMap<SuiteRole, SuiteRoleMembership>(),
            psFactory, authorizationManager,
            authenticationSystem, Arrays.asList(SuiteRole.values()),
            Arrays.asList(Fixtures.createSite("A", "i-a"), Fixtures.createSite("T", "i-t")),
            true
        );
    }

    ////// create

    public void testCreateWithoutUserSetsBlankUserInfo() throws Exception {
        ProvisionUserCommand actual = ProvisionUserCommand.createForUnknownUser(psFactory,
            authorizationManager, authenticationSystem, Arrays.asList(SuiteRole.values()),
            Arrays.asList(Fixtures.createSite("A", "i-a"), Fixtures.createSite("T", "i-t")),
            true);

        assertNotNull("User not created", actual.getUser());
        assertNotNull("Current should be set", actual.getCurrentRoles());
        assertTrue("Current should be set empty", actual.getCurrentRoles().isEmpty());
    }

    ////// apply

    public void testApplyUpdatesUserIfAlreadySaved() throws Exception {
        /* expect */ authorizationManager.modifyUser(user);
        replayMocks();

        command.apply();
        verifyMocks();
    }

    public void testApplyCreatesUserIfNotSaved() throws Exception {
        user.setUserId(null);
        /* expect */ authorizationManager.createUser(user);
        replayMocks();

        command.apply();
        verifyMocks();
    }

    public void testApplyCreatesUserIfLookupFailsWhenSoConfigured() throws Exception {
        user.setUserId(null);
        command.setLookUpBoundUser(true);
        
        expect(authorizationManager.getUser(user.getLoginName())).andReturn(null);
        /* expect */ authorizationManager.createUser(user);
        replayMocks();

        command.apply();
        verifyMocks();
    }

    public void testApplyLooksForExistingUserByUsernameIfConfiguredAndProvidedUserNotAlreadySaved() throws Exception {
        command.setLookUpBoundUser(true);

        user.setUserId(null);
        user.setEmailId("foo@nihil.it");

        User savedJo = AuthorizationObjectFactory.createCsmUser("jo");
        savedJo.setUserId(13L);
        savedJo.setUpdateDate(new Date());
        savedJo.setFirstName("Josephine");
        expect(authorizationManager.getUser(user.getLoginName())).andReturn(savedJo);

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
        expectRoleChange("data_reader", "add", "site", "i-t");

        SuiteRoleMembership srm = expectGetAndReplaceMembership(SuiteRole.DATA_READER);
        replayMocks();

        command.apply();
        verifyMocks();
        assertTrue("Membership not made for specified site", srm.getSiteIdentifiers().contains("i-t"));
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
        expectRoleChange("data_reader", "remove", "site", "i-t");

        SuiteRoleMembership srm = expectGetAndReplaceMembership(SuiteRole.DATA_READER).forSites("i-t", "i-a");
        replayMocks();

        command.apply();
        verifyMocks();
        assertEquals("Removed site not removed", 1, srm.getSiteIdentifiers().size());
        assertEquals("Wrong site removed", "i-a", srm.getSiteIdentifiers().get(0));
    }

    public void testApplySetsPasswordToRequestedValueIfUsingLocalPasswordsAndThePasswordIsSet() throws Exception {
        command.setPassword("hullaballo");
        expect(authenticationSystem.usesLocalPasswords()).andReturn(true);
        replayMocks();

        command.apply();
        verifyMocks();
        assertEquals("Password not set", "hullaballo", user.getPassword());
    }

    public void testApplySetsPasswordToRandomValueIfUsingNotLocalPasswordsAndTheUserIsNew() throws Exception {
        command.getUser().setUserId(null);
        expect(authenticationSystem.usesLocalPasswords()).andReturn(false);
        /* expect */ authorizationManager.createUser(user);
        replayMocks();

        command.apply();
        verifyMocks();
        assertNotNull("Password not set", user.getPassword());
        assertTrue("Password not of expected length",
            16 < user.getPassword().length() && user.getPassword().length() <= 32);
        String candidate = user.getPassword();
        for (int i = 0; i < candidate.length(); i++) {
            assertTrue("Character " + i + " (" + candidate.charAt(i) + ") out of range",
                ' ' < candidate.charAt(i) && candidate.charAt(i) <= '~');
        }
    }

    public void testApplyDoesNotSetPasswordToRandomValueIfNotLocalPasswordsAndTheUserExists() throws Exception {
        expect(authenticationSystem.usesLocalPasswords()).andReturn(false);
        replayMocks();

        command.apply();
        verifyMocks();
        assertNull("Password should not be set", user.getPassword());
    }

    public void testApplyDoesNotSetPasswordIfBlank() throws Exception {
        command.setPassword("\t");
        replayMocks();

        command.apply();
        verifyMocks();
        assertNull("Password should not be set", user.getPassword());
    }

    private SuiteRoleMembership expectGetAndReplaceMembership(SuiteRole expectedRole) {
        SuiteRoleMembership srm = new SuiteRoleMembership(expectedRole, null, null);
        expect(pSession.getProvisionableRoleMembership(expectedRole)).andReturn(srm);
        /* expect */ pSession.replaceRole(srm);
        return srm;
    }

    ////// authorization

    public void testRoleChangesForUnallowedRolesIgnored() throws Exception {
        ProvisionUserCommand limitedCommand = new ProvisionUserCommand(user,
            Collections.<SuiteRole, SuiteRoleMembership>emptyMap(),
            psFactory, authorizationManager,
            authenticationSystem, Arrays.asList(SuiteRole.USER_ADMINISTRATOR),
            Arrays.asList(Fixtures.createSite("A", "i-a"), Fixtures.createSite("T", "i-t")),
            true
        );

        expectRoleChange(limitedCommand, "system_administrator", "add", null, null);
        replayMocks(); // nothing expected

        limitedCommand.apply();
        verifyMocks();
    }

    public void testSiteChangeWhenDisallowedIgnored() throws Exception {
        ProvisionUserCommand limitedCommand = new ProvisionUserCommand(user,
            Collections.<SuiteRole, SuiteRoleMembership>emptyMap(),
            psFactory, authorizationManager,
            authenticationSystem, Arrays.asList(SuiteRole.values()),
            Arrays.asList(Fixtures.createSite("T", "i-t")),
            true
        );

        expectRoleChange(limitedCommand, "data_reader", "add", "site", "i-a");
        replayMocks(); // nothing expected

        limitedCommand.apply();
        verifyMocks();
    }

    public void testAllSiteChangesWhenDisallowedIgnored() throws Exception {
        ProvisionUserCommand limitedCommand = new ProvisionUserCommand(user,
            Collections.<SuiteRole, SuiteRoleMembership>emptyMap(),
            psFactory, authorizationManager,
            authenticationSystem, Arrays.asList(SuiteRole.values()),
            Arrays.asList(Fixtures.createSite("A", "i-a"), Fixtures.createSite("T", "i-t")),
            false
        );

        expectRoleChange(limitedCommand, "data_reader", "add", "site", "__ALL__");
        replayMocks(); // nothing expected

        limitedCommand.apply();
        verifyMocks();
    }

    ////// validation

    public void testInvalidWhenBlankUsername() throws Exception {
        command.getUser().setLoginName("\t");
        doValidate();
        assertFieldErrorCount("user.loginName", 1);
    }

    public void testInvalidWhenNewInstanceWithDuplicateUsernameAndLookupNotConfigured() throws Exception {
        user.setUserId(null);
        command.setLookUpBoundUser(false);
        command.getUser().setLoginName("zap");

        User expectedMatch = new User();
        expectedMatch.setUserId(14L);
        expect(authorizationManager.getUser("zap")).andReturn(expectedMatch);

        doValidate();

        assertFieldErrorCount("user.loginName", 1);
    }

    public void testValidWhenNewInstanceWithDuplicateUsernameAndLookupIsConfigured() throws Exception {
        user.setUserId(null);
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
        expect(authorizationManager.getUser("zap")).andReturn(user);

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
        user.setUserId(null);
        command.setPassword(null);

        doValidate();
        assertFieldErrorCount("password", 1);
    }

    public void testPasswordNotRequiredForNewUserIfLocalPasswordsNotUsed() throws Exception {
        expect(authenticationSystem.usesLocalPasswords()).andReturn(false);
        user.setUserId(null);
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
        assertEquals("new psc.admin.ProvisionableUser('jo', {\n\n})", command.getJavaScriptProvisionableUser());
    }

    public void testJavascriptUserWithOneGroupOnlyRole() throws Exception {
        command.getCurrentRoles().put(SuiteRole.SYSTEM_ADMINISTRATOR,
            new SuiteRoleMembership(SuiteRole.SYSTEM_ADMINISTRATOR, null, null));
        assertEquals("new psc.admin.ProvisionableUser('jo', {\n  system_administrator: {  }\n})",
            command.getJavaScriptProvisionableUser());
    }

    public void testJavascriptUserWithOneSiteScopedRole() throws Exception {
        command.getCurrentRoles().put(SuiteRole.USER_ADMINISTRATOR,
            new SuiteRoleMembership(SuiteRole.USER_ADMINISTRATOR, null, null).forSites("A", "B"));
        assertEquals("new psc.admin.ProvisionableUser('jo', {\n  user_administrator: { sites: ['A', 'B'] }\n})",
            command.getJavaScriptProvisionableUser());
    }

    public void testJavascriptUserWithOneSitePlusStudyScopedRole() throws Exception {
        command.getCurrentRoles().put(SuiteRole.DATA_READER,
            new SuiteRoleMembership(SuiteRole.DATA_READER, null, null).forSites("A", "B").forAllStudies());
        assertEquals("new psc.admin.ProvisionableUser('jo', {\n  data_reader: { sites: ['A', 'B'], studies: ['__ALL__'] }\n})",
            command.getJavaScriptProvisionableUser());
    }

    public void testJavascriptUserWithMultipleRoles() throws Exception {
        command.getCurrentRoles().put(SuiteRole.USER_ADMINISTRATOR,
            new SuiteRoleMembership(SuiteRole.USER_ADMINISTRATOR, null, null).forSites("A", "B"));
        command.getCurrentRoles().put(SuiteRole.DATA_READER,
            new SuiteRoleMembership(SuiteRole.DATA_READER, null, null).forAllSites().forStudies("T", "Q"));
        assertEquals("new psc.admin.ProvisionableUser('jo', {\n  user_administrator: { sites: ['A', 'B'] },\n  data_reader: { sites: ['__ALL__'], studies: ['T', 'Q'] }\n})",
            command.getJavaScriptProvisionableUser());
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
