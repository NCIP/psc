package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;

/**
 * @author Rhett Sutphin
 */
public class StudyCalendarTestWebApplicationContextBuilder {
    public static XmlWebApplicationContext createWebApplicationContext(
        ServletContext servletContext
    ) {
        return createPscWebApplicationContext(servletContext, new String[] {
            "classpath:applicationContext-command.xml",
            "classpath:applicationContext-webflow.xml",
            "classpath:applicationContext-web-osgi.xml",
            "classpath:applicationContext-web-testing-osgi.xml",
        });
    }

    public static WebApplicationContext createRealWebApplicationContext(ServletContext servletContext) {
        return createPscWebApplicationContext(servletContext, new String[] {
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
        ApplicationContext parent = StudyCalendarTestCase.getDeployedApplicationContext();
        XmlWebApplicationContext context = new XmlWebApplicationContext();
        context.setParent(parent);
        context.setServletContext(servletContext);
        context.setConfigLocations(configLocations);
        context.refresh();
        return context;
    }
}
