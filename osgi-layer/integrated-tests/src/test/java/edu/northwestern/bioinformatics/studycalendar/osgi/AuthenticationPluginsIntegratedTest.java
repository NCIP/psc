package edu.northwestern.bioinformatics.studycalendar.osgi;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarApplicationContextBuilder;
import edu.northwestern.bioinformatics.studycalendar.security.AuthenticationSystemConfiguration;
import edu.northwestern.bioinformatics.studycalendar.security.internal.AuthenticationSystemSocket;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationTestCase;
import org.acegisecurity.userdetails.memory.InMemoryDaoImpl;
import org.acegisecurity.userdetails.memory.UserMap;
import org.osgi.framework.BundleContext;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

public class AuthenticationPluginsIntegratedTest extends AuthenticationTestCase {
    private static BundleContext bundleContext;

    private AuthenticationSystemSocket socket;
    private AuthenticationSystemConfiguration asConfiguration;

    private MockHttpServletRequest request = new MockHttpServletRequest();
    private MockHttpServletResponse response = new MockHttpServletResponse();
    private MockFilterChain filterChain = new MockFilterChain();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ApplicationContext applicationContext
            = StudyCalendarApplicationContextBuilder.createDeployedApplicationContext();

        InMemoryDaoImpl userDetails = new InMemoryDaoImpl();
        userDetails.setUserMap(new UserMap());
        userDetails.getUserMap().addUser(Fixtures.createUser("juno"));
        userDetails.getUserMap().addUser(Fixtures.createUser("zelda"));

        asConfiguration = new AuthenticationSystemConfiguration();
        asConfiguration.setBundleContext(getBundleContext());
//        asConfiguration.setDelegate(new TransientConfiguration(DefaultConfigurationProperties.empty()));

        socket = new AuthenticationSystemSocket();
        socket.setConfiguration(asConfiguration);
        socket.afterPropertiesSet();
    }

    protected static synchronized BundleContext getBundleContext() throws IOException {
        return OsgiLayerIntegratedTestHelper.getBundleContext();
    }

    public void testGetDefaultAuthenticationPlugin() throws Exception {
        AuthenticationSystem system = asConfiguration.getAuthenticationSystem();
        assertEquals("Wrong default system", "Local", system.name());
    }
    /*
    public void testSwitchedAuthenticationWorks() throws Exception {
        asConfiguration.set(
            AuthenticationSystemConfiguration.AUTHENTICATION_SYSTEM,
            "edu.northwestern.bioinformatics.psc-authentication-insecure-plugin"
        );
        AuthenticationSystem actual = asConfiguration.getAuthenticationSystem();
        assertEquals("Wrong system configured", "Insecure", actual.name());
    }

    public void testLocalAuthenticationSystemEntryPointIsAccessible() throws Exception {
        switchToLocal();
        socket.commence(request, response, null);
        // expect no class cast exceptions
    }

    public void testCasFilterApplies() throws Exception {
        switchToCas();
        socket.doFilter(request, response, filterChain);
        assertEquals("Filter chain not continued", request, filterChain.getRequest());
        assertEquals("Filter chain not continued", response, filterChain.getResponse());
    }

    public void testLocalAuthenticationManagerApplies() throws Exception {
        switchToLocal();
        try {
            socket.authenticate(
                new UsernamePasswordAuthenticationToken("juno", "whatever"));
            fail("Expected exception not thrown");
        } catch (BadCredentialsException expected) {
            // good
        }
    }

    public void testCasAuthenticationManagerApplies() throws Exception {
        switchToCas();
        Authentication result = socket.authenticate(
            new UsernamePasswordAuthenticationToken(CasProcessingFilter.CAS_STATELESS_IDENTIFIER, "foo"));
        assertFalse(result.isAuthenticated());
    }

    public void testCasLogoutFilterApplies() throws Exception {
        AuthenticationSystem system = switchToCas();
        system.logoutFilter().doFilter(request, response, filterChain);
        assertEquals("Filter chain not continued", request, filterChain.getRequest());
        assertEquals("Filter chain not continued", response, filterChain.getResponse());
    }

    public void testWebSSOLogoutFilterApplies() throws Exception {
        AuthenticationSystem system = switchToWebSSO();
        system.logoutFilter().doFilter(request, response, filterChain);
        assertEquals("Filter chain not continued", request, filterChain.getRequest());
        assertEquals("Filter chain not continued", response, filterChain.getResponse());
    }

    private AuthenticationSystem switchToLocal() {
        switchToSystem("local-plugin");
        return asConfiguration.getAuthenticationSystem();
    }

    private AuthenticationSystem switchToCas() {
        return switchToCasBasedSystem("cas-plugin");
    }

    private AuthenticationSystem switchToWebSSO() {
        return switchToCasBasedSystem("websso-plugin");
    }

    private AuthenticationSystem switchToCasBasedSystem(String pluginName) {
        switchToSystem(pluginName);
        asConfiguration.set(CasAuthenticationSystem.APPLICATION_URL, "http://localhost:5600/psc");
        asConfiguration.set(CasAuthenticationSystem.SERVICE_URL, "https://cas");
        return asConfiguration.getAuthenticationSystem();
    }

    private void switchToSystem(String pluginName) {
        asConfiguration.set(
            AuthenticationSystemConfiguration.AUTHENTICATION_SYSTEM,
            "edu.northwestern.bioinformatics.psc-authentication-" + pluginName
        );
    }
    */
}