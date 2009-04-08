package edu.northwestern.bioinformatics.studycalendar.core.osgi;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.Membrane;
import static org.easymock.EasyMock.expect;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.mock.MockServiceReference;

/**
 * @author Rhett Sutphin
 */
public class OsgiLayerToolsTest extends StudyCalendarTestCase {
    private OsgiLayerTools tools;

    private BundleContext bundleContext;
    private Membrane membrane;
    private static final String SERVICE_NAME = "org.someservices.OneService";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        bundleContext = registerMockFor(BundleContext.class);
        membrane = registerMockFor(Membrane.class);

        tools = new OsgiLayerTools();
        tools.setBundleContext(bundleContext);
        tools.setMembrane(membrane);
    }

    public void testGetRequiredServiceWhenAvailable() throws Exception {
        ServiceReference expectedRef = new MockServiceReference();
        Object expectedFarService = new Object(), expectedNearService = new Object();
        expect(bundleContext.getServiceReference(SERVICE_NAME)).andReturn(expectedRef);
        expect(bundleContext.getService(expectedRef)).andReturn(expectedFarService);
        expect(membrane.farToNear(expectedFarService)).andReturn(expectedNearService);
        replayMocks();

        assertSame(expectedNearService, tools.getRequiredService(SERVICE_NAME));
        verifyMocks();
    }
    
    public void testGetRequiredServiceWhenNotAvailable() throws Exception {
        expect(bundleContext.getServiceReference(SERVICE_NAME)).andReturn(null);
        replayMocks();

        try {
            tools.getRequiredService(SERVICE_NAME);
            fail("Exception not thrown");
        } catch (StudyCalendarSystemException scse) {
            assertEquals("Service org.someservices.OneService not available in the OSGi layer",
                scse.getMessage());
        }
    }
    
    public void testGetNonRequiredServiceWhenAvailable() throws Exception {
        ServiceReference expectedRef = new MockServiceReference();
        Object expectedFarService = new Object(), expectedNearService = new Object();
        expect(bundleContext.getServiceReference(SERVICE_NAME)).andReturn(expectedRef);
        expect(bundleContext.getService(expectedRef)).andReturn(expectedFarService);
        expect(membrane.farToNear(expectedFarService)).andReturn(expectedNearService);
        replayMocks();

        assertSame(expectedNearService, tools.getOptionalService(SERVICE_NAME));
        verifyMocks();
    }

    public void testGetNonRequiredServiceNotAvailable() throws Exception {
        expect(bundleContext.getServiceReference(SERVICE_NAME)).andReturn(null);
        replayMocks();

        assertNull(tools.getOptionalService(SERVICE_NAME));
        verifyMocks();
    }
}
