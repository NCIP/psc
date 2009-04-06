package edu.northwestern.bioinformatics.studycalendar.osgi.hostservices.impl;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.osgi.hostservices.impl.HostBeansImpl;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import gov.nih.nci.cabig.ctms.testing.MockRegistry;
import junit.framework.TestCase;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UserDetails;
import static org.easymock.EasyMock.expect;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;
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
    private GenericApplicationContext applicationContext;
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

        StaticListableBeanFactory beans = new StaticListableBeanFactory();
        beans.addBean("dataSource", dataSource);
        beans.addBean("pscUserDetailsService", pscUserDetailsService);
        applicationContext = new GenericApplicationContext(new DefaultListableBeanFactory(beans));
        applicationContext.refresh();
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
