package edu.northwestern.bioinformatics.studycalendar.core.osgi;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.Membrane;
import static org.easymock.EasyMock.expect;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.mock.MockServiceReference;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class OsgiLayerToolsTest extends StudyCalendarTestCase {
    private static final Class<?> SERVICE_TYPE = StringPermuter.class;
    private static final String SERVICE_NAME = SERVICE_TYPE.getName();

    private OsgiLayerTools tools;

    private BundleContext bundleContext;
    private Membrane membrane;

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
        StringPermuter expectedFarService = new Yeller(),
            expectedNearService = new Yeller();
        expectServiceAvailable(expectedFarService, expectedNearService);
        replayMocks();

        assertSame(expectedNearService, tools.getRequiredService(SERVICE_TYPE));
        verifyMocks();
    }
    
    public void testGetRequiredServiceWhenNotAvailable() throws Exception {
        expect(bundleContext.getServiceReference(SERVICE_NAME)).andReturn(null);
        replayMocks();

        try {
            tools.getRequiredService(SERVICE_TYPE);
            fail("Exception not thrown");
        } catch (StudyCalendarSystemException scse) {
            assertEquals("Service " + SERVICE_NAME + " not available in the OSGi layer",
                scse.getMessage());
        }
    }
    
    public void testGetNonRequiredServiceWhenAvailable() throws Exception {
        StringPermuter expectedFarService = new Yeller(),
            expectedNearService = new Yeller();
        expectServiceAvailable(expectedFarService, expectedNearService);
        replayMocks();

        assertSame(expectedNearService, tools.getOptionalService(SERVICE_TYPE));
        verifyMocks();
    }

    public void testGetNonRequiredServiceNotAvailable() throws Exception {
        expect(bundleContext.getServiceReference(SERVICE_NAME)).andReturn(null);
        replayMocks();

        assertNull(tools.getOptionalService(SERVICE_TYPE));
        verifyMocks();
    }

    public void testGetServicesWithMultipleMatches() throws Exception {
        StringPermuter[] expectedFar =  { new Yeller(), new Cummingsizer() };
        StringPermuter[] expectedNear =  { new Yeller(), new Cummingsizer() };
        ServiceReference[] expectedRefs = { new MockServiceReference(), new MockServiceReference() };
        expect(bundleContext.getServiceReferences(SERVICE_NAME, null)).andReturn(expectedRefs);
        for (int i = 0 ; i < 2 ; i++) {
            expect(bundleContext.getService(expectedRefs[i])).andReturn(expectedFar[i]);
            expect(membrane.farToNear(expectedFar[i])).andReturn(expectedNear[i]);
        }
        replayMocks();

        List<StringPermuter> actual = tools.getServices(StringPermuter.class);
        assertSame("Wrong service 0", expectedNear[0], actual.get(0));
        assertSame("Wrong service 1", expectedNear[1], actual.get(1));
        assertEquals("Wrong number of services found", 2, actual.size());
        verifyMocks();
    }

    public void testGetServicesWithNoMatches() throws Exception {
        expect(bundleContext.getServiceReferences(SERVICE_NAME, null)).andReturn(null);
        replayMocks();

        List<StringPermuter> actual = tools.getServices(StringPermuter.class);
        assertTrue("Should be nothing returned", actual.isEmpty());
        verifyMocks();
    }

    public void testGetServicesWhenOneBecomesInvalid() throws Exception {
        StringPermuter[] expectedFar =  { new Yeller(), new Cummingsizer() };
        StringPermuter[] expectedNear =  { new Yeller() };
        ServiceReference[] expectedRefs = { new MockServiceReference(), new MockServiceReference() };
        expect(bundleContext.getServiceReferences(SERVICE_NAME, null)).andReturn(expectedRefs);
        expect(bundleContext.getService(expectedRefs[0])).andReturn(expectedFar[0]);
        expect(membrane.farToNear(expectedFar[0])).andReturn(expectedNear[0]);
        expect(bundleContext.getService(expectedRefs[1])).andReturn(null);
        replayMocks();

        List<StringPermuter> actual = tools.getServices(StringPermuter.class);
        assertSame("Wrong service 0", expectedNear[0], actual.get(0));
        assertEquals("Should be only one service", 1, actual.size());
        verifyMocks();
    }
    
    private void expectServiceAvailable(StringPermuter expectedFarService, StringPermuter expectedNearService) {
        ServiceReference expectedRef = new MockServiceReference();
        expect(bundleContext.getServiceReference(SERVICE_NAME)).andReturn(expectedRef);
        expect(bundleContext.getService(expectedRef)).andReturn(expectedFarService);
        expect(membrane.farToNear(expectedFarService)).andReturn(expectedNearService);
    }

    private interface StringPermuter {
        String permute(String in);
    }

    public static class Yeller implements StringPermuter {
        public String permute(String in) {
            return in.toUpperCase();
        }
    }

    public static class Cummingsizer implements StringPermuter {
        public String permute(String in) {
            return in.toLowerCase();
        }
    }
}
