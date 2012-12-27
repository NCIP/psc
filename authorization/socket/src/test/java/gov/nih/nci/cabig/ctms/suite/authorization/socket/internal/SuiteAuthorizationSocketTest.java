/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package gov.nih.nci.cabig.ctms.suite.authorization.socket.internal;

import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteAuthorizationSource;
import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteUser;
import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteUserRoleLevel;
import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteUserSearchOptions;
import gov.nih.nci.cabig.ctms.suite.authorization.socket.internal.SuiteAuthorizationSocket;
import gov.nih.nci.cabig.ctms.testing.MockRegistry;
import gov.nih.nci.security.authorization.domainobjects.Group;
import gov.nih.nci.security.authorization.domainobjects.ProtectionElement;
import gov.nih.nci.security.authorization.domainobjects.ProtectionElementPrivilegeContext;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
import gov.nih.nci.security.authorization.domainobjects.Role;
import gov.nih.nci.security.authorization.domainobjects.User;
import gov.nih.nci.security.dao.GroupSearchCriteria;
import gov.nih.nci.security.dao.InstanceLevelMappingElementSearchCriteria;
import gov.nih.nci.security.dao.ProtectionElementSearchCriteria;
import gov.nih.nci.security.dao.ProtectionGroupSearchCriteria;
import gov.nih.nci.security.dao.RoleSearchCriteria;
import gov.nih.nci.security.dao.UserSearchCriteria;
import gov.nih.nci.security.exceptions.CSObjectNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.BeanWrapperImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings( { "unchecked" })
public class SuiteAuthorizationSocketTest {
    private SuiteAuthorizationSocket socket;
    private SuiteAuthorizationSource plugin;

    private MockRegistry mocks = new MockRegistry();

    @Before
    public void before() throws Exception {
        plugin = mocks.registerMockFor(SuiteAuthorizationSource.class);

        socket = new SuiteAuthorizationSocket(plugin);
    }

    @Test
    public void getObjectsForRoleReturnsDummyRoleForKnownSuiteRole() throws Exception {
        SuiteRole suiteRole = SuiteRole.DATA_ANALYST;

        Role param = new Role();
        param.setName(suiteRole.getCsmName());

        List<Role> actualResults = socket.getObjects(new RoleSearchCriteria(param));
        assertThat(actualResults.size(), is(1));
        Role actual = actualResults.get(0);
        assertThat(actual.getName(), is(suiteRole.getCsmName()));
        assertThat(actual.getId(), is((long) suiteRole.ordinal()));
    }

    @Test
    public void getObjectsForRoleReturnsNothingForUnknownSuiteRole() throws Exception {
        Role param = new Role();
        param.setName("fred's special role");

        List<Role> actualResults = socket.getObjects(new RoleSearchCriteria(param));
        assertThat(actualResults.size(), is(0));
    }

    @Test
    public void getObjectsForRoleAbortsForLikeCriteria() throws Exception {
        Role param = new Role();
        param.setName("%fred%");

        try {
            socket.getObjects(new RoleSearchCriteria(param));
            fail("Exception not thrown");
        } catch (UnsupportedOperationException uoe) {
            assertThat(uoe.getMessage(), is("Unsupported criterion: only exact matches are supported for Role"));
        }
    }

    @Test
    public void getObjectsForRoleAbortsForBlankCriteria() throws Exception {
        Role param = new Role();

        try {
            socket.getObjects(new RoleSearchCriteria(param));
            fail("Exception not thrown");
        } catch (UnsupportedOperationException uoe) {
            assertThat(uoe.getMessage(), is("Unsupported criterion: only exact matches are supported for Role"));
        }
    }

    @Test
    public void rolesReturnedCanBeToStringed() throws Exception {
        Role param = new Role();
        param.setName(SuiteRole.LAB_DATA_USER.getCsmName());

        assertThat(socket.getObjects(new RoleSearchCriteria(param)).get(0).toString(),
            is(not(nullValue())));
    }

