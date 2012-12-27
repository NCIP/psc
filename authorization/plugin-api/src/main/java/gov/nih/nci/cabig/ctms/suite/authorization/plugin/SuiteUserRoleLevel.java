/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package gov.nih.nci.cabig.ctms.suite.authorization.plugin;

/**
 * Defines the desired level of user role detail when invoking a plugin
 * methodd. Plugins can use this information to do performance optimization
 * if worthwhile.
 *
 * @since 2.10
 * @author Rhett Sutphin
 */
@SuppressWarnings( { "UnusedDeclaration" })
public enum SuiteUserRoleLevel {
    /** No role data will be considered for the returned user. */
    NONE,
    /**
     * Only the existence of particular roles will be considered.
     * All roles for the user (including those with incomplete scope) should be returned.
     */
    ROLES,
    /**
     * All role and scope data will be considered for the returned user.
     * All roles for the user (including those with incomplete scope) should be returned.
     */
    ROLES_AND_SCOPES
}
