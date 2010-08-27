package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.restlet.data.Method;

import java.util.Collection;

/**
 * @see AbstractPscResource
 * @author Rhett Sutphin
 */
public interface AuthorizedResource {
    /**
     * Returns a collection of {@link ResourceAuthorization}s for
     * the specified method on this resource.  If it returns an empty collection,
     * no roles/scopes are permitted to perform the method. If it returns null, any
     * role/scope is permitted.
     * <p>
     * Implementors are not required to ensure that it returns the correct value
     * for every possible method -- just those that the resource actually
     * implements.
     */
    Collection<ResourceAuthorization> authorizations(Method method);
}
