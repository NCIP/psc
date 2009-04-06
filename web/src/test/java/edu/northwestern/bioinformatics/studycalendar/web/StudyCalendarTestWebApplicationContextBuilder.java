package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

import javax.servlet.ServletContext;

/**
 * @author Rhett Sutphin
 */
public class StudyCalendarTestWebApplicationContextBuilder {
    public static XmlWebApplicationContext createWebApplicationContextForServlet(
        String servletName, ServletContext servletContext
    ) {
        ApplicationContext parent = StudyCalendarTestCase.getDeployedApplicationContext();
        XmlWebApplicationContext context = new XmlWebApplicationContext();
        context.setParent(parent);
        context.setServletContext(servletContext);
        context.setConfigLocations(new String[] {
            "classpath:applicationContext-command.xml",
            "classpath:applicationContext-webflow.xml",
            "classpath:applicationContext-osgi.xml",
            "classpath:applicationContext-testing-osgi.xml",
            "WEB-INF/" + servletName + "-servlet.xml"
        });
        context.refresh();
        return context;
    }
}
