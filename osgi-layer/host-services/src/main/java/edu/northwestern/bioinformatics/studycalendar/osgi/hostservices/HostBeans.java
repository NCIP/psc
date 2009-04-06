package edu.northwestern.bioinformatics.studycalendar.osgi.hostservices;

import org.springframework.context.ApplicationContext;

/**
 * @author Rhett Sutphin
 */
public interface HostBeans {
    void setHostApplicationContext(ApplicationContext hostContext);
}
