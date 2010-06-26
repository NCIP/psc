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
import java.util.Date;

import static org.easymock.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class UserAdministrationCommandTest extends WebTestCase {
    private UserAdministrationCommand command;

    private User user;
    private ProvisioningSession pSession;
    private AuthorizationManager authorizationManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        user = new User();
        user.setUpdateDate(new Date()); // or CSM pukes
        pSession = registerMockFor(ProvisioningSession.class);
        authorizationManager = registerNiceMockFor(AuthorizationManager.class);

        command = new UserAdministrationCommand(user, 
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
        UserAdministrationCommand limitedCommand = new UserAdministrationCommand(user,
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
        UserAdministrationCommand limitedCommand = new UserAdministrationCommand(user,
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
        UserAdministrationCommand limitedCommand = new UserAdministrationCommand(user,
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

    private void expectRoleChange(String roleKey, String changeKind) {
        expectRoleChange(roleKey, changeKind, null, null);
    }

    private void expectRoleChange(String roleKey, String changeKind, String scopeType, String scopeIdent) {
        expectRoleChange(this.command, roleKey, changeKind, scopeType, scopeIdent);
    }

    private void expectRoleChange(
        UserAdministrationCommand command,
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