    @Test
    public void getObjectsForGroupReturnsDummyGroupForKnownSuiteRole() throws Exception {
        SuiteRole suiteRole = SuiteRole.AE_RULE_AND_REPORT_MANAGER;

        Group param = new Group();
        param.setGroupName(suiteRole.getCsmName());

        List<Group> actualResults = socket.getObjects(new GroupSearchCriteria(param));
        assertThat(actualResults.size(), is(1));
        Group actual = actualResults.get(0);
        assertThat(actual.getGroupName(), is(suiteRole.getCsmName()));
        assertThat(actual.getGroupId(), is((long) suiteRole.ordinal()));
    }

    @Test
    public void getObjectsForGroupReturnsNothingForUnknownSuiteRole() throws Exception {
        Group param = new Group();
        param.setGroupName("fred's special group");

        List<Group> actualResults = socket.getObjects(new GroupSearchCriteria(param));
        assertThat(actualResults.size(), is(0));
    }

    @Test
    public void getObjectsForGroupAbortsForLikeCriteria() throws Exception {
        Group param = new Group();
        param.setGroupName("%fred%");

        try {
            socket.getObjects(new GroupSearchCriteria(param));
            fail("Exception not thrown");
        } catch (UnsupportedOperationException uoe) {
            assertThat(uoe.getMessage(), is("Unsupported criterion: only exact matches are supported for Group"));
        }
    }

    @Test
    public void getObjectsForGroupAbortsForBlankCriteria() throws Exception {
        Group param = new Group();

        try {
            socket.getObjects(new GroupSearchCriteria(param));
            fail("Exception not thrown");
        } catch (UnsupportedOperationException uoe) {
            assertThat(uoe.getMessage(), is("Unsupported criterion: only exact matches are supported for Group"));
        }
    }

    @Test
    public void groupsReturnedCanBeToStringed() throws Exception {
        Group param = new Group();
        param.setGroupName(SuiteRole.STUDY_CALENDAR_TEMPLATE_BUILDER.getCsmName());

        assertThat(socket.getObjects(new GroupSearchCriteria(param)).get(0).toString(),
            is(not(nullValue())));
    }

    @Test
    public void getObjectsForPGReturnsDummyPGForName() throws Exception {
        ProtectionGroup param = new ProtectionGroup();
        param.setProtectionGroupName("Study.FOO1701");

        List<ProtectionGroup> actualResults =
            socket.getObjects(new ProtectionGroupSearchCriteria(param));
        assertThat(actualResults.size(), is(1));
        ProtectionGroup actual = actualResults.get(0);
        assertThat(actual.getProtectionGroupName(), is("Study.FOO1701"));
        assertThat(actual.getProtectionGroupId(), is(not(nullValue())));
    }

    @Test
    public void getObjectsForPGAbortsForLikeCriteria() throws Exception {
        ProtectionGroup param = new ProtectionGroup();
        param.setProtectionGroupName("%FOO%");

        try {
            socket.getObjects(new ProtectionGroupSearchCriteria(param));
            fail("Exception not thrown");
        } catch (UnsupportedOperationException uoe) {
            assertThat(uoe.getMessage(), is("Unsupported criterion: only exact matches are supported for ProtectionGroup"));
        }
    }

    @Test
    public void getObjectsForPGAbortsForBlankCriteria() throws Exception {
        ProtectionGroup param = new ProtectionGroup();

        try {
            socket.getObjects(new ProtectionGroupSearchCriteria(param));
            fail("Exception not thrown");
        } catch (UnsupportedOperationException uoe) {
            assertThat(uoe.getMessage(), is("Unsupported criterion: only exact matches are supported for ProtectionGroup"));
        }
    }

    @Test
    public void pgReturnedCanBeToStringed() throws Exception {
        ProtectionGroup param = new ProtectionGroup();
        param.setProtectionGroupName("Study.FOO1701");

        List<ProtectionGroup> actualResults =
            socket.getObjects(new ProtectionGroupSearchCriteria(param));
        assertThat(actualResults.get(0).toString(), is(not(nullValue())));
    }

    @Test
    public void getObjectsForPEReturnsDummyPEForName() throws Exception {
        ProtectionElement param = new ProtectionElement();
        param.setObjectId("Study.FOO1701");

        List<ProtectionElement> actualResults =
            socket.getObjects(new ProtectionElementSearchCriteria(param));
        assertThat(actualResults.size(), is(1));
        ProtectionElement actual = actualResults.get(0);
        assertThat(actual.getProtectionElementName(), is("Study.FOO1701"));
        assertThat(actual.getObjectId(), is("Study.FOO1701"));
        assertThat(actual.getProtectionElementId(), is(not(nullValue())));
    }

