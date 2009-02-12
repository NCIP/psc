package edu.northwestern.bioinformatics.studycalendar.security;

import edu.northwestern.bioinformatics.studycalendar.testing.DaoTestCase;
import static edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase.*;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.CasAuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.KnownAuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.StubAuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import org.springframework.context.ApplicationContext;

/**
 * @author Rhett Sutphin
 */
public class AuthenticationSystemConfigurationTest extends DaoTestCase {
    private AuthenticationSystemConfiguration configuration;
    private ApplicationContext context;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        context = getDeployedApplicationContext();
        configuration = (AuthenticationSystemConfiguration) context.getBean("authenticationSystemConfiguration");
        configuration.setApplicationContext(context);
    }

    public void testDefaultAuthSystemIsLocal() throws Exception {
        configuration.reset(AuthenticationSystemConfiguration.AUTHENTICATION_SYSTEM);
        assertEquals(KnownAuthenticationSystem.LOCAL.name(), configuration.get(AuthenticationSystemConfiguration.AUTHENTICATION_SYSTEM));
    }

    public void testPropertiesReflectConfiguredAuthenticationSystem() throws Exception {
        assertNotNull("No properties for local", configuration.getProperties());
        assertEquals("Wrong number of properties for local", 1, configuration.getProperties().size());
        assertContains("Sole property for local should be auth system", configuration.getProperties().getAll(), AuthenticationSystemConfiguration.AUTHENTICATION_SYSTEM);

        setMinimumCasParameters();
        selectAuthenticationSystem(KnownAuthenticationSystem.CAS.name());
        assertNotNull("No properties for CAS", configuration.getProperties());
        assertContains("CAS props should contain auth system", configuration.getProperties().getAll(), AuthenticationSystemConfiguration.AUTHENTICATION_SYSTEM);
        assertContains("CAS props should contain CAS system props", configuration.getProperties().getAll(), CasAuthenticationSystem.APPLICATION_URL);
        assertEquals("Wrong number of properties for CAS", 4, configuration.getProperties().size());
        assertEquals("Property details should be available for per-system props",
            "Service URL", configuration.getProperties().getNameFor(CasAuthenticationSystem.SERVICE_URL.getKey()));
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

        setMinimumCasParameters();
        selectAuthenticationSystem(KnownAuthenticationSystem.CAS.name());
        AuthenticationSystem newSystem = configuration.getAuthenticationSystem();
        assertNotSame("New system is not new", initialSystem, newSystem);
        assertTrue("Wrong new system created",
            KnownAuthenticationSystem.CAS.getAuthenticationSystemClass().isAssignableFrom(newSystem.getClass()));
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
    
    public void testIsCustomWhenCustom() throws Exception {
        selectAuthenticationSystem("java.lang.String");
        assertTrue(configuration.isCustomAuthenticationSystem());
    }

    public void testIsCustomWhenNotCustom() throws Exception {
        selectAuthenticationSystem("CAS");
        assertFalse(configuration.isCustomAuthenticationSystem());
    }

    private void setMinimumCasParameters() {
        configuration.set(CasAuthenticationSystem.SERVICE_URL, "not-null");
        configuration.set(CasAuthenticationSystem.APPLICATION_URL, "not-null");
    }

    private void selectAuthenticationSystem(String requested) {
        configuration.set(AuthenticationSystemConfiguration.AUTHENTICATION_SYSTEM, requested);
        interruptSession();
    }
}
