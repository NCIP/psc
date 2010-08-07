package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.AuthorizationScopeMappings;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.PscUserBuilder;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.VisibleStudyParameters;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;
import gov.nih.nci.cabig.ctms.suite.authorization.ProvisioningSession;
import gov.nih.nci.cabig.ctms.suite.authorization.ProvisioningSessionFactory;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.security.AuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.User;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory.*;
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
    private Study studyA, studyB, studyC;

    private ProvisioningSession pSession;
    private AuthorizationManager authorizationManager;
    private ProvisioningSessionFactory psFactory;
    private AuthenticationSystem authenticationSystem;
    private SiteDao siteDao;
    private StudyDao studyDao;

    private Errors errors;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        csmUser = createCsmUser("jo");
        csmUser.setUserId(15L);

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

        studyA = Fixtures.createBasicTemplate("A");
        studyB = Fixtures.createBasicTemplate("B");
        studyC = Fixtures.createBasicTemplate("C");
        studyDao = new StubStudyDao();

        PscUser principal = new PscUserBuilder("zelda").add(PscRole.USER_ADMINISTRATOR).forAllSites().toUser();
        principal.getCsmUser().setUserId(99L);
        SecurityContextHolderTestHelper.setSecurityContext(principal);

        command = create(pscUser);
    }

    private ProvisionUserCommand create(PscUser existingUser) {
        return create(existingUser, new PscUserBuilder("sam").
            add(PscRole.USER_ADMINISTRATOR).forAllSites().
            toUser());
    }

    private ProvisionUserCommand create(PscUser existingUser, PscUser provisioner) {
        return ProvisionUserCommand.create(existingUser,
            psFactory, authorizationManager, authenticationSystem, applicationSecurityManager, siteDao, studyDao,
            provisioner
        );
    }

    ////// create

    public void testCreateWithoutUserSetsBlankUserInfo() throws Exception {
        ProvisionUserCommand actual = create(null);

        assertNotNull("User not created", actual.getUser());
        assertNotNull("Current should be set", actual.getCurrentRoles());
        assertTrue("Current should be set empty", actual.getCurrentRoles().isEmpty());
    }

    public void testCreateForSysAdminProvisioner() throws Exception {
        PscUser provisioner = new PscUserBuilder("jo").
            add(PscRole.SYSTEM_ADMINISTRATOR).
            toUser();
        ProvisionUserCommand actual = create(pscUser, provisioner);

        assertEquals("Wrong number of provisionable roles: " + actual.getProvisionableRoles(),
            2, actual.getProvisionableRoles().size());
        assertMayProvision(SuiteRole.USER_ADMINISTRATOR, actual);
        assertMayProvision(SuiteRole.SYSTEM_ADMINISTRATOR, actual);

        assertEquals("Should not be able to provision for any specific sites",
            0, actual.getProvisionableSites().size());
        assertTrue("Should be able to provision for \"all sites\"",
            actual.getCanProvisionAllSites());

        assertEquals("Should not be able to provision for any specific managed studies",
            0, actual.getProvisionableManagedStudies().size());
        assertFalse("Should be not able to provision for \"all studies\" for template managers",
            actual.getCanProvisionManagementOfAllStudies());

        assertEquals("Should not be able to provision for any specific participating studies",
            0, actual.getProvisionableParticipatingStudies().size());
        assertFalse("Should be not able to provision for \"all studies\" for site participation",
            actual.getCanProvisionParticipationInAllStudies());
    }

    public void testCreateForUnlimitedUserAdministrator() throws Exception {
        PscUser provisioner = new PscUserBuilder("jo").
            add(PscRole.USER_ADMINISTRATOR).forAllSites().
            toUser();
        ProvisionUserCommand actual = create(pscUser, provisioner);

        assertEquals("Wrong number of provisionable roles: " + actual.getProvisionableRoles(),
            SuiteRole.values().length, actual.getProvisionableRoles().size());
        for (SuiteRole role : SuiteRole.values()) {
            assertMayProvision(role, actual);
        }

        assertEquals("Should be able to provision for any specific site",
            2, actual.getProvisionableSites().size());
        assertTrue("Should be able to provision for austin",
            actual.getProvisionableSites().contains(austin));
        assertTrue("Should be able to provision for san antonio",
            actual.getProvisionableSites().contains(sanAntonio));
        assertTrue("Should be able to provision for \"all sites\"",
            actual.getCanProvisionAllSites());

        assertEquals("Should be able to provision for all specific managed studies",
            2, actual.getProvisionableManagedStudies().size());
        assertContains(actual.getProvisionableManagedStudies(), studyA);
        assertContains(actual.getProvisionableManagedStudies(), studyB);
        assertTrue("Should be able to provision for \"all studies\" for template managers",
            actual.getCanProvisionManagementOfAllStudies());

        assertEquals("Should be able to provision for all specific participating studies",
            2, actual.getProvisionableParticipatingStudies().size());
        assertContains(actual.getProvisionableParticipatingStudies(), studyC);
        assertContains(actual.getProvisionableParticipatingStudies(), studyB);
        assertTrue("Should be able to provision for \"all studies\" for site participation",
            actual.getCanProvisionParticipationInAllStudies());
    }

    public void testCreateForSiteLimitedUserAdministrator() throws Exception {
        PscUser provisioner = new PscUserBuilder("jo").
            add(PscRole.USER_ADMINISTRATOR).forSites(sanAntonio).
            toUser();
        ProvisionUserCommand actual = create(pscUser, provisioner);

        assertEquals("Wrong number of provisionable roles: " + actual.getProvisionableRoles(),
            SuiteRole.values().length, actual.getProvisionableRoles().size());

        assertEquals("Should be able to provision for the specific site",
            1, actual.getProvisionableSites().size());
        assertTrue("Should be able to provision for san antonio",
            actual.getProvisionableSites().contains(sanAntonio));
        assertFalse("Should not be able to provision for \"all sites\"",
            actual.getCanProvisionAllSites());

        assertEquals("Should be able to provision for some specific managed studies",
            1, actual.getProvisionableManagedStudies().size());
        assertContains(actual.getProvisionableManagedStudies(), studyA);
        assertTrue("Should be able to provision for \"all studies\" for template managers",
            actual.getCanProvisionManagementOfAllStudies());

        assertEquals("Should be able to provision for some specific participating studies",
            1, actual.getProvisionableParticipatingStudies().size());
        assertContains(actual.getProvisionableParticipatingStudies(), studyC);
        assertTrue("Should be able to provision for \"all studies\" for site participation",
            actual.getCanProvisionParticipationInAllStudies());
    }

    public void testCreateForRandomProvisioner() throws Exception {
        PscUser provisioner = new PscUserBuilder("jo").
            add(PscRole.SUBJECT_MANAGER).forSites(sanAntonio).
            toUser();
        ProvisionUserCommand actual = create(pscUser, provisioner);

        assertEquals("Wrong number of provisionable roles: " + actual.getProvisionableRoles(),
            0, actual.getProvisionableRoles().size());
    }

    public void testCreateForNoProvisionerIsBlank() throws Exception {
        ProvisionUserCommand actual = create(pscUser, null);

        assertEquals("Wrong number of provisionable roles: " + actual.getProvisionableRoles(),
            0, actual.getProvisionableRoles().size());
    }

    private void assertMayProvision(SuiteRole expectedRole, ProvisionUserCommand actual) {
        assertTrue("Should be able to provision " + expectedRole,
            actual.getProvisionableRoles().contains(new ProvisioningRole(expectedRole)));
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

        User savedJo = createCsmUser("jo");
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

    public void testApplyAppliesAddAllSiteScope() throws Exception {
        expectRoleChange("data_reader", "add", "site", "__ALL__");

        SuiteRoleMembership srm = expectGetAndReplaceMembership(SuiteRole.DATA_READER);
        replayMocks();

        command.apply();
        verifyMocks();
        assertTrue("Membership not made for all sites", srm.isAllSites());
    }

    public void testApplyAppliesAddSingleSiteScope() throws Exception {
        expectRoleChange("data_reader", "add", "site", sanAntonio.getAssignedIdentifier());

        SuiteRoleMembership srm = expectGetAndReplaceMembership(SuiteRole.DATA_READER);
        replayMocks();

        command.apply();
        verifyMocks();
        assertTrue("Membership not made for specified site",
            srm.getSiteIdentifiers().contains(sanAntonio.getAssignedIdentifier()));
    }

    public void testApplyAppliesAddAllStudyScope() throws Exception {
        expectRoleChange("data_reader", "add", "study", "__ALL__");

        SuiteRoleMembership srm = expectGetAndReplaceMembership(SuiteRole.DATA_READER);
        replayMocks();

        command.apply();
        verifyMocks();
        assertTrue("Membership not made for all sites", srm.isAllStudies());
    }

    public void testApplyAppliesAddSingleStudyScope() throws Exception {
        expectRoleChange("data_reader", "add", "study", studyB.getAssignedIdentifier());

        SuiteRoleMembership srm = expectGetAndReplaceMembership(SuiteRole.DATA_READER);
        replayMocks();

        command.apply();
        verifyMocks();
        assertTrue("Membership not made for specified study",
            srm.getStudyIdentifiers().contains(studyB.getAssignedIdentifier()));
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

    public void testApplyAppliesRemoveSingleSiteScope() throws Exception {
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

    public void testApplyAppliesRemoveSingleStudyScope() throws Exception {
        expectRoleChange("data_reader", "remove", "study", "C");

        SuiteRoleMembership srm = expectGetAndReplaceMembership(SuiteRole.DATA_READER).
            forStudies(studyB, studyC);
        replayMocks();

        command.apply();
        verifyMocks();
        assertEquals("Removed study not removed", 1, srm.getStudyIdentifiers().size());
        assertEquals("Wrong studye removed",
            studyB.getAssignedIdentifier(), srm.getStudyIdentifiers().get(0));
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

    public void testApplySetsStaleIfModifyingSelf() throws Exception {
        PscUser existingUser = createPscUser("zelda");
        existingUser.getCsmUser().setUserId(99L);

        /* expect */ authorizationManager.modifyUser(existingUser.getCsmUser());

        ProvisionUserCommand selfMod = create(existingUser);
        replayMocks();

        selfMod.apply();
        verifyMocks();

        assertTrue("Security context principal should be stale",
            applicationSecurityManager.getUser().isStale());
    }

    public void testApplyDoesNotSetStaleIfNotModifyingSelf() throws Exception {
        replayMocks();

        command.apply();
        verifyMocks();

        assertFalse("Security context principal should not be stale",
            applicationSecurityManager.getUser().isStale());
    }

    public void testApplyDoesNotSetStaleIfNoApplicationSecurityManager() throws Exception {
        ProvisionUserCommand setup = ProvisionUserCommand.create(pscUser,
            psFactory, authorizationManager, authenticationSystem, null,
            siteDao, studyDao, null
        );
        replayMocks();

        setup.apply();
        verifyMocks();
        // no exceptions
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

    public void testProvisioningAllowedForManagedStudyWithTemplateManagerRole() throws Exception {
        command.setProvisionableManagedStudies(Arrays.asList(studyA));
        command.setProvisionableParticipatingStudies(Arrays.asList(studyB));

        expectRoleChange(command, "study_calendar_template_builder", "add", "study", "A");
        SuiteRoleMembership srm = expectGetAndReplaceMembership(SuiteRole.STUDY_CALENDAR_TEMPLATE_BUILDER);
        replayMocks();

        command.apply();
        verifyMocks();
        assertContains("Study not added", srm.getStudyIdentifiers(), "A");
    }

    public void testProvisioningNotAllowedForParticipatingStudyWithTemplateManagerRole() throws Exception {
        command.setProvisionableManagedStudies(Arrays.asList(studyA));
        command.setProvisionableParticipatingStudies(Arrays.asList(studyB));

        expectRoleChange(command, "study_calendar_template_builder", "add", "study", "B");
        replayMocks();  // expect nothing

        command.apply();
        verifyMocks();
    }

    public void testProvisioningAllowedForParticipatingStudyWithParticipationRole() throws Exception {
        command.setProvisionableManagedStudies(Arrays.asList(studyA));
        command.setProvisionableParticipatingStudies(Arrays.asList(studyB));

        expectRoleChange(command, "study_subject_calendar_manager", "add", "study", "B");
        SuiteRoleMembership srm = expectGetAndReplaceMembership(SuiteRole.STUDY_SUBJECT_CALENDAR_MANAGER);
        replayMocks();

        command.apply();
        verifyMocks();
        assertContains("Study not added", srm.getStudyIdentifiers(), "B");
    }

    public void testProvisioningNotAllowedForManagedStudyWithParticipationRole() throws Exception {
        command.setProvisionableManagedStudies(Arrays.asList(studyA));
        command.setProvisionableParticipatingStudies(Arrays.asList(studyB));

        expectRoleChange(command, "study_subject_calendar_manager", "add", "study", "A");
        replayMocks();  // expect nothing

        command.apply();
        verifyMocks();
    }

    public void testProvisioningAllowedForParticipatingStudyWithTemplateManagerAndParticipationRole() throws Exception {
        command.setProvisionableManagedStudies(Arrays.asList(studyA));
        command.setProvisionableParticipatingStudies(Arrays.asList(studyB));

        expectRoleChange(command, "study_qa_manager", "add", "study", "B");
        SuiteRoleMembership srm = expectGetAndReplaceMembership(SuiteRole.STUDY_QA_MANAGER);
        replayMocks();

        command.apply();
        verifyMocks();
        assertContains("Study not added", srm.getStudyIdentifiers(), "B");
    }

    public void testProvisioningAllowedForManagedStudyWithTemplateManagerAndParticipationRole() throws Exception {
        command.setProvisionableManagedStudies(Arrays.asList(studyA));
        command.setProvisionableParticipatingStudies(Arrays.asList(studyB));

        expectRoleChange(command, "study_qa_manager", "add", "study", "A");
        SuiteRoleMembership srm = expectGetAndReplaceMembership(SuiteRole.STUDY_QA_MANAGER);
        replayMocks();

        command.apply();
        verifyMocks();
        assertContains("Study not added", srm.getStudyIdentifiers(), "A");
    }

    public void testAllStudiesProvisioningAllowedWithAllManagedPermissionAndTemplateManagerRole() throws Exception {
        command.setCanProvisionManagingAllStudies(true);
        command.setCanProvisionParticipateInAllStudies(false);

        expectRoleChange(command, "study_calendar_template_builder", "add", "study", "__ALL__");
        SuiteRoleMembership srm = expectGetAndReplaceMembership(SuiteRole.STUDY_CALENDAR_TEMPLATE_BUILDER);
        replayMocks();

        command.apply();
        verifyMocks();
        assertTrue("Not added", srm.isAllStudies());
    }

    public void testAllStudiesProvisioningNotAllowedWithAllParticipatingPermissionAndTemplateManagerRole() throws Exception {
        command.setCanProvisionManagingAllStudies(false);
        command.setCanProvisionParticipateInAllStudies(true);

        expectRoleChange(command, "study_calendar_template_builder", "add", "study", "__ALL__");
        replayMocks(); // expect nothing

        command.apply();
        verifyMocks();
    }

    public void testAllStudiesProvisioningAllowedWithAllParticipatingPermissionAndParticipationRole() throws Exception {
        command.setCanProvisionManagingAllStudies(false);
        command.setCanProvisionParticipateInAllStudies(true);

        expectRoleChange(command, "study_subject_calendar_manager", "add", "study", "__ALL__");
        SuiteRoleMembership srm = expectGetAndReplaceMembership(SuiteRole.STUDY_SUBJECT_CALENDAR_MANAGER);
        replayMocks();

        command.apply();
        verifyMocks();
        assertTrue("Not added", srm.isAllStudies());
    }

    public void testAllStudiesProvisioningNotAllowedWithAllManagingPermissionAndParticipationRole() throws Exception {
        command.setCanProvisionManagingAllStudies(true);
        command.setCanProvisionParticipateInAllStudies(false);

        expectRoleChange(command, "study_subject_calendar_manager", "add", "study", "__ALL__");
        replayMocks(); // expect nothing

        command.apply();
        verifyMocks();
    }

    public void testAllStudiesProvisioningAllowedWithAllParticipatingPermissionAndBothRole() throws Exception {
        command.setCanProvisionManagingAllStudies(false);
        command.setCanProvisionParticipateInAllStudies(true);

        expectRoleChange(command, "data_reader", "add", "study", "__ALL__");
        SuiteRoleMembership srm = expectGetAndReplaceMembership(SuiteRole.DATA_READER);
        replayMocks();

        command.apply();
        verifyMocks();
        assertTrue("Not added", srm.isAllStudies());
    }

    public void testAllStudiesProvisioningAllowedWithAllManagingPermissionAndBothRole() throws Exception {
        command.setCanProvisionManagingAllStudies(true);
        command.setCanProvisionParticipateInAllStudies(false);

        expectRoleChange(command, "data_reader", "add", "study", "__ALL__");
        SuiteRoleMembership srm = expectGetAndReplaceMembership(SuiteRole.DATA_READER);
        replayMocks();

        command.apply();
        verifyMocks();
        assertTrue("Not added", srm.isAllStudies());
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

    ////// javascript setup data
    
    public void testJavaScriptUserForEmptyUserIsCorrect() throws Exception {
        assertEquals("new psc.admin.ProvisionableUser('jo', {})", command.getJavaScriptProvisionableUser());
    }

    public void testJavaScriptUserWithOneGroupOnlyRole() throws Exception {
        ProvisionUserCommand actual = create(new PscUserBuilder("jo").add(PscRole.SYSTEM_ADMINISTRATOR).toUser());
        assertEquals("new psc.admin.ProvisionableUser('jo', {\"system_administrator\": {}})",
            actual.getJavaScriptProvisionableUser());
    }

    public void testJavaScriptUserWithOneSiteScopedRole() throws Exception {
        ProvisionUserCommand actual = create(
            new PscUserBuilder("jo").add(PscRole.USER_ADMINISTRATOR).forSites(austin).toUser());
        assertEquals("new psc.admin.ProvisionableUser('jo', {\"user_administrator\": {\"sites\": [\"i-a\"]}})",
            actual.getJavaScriptProvisionableUser());
    }

    public void testJavaScriptUserWithOneSitePlusStudyScopedRole() throws Exception {
        ProvisionUserCommand actual = create(
            new PscUserBuilder("jo").add(PscRole.DATA_READER).forSites(sanAntonio).forAllStudies().toUser());
        assertEquals("new psc.admin.ProvisionableUser('jo', {\"data_reader\": {\n    \"sites\": [\"i-sa\"],\n    \"studies\": [\"__ALL__\"]\n}})",
            actual.getJavaScriptProvisionableUser());
    }

    public void testJavaScriptUserWithMultipleRoles() throws Exception {
        ProvisionUserCommand actual = create(
            new PscUserBuilder("jo").
                add(PscRole.USER_ADMINISTRATOR).forSites(austin).
                add(PscRole.DATA_READER).forAllSites().
                    forStudies(Fixtures.createBasicTemplate("T"), Fixtures.createBasicTemplate("Q")).
                toUser());
        assertEquals("new psc.admin.ProvisionableUser('jo', {\n    \"data_reader\": {\n        \"sites\": [\"__ALL__\"],\n        \"studies\": [\n            \"T\",\n            \"Q\"\n        ]\n    },\n    \"user_administrator\": {\"sites\": [\"i-a\"]}\n})",
            actual.getJavaScriptProvisionableUser());
    }

    public void testJavaScriptProvisionableSites() throws Exception {
        JSONArray list = command.buildJavaScriptProvisionableSites();
        assertEquals("Wrong number of sites", 3, list.length());
        assertEquals("Wrong 1st site", "__ALL__", list.getJSONObject(0).get("identifier"));
        assertEquals("Wrong 2nd site", "i-a",     list.getJSONObject(1).get("identifier"));
        assertEquals("Wrong 3rd site", "i-sa",    list.getJSONObject(2).get("identifier"));
    }

    public void testJavaScriptProvisionableSitesIncludesAllWhenAll() throws Exception {
        command.setCanProvisionAllSites(true);

        JSONArray list = command.buildJavaScriptProvisionableSites();
        assertEquals("Should be all", "__ALL__", list.getJSONObject(0).get("identifier"));
    }

    public void testJavaScriptProvisionableSitesDoesNotIncludeAllWhenNotAll() throws Exception {
        command.setCanProvisionAllSites(false);

        JSONArray list = command.buildJavaScriptProvisionableSites();
        assertNotEquals("Should not be all", "__ALL__", list.getJSONObject(0).get("identifier"));
    }

    public void testJavaScriptProvisionableStudiesIncludesTemplateManagementStudies() throws Exception {
        command.setProvisionableManagedStudies(Arrays.asList(studyC, studyA));
        command.setCanProvisionManagingAllStudies(true);

        JSONObject map = command.buildJavaScriptProvisionableStudies();
        JSONArray actual = map.optJSONArray("template_management");
        assertNotNull(actual);
        assertEquals("Wrong number of studies", 3, actual.length());
        assertEquals("Wrong 1st study", "__ALL__", actual.getJSONObject(0).get("identifier"));
        assertEquals("Wrong 2nd study", "A", actual.getJSONObject(1).get("identifier"));
        assertEquals("Wrong 3rd study", "C", actual.getJSONObject(2).get("identifier"));
    }

    public void testJavaScriptProvisionableStudiesIncludesSiteParticipationStudies() throws Exception {
        command.setProvisionableParticipatingStudies(Arrays.asList(studyC));
        command.setCanProvisionParticipateInAllStudies(true);

        JSONObject map = command.buildJavaScriptProvisionableStudies();
        JSONArray actual = map.optJSONArray("site_participation");
        assertNotNull(actual);
        assertEquals("Wrong number of studies", 2, actual.length());
        assertEquals("Wrong 1st study", "__ALL__", actual.getJSONObject(0).get("identifier"));
        assertEquals("Wrong 2nd study", "C", actual.getJSONObject(1).get("identifier"));
    }

    public void testJavaScriptProvisionableStudiesIncludesBothKindsOfStudies() throws Exception {
        command.setProvisionableManagedStudies(Arrays.asList(studyC, studyA));
        command.setCanProvisionManagingAllStudies(false);
        command.setProvisionableParticipatingStudies(Arrays.asList(studyC, studyB));
        command.setCanProvisionParticipateInAllStudies(false);

        JSONObject map = command.buildJavaScriptProvisionableStudies();
        JSONArray actual = map.optJSONArray("template_management+site_participation");
        assertNotNull(actual);
        assertEquals("Wrong number of studies", 3, actual.length());
        assertEquals("Wrong 1st study", "A", actual.getJSONObject(0).get("identifier"));
        assertEquals("Wrong 2nd study", "B", actual.getJSONObject(1).get("identifier"));
        assertEquals("Wrong 3rd study", "C", actual.getJSONObject(2).get("identifier"));
    }

    public void testJavaScriptProvisionableStudiesIncludesAllForBothIfParticipationAll() throws Exception {
        command.setCanProvisionManagingAllStudies(false);
        command.setCanProvisionParticipateInAllStudies(true);

        JSONObject map = command.buildJavaScriptProvisionableStudies();
        JSONArray actual = map.optJSONArray("template_management+site_participation");
        assertNotNull(actual);
        assertEquals("Should have all", "__ALL__", actual.getJSONObject(0).get("identifier"));
    }

    public void testJavaScriptProvisionableStudiesIncludesAllForBothIfManagingAll() throws Exception {
        command.setCanProvisionManagingAllStudies(true);
        command.setCanProvisionParticipateInAllStudies(false);

        JSONObject map = command.buildJavaScriptProvisionableStudies();
        JSONArray actual = map.optJSONArray("template_management+site_participation");
        assertNotNull(actual);
        assertEquals("Should have all", "__ALL__", actual.getJSONObject(0).get("identifier"));
    }

    public void testJavaScriptProvisionableStudiesAlwaysPutsAllFirst() throws Exception {
        command.setProvisionableParticipatingStudies(Arrays.asList(Fixtures.createBasicTemplate("[ABC 1234]")));
        command.setCanProvisionParticipateInAllStudies(true);

        JSONObject map = command.buildJavaScriptProvisionableStudies();
        JSONArray actual = map.optJSONArray("site_participation");
        assertNotNull(actual);
        assertEquals("Wrong number of studies", 2, actual.length());
        assertEquals("Wrong 1st study", "__ALL__", actual.getJSONObject(0).get("identifier"));
        assertEquals("Wrong 2nd study", "[ABC 1234]", actual.getJSONObject(1).get("identifier"));
    }

    ////// HELPERS

    private void expectRoleChange(String roleKey, String changeKind) {
        expectRoleChange(roleKey, changeKind, null, null);
    }

    private void expectRoleChange(String roleKey, String changeKind, String scopeType, String scopeIdent) {
        expectRoleChange(this.command, roleKey, changeKind, scopeType, scopeIdent);
    }

    private void expectRoleChange(
        ProvisionUserCommand cmd,
        String roleKey, String changeKind, String scopeType, String scopeIdent
    ) {
        MapBuilder<String, String> mb = new MapBuilder<String, String>().
            put("role", roleKey).put("kind", changeKind);
        if (scopeType != null) {
            mb.put("scopeType", scopeType).put("scopeIdentifier", scopeIdent);
        }

        cmd.getRoleChanges().put(new JSONObject(mb.toMap()));
    }

    ////// INNER CLASSES

    private class StubStudyDao extends StudyDao {
        @Override
        public List<Study> getAll() {
            return Arrays.asList(studyA, studyB, studyC);
        }

        @Override
        public List<Study> getVisibleStudiesForTemplateManagement(VisibleStudyParameters params) {
            if (params.isAllManagingSites()) {
                return Arrays.asList(studyA, studyB);
            } else {
                return Arrays.asList(studyA);
            }
        }

        @Override
        public List<Study> getVisibleStudiesForSiteParticipation(VisibleStudyParameters params) {
            if (params.isAllParticipatingSites()) {
                return Arrays.asList(studyB, studyC);
            } else {
                return Arrays.asList(studyC);
            }
        }
    }
}