    @Test
    public void getObjectsForPEAbortsForLikeCriteria() throws Exception {
        ProtectionElement param = new ProtectionElement();
        param.setObjectId("%FOO%");

        try {
            socket.getObjects(new ProtectionElementSearchCriteria(param));
            fail("Exception not thrown");
        } catch (UnsupportedOperationException uoe) {
            assertThat(uoe.getMessage(), is("Unsupported criterion: only exact matches are supported for ProtectionElement"));
        }
    }

    @Test
    public void getObjectsForPEAbortsForBlankCriteria() throws Exception {
        ProtectionElement param = new ProtectionElement();

        try {
            socket.getObjects(new ProtectionElementSearchCriteria(param));
            fail("Exception not thrown");
        } catch (UnsupportedOperationException uoe) {
            assertThat(uoe.getMessage(), is("Unsupported criterion: only exact matches are supported for ProtectionElement"));
        }
    }

    @Test
    public void peReturnedCanBeToStringed() throws Exception {
        ProtectionElement param = new ProtectionElement();
        param.setProtectionElementName("Study.FOO1701");

        List<ProtectionElement> actualResults =
            socket.getObjects(new ProtectionElementSearchCriteria(param));
        assertThat(actualResults.get(0).toString(), is(not(nullValue())));
    }

    @Test
    public void getObjectsForUnknownCriteriaTypeThrowsUnsupportedException() throws Exception {
        try {
            socket.getObjects(new InstanceLevelMappingElementSearchCriteria(null));
            fail("Exception not thrown");
        } catch (UnsupportedOperationException uoe) {
            assertThat(uoe.getMessage(),
                is("Unsupported criteria object type: InstanceLevelMappingElement"));
        }
    }

    @Test
    public void getProtectionElementsByPgIdGivesNothingForNeverBeforeSeenPgId() throws Exception {
        assertThat(socket.getProtectionElements("14").size(), is(0));
    }

    @Test
    public void getProtectionElementsByPgIdGivesSolePEForPreviouslyReferencedPG() throws Exception {
        String name = "Study.BAR 8901";
        ProtectionGroup existingPG;
        {
            ProtectionGroup param = new ProtectionGroup();
            param.setProtectionGroupName(name);
            List<ProtectionGroup> results = socket.getObjects(new ProtectionGroupSearchCriteria(param));
            existingPG = results.get(0);
        }

        Set<ProtectionElement> actualResults =
            socket.getProtectionElements(existingPG.getProtectionGroupId().toString());
        assertThat(actualResults.size(), is(1));
        ProtectionElement actualPE = actualResults.iterator().next();
        assertThat(actualPE.getProtectionElementName(), is(name));
        assertThat(actualPE.getProtectionElementId(), is(existingPG.getProtectionGroupId()));
    }

    @Test
    public void getProtectionElementByObjectIdGivesDummyPE() throws Exception {
        String expectedObjectId = "Site.QUUX0522";

        ProtectionElement actual = socket.getProtectionElement(expectedObjectId);
        assertThat(actual.getObjectId(), is(expectedObjectId));
        assertThat(actual.getProtectionElementName(), is(expectedObjectId));
        assertThat(actual.getProtectionElementId(), is(not(nullValue())));
    }

    @Test
    public void getProtectionElementAndGetObjectForPEGiveTheSamePE() throws Exception {
        String expectedObjectId = "Site.QUUX0522";

        ProtectionElement byObjectId = socket.getProtectionElement(expectedObjectId);
        ProtectionElement crit = new ProtectionElement();
        crit.setObjectId(expectedObjectId);
        ProtectionElement byCriteria =
            (ProtectionElement) socket.getObjects(new ProtectionElementSearchCriteria(crit)).get(0);

        assertThat(
            byObjectId.getProtectionElementId(),
            is(equalTo(byCriteria.getProtectionElementId())));
    }

