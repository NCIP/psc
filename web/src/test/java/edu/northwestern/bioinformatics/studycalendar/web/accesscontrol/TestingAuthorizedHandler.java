/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;

import java.util.Collection;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class TestingAuthorizedHandler implements PscAuthorizedHandler {
    private Collection<ResourceAuthorization> authorizations;

    public static TestingAuthorizedHandler all() {
        return new TestingAuthorizedHandler(null);
    }

    public TestingAuthorizedHandler(PscRole... roles) {
        if (roles == null) {
            this.authorizations = ALL_AUTHORIZED;
        } else {
            this.authorizations = ResourceAuthorization.createCollection(roles);
        }
    }

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return authorizations;
    }
}
