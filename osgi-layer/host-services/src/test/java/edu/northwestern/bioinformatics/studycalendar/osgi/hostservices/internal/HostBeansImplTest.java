package edu.northwestern.bioinformatics.studycalendar.osgi.hostservices.internal;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUserDetailsService;
import gov.nih.nci.cabig.ctms.testing.MockRegistry;
import junit.framework.TestCase;
import org.apache.felix.cm.PersistenceManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.springframework.osgi.mock.MockBundleContext;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static org.easymock.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "RawUseOfParameterizedType" })
public class HostBeansImplTest extends TestCase {
    private MockRegistry mockRegistry;
    private Map<String, Object> registeredServices;
    private Map<String,Dictionary> registeredServiceProperties;

    private HostBeansImpl impl;
    private PscUserDetailsService pscUserDetailsService;
    private PersistenceManager persistenceManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockRegistry = new MockRegistry();
        registeredServices = new HashMap<String, Object>();
        registeredServiceProperties = new HashMap<String, Dictionary>();

        BundleContext bundleContext = new MockBundleContext() {
            @Override
            public ServiceRegistration registerService(String s, Object o, Dictionary dictionary) {
                registeredServices.put(s, o);
                registeredServiceProperties.put(s, dictionary);
                return super.registerService(s, o, dictionary);
            }
        };

        impl = new HostBeansImpl();
        impl.registerServices(bundleContext);
        pscUserDetailsService = mockRegistry.registerMockFor(PscUserDetailsService.class);
        persistenceManager = mockRegistry.registerMockFor(PersistenceManager.class);
    }

    public void testProxyServiceRegisteredForUserDetailsService() throws Exception {
        String serviceName = PscUserDetailsService.class.getName();
        assertTrue("Missing service", registeredServices.containsKey(serviceName));
        Object service = registeredServices.get(serviceName);
        assertNotNull(service);
        assertTrue(service instanceof PscUserDetailsService);
    }

    @SuppressWarnings({ "EqualsBetweenInconvertibleTypes" })
    public void testDeferredProxyServicesUseDefaultsIfCalledEarly() throws Exception {
        PersistenceManager proxied = (PersistenceManager) registeredServices.get(PersistenceManager.class.getName());
        assertNull(proxied.load("foo"));
        assertFalse(proxied.exists("bar"));
    }

    public void testUserDetailsServiceDelegatesToPscUserDetailsServiceBean() throws Exception {
        PscUserDetailsService actual = (PscUserDetailsService) registeredServices.get(PscUserDetailsService.class.getName());
        impl.setPscUserDetailsService(pscUserDetailsService);

        PscUser expectedUser = new PscUser(null, null);
        expect(pscUserDetailsService.loadUserByUsername("joe")).andReturn(expectedUser);
        mockRegistry.replayMocks();
        assertSame(expectedUser, actual.loadUserByUsername("joe"));
        mockRegistry.verifyMocks();
    }

    public void testPersistenceManagerDelegatesToPersistenceManagerBean() throws Exception {
        PersistenceManager actual = (PersistenceManager) registeredServices.get(PersistenceManager.class.getName());
        impl.setPersistenceManager(persistenceManager);

        Dictionary expected = new Hashtable();
        expect(persistenceManager.load("abc")).andReturn(expected);
        mockRegistry.replayMocks();
        assertSame(expected, actual.load("abc"));
        mockRegistry.verifyMocks();
    }

    public void testProxiesRespondToToString() throws Exception {
        impl.setPscUserDetailsService(pscUserDetailsService);
        mockRegistry.replayMocks();
        assertEquals("DeferredBean for EasyMock for interface edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUserDetailsService",
            getActualService(PscUserDetailsService.class).toString());
    }

    @SuppressWarnings( { "unchecked" })
    private <T> T getActualService(Class<T> clazz) {
        return (T) registeredServices.get(clazz.getName());
    }
}
