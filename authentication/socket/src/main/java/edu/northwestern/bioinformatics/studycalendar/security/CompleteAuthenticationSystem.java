/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security;

import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import org.acegisecurity.context.SecurityContext;

import javax.servlet.Filter;

/**
 * @author Rhett Sutphin
 */
public interface CompleteAuthenticationSystem extends Filter {
    String SERVICE_PID = "edu.northwestern.bioinformatics.studycalendar.security.psc-authentication-socket";

    AuthenticationSystem getCurrentAuthenticationSystem();

    /**
     * There are separate instances of {@link SecurityContextHolder} (separate instances of the
     * class) for the host system and for the OSGi bundle.  This allows access to the OSGi bundle
     * version so that its contents can be bridged into the host system. 
     * @return
     */
    SecurityContext getCurrentSecurityContext();
}
