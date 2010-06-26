package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;
import gov.nih.nci.cabig.ctms.suite.authorization.ProvisioningSession;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.security.AuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.User;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;

import static org.easymock.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class ProvisionUserCommandTest extends WebTestCase {
    private ProvisionUserCommand command;

    private User user;
    private ProvisioningSession pSession;
    private AuthorizationManager authorizationManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        user = new User();
        user.setLoginName("jo");
        user.setUpdateDate(new Date()); // or CSM pukes
        pSession = registerMockFor(ProvisioningSession.class);
        authorizationManager = registerNiceMockFor(AuthorizationManager.class);

        command = new ProvisionUserCommand(user,
            new LinkedHashMap<SuiteRole, SuiteRoleMembership>(),
            pSession, authorizationManager,
            Arrays.asList(SuiteRole.values()),
            Arrays.asList(Fixtures.createSite("A", "i-a"), Fixtures.createSite("T", "i-t")),
            true
        );
    }

    ////// apply

    public void testApplyUpdatesUser() throws Exception {
        authorizationManager.modifyUser(user);
        replayMocks();

        command.apply();
        verifyMocks();
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
            pSession, authorizationManager,
            Arrays.asList(SuiteRole.USER_ADMINISTRATOR),
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
            pSession, authorizationManager,
            Arrays.asList(SuiteRole.values()),
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
            pSession, authorizationManager,
            Arrays.asList(SuiteRole.values()),
            Arrays.asList(Fixtures.createSite("A", "i-a"), Fixtures.createSite("T", "i-t")),
            false
        );

        expectRoleChange(limitedCommand, "data_reader", "add", "site", "__ALL__");
        replayMocks(); // nothing expected

        limitedCommand.apply();
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
