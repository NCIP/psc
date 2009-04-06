package edu.northwestern.bioinformatics.studycalendar.osgi.hostservices;

import org.acegisecurity.userdetails.UserDetailsService;

import javax.sql.DataSource;

/**
 * @author Rhett Sutphin
 */
public interface HostBeans {
    void setDataSource(DataSource dataSource);
    void setUserDetailsService(UserDetailsService userDetailsService);
}
