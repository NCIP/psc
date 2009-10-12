package edu.northwestern.bioinformatics.studycalendar.security.plugin.websso;

import edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.CasAuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.CasBasedAuthenticationSystemTestCase;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import gov.nih.nci.cabig.caaers.web.security.cas.CaaersCasProxyTicketValidator;
import org.acegisecurity.providers.ProviderManager;
import org.acegisecurity.providers.cas.CasAuthenticationProvider;

/**
 * This only tests the differences between
 * {@link edu.northwestern.bioinformatics.studycalendar.security.plugin.websso.WebSSOAuthenticationSystem}
 * and {@link edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.CasAuthenticationSystem}.
 *
 * @author Saurabh Agrawal
 * @author Kruttik Aggarwal
 * @author Rhett Sutphin
 */
public class WebSSOAuthenticationSystemTest extends CasBasedAuthenticationSystemTestCase {
    private static final String EXPECTED_HOST_KEY = "/tmp/key-etc.txt";
    private static final String EXPECTED_HOST_CERT = "/tmp/cert-etc.txt";

    private WebSSOAuthenticationSystem system;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        system = new WebSSOAuthenticationSystem();
        system.setBundleContext(bundleContext);
    }

    public void testInitializeAuthManager() throws Exception {
        configuration.set(CasAuthenticationSystem.SERVICE_URL, EXPECTED_SERVICE_URL);
        configuration.set(CasAuthenticationSystem.APPLICATION_URL, EXPECTED_APP_URL);
        configuration.set(WebSSOAuthenticationSystem.HOST_CERT, EXPECTED_HOST_CERT);
        configuration.set(WebSSOAuthenticationSystem.HOST_KEY, EXPECTED_HOST_KEY);
        doValidInitialize();
        assertTrue("Wrong type", getSystem().authenticationManager() instanceof ProviderManager);
        ProviderManager manager = (ProviderManager) getSystem().authenticationManager();
        assertEquals("Wrong number of providers", 2, manager.getProviders().size());
        assertTrue("First provider is not CAS provider",
                manager.getProviders().get(0) instanceof CasAuthenticationProvider);
        CasAuthenticationProvider provider = (CasAuthenticationProvider) manager.getProviders().get(0);

        assertTrue("Wrong kind of authorities populator",
                provider.getCasAuthoritiesPopulator() instanceof WebSSOAuthoritiesPopulator);

        assertTrue("Wrong type of ticket validator",
            provider.getTicketValidator() instanceof CaaersCasProxyTicketValidator);
    }

    public void testHostKeyRequired() throws Exception {
        configuration.set(CasAuthenticationSystem.SERVICE_URL, EXPECTED_SERVICE_URL);
        configuration.set(CasAuthenticationSystem.APPLICATION_URL, EXPECTED_APP_URL);
        configuration.set(WebSSOAuthenticationSystem.HOST_CERT, EXPECTED_HOST_CERT);
        try {
            getSystem().validate(configuration);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals("Host key is required for the selected authentication system",
                    scve.getMessage());
        }
    }

    public void testHostCertRequired() throws Exception {
        configuration.set(CasAuthenticationSystem.SERVICE_URL, EXPECTED_SERVICE_URL);
        configuration.set(CasAuthenticationSystem.APPLICATION_URL, EXPECTED_APP_URL);
        configuration.set(WebSSOAuthenticationSystem.HOST_KEY, EXPECTED_HOST_KEY);
        try {
            getSystem().validate(configuration);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals("Host certificate is required for the selected authentication system",
                    scve.getMessage());
        }
    }

    @Override
    public CasAuthenticationSystem getSystem() {
        return system;
    }
}