    @Test
    public void getProtectionElementAndGetObjectForDifferentPEsGiveTheDifferentPEs() throws Exception {
        ProtectionElement byObjectId = socket.getProtectionElement("Site.QU0522");

        ProtectionElement crit = new ProtectionElement();
        crit.setObjectId("Site.QU0531");
        ProtectionElement byCriteria =
            (ProtectionElement) socket.getObjects(new ProtectionElementSearchCriteria(crit)).get(0);

        assertThat(
            byObjectId.getProtectionElementId(),
            is(not(equalTo(byCriteria.getProtectionElementId()))));
    }

    @Test
    public void getUserByUsernameReturnsNullForUnknown() throws Exception {
        expect(plugin.getUser("fred", SuiteUserRoleLevel.NONE)).andReturn(null);
        mocks.replayMocks();

        assertThat(socket.getUser("fred"), is(nullValue()));
    }

    @Test
    public void suiteUserConversionIncludesTheUsername() throws Exception {
        User actual = doGetUser(userBuilder().username("fred").toUser());

        assertThat(actual.getLoginName(), is("fred"));
    }

    @Test
    public void suiteUserConversionIncludesTheId() throws Exception {
        User actual = doGetUser(userBuilder().id(11).toUser());

        assertThat(actual.getUserId(), is(11L));
    }

    @Test
    public void suiteUserConversionIncludesTheFirstName() throws Exception {
        User actual = doGetUser(userBuilder().name("Frederick", "Blue").toUser());

        assertThat(actual.getFirstName(), is("Frederick"));
    }

    @Test
    public void suiteUserConversionIncludesTheLastName() throws Exception {
        User actual = doGetUser(userBuilder().name("Frederick", "Blue").toUser());

        assertThat(actual.getLastName(), is("Blue"));
    }

    @Test
    public void suiteUserConversionIncludesTheEmailAddress() throws Exception {
        User actual = doGetUser(userBuilder().emailAddress("fred@example.com").toUser());

        assertThat(actual.getEmailId(), is("fred@example.com"));
    }

    @Test
    public void suiteUserConversionIncludesTheEndDate() throws Exception {
        Date expected = new Date();
        User actual = doGetUser(userBuilder().accountEndsOn(expected).toUser());

        assertThat(actual.getEndDate(), is(expected));
    }

    @Test
    public void createdUserCanBeToStringed() throws Exception {
        User actual = doGetUser(userBuilder().emailAddress("fred@example.com").toUser());

        assertThat(actual.toString(), is(not(nullValue())));
    }

    @Test
    public void getUserByIdReturnsUserIfInSource() throws Exception {
        expect(plugin.getUser(67, SuiteUserRoleLevel.NONE)).andReturn(
            userBuilder().id(67).username("betty").toUser());
        mocks.replayMocks();

        User actual = socket.getUserById("67");
        assertThat(actual.getLoginName(), is("betty"));
    }

    @Test
    public void getUserByIdThrowsExceptionIfNotInSource() throws Exception {
        expect(plugin.getUser(67, SuiteUserRoleLevel.NONE)).andReturn(null);
        mocks.replayMocks();

        try {
            socket.getUserById("67");
            fail("Exception not thrown");
        } catch (CSObjectNotFoundException e) {
            assertThat(e.getMessage(), is("No user with ID 67 in the source"));
        }
    }

