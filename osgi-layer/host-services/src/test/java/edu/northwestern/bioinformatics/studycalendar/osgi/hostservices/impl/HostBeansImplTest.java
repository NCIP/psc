package edu.northwestern.bioinformatics.studycalendar.osgi.hostservices.impl;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import edu.northwestern.bioinformatics.studycalendar.tools.spring.ConcreteStaticApplicationContext;
import gov.nih.nci.cabig.ctms.testing.MockRegistry;
import junit.framework.TestCase;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import static org.easymock.EasyMock.expect;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.springframework.context.ApplicationContext;
import org.springframework.osgi.mock.MockBundleContext;

import javax.sql.DataSource;
import java.util.Dictionary;
import java.util.HashMap;
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
    private ApplicationContext applicationContext;
    private DataSource dataSource;
    private UserDetailsService pscUserDetailsService;

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
        pscUserDetailsService = mockRegistry.registerMockFor(UserDetailsService.class);

        applicationContext = ConcreteStaticApplicationContext.create(
            new MapBuilder<String, Object>().
                put("dataSource", dataSource).
                put("pscUserDetailsService", pscUserDetailsService).
                toMap()
        );
    }

    public void testProxyServiceRegisteredForDataSource() throws Exception {
        assertTrue("Missing service", registeredServices.containsKey("javax.sql.DataSource"));
        Object service = registeredServices.get("javax.sql.DataSource");
        assertNotNull(service);
        assertTrue(service instanceof DataSource);
    }

    public void testProxyServiceRegisteredForUserDetailsService() throws Exception {
        assertTrue("Missing service", registeredServices.containsKey("org.acegisecurity.userdetails.UserDetailsService"));
        Object service = registeredServices.get("org.acegisecurity.userdetails.UserDetailsService");
        assertNotNull(service);
        assertTrue(service instanceof UserDetailsService);
    }

    public void testDeferredProxyServicesThrowAnExceptionIfInvokedEarly() throws Exception {
        Object service = registeredServices.get("javax.sql.DataSource");
        DataSource proxied = (DataSource) service;
        try {
            proxied.getConnection();
            fail("Exception not thrown");
        } catch (StudyCalendarSystemException expected) {
            assertEquals(
                "Cannot invoke method on host bean dataSource because it is not available yet",
                expected.getMessage());
        }
    }

    public void testDataSourceDelegatesToDataSourceBean() throws Exception {
        DataSource actual = (DataSource) registeredServices.get("javax.sql.DataSource");
        impl.setHostApplicationContext(applicationContext);

        expect(dataSource.getLoginTimeout()).andReturn(923);
        mockRegistry.replayMocks();
        assertEquals(923, actual.getLoginTimeout());
        mockRegistry.verifyMocks();
    }

    public void testUserDetailsServiceDelegatesToPscUserDetailsServiceBean() throws Exception {
        UserDetailsService actual = (UserDetailsService) registeredServices.get(UserDetailsService.class.getName());
        impl.setHostApplicationContext(applicationContext);

        UserDetails expectedUser = Fixtures.createUser("Joe");
        expect(pscUserDetailsService.loadUserByUsername("joe")).andReturn(expectedUser);
        mockRegistry.replayMocks();
        assertSame(expectedUser, actual.loadUserByUsername("joe"));
        mockRegistry.verifyMocks();
    }
}
