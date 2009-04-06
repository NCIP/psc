package edu.northwestern.bioinformatics.studycalendar.security;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystemLoadingFailure;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.local.LocalAuthenticationSystem;
import gov.nih.nci.cabig.ctms.tools.configuration.TransientConfiguration;
import org.acegisecurity.userdetails.UserDetailsService;
import static org.easymock.classextension.EasyMock.expect;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.mock.MockBundle;
import org.springframework.osgi.mock.MockServiceReference;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.sql.DataSource;
import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public class AuthenticationSystemConfigurationTest extends StudyCalendarTestCase {
    private AuthenticationSystemConfiguration configuration;
    private BundleContext bundleContext;
    private DataSource dataSource;
    private UserDetailsService userDetailsService;

    private final MockPlugin localPlugin
        = new MockPlugin(
            "edu.northwestern.bioinformatics.psc-authentication-local-plugin",
            new LocalAuthenticationSystem());
    private final MockPlugin testablePlugin
        = new MockPlugin(
            "edu.northwestern.bioinformatics.psc-authentication-testable-plugin",
            new TestableAuthenticationSystem());
    private final MockPlugin stubPlugin
        = new MockPlugin(
            "edu.northwestern.bioinformatics.psc-authentication-stub-plugin",
            new StubAuthenticationSystem());

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        dataSource = registerMockFor(DataSource.class);
        userDetailsService = registerMockFor(UserDetailsService.class);
        bundleContext = registerMockFor(BundleContext.class);

        configuration = new AuthenticationSystemConfiguration();
        configuration.setDelegate(new TransientConfiguration(AuthenticationSystemConfiguration.UNIVERSAL_PROPERTIES));
        configuration.setBundleContext(bundleContext);
        configuration.setUserDetailsService(userDetailsService);
        configuration.setDataSource(dataSource);
    }

    private void expectServiceReferences(MockPlugin... plugins) throws InvalidSyntaxException {
        ServiceReference[] refs = new ServiceReference[plugins.length];
        for (int i = 0; i < plugins.length; i++) refs[i] = plugins[i].getServiceReference();
        expect(bundleContext.getServiceReferences(AuthenticationSystem.class.getName(), null)).andStubReturn(refs);
    }

    private void expectOsgiSelectedServiceReferenceIs(MockPlugin plugin) {
        expect(bundleContext.getServiceReference(AuthenticationSystem.class.getName())).
            andStubReturn(plugin.getServiceReference());
    }

    private void expectSelectedService(MockPlugin plugin) {
        expect(bundleContext.getService(plugin.getServiceReference())).andReturn(plugin.getSystem());
    }

    private void expectUngetService(MockPlugin plugin) {
        expect(bundleContext.ungetService(plugin.getServiceReference())).andReturn(true);
    }

    public void testUnknownSelectedSystemSelectsDefault() throws Exception {
        expectServiceReferences(localPlugin, testablePlugin);
        expectOsgiSelectedServiceReferenceIs(localPlugin);
        expectSelectedService(localPlugin);
        replayMocks();

        selectAuthenticationSystem(stubPlugin.getSymbolicName());
        assertEquals("Wrong service selected", localPlugin.getSystem().getClass(),
            configuration.getAuthenticationSystem().getClass());
    }

    public void testFailureIfNoAuthenticationSystemsAvailable() throws Exception {
        expect(bundleContext.getServiceReference(AuthenticationSystem.class.getName())).andReturn(null);
        replayMocks();

        try {
            configuration.getAuthenticationSystem();
            fail("Exception not thrown");
        } catch (AuthenticationSystemLoadingFailure failure) {
            assertEquals(
                "No authentication system plugins available from the OSGi layer.  Plugins must be both installed and activated to be used.",
                failure.getMessage());
        }
    }

    public void testPropertiesReflectConfiguredAuthenticationSystem() throws Exception {
        expectOsgiSelectedServiceReferenceIs(localPlugin);
        expectSelectedService(localPlugin);
        replayMocks();

        assertNotNull("No properties for local", configuration.getProperties());
        assertEquals("Wrong number of properties for local", 1, configuration.getProperties().size());
        assertContains("Sole property for local should be auth system", configuration.getProperties().getAll(), AuthenticationSystemConfiguration.AUTHENTICATION_SYSTEM);

        resetMocks();
        expectServiceReferences(localPlugin, testablePlugin);
        expectSelectedService(testablePlugin);
        replayMocks();
        
        setMinimumTestSystemParameters();
        selectAuthenticationSystem(testablePlugin.getSymbolicName());
        assertNotNull("No properties for TAS", configuration.getProperties());
        assertContains("TAS props should contain auth system", configuration.getProperties().getAll(), AuthenticationSystemConfiguration.AUTHENTICATION_SYSTEM);
        assertContains("TAS props should contain TAS system props", configuration.getProperties().getAll(), TestableAuthenticationSystem.APPLICATION_URL);
        assertEquals("Wrong number of properties for TAS", 3, configuration.getProperties().size());
        assertEquals("Property details should be available for per-system props",
            "Service URL", configuration.getProperties().getNameFor(TestableAuthenticationSystem.SERVICE_URL.getKey()));
    }

    public void testCreateDefaultAuthenticationSystem() throws Exception {
        expectOsgiSelectedServiceReferenceIs(localPlugin);
        expectSelectedService(localPlugin);
        replayMocks();

        configuration.reset(AuthenticationSystemConfiguration.AUTHENTICATION_SYSTEM);
        AuthenticationSystem system = configuration.getAuthenticationSystem();
        assertNotNull("No system created", system);
        assertTrue("Wrong system created: " + system.getClass().getName(),
            localPlugin.getSystem().getClass().isAssignableFrom(system.getClass()));
    }

    public void testCreateNewAuthenticationSystemAfterSystemSwitch() throws Exception {
        expectOsgiSelectedServiceReferenceIs(localPlugin);
        expectSelectedService(localPlugin);
        replayMocks();

        AuthenticationSystem initialSystem = configuration.getAuthenticationSystem();
        assertNotNull("No system created initially", initialSystem);

        resetMocks();
        expectServiceReferences(localPlugin, stubPlugin, testablePlugin);
        expectUngetService(localPlugin);
        expectSelectedService(stubPlugin);
        replayMocks();
        
        setMinimumTestSystemParameters();
        selectAuthenticationSystem(stubPlugin.getSymbolicName());
        AuthenticationSystem newSystem = configuration.getAuthenticationSystem();
        assertNotSame("New system is not new", initialSystem, newSystem);
        assertTrue("Wrong new system created",
            StubAuthenticationSystem.class.isAssignableFrom(newSystem.getClass()));
        verifyMocks();
    }

    public void testCreatedAuthenticationSystemIsInitialized() throws Exception {
        expectServiceReferences(localPlugin, stubPlugin, testablePlugin);
        expectSelectedService(stubPlugin);
        replayMocks();

        selectAuthenticationSystem(stubPlugin.getSymbolicName());

        assertTrue("System is wrong class",
            configuration.getAuthenticationSystem() instanceof StubAuthenticationSystem);
        StubAuthenticationSystem system = (StubAuthenticationSystem) configuration.getAuthenticationSystem();
        assertSame("Wrong dataSource used during initialization", dataSource,
            system.getInitialDataSource());
        assertSame("Wrong userDetailsService used during initialization", userDetailsService,
            system.getInitialUserDetailsService());
        // can't compare the configuration objects themselves because one's a CGLIB proxy
        assertSame("Wrong configuration used during initialization", configuration.getProperties(),
            system.getInitialConfiguration().getProperties());
    }
    
    public void testInitializationExceptionIsPropagated() throws Exception {
        expectServiceReferences(localPlugin, stubPlugin, testablePlugin);
        expectSelectedService(stubPlugin);
        replayMocks();

        selectAuthenticationSystem(stubPlugin.getSymbolicName());
        configuration.set(StubAuthenticationSystem.EXPECTED_INITIALIZATION_ERROR_MESSAGE, "Bad news");

        try {
            configuration.getAuthenticationSystem();
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals("Bad news", scve.getMessage());
        }
    }
    
    private void setMinimumTestSystemParameters() {
        configuration.set(TestableAuthenticationSystem.SERVICE_URL, "not-null");
        configuration.set(TestableAuthenticationSystem.APPLICATION_URL, "not-null");
    }

    private void selectAuthenticationSystem(String requested) {
        configuration.set(AuthenticationSystemConfiguration.AUTHENTICATION_SYSTEM, requested);
    }

    private static class DummyFilter implements Filter {
        public void init(FilterConfig filterConfig) throws ServletException {
            throw new UnsupportedOperationException("init not implemented");
        }

        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
            throw new UnsupportedOperationException("doFilter not implemented");
        }

        public void destroy() {
            throw new UnsupportedOperationException("destroy not implemented");
        }
    }

    private class MockPlugin {
        private final String symbolicName;
        private Bundle bundle;
        private ServiceReference serviceReference;

        private AuthenticationSystem system;

        private MockPlugin(final String symbolicName, AuthenticationSystem instance) {
            this.symbolicName = symbolicName;
            bundle = new MockBundle() {
                @Override public String getSymbolicName() { return symbolicName; }
            };
            serviceReference = new MockServiceReference(bundle) {
                @Override
                public String toString() {
                    return new StringBuilder().append("MockServiceReference")
                        .append('@').append(Integer.toHexString(System.identityHashCode(this)))
                        .append("[bundle=").append(getBundle().getSymbolicName()).append(']')
                        .toString();
                }
            };
            system = instance;
        }

        public String getSymbolicName() {
            return symbolicName;
        }

        public Bundle getBundle() {
            return bundle;
        }

        public ServiceReference getServiceReference() {
            return serviceReference;
        }

        public AuthenticationSystem getSystem() {
            return system;
        }
    }
}
