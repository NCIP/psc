package edu.northwestern.bioinformatics.studycalendar.osgi.hostservices;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUserDetailsService;
import gov.nih.nci.security.AuthorizationManager;
import org.apache.felix.cm.PersistenceManager;

import javax.sql.DataSource;

/**
 * @author Rhett Sutphin
 */
public interface HostBeans {
    void setPscUserDetailsService(PscUserDetailsService userDetailsService);
    void setPersistenceManager(PersistenceManager persistenceManager);

    void setAuthorizationManager(AuthorizationManager authorizationManager);
}
