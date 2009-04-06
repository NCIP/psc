package edu.northwestern.bioinformatics.studycalendar.osgi;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarApplicationContextBuilder;
import edu.northwestern.bioinformatics.studycalendar.security.AuthenticationSystemConfiguration;
import edu.northwestern.bioinformatics.studycalendar.security.AuthenticationSystemSocket;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationTestCase;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.CasAuthenticationSystem;
import gov.nih.nci.cabig.ctms.tools.configuration.DefaultConfigurationProperties;
import gov.nih.nci.cabig.ctms.tools.configuration.TransientConfiguration;
import org.acegisecurity.userdetails.memory.InMemoryDaoImpl;
import org.acegisecurity.userdetails.memory.UserMap;
import org.dynamicjava.osgi.da_launcher.Launcher;
import org.dynamicjava.osgi.da_launcher.LauncherFactory;
import org.dynamicjava.osgi.da_launcher.LauncherSettings;
import org.osgi.framework.BundleContext;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class AuthenticationPluginsIntegratedTest extends AuthenticationTestCase {
    private AuthenticationSystemSocket socket;
    private AuthenticationSystemConfiguration asConfiguration;
    private BundleContext bundleContext;

    private MockHttpServletRequest request = new MockHttpServletRequest();
    private MockHttpServletResponse response = new MockHttpServletResponse();
    private MockFilterChain filterChain = new MockFilterChain();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        LauncherSettings settings = new LauncherSettings(
            getModuleRelativeDirectory("osgi-layer", "target/test/da-launcher").getAbsolutePath());
        Launcher launcher = new LauncherFactory(settings).createLauncher();
        launcher.launch();
        bundleContext = launcher.getOsgiFramework().getBundleContext();

        ApplicationContext applicationContext
            = StudyCalendarApplicationContextBuilder.createDeployedApplicationContext();

        InMemoryDaoImpl userDetails = new InMemoryDaoImpl();
        userDetails.setUserMap(new UserMap());
        userDetails.getUserMap().addUser(Fixtures.createUser("juno"));
        userDetails.getUserMap().addUser(Fixtures.createUser("zelda"));

        asConfiguration = new AuthenticationSystemConfiguration();
        asConfiguration.setBundleContext(bundleContext);
        asConfiguration.setDelegate(new TransientConfiguration(DefaultConfigurationProperties.empty()));
        asConfiguration.setUserDetailsService(userDetails);
        asConfiguration.setDataSource((DataSource) applicationContext.getBean("dataSource"));

        socket = new AuthenticationSystemSocket();
        socket.setConfiguration(asConfiguration);
        socket.afterPropertiesSet();
    }

    private static File getModuleRelativeDirectory(String moduleName, String directory) throws IOException {
        File dir = new File(directory);
        if (dir.exists()) return dir;

        dir = new File(moduleName.replaceAll(":", "/"), directory);
        if (dir.exists()) return dir;

        throw new FileNotFoundException(
            String.format("Could not find directory %s relative to module %s from current directory %s",
                directory, moduleName, new File(".").getCanonicalPath()));
    }

    public void testGetDefaultAuthenticationPlugin() throws Exception {
        AuthenticationSystem system = asConfiguration.getAuthenticationSystem();
        assertEquals("Wrong default system", "Local", system.name());
    }

    public void testSwitchedAuthenticationWorks() throws Exception {
        asConfiguration.set(
            AuthenticationSystemConfiguration.AUTHENTICATION_SYSTEM,
            "edu.northwestern.bioinformatics.psc-authentication-insecure-plugin"
        );
        AuthenticationSystem actual = asConfiguration.getAuthenticationSystem();
        assertEquals("Wrong system configured", "Insecure", actual.name());
    }

    public void testCasLogoutFilterApplies() throws Exception {
        AuthenticationSystem system = switchToCas();
        system.logoutFilter().doFilter(request, response, filterChain);
        // expect no exceptions
    }

    private AuthenticationSystem switchToCas() {
        asConfiguration.set(
            AuthenticationSystemConfiguration.AUTHENTICATION_SYSTEM,
            "edu.northwestern.bioinformatics.psc-authentication-cas-plugin"
        );
        asConfiguration.set(CasAuthenticationSystem.APPLICATION_URL, "http://localhost:5600/psc");
        asConfiguration.set(CasAuthenticationSystem.SERVICE_URL, "https://cas");
        return asConfiguration.getAuthenticationSystem();
    }
}