package edu.northwestern.bioinformatics.studycalendar.osgi.hostservices;

import edu.northwestern.bioinformatics.studycalendar.security.acegi.PscUserDetailsService;

import javax.sql.DataSource;

/**
 * @author Rhett Sutphin
 */
public interface HostBeans {
    void setDataSource(DataSource dataSource);
    void setPscUserDetailsService(PscUserDetailsService userDetailsService);
}
