package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import org.restlet.data.Method;

import java.util.Collection;

/**
 * @see AbstractPscResource
 * @author Rhett Sutphin
 */
public interface AuthorizedResource {
    /**
     * Returns an array containing the roles which are allowed to perform
     * the specified method on this resource.  If it returns an empty collection,
     * no roles are permitted to perform the method.. If it returns null, any
     * role is permitted.
     * <p>
     * Implementors are not required to ensure that it returns the correct value
     * for every possible method -- just those that the resource actually
     * implements.
     */
    @Deprecated
    Collection<Role> legacyAuthorizedRoles(Method method);
}
