/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarApplicationContextBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

import javax.servlet.ServletContext;

/**
 * @author Rhett Sutphin
 */
public class StudyCalendarTestWebApplicationContextBuilder {
    public static XmlWebApplicationContext createWebApplicationContext(
        ServletContext servletContext
    ) {
        return createPscWebApplicationContext(servletContext, new String[] {
            "classpath:applicationContext-web.xml",
            "classpath:applicationContext-command.xml",
            "classpath:applicationContext-webflow.xml",
            "classpath:applicationContext-web-osgi.xml",
            "classpath:applicationContext-web-testing-osgi.xml",
        });
    }

    public static WebApplicationContext createRealWebApplicationContext(ServletContext servletContext) {
        return createPscWebApplicationContext(servletContext, new String[] {
            "classpath:applicationContext-web.xml",
            "classpath:applicationContext-command.xml",
            "classpath:applicationContext-webflow.xml",
            "classpath:applicationContext-web-osgi.xml"
        });
    }

    public static XmlWebApplicationContext createWebApplicationContextForServlet(
        String servletName, ServletContext servletContext
    ) {
        ApplicationContext parent = createWebApplicationContext(servletContext);
        XmlWebApplicationContext context = new XmlWebApplicationContext();
        context.setParent(parent);
        context.setServletContext(servletContext);
        context.setConfigLocation("WEB-INF/" + servletName + "-servlet.xml");
        context.refresh();
        return context;
    }

    private static XmlWebApplicationContext createPscWebApplicationContext(ServletContext servletContext, String[] configLocations) {
        int coreCount = StudyCalendarApplicationContextBuilder.DEPLOYED_CONFIG_LOCATIONS.length;
        String[] loc = new String[coreCount + configLocations.length];
        System.arraycopy(
            StudyCalendarApplicationContextBuilder.DEPLOYED_CONFIG_LOCATIONS, 0,
            loc, 0, coreCount);
        System.arraycopy(
            configLocations, 0, loc, coreCount, configLocations.length);
        XmlWebApplicationContext context = new XmlWebApplicationContext();
        context.setServletContext(servletContext);
        context.setConfigLocations(loc);
        context.refresh();
        return context;
    }
}
