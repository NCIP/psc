/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package gov.nih.nci.cabig.ctms.suite.authorization.plugins.mock.internal;

import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteUser;
import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteUserRoleLevel;
import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteUserSearchOptions;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author Rhett Sutphin
 */
public class StaticSuiteAuthorizationSourceTest {
    private StaticSuiteAuthorizationSource source;

    @Before
    public void before() throws Exception {
        source = new StaticSuiteAuthorizationSource();
    }

    @Test
    public void itReturnsTheSingleUserForTheCorrectId() throws Exception {
        SuiteUser actual = source.getUser(1, /* ignored */ null);
        assertThat(actual.getUsername(), is("superuser"));
    }

    @Test
    public void itReturnsNullForUnknownUserId() throws Exception {
        assertThat(source.getUser(2, SuiteUserRoleLevel.NONE), is(nullValue()));
    }

    @Test
    public void itReturnsTheSingleUserForTheCorrectUsername() throws Exception {
        SuiteUser actual = source.getUser("superuser", /* ignored */ null);
        assertThat(actual.getId(), is(1));
    }

    @Test
    public void itReturnsNullForUnknownUsername() throws Exception {
        assertThat(source.getUser("harold", SuiteUserRoleLevel.NONE), is(nullValue()));
    }

    @Test
    public void userIncludesAllRoles() throws Exception {
        SuiteUser actual = source.getUser(1, SuiteUserRoleLevel.ROLES_AND_SCOPES);
        assertThat(actual.getRoleMemberships().size(), is(SuiteRole.values().length));
    }

    @Test
    public void userIncludesAllScopes() throws Exception {
        SuiteUser actual = source.getUser(1, SuiteUserRoleLevel.ROLES_AND_SCOPES);
        // by example only
        SuiteRoleMembership dataReader = actual.getRoleMemberships().get(SuiteRole.DATA_READER);
        assertThat(dataReader.isAllSites(), is(true));
        assertThat(dataReader.isAllStudies(), is(true));
    }

    @Test
    public void getByRoleIncludesOnlyTheUser() throws Exception {
        Collection<SuiteUser> actual =
            source.getUsersByRole(SuiteRole.PERSON_AND_ORGANIZATION_INFORMATION_MANAGER);
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getUsername(), is("superuser"));
    }

    @Test
    public void searchIncludesTheUserWhenUsernameMatches() throws Exception {
        Collection<SuiteUser> actual =
            source.searchUsers(SuiteUserSearchOptions.forUsernameSubstring("upe"));
        assertThat(actual.size(), is(1));
    }

    @Test
    public void searchIsEmptyWhenUsernameDoesNotMatch() throws Exception {
        Collection<SuiteUser> actual =
            source.searchUsers(SuiteUserSearchOptions.forUsernameSubstring("fred"));
        assertThat(actual.size(), is(0));
    }

    @Test
    public void searchIncludesTheUserWhenFirstNameMatches() throws Exception {
        Collection<SuiteUser> actual =
            source.searchUsers(SuiteUserSearchOptions.forFirstNameSubstring("Su"));
        assertThat(actual.size(), is(1));
    }

    @Test
    public void searchIsEmptyWhenFirstNameDoesNotMatch() throws Exception {
        Collection<SuiteUser> actual =
            source.searchUsers(SuiteUserSearchOptions.forFirstNameSubstring("Fr"));
        assertThat(actual.size(), is(0));
    }

    @Test
    public void searchIncludesTheUserWhenLastNameMatches() throws Exception {
        Collection<SuiteUser> actual =
            source.searchUsers(SuiteUserSearchOptions.forLastNameSubstring("er"));
        assertThat(actual.size(), is(1));
    }

    @Test
    public void searchIsEmptyWhenLastNameDoesNotMatch() throws Exception {
        Collection<SuiteUser> actual =
            source.searchUsers(SuiteUserSearchOptions.forLastNameSubstring("re"));
        assertThat(actual.size(), is(0));
    }

    @Test
    public void searchMatchesWhenAllCriteriaMatch() throws Exception {
        Collection<SuiteUser> actual =
            source.searchUsers(SuiteUserSearchOptions.forAllNames("u"));
        assertThat(actual.size(), is(1));
    }

    @Test
    public void searchDoesNotMatchWhenOnlyOneCriterionMatches() throws Exception {
        Collection<SuiteUser> actual =
            source.searchUsers(SuiteUserSearchOptions.forAllNames("super"));
        assertThat(actual.size(), is(0));
    }

    @Test
    public void searchForAllReturnsSoleUser() throws Exception {
        Collection<SuiteUser> actual =
            source.searchUsers(SuiteUserSearchOptions.allUsers());
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getUsername(), is("superuser"));
    }
}
