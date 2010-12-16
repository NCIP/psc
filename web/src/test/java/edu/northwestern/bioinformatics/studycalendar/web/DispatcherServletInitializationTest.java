package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import junit.framework.TestCase;
import org.restlet.resource.Resource;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.servlet.mvc.Controller;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import static edu.northwestern.bioinformatics.studycalendar.web.StudyCalendarTestWebApplicationContextBuilder.createWebApplicationContextForServlet;

/**
 * @author Rhett Sutphin
 */
public class DispatcherServletInitializationTest extends TestCase {
    private MockServletContext servletContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        servletContext = new MockServletContext(
            WebTestCase.findWebappSrcDirectory(), new FileSystemResourceLoader());
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

    public void testAllUiControllersHaveAuthorizationInformation() throws Exception {
        ApplicationContext ctxt = createWebApplicationContextForServlet("spring", servletContext);

        String[] beanNames = ctxt.getBeanNamesForType(Controller.class);
        Set<String> noAuthorizationInfo = new LinkedHashSet<String>();

        for (String beanName : beanNames) {
            Object controller = ctxt.getBean(beanName);
            if (!(controller instanceof PscAuthorizedHandler)) {
                noAuthorizationInfo.add(controller.getClass().getName());
            }
        }

        if (!noAuthorizationInfo.isEmpty()) {
            StringBuilder msg = new StringBuilder().append("The following ").
                append(noAuthorizationInfo.size()).
                append(" controllers contain no unified authorization information and therefore will never execute: ");
            for (String s : noAuthorizationInfo) {
                msg.append("\n  ").append(s);
            }

            fail(msg.toString());
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
