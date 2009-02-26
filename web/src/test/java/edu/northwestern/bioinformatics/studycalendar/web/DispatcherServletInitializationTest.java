package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarApplicationContextBuilder;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.assertPositive;
import junit.framework.TestCase;
import org.restlet.resource.Resource;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * @author Rhett Sutphin
 */
public class DispatcherServletInitializationTest extends TestCase {
    private MockServletContext servletContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        servletContext = new MockServletContext("src/main/webapp", new FileSystemResourceLoader());
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
        ApplicationContext parent = StudyCalendarTestCase.getDeployedApplicationContext();
        XmlWebApplicationContext context = new XmlWebApplicationContext();
        context.setParent(parent);
        context.setServletContext(servletContext);
        context.setConfigLocations(new String[] { 
            "classpath:applicationContext-command.xml",
            "classpath:applicationContext-webflow.xml",
            "WEB-INF/" + servletName + "-servlet.xml" 
        });
        context.refresh();
        return context;
    }
}
