package edu.northwestern.bioinformatics.studycalendar.security.plugin.websso;

import edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.CasAuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.CasBasedAuthenticationSystemTestCase;
import gov.nih.nci.cabig.caaers.web.security.cas.CaaersCasProxyTicketValidator;
import org.acegisecurity.providers.ProviderManager;
import org.acegisecurity.providers.cas.CasAuthenticationProvider;

/**
 * This only tests the differences between
 * {@link edu.northwestern.bioinformatics.studycalendar.security.plugin.websso.WebSSOAuthenticationSystem}
 * and {@link edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.CasAuthenticationSystem}.
 *
 * @author Saurabh Agrawal
 * @author Rhett Sutphin
 */
public class WebSSOAuthenticationSystemTest extends CasBasedAuthenticationSystemTestCase {
    
	protected static final String EXPECTED_HOST_KEY = "/tmp/etc.txt";
    protected static final String EXPECTED_HOST_CERT = "/tmp/etc.txt";
	private WebSSOAuthenticationSystem system;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        system = new WebSSOAuthenticationSystem();
        system.setBundleContext(bundleContext);
    }

    public void testInitializeAuthManager() throws Exception {
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

    @Override
    public CasAuthenticationSystem getSystem() {
        return system;
    }
}