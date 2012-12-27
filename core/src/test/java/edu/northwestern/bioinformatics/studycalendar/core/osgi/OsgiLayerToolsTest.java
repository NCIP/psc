/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.core.osgi;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBasedDictionary;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.Membrane;
import static org.easymock.EasyMock.expect;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;
import org.springframework.osgi.mock.MockBundle;
import org.springframework.osgi.mock.MockServiceReference;

import java.util.List;
import java.util.Hashtable;

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

    public void testUpdateConfiguration() throws Exception {
        MapBasedDictionary<Object, Object> expectedDict = new MapBasedDictionary<Object, Object>();
        String expectedPid = "some-pid";

        ConfigurationAdmin cm = expectGetConfigurationAdmin();

        MockServiceReference sr1 = createServiceReferenceWithPid("some-other-pid");
        MockServiceReference sr2 = createServiceReferenceWithPid(expectedPid);
        expect(bundleContext.getServiceReferences(ManagedService.class.getName(), null)).
            andReturn(new ServiceReference[] { sr1, sr2 });

        Configuration conf = registerMockFor(Configuration.class);
        expect(cm.getConfiguration(expectedPid, sr2.getBundle().getLocation())).andReturn(conf);
        /* expect */ conf.update(expectedDict);

        replayMocks();
        tools.updateConfiguration(expectedDict, expectedPid);
        verifyMocks();
    }

    public void testUpdateConfigurationWithUnknownPid() throws Exception {
        MapBasedDictionary<Object, Object> expectedDict = new MapBasedDictionary<Object, Object>();

        MockServiceReference sr1 = createServiceReferenceWithPid("pid-1");
        MockServiceReference sr2 = createServiceReferenceWithPid("pid-2");
        expect(bundleContext.getServiceReferences(ManagedService.class.getName(), null)).
            andReturn(new ServiceReference[] { sr1, sr2 });

        replayMocks();
        tools.updateConfiguration(expectedDict, "unknown-pid");
        verifyMocks();
    }

    public void testUpdateConfigurationWithNoManagedServices() throws Exception {
        MapBasedDictionary<Object, Object> expectedDict = new MapBasedDictionary<Object, Object>();

        expect(bundleContext.getServiceReferences(ManagedService.class.getName(), null)).andReturn(null);

        replayMocks();
        tools.updateConfiguration(expectedDict, "unknown-pid");
        verifyMocks();
    }
    
    public void testUpdateConfigurationWithoutConfigurationAdmin() throws Exception {
        expect(bundleContext.getServiceReference(ConfigurationAdmin.class.getName())).andReturn(null);

        MockServiceReference sr1 = createServiceReferenceWithPid("some-pid");
        expect(bundleContext.getServiceReferences(ManagedService.class.getName(), null)).
            andReturn(new ServiceReference[] { sr1 });

        replayMocks();
        try {
            tools.updateConfiguration(new Hashtable<Object, Object>(), "some-pid");
            fail("exception not thrown");
        } catch (StudyCalendarSystemException scse) {
            assertEquals("OSGi CM service not available.  Unable to update some-pid.", scse.getMessage());
        }
    }

    public void testUpdateConfigurationForBundle() throws Exception {
        MapBasedDictionary<Object, Object> expectedDict = new MapBasedDictionary<Object, Object>();
        ServiceReference sr = createServiceReferenceWithPid("pid-1");

        ConfigurationAdmin cm = expectGetConfigurationAdmin();
        Configuration conf = registerMockFor(Configuration.class);
        expect(cm.getConfiguration("pid-1", sr.getBundle().getLocation())).andReturn(conf);
        /* expect */ conf.update(expectedDict);

        replayMocks();
        tools.updateConfiguration(expectedDict, sr.getBundle(), "pid-1");
        verifyMocks();
    }

    private MockServiceReference createServiceReferenceWithPid(String pid) {
        MockServiceReference sr = new MockServiceReference();
        sr.setProperties(new MapBuilder<String, String>().put(Constants.SERVICE_PID, pid).toDictionary());
        ((MockBundle) sr.getBundle()).setLocation("Bundle for " + pid);
        return sr;
    }

    private ConfigurationAdmin expectGetConfigurationAdmin() {
        MockServiceReference cmRef = expectConfigurationAdminAvailable();
        ConfigurationAdmin cm = registerMockFor(ConfigurationAdmin.class);
        expect(bundleContext.getService(cmRef)).andReturn(cm);
        expect(membrane.farToNear(cm)).andReturn(cm);
        return cm;
    }

    private MockServiceReference expectConfigurationAdminAvailable() {
        MockServiceReference cmRef = new MockServiceReference();
        expect(bundleContext.getServiceReference(ConfigurationAdmin.class.getName())).andReturn(cmRef);
        return cmRef;
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
