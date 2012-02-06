package gov.nih.nci.cabig.ctms.suite.authorization.plugin;

import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;

import java.util.Collection;

/**
 * The main interface for PSC's pluggable authorization system. You may provide an implementation
 * of this class in order to retrieve authorization information from another system. Any
 * implementation provided replaces PSC's internal authorization database.
 * <p>
 * PSC's authorization scheme (and thus this API) is based on the common authorization scheme for
 * the CTMS Suite. In addition to the classes in this module, it depends on the
 * {@link gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole} and
 * {@link gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership} classes from
 * ctms-commons-suite-authorization.
 * <p>
 *
 * @since 2.10
 * @author Rhett Sutphin
 */
@SuppressWarnings( { "UnusedDeclaration" })
public interface SuiteAuthorizationSource {
    /**
     * Returns the single user with exactly the given username, or null if there is no such user.
     * <p>
     * The returned user must have all the mandatory fields in {@link SuiteUser} filled in.
     */
    SuiteUser getUser(String username, SuiteUserRoleLevel desiredDetail);

    /**
     * Returns the single user with the given numeric ID, or null if there is no such user.
     * <p>
     * The returned user must have all the mandatory fields in {@link SuiteUser} filled in.
     */
    SuiteUser getUser(long id, SuiteUserRoleLevel desiredDetail);

    /**
     * Returns those users which have the given role, regardless of completeness of scope.
     * No role memberships should be included.
     */
    Collection<SuiteUser> getUsersByRole(SuiteRole role);

    /**
     * Returns those users whose user details match <i>any</i> of the given criteria.
     * If the criteria are all null, return all users.
     */
    Collection<SuiteUser> searchUsers(SuiteUserSearchOptions criteria);
}
