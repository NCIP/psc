package edu.northwestern.bioinformatics.studycalendar.security;

import org.osgi.service.cm.ManagedService;

import javax.servlet.Filter;

/**
 * @author Rhett Sutphin
 */
public interface CompleteAuthenticationSystem extends Filter, ManagedService {
    String SERVICE_PID = "edu.northwestern.bioinformatics.studycalendar.security.psc-authentication-socket";
}
