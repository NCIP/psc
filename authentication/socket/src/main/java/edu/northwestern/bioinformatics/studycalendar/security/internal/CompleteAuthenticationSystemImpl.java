/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.internal;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarUserException;
import edu.northwestern.bioinformatics.studycalendar.security.AuthenticationSystemConfiguration;
import edu.northwestern.bioinformatics.studycalendar.security.CompleteAuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.tools.MultipleFilterFilter;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.springframework.beans.factory.annotation.Required;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;

/**
 * @author Rhett Sutphin
 */
public class CompleteAuthenticationSystemImpl extends MultipleFilterFilter implements CompleteAuthenticationSystem, ManagedService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private AuthenticationSystemConfiguration authenticationSystemConfiguration;

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public void updated(Dictionary dictionary) throws ConfigurationException {
        log.debug("Updating authentication system with {}", dictionary);
        if (dictionary != null) {
            authenticationSystemConfiguration.updated(dictionary);
            try {
                log.info("Initializing authentication system plugin from bundle {}",
                    dictionary.get(AuthenticationSystemConfiguration.AUTHENTICATION_SYSTEM.getKey()));
                authenticationSystemConfiguration.getAuthenticationSystem();
            } catch (StudyCalendarUserException scue) {
                throw new ConfigurationException("Unknown",
                    "Problem initializing authentication system: " + scue.getMessage(), scue);
            }
        }
    }

    public AuthenticationSystem getCurrentAuthenticationSystem() {
        return authenticationSystemConfiguration.getAuthenticationSystem();
    }

    public SecurityContext getCurrentSecurityContext() {
        return SecurityContextHolder.getContext();
    }

    ////// CONFIGURATION

    @Required
    public void setConfiguration(AuthenticationSystemConfiguration authenticationSystemConfiguration) {
        this.authenticationSystemConfiguration = authenticationSystemConfiguration;
    }
}
