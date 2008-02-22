package edu.northwestern.bioinformatics.studycalendar;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.restlet.resource.Resource;
import gov.nih.nci.cabig.ctms.tools.BuildInfo;

import java.util.Date;

/**
 * These tests are intended to verify that the various application contexts will all load when
 * the application is deployed.
 *
 * @author Rhett Sutphin
 */
public class ApplicationContextInitializationTest extends StudyCalendarTestCase {

    private MockServletContext servletContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        servletContext = new MockServletContext("src/main/webapp", new FileSystemResourceLoader());
    }

    public void testApplicationContextItself() throws Exception {
        getDeployedApplicationContext();
        // no exceptions
    }

    public void testBuildInfoTimestampIsParsed() throws Exception {
        BuildInfo buildInfo = (BuildInfo) getDeployedApplicationContext().getBean("buildInfo");
        assertNotNull(buildInfo.getTimestamp());
        // note that this assertion will fail if you build and then run the tests much later
        assertDatesClose("Build timestamp is not recent (this test will fail if you run it a long time after you build)",
            new Date(), buildInfo.getTimestamp(), 3 * 60 * 60 * 1000 /* 3 hours */);
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
        ApplicationContext ctxt = createWebApplicationContextForServlet("restful-api");

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
            createWebApplicationContextForServlet(servletName);
        } catch (Exception e) {
            throw new StudyCalendarError("Loading the configuration for MVC servlet '" + servletName
                + "' failed:  " + e.getMessage(), e);
        }
    }

    private XmlWebApplicationContext createWebApplicationContextForServlet(String servletName) {
        ApplicationContext parent = getDeployedApplicationContext();
        XmlWebApplicationContext context = new XmlWebApplicationContext();
        context.setParent(parent);
        context.setServletContext(servletContext);
        context.setConfigLocations(new String[] { "WEB-INF/" + servletName + "-servlet.xml" });
        context.refresh();
        return context;
    }
}
