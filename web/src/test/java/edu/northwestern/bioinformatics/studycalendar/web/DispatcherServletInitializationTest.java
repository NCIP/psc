package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import static edu.northwestern.bioinformatics.studycalendar.web.StudyCalendarTestWebApplicationContextBuilder.createWebApplicationContextForServlet;
import junit.framework.TestCase;
import org.restlet.resource.Resource;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockServletContext;

import java.io.File;

/**
 * @author Rhett Sutphin
 */
public class DispatcherServletInitializationTest extends TestCase {
    private MockServletContext servletContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        servletContext = new MockServletContext(findWebappDirectory(), new FileSystemResourceLoader());
    }

    private String findWebappDirectory() {
        File dir = new File("src/main/webapp");
        if (dir.exists()) {
            return dir.getPath();
        }
        dir = new File("web", dir.toString());
        if (dir.exists()) {
            return dir.getPath();
        }
        throw new IllegalStateException("Could not find webapp path");
    }

    public void testSpringServletContext() throws Exception {
        assertDispatcherServletConfigLoads("spring");
    }

    public void testPublicServletContext() throws Exception {
        assertDispatcherServletConfigLoads("public");
    }

    public void testSetupServletContext() throws Exception {
        assertDispatcherServletConfigLoads("setup");
    }

    public void testRestfulApiServletContext() throws Exception {
        assertDispatcherServletConfigLoads("restful-api");
    }

    public void testAllResourcesArePrototypeScope() throws Exception {
        ApplicationContext ctxt = createWebApplicationContextForServlet("restful-api", servletContext);

        String[] beanNames = ctxt.getBeanNamesForType(Resource.class);
        assertTrue("Should have found at least one resource", beanNames.length > 0);

        for (String beanName : beanNames) {
            Object firstTime = ctxt.getBean(beanName);
            Object secondTime = ctxt.getBean(beanName);
            assertNotSame(beanName + " is not a prototype", firstTime, secondTime);
        }
    }

    private void assertDispatcherServletConfigLoads(String servletName) throws Exception {
        try {
            createWebApplicationContextForServlet(servletName, servletContext);
        } catch (Exception e) {
            throw new StudyCalendarError("Loading the configuration for MVC servlet '" + servletName
                + "' failed:  " + e.getMessage(), e);
        }
    }
}
