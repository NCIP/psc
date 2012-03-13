package edu.northwestern.bioinformatics.studycalendar.osgi.hostservices;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUserDetailsService;

/**
 * @author Rhett Sutphin
 */
public interface HostBeans {
    void setPscUserDetailsService(PscUserDetailsService userDetailsService);
}
