/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import gov.nih.nci.security.authentication.LockoutManager;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;

/**
 * CSM's static configuration needs to happen before the application context loads any of its
 * classes and CSM sets them to immutable defaults.
 *
 * @author Rhett Sutphin
 */
public class CsmStaticConfigurator implements ServletContextListener {

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        // The CSM lockout system is buggy, so disable it
        LockoutManager.initialize("0", "0", "0");
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) { }
}
