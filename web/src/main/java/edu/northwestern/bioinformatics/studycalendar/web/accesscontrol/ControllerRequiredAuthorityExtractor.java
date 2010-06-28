package edu.northwestern.bioinformatics.studycalendar.web.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.LegacyModeSwitch;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import org.acegisecurity.GrantedAuthority;
import org.springframework.web.servlet.mvc.Controller;

/**
 * @author Rhett Sutphin
 */
public class ControllerRequiredAuthorityExtractor {
    private LegacyModeSwitch legacyModeSwitch;

    public GrantedAuthority[] getRequiredAuthoritiesForController(Class<?> controller) {
        if (legacyModeSwitch.isOn()) {
            return getLegacyRequiredAuthoritiesForController(controller);
        } else {
            AuthorizedForAll all = controller.getAnnotation(AuthorizedForAll.class);
            if (all != null) return PscRole.values();
            AuthorizedFor af = controller.getAnnotation(AuthorizedFor.class);
            if (af != null) return af.value();
            return new PscRole[0];
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
