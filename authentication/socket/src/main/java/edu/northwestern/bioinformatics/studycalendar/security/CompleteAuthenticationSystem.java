package edu.northwestern.bioinformatics.studycalendar.security;

import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import org.osgi.service.cm.ManagedService;

import javax.servlet.Filter;

/**
 * @author Rhett Sutphin
 */
public interface CompleteAuthenticationSystem extends Filter {
    String SERVICE_PID = "edu.northwestern.bioinformatics.studycalendar.security.psc-authentication-socket";

    AuthenticationSystem getCurrentAuthenticationSystem();
}
