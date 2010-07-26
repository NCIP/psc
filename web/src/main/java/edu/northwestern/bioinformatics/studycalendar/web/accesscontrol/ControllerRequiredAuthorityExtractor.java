package edu.northwestern.bioinformatics.studycalendar.web.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.LegacyModeSwitch;
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

    private LegacyModeSwitch legacyModeSwitch;

    public GrantedAuthority[] getAllowedAuthoritiesForController(Controller controller) {
        Class<?> controllerClass = controller.getClass();
        if (legacyModeSwitch.isOn()) {
            return getLegacyRequiredAuthoritiesForController(controllerClass);
        } else {
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
            Set<PscRole> roles = new LinkedHashSet<PscRole>();
            for (ResourceAuthorization auth : auths) roles.add(auth.getRole());
            return roles.toArray(new PscRole[roles.size()]);
        }
    }

    private Role[] getLegacyRequiredAuthoritiesForController(Class<?> clazz) {
        AccessControl ac = clazz.getAnnotation(AccessControl.class);
        Role[] roles = Role.values();
        if (ac != null) {
             roles = ac.roles();
        }
        return roles;
    }

    ////// CONFIGURATION

    public void setLegacyModeSwitch(LegacyModeSwitch lmSwitch) {
        this.legacyModeSwitch = lmSwitch;
    }
}
