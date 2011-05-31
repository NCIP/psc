package gov.nih.nci.cabig.ctms.suite.authorization.plugins.mock.internal;

import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteAuthorizationSource;
import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteUser;
import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteUserRoleLevel;
import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteUserSearchOptions;

import java.util.Collection;
import java.util.Collections;

/**
 * A trivial authorization plugin which can authorize a single user named "superuser"
 * that has access to everything.
 *
 * @author Rhett Sutphin
 */
public class StaticSuiteAuthorizationSource implements SuiteAuthorizationSource {
    private final SuiteUser soleUser;

    public StaticSuiteAuthorizationSource() {
        SuiteUser.Builder builder = new SuiteUser.Builder(true).
            id(1).
            username("superuser").
            name("Sue", "User").
            emailAddress("root@nihil.it");
        for (SuiteRole role : SuiteRole.values()) {
            SuiteRoleMembership membership = new SuiteRoleMembership(role, null, null);
            if (role.isSiteScoped()) membership.forAllSites();
            if (role.isStudyScoped()) membership.forAllStudies();
            builder.addRoleMembership(membership);
        }
        soleUser = builder.toUser();
    }

    public SuiteUser getUser(String username, SuiteUserRoleLevel desiredDetail) {
        return username.equals(soleUser.getUsername()) ? soleUser : null;
    }

    public SuiteUser getUser(long id, SuiteUserRoleLevel desiredDetail) {
        return id == soleUser.getId() ? soleUser : null;
    }

    public Collection<SuiteUser> getUsersByRole(SuiteRole role) {
        return Collections.singleton(soleUser);
    }

    public Collection<SuiteUser> searchUsers(SuiteUserSearchOptions criteria) {
        boolean usernameMatch = criteria.getUsernameSubstring() == null ||
            containsIgnoreCase(soleUser.getUsername(), criteria.getUsernameSubstring());
        boolean firstNameMatch = criteria.getFirstNameSubstring() == null ||
            containsIgnoreCase(soleUser.getFirstName(), criteria.getFirstNameSubstring());
        boolean lastNameMatch = criteria.getLastNameSubstring() == null ||
            containsIgnoreCase(soleUser.getLastName(), criteria.getLastNameSubstring());
        if (usernameMatch && firstNameMatch && lastNameMatch) {
            return Collections.singleton(soleUser);
        } else {
            return Collections.emptySet();
        }
    }

    private boolean containsIgnoreCase(String str, String candidate) {
        return str.toLowerCase().contains(candidate.toLowerCase());
    }
}
