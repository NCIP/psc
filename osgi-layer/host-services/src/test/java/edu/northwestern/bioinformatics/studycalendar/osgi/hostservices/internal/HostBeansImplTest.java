package edu.northwestern.bioinformatics.studycalendar.osgi.hostservices.internal;

import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.security.acegi.PscUserDetailsService;
import gov.nih.nci.cabig.ctms.testing.MockRegistry;
import junit.framework.TestCase;
import org.acegisecurity.userdetails.UserDetailsService;
import org.apache.felix.cm.PersistenceManager;
import static org.easymock.EasyMock.expect;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.springframework.osgi.mock.MockBundleContext;

import javax.sql.DataSource;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "RawUseOfParameterizedType" })
public class HostBeansImplTest extends TestCase {
    private MockRegistry mockRegistry;
    private BundleContext bundleContext;
    private Map<String, Object> registeredServices;
    private HostBeansImpl impl;
    private DataSource dataSource;
    private PscUserDetailsService pscUserDetailsService;
    private PersistenceManager persistenceManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockRegistry = new MockRegistry();
        registeredServices = new HashMap<String, Object>();

        bundleContext = new MockBundleContext() {
            @Override
            public ServiceRegistration registerService(String s, Object o, Dictionary dictionary) {
                registeredServices.put(s, o);
                return super.registerService(s, o, dictionary);
            }
        };

        impl = new HostBeansImpl();
        impl.registerServices(bundleContext);
        dataSource = mockRegistry.registerMockFor(DataSource.class);
        pscUserDetailsService = mockRegistry.registerMockFor(PscUserDetailsService.class);
        persistenceManager = mockRegistry.registerMockFor(PersistenceManager.class);
    }

    public void testProxyServiceRegisteredForDataSource() throws Exception {
        assertTrue("Missing service", registeredServices.containsKey("javax.sql.DataSource"));
        Object service = registeredServices.get("javax.sql.DataSource");
        assertNotNull(service);
        assertTrue(service instanceof DataSource);
    }

    public void testProxyServiceRegisteredForUserDetailsService() throws Exception {
        String serviceName = PscUserDetailsService.class.getName();
        assertTrue("Missing service", registeredServices.containsKey(serviceName));
        Object service = registeredServices.get(serviceName);
        assertNotNull(service);
        assertTrue(service instanceof UserDetailsService);
    }

    public void testDeferredProxyServicesUseDefaultsIfCalledEarly() throws Exception {
        DataSource proxied = (DataSource) registeredServices.get("javax.sql.DataSource");
        assertNull(proxied.getConnection());
        assertEquals(0, proxied.getLoginTimeout());
        assertFalse(proxied.equals("anything"));
    }

    public void testDataSourceDelegatesToDataSourceBean() throws Exception {
        DataSource actual = (DataSource) registeredServices.get("javax.sql.DataSource");
        impl.setDataSource(dataSource);

        expect(dataSource.getLoginTimeout()).andReturn(923);
        mockRegistry.replayMocks();
        assertEquals(923, actual.getLoginTimeout());
        mockRegistry.verifyMocks();
    }

    public void testUserDetailsServiceDelegatesToPscUserDetailsServiceBean() throws Exception {
        PscUserDetailsService actual = (PscUserDetailsService) registeredServices.get(PscUserDetailsService.class.getName());
        impl.setPscUserDetailsService(pscUserDetailsService);

        User expectedUser = Fixtures.createUser("Joe");
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
}