    @Test
    public void getUserObjectsForExactUsernameGetsUserByName() throws Exception {
        expect(plugin.getUser("fred", SuiteUserRoleLevel.NONE)).andReturn(
            userBuilder().id(13).toUser());

        User param = new User();
        param.setLoginName("fred");
        List<User> actual = doGetUserObjects(param);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getUserId(), is(13L));
    }

    @Test
    public void getUserObjectsForUserSubstringSearches() throws Exception {
        expect(plugin.searchUsers(SuiteUserSearchOptions.forUsernameSubstring("ada"))).
            andReturn(Arrays.asList(
                userBuilder().username("adaline").toUser(),
                userBuilder().username("bradavon").toUser()));

        User param = new User();
        param.setLoginName("%ada%");
        List<User> actual = doGetUserObjects(param);
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0).getLoginName(), is("adaline"));
        assertThat(actual.get(1).getLoginName(), is("bradavon"));
    }

    @Test
    public void getUserObjectsForFirstNameSubstringSearches() throws Exception {
        expect(plugin.searchUsers(SuiteUserSearchOptions.forFirstNameSubstring("Ada"))).
            andReturn(Arrays.asList(userBuilder().username("adaline").toUser()));

        User param = new User();
        param.setFirstName("%Ada%");
        List<User> actual = doGetUserObjects(param);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getLoginName(), is("adaline"));
    }

    @Test
    public void getUserObjectsForLastNameSubstringSearches() throws Exception {
        expect(plugin.searchUsers(SuiteUserSearchOptions.forLastNameSubstring("Jone"))).
            andReturn(Arrays.asList(userBuilder().username("adaline").toUser()));

        User param = new User();
        param.setLastName("%Jone%");
        List<User> actual = doGetUserObjects(param);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getLoginName(), is("adaline"));
    }

    @Test
    public void getUserObjectsForExactFirstNameAborts() throws Exception {
        User param = new User();
        param.setFirstName("Fred");

        assertSubstringOnlyForUserPropertyErrorOccurs(param, "firstName");
    }

    @Test
    public void getUserObjectsForHeadOfFirstNameAborts() throws Exception {
        User param = new User();
        param.setFirstName("Fred%");

        assertSubstringOnlyForUserPropertyErrorOccurs(param, "firstName");
    }

    @Test
    public void getUserObjectsForTailOfFirstNameAborts() throws Exception {
        User param = new User();
        param.setFirstName("%Fred");

        assertSubstringOnlyForUserPropertyErrorOccurs(param, "firstName");
    }

    @Test
    public void getUserObjectsForExactLastNameAborts() throws Exception {
        User param = new User();
        param.setLastName("Fred");

        assertSubstringOnlyForUserPropertyErrorOccurs(param, "lastName");
    }

    @Test
    public void getUserObjectsForHeadOfLastNameAborts() throws Exception {
        User param = new User();
        param.setLastName("Fred%");

        assertSubstringOnlyForUserPropertyErrorOccurs(param, "lastName");
    }

    @Test
    public void getUserObjectsForTailOfLastNameAborts() throws Exception {
        User param = new User();
        param.setLastName("%Fred");

        assertSubstringOnlyForUserPropertyErrorOccurs(param, "lastName");
    }

    @Test
    public void getUserObjectsForOtherUserPropertiesFails() throws Exception {
        User param = new User();
        param.setEmailId("fred@etc.com");

        assertUnsupportedUserCriteriaEncounteredForSearch(
            param, "Unsupported criterion: emailId searches not supported for User");
    }

    @Test
    public void getUserObjectsForHeadOfUsernameAborts() throws Exception {
        User param = new User();
        param.setLoginName("fred%");

        assertUnsupportedUserCriteriaEncounteredForSearch(param,
            "Unsupported criterion: only substring or exact matches are supported for User#loginName");
    }

    @Test
    public void getUserObjectsForTailOfUsernameAborts() throws Exception {
        User param = new User();
        param.setLoginName("%fred");

        assertUnsupportedUserCriteriaEncounteredForSearch(param,
            "Unsupported criterion: only substring or exact matches are supported for User#loginName");
    }

    private void assertSubstringOnlyForUserPropertyErrorOccurs(User param, String expectedProperty) {
        String expectedMessage = "Unsupported criterion: only substring matches are supported for User#" + expectedProperty;
        assertUnsupportedUserCriteriaEncounteredForSearch(param, expectedMessage);
    }

    private void assertUnsupportedUserCriteriaEncounteredForSearch(User param, String expectedMessage) {
        try {
            socket.getObjects(new UserSearchCriteria(param));
            fail("Exception not thrown");
        } catch (UnsupportedOperationException uoe) {
            assertThat(uoe.getMessage(), is(expectedMessage));
        }
    }

    @Test
    public void getUserObjectsForMultipleLegalCriteria() throws Exception {
        expect(plugin.searchUsers(SuiteUserSearchOptions.forAllNames("ada"))).
            andReturn(Arrays.asList(userBuilder().username("adaline").toUser()));

        User param = new User();
        param.setLoginName("%ada%");
        param.setFirstName("%ada%");
        param.setLastName("%ada%");
        List<User> actual = doGetUserObjects(param);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getLoginName(), is("adaline"));
    }

    @Test
    public void getUserObjectsForNoResultsReturnsNoResults() throws Exception {
        expect(plugin.searchUsers(SuiteUserSearchOptions.forLastNameSubstring("Dr"))).
            andReturn(Collections.<SuiteUser>emptyList());

        User param = new User();
        param.setLastName("%Dr%");
        List<User> actual = doGetUserObjects(param);
        assertThat(actual.size(), is(0));
    }

    @Test
    public void getUserObjectsForBlankCriteriaSearchesForAllUsers() throws Exception {
        expect(plugin.searchUsers(SuiteUserSearchOptions.allUsers())).
            andReturn(Arrays.asList(
                userBuilder().username("adaline").toUser(),
                userBuilder().username("bradavon").toUser()));

        User param = new User();
        List<User> actual = doGetUserObjects(param);
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0).getLoginName(), is("adaline"));
        assertThat(actual.get(1).getLoginName(), is("bradavon"));
    }

    @Test
    public void getUsersForGroup() throws Exception {
        SuiteRole suiteRole = SuiteRole.STUDY_CREATOR;
        expect(plugin.getUsersByRole(suiteRole)).andReturn(Arrays.asList(
            userBuilder().username("jane").toUser(), userBuilder().username("jack").toUser()));
        mocks.replayMocks();

        Set<User> actual = socket.getUsers(Integer.toString(suiteRole.ordinal()));
        assertThat(actual.size(), is(2));
    }

    @Test
    public void getUsersForGroupWithInvalidGroupId() throws Exception {
        try {
            socket.getUsers("982");
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            assertThat(iae.getMessage(), is("982 does not correspond to a SuiteRole"));
        }
    }

    @Test
    public void getGroupsForUser() throws Exception {
        expect(plugin.getUser(54, SuiteUserRoleLevel.ROLES)).andReturn(
            userBuilder().username("fred").id(54).
                addRoleMembership(new SuiteRoleMembership(SuiteRole.DATA_READER, null, null).forAllSites()).
                addRoleMembership(new SuiteRoleMembership(SuiteRole.DATA_IMPORTER, null, null)).
                addRoleMembership(new SuiteRoleMembership(SuiteRole.SYSTEM_ADMINISTRATOR, null, null)).
                toUser());
        mocks.replayMocks();

        Set<Group> actual = socket.getGroups("54");
        mocks.verifyMocks();

        assertThat(actual.size(), is(3));
        Collection<String> names = collect("groupName", actual);
        assertThat(names, hasItem("data_reader"));
        assertThat(names, hasItem("data_importer"));
        assertThat(names, hasItem("system_administrator"));
    }

    @Test
    public void getGroupsForUnknownUserThrowsException() throws Exception {
        expect(plugin.getUser(54, SuiteUserRoleLevel.ROLES)).andReturn(null);
        mocks.replayMocks();

        try {
            socket.getGroups("54");
            fail("Exception not thrown");
        } catch (CSObjectNotFoundException e) {
            assertThat(e.getMessage(), is("No user with ID 54 in the source"));
        }
    }

    @Test
    public void getProtEltPrivCtxtReturnsRightNumberOfContexts() throws Exception {
        Set<ProtectionElementPrivilegeContext> actual = doGetProtectionElementPrivilegeContextSample();
        assertThat(actual.size(), is(4));
    }

    @Test
    public void getProtEltPrivCtxtIncludesAllSitePrivileges() throws Exception {
        ProtectionElementPrivilegeContext actual = findProtectionElementPrivilegeContextForPE(
            "HealthcareSite", doGetProtectionElementPrivilegeContextSample());

        assertThat(actual.getPrivileges().size(), is(2));
        Collection<String> actualPrivilegeNames = collect("name", actual.getPrivileges());
        assertThat(actualPrivilegeNames, hasItem("data_reader"));
        assertThat(actualPrivilegeNames, hasItem("study_calendar_template_builder"));
    }

    @Test
    public void getProtEltPrivCtxtIncludesAllStudyPrivileges() throws Exception {
        ProtectionElementPrivilegeContext actual = findProtectionElementPrivilegeContextForPE(
            "Study", doGetProtectionElementPrivilegeContextSample());

        assertThat(actual.getPrivileges().size(), is(1));
        Collection<String> actualPrivilegeNames = collect("name", actual.getPrivileges());
        assertThat(actualPrivilegeNames, hasItem("study_calendar_template_builder"));
    }

    @Test
    public void getProtEltPrivCtxtIncludesParticularSitePrivileges() throws Exception {
        ProtectionElementPrivilegeContext actual = findProtectionElementPrivilegeContextForPE(
            "HealthcareSite.QU036", doGetProtectionElementPrivilegeContextSample());

        assertThat(actual.getPrivileges().size(), is(1));
        Collection<String> actualPrivilegeNames = collect("name", actual.getPrivileges());
        assertThat(actualPrivilegeNames, hasItem("study_team_administrator"));
    }

    @Test
    public void getProtEltPrivCtxtIncludesParticularStudyPrivileges() throws Exception {
        ProtectionElementPrivilegeContext actual = findProtectionElementPrivilegeContextForPE(
            "Study.FOO 1701", doGetProtectionElementPrivilegeContextSample());

        assertThat(actual.getPrivileges().size(), is(1));
        Collection<String> actualPrivilegeNames = collect("name", actual.getPrivileges());
        assertThat(actualPrivilegeNames, hasItem("data_reader"));
    }

    @Test
    public void getProtEltPrivCtxtForUnknownUserThrowsException() throws Exception {
        expect(plugin.getUser(36, SuiteUserRoleLevel.ROLES_AND_SCOPES)).andReturn(null);
        mocks.replayMocks();

        try {
            socket.getProtectionElementPrivilegeContextForUser("36");
            fail("Exception not thrown");
        } catch (CSObjectNotFoundException e) {
            assertThat(e.getMessage(), is("No user with ID 36 in the source"));
        }
    }

    private ProtectionElementPrivilegeContext findProtectionElementPrivilegeContextForPE(
        String peObjectId, Set<ProtectionElementPrivilegeContext> protectionElementPrivilegeContexts
    ) {
        for (ProtectionElementPrivilegeContext ctxt : protectionElementPrivilegeContexts) {
            if (ctxt.getProtectionElement().getObjectId().equals(peObjectId)) {
                return ctxt;
            }
        }
        fail("No context for PE " + peObjectId);
        return null; // unreachable
    }

    private Set<ProtectionElementPrivilegeContext> doGetProtectionElementPrivilegeContextSample() throws CSObjectNotFoundException {
        expect(plugin.getUser(54, SuiteUserRoleLevel.ROLES_AND_SCOPES)).andReturn(
            userBuilder().username("fred").id(54).
                addRoleMembership(new SuiteRoleMembership(SuiteRole.DATA_READER, null, null).
                    forAllSites().forStudies("FOO 1701")).
                addRoleMembership(new SuiteRoleMembership(SuiteRole.STUDY_TEAM_ADMINISTRATOR, null, null).
                    forSites("QU036")).
                addRoleMembership(new SuiteRoleMembership(SuiteRole.STUDY_CALENDAR_TEMPLATE_BUILDER, null, null).
                    forAllSites().forAllStudies()).
                addRoleMembership(new SuiteRoleMembership(SuiteRole.BUSINESS_ADMINISTRATOR, null, null)).
                toUser());
        mocks.replayMocks();

        Set<ProtectionElementPrivilegeContext> actual = socket.getProtectionElementPrivilegeContextForUser("54");
        mocks.verifyMocks();
        return actual;
    }

    private <T> Collection<T> collect(String property, Collection<?> outOf) {
        List<T> collected = new ArrayList<T>(outOf.size());
        for (Object one : outOf) {
            collected.add((T) new BeanWrapperImpl(one).getPropertyValue(property));
        }
        return collected;
    }

    ////// HELPERS

    private User doGetUser(SuiteUser pluginUser) {
        expect(plugin.getUser("fred", SuiteUserRoleLevel.NONE)).andReturn(pluginUser);

        mocks.replayMocks();
        return socket.getUser("fred");
    }

    private List<User> doGetUserObjects(User param) {
        mocks.replayMocks();
        List results = socket.getObjects(new UserSearchCriteria(param));
        mocks.verifyMocks();
        return results;
    }

    private SuiteUser.Builder userBuilder() {
        return new SuiteUser.Builder(false);
    }
}
