/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import org.acegisecurity.GrantedAuthority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.Controller;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Rhett Sutphin
 */
// TODO: once ControllerSecureUrlCreator is removed, this will only be used by SecureSectionInterceptor
// Consider pushing its behavior down to that class and removing this one.
public class ControllerRequiredAuthorityExtractor {
    private final Logger log = LoggerFactory.getLogger(getClass());

    public GrantedAuthority[] getAllowedAuthoritiesForController(Controller controller) {
        if (!(controller instanceof PscAuthorizedHandler)) {
            return new GrantedAuthority[0];
        }
        Collection<ResourceAuthorization> auths = null;
        try {
            auths = ((PscAuthorizedHandler) controller).
                authorizations("GET", Collections.<String, String[]>emptyMap());
        } catch (Exception e) {
            log.error("Extracting authorizations from " + controller + " failed.  Will lock down.", e);
            auths = PscAuthorizedHandler.NONE_AUTHORIZED;
        }
        if (auths == null) {
            return PscRole.values();
        } else {
            Set<PscRole> roles = new LinkedHashSet<PscRole>();
            for (ResourceAuthorization auth : auths) {
                roles.add(auth.getRole());
            }
            return roles.toArray(new PscRole[roles.size()]);
        }
    }
}
