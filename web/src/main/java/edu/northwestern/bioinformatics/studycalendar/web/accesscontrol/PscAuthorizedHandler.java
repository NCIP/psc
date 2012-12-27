/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.accesscontrol;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Interface which all authentication-required controllers in PSC must implement.  Provides
 * first-pass authorization simple role and scope checking, leaving more detailed authorization to
 * be done by each controller as necessary.
 *
 * @see edu.northwestern.bioinformatics.studycalendar.restlets.AuthorizedResource
 * @author Rhett Sutphin
 */
public interface PscAuthorizedHandler {
    /** Literate return value for {@link #authorizations} indicating that any user is authorized for the handler/request. */
    Collection<ResourceAuthorization> ALL_AUTHORIZED = null;
    /** Literate return value for {@link #authorizations} indicating that no user is ever authorized for the handler/request. */
    Collection<ResourceAuthorization> NONE_AUTHORIZED = Collections.emptySet();

    /**
     * Determines and returns the applicable authorizations for a particular request.
     * If it returns an empty collection, no roles/scopes are permitted for the request. If it
     * returns null, any role/scope is permitted.
     * <p>
     * The returned collection will not be modified, so for the common case where the input
     * parameters do not change the authorizations, it is acceptable for them to be cached
     * statically in the handler.
     * <p>
     * Implementors are not required to ensure that it returns the correct value
     * for every possible request -- just those that the resource actually
     * implements.
     * <p>
     * Implementors must not throw exceptions for missing parameters, etc., out of this method.
     * Bad requests should pass through and be handled by the main controller flow.
     *
     * @see #ALL_AUTHORIZED
     * @see #NONE_AUTHORIZED
     */
    Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) throws Exception;
}
