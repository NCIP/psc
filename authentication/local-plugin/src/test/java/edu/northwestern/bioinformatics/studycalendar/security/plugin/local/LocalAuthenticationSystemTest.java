package edu.northwestern.bioinformatics.studycalendar.security.plugin.local;

import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationTestCase;
import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;
import org.acegisecurity.providers.ProviderManager;

/**
 * @author Rhett Sutphin
 */
public class LocalAuthenticationSystemTest extends AuthenticationTestCase {
    private LocalAuthenticationSystem system;
    private Configuration configuration;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        configuration = blankConfiguration();

        system = new LocalAuthenticationSystem();
    }

    public void testInitializeAuthManager() throws Exception {
        replayMocks();
        system.initialize(getMockApplicationContext(), configuration);
        assertTrue("Wrong type", system.authenticationManager() instanceof ProviderManager);
        ProviderManager manager = (ProviderManager) system.authenticationManager();
        assertEquals("Wrong number of providers", 2, manager.getProviders().size());
        assertTrue("First provider is not PSC-local provider",
                manager.getProviders().get(0) instanceof PscAuthenticationProvider);
    }
}
