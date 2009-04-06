package edu.northwestern.bioinformatics.studycalendar.security;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.KnownAuthenticationSystem;
import gov.nih.nci.cabig.ctms.tools.configuration.TransientConfiguration;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.web.context.support.StaticWebApplicationContext;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public class AuthenticationSystemConfigurationTest extends StudyCalendarTestCase {
    private AuthenticationSystemConfiguration configuration;
    private StaticWebApplicationContext context;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        context = new StaticWebApplicationContext();
        configuration = new AuthenticationSystemConfiguration();
        configuration.setApplicationContext(context);
        configuration.setDelegate(new TransientConfiguration(AuthenticationSystemConfiguration.UNIVERSAL_PROPERTIES));

        context.registerSingleton("dataSource", SingleConnectionDataSource.class);
        context.registerSingleton("pscUserDetailsService", PscUserDetailsService.class);
        context.registerSingleton("defaultLogoutFilter", DummyFilter.class);
        context.refresh();
    }

    public void testDefaultAuthSystemIsLocal() throws Exception {
        configuration.reset(AuthenticationSystemConfiguration.AUTHENTICATION_SYSTEM);
        assertEquals(KnownAuthenticationSystem.LOCAL.name(), configuration.get(AuthenticationSystemConfiguration.AUTHENTICATION_SYSTEM));
    }

    public void testPropertiesReflectConfiguredAuthenticationSystem() throws Exception {
        assertNotNull("No properties for local", configuration.getProperties());
        assertEquals("Wrong number of properties for local", 1, configuration.getProperties().size());
        assertContains("Sole property for local should be auth system", configuration.getProperties().getAll(), AuthenticationSystemConfiguration.AUTHENTICATION_SYSTEM);

        setMinimumTestSystemParameters();
        selectAuthenticationSystem(TestableAuthenticationSystem.class.getName());
        assertNotNull("No properties for TAS", configuration.getProperties());
        assertContains("TAS props should contain auth system", configuration.getProperties().getAll(), AuthenticationSystemConfiguration.AUTHENTICATION_SYSTEM);
        assertContains("TAS props should contain TAS system props", configuration.getProperties().getAll(), TestableAuthenticationSystem.APPLICATION_URL);
        assertEquals("Wrong number of properties for TAS", 3, configuration.getProperties().size());
        assertEquals("Property details should be available for per-system props",
            "Service URL", configuration.getProperties().getNameFor(TestableAuthenticationSystem.SERVICE_URL.getKey()));
    }

    public void testCreateDefaultAuthenticationSystem() throws Exception {
        configuration.reset(AuthenticationSystemConfiguration.AUTHENTICATION_SYSTEM);
        AuthenticationSystem system = configuration.getAuthenticationSystem();
        assertNotNull("No system created", system);
        assertTrue("Wrong system created: " + system.getClass().getName(),
            KnownAuthenticationSystem.LOCAL.getAuthenticationSystemClass().isAssignableFrom(system.getClass()));
    }

    public void testCreateNewAuthenticationSystemAfterSystemSwitch() throws Exception {
        AuthenticationSystem initialSystem = configuration.getAuthenticationSystem();
        assertNotNull("No system created initially", initialSystem);

        setMinimumTestSystemParameters();
        selectAuthenticationSystem(TestableAuthenticationSystem.class.getName());
        AuthenticationSystem newSystem = configuration.getAuthenticationSystem();
        assertNotSame("New system is not new", initialSystem, newSystem);
        assertTrue("Wrong new system created",
            TestableAuthenticationSystem.class.isAssignableFrom(newSystem.getClass()));
    }

    public void testCreateAuthenticationSystemFromClassName() throws Exception {
        selectAuthenticationSystem(StubAuthenticationSystem.class.getName());

        AuthenticationSystem system = configuration.getAuthenticationSystem();
        assertNotNull("System not created", system);
        assertTrue("System is wrong class: " + system.getClass().getName(), system instanceof StubAuthenticationSystem);
    }

    public void testCreatedAuthenticationSystemIsInitialized() throws Exception {
        selectAuthenticationSystem(StubAuthenticationSystem.class.getName());

        assertTrue("System is wrong class",
            configuration.getAuthenticationSystem() instanceof StubAuthenticationSystem);
        StubAuthenticationSystem system = (StubAuthenticationSystem) configuration.getAuthenticationSystem();
        assertSame("Wrong application context used during initialization", context,
            system.getInitialApplicationContext());
        // can't compare the configuration objects themselves because one's a CGLIB proxy
        assertSame("Wrong configuration used during initialization", configuration.getProperties(),
            system.getInitialConfiguration().getProperties());
    }
    
    public void testInitializationExceptionIsPropagated() throws Exception {
        configuration.set(StubAuthenticationSystem.EXPECTED_INITIALIZATION_ERROR_MESSAGE, "Bad news");
        selectAuthenticationSystem(StubAuthenticationSystem.class.getName());

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
}
