/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.plugin;

import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Rhett Sutphin
*/
public class AuthenticationSystemValidator {
    public static void validateRequiredElementsCreated(
        AuthenticationSystem system
    ) throws AuthenticationSystemInitializationFailure {
        List<String> missing = new ArrayList<String>(3);
        if (system.authenticationManager() == null) {
            missing.add("authenticationManager()");
        }
        if (system.entryPoint() == null) {
            missing.add("entryPoint()");
        }
        if (!missing.isEmpty()) {
            String list = missing.get(missing.size() - 1);
            if (missing.size() >= 2) {
                list = String.format(
                    "%s or %s",
                    StringUtils.join(missing.subList(0, missing.size() - 1).iterator(), ", "),
                    list);
            }
            throw new AuthenticationSystemInitializationFailure(
                "%s must not return null from %s", system.getClass().getSimpleName(), list);
        }
    }

    // static class
    private AuthenticationSystemValidator() { }
}
