/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.tools;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.osgi.OsgiLayerStartupListener;
import org.osgi.framework.BundleContext;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockServletContext;
import org.springframework.osgi.mock.MockBundleContext;
import org.springframework.web.context.support.StaticWebApplicationContext;

/**
 * @author Rhett Sutphin
 */
public class BundleContextLocatorTest extends StudyCalendarTestCase {
    private BundleContextLocator locator;
    private BundleContext bundleContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        bundleContext = new MockBundleContext();
        locator = new BundleContextLocator();
    }

    public void testShouldBeSingleton() throws Exception {
        assertTrue(locator.isSingleton());
    }

    public void testClassIsBundleContext() throws Exception {
        assertEquals(BundleContext.class, locator.getObjectType());
    }

    public void testObjectIsRetrievedFromServletContext() throws Exception {
        MockServletContext servletContext = new MockServletContext();
        servletContext.setAttribute(OsgiLayerStartupListener.BUNDLE_CONTEXT_ATTRIBUTE, bundleContext);

        StaticWebApplicationContext applicationContext = new StaticWebApplicationContext();
        applicationContext.setServletContext(servletContext);
        locator.setApplicationContext(applicationContext);

        assertSame("BundleContext not found", bundleContext, locator.getObject());
    }
    
    public void testExceptionIfApplicationContextNotWeb() throws Exception {
        locator.setApplicationContext(new StaticApplicationContext());

        try {
            locator.getObject();
            fail("Exception not thrown");
        } catch (StudyCalendarSystemException actual) {
            assertEquals("Cannot extract bundle context from normal application context.  (It's in the ServletContext.)", actual.getMessage());
        }
    }
}
