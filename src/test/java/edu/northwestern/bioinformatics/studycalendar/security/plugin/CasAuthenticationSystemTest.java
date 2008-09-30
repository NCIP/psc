package edu.northwestern.bioinformatics.studycalendar.security.plugin;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import org.acegisecurity.providers.ProviderManager;
import org.acegisecurity.providers.cas.CasAuthenticationProvider;
import org.acegisecurity.providers.cas.populator.DaoCasAuthoritiesPopulator;
import org.acegisecurity.providers.cas.proxy.AcceptAnyCasProxy;
import org.acegisecurity.providers.cas.ticketvalidator.CasProxyTicketValidator;
import org.acegisecurity.ui.cas.CasProcessingFilterEntryPoint;
import org.acegisecurity.ui.cas.ServiceProperties;
import org.acegisecurity.ui.logout.LogoutFilter;
import org.apache.commons.lang.StringUtils;

/**
 * @author Rhett Sutphin
 */
public class CasAuthenticationSystemTest extends CasBasedAuthenticationSystemTestCase {
    private CasAuthenticationSystem system;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        system = new CasAuthenticationSystem();
    }

    public void testServiceUrlRequired() throws Exception {
        configuration.set(CasAuthenticationSystem.APPLICATION_URL, EXPECTED_APP_URL);
        try {
            getSystem().initialize(applicationContext, configuration);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals("Service URL is required for the selected authentication system",
                    scve.getMessage());
        }
    }

    public void testApplicationUrlRequired() throws Exception {
        configuration.set(CasAuthenticationSystem.SERVICE_URL, EXPECTED_SERVICE_URL);
        try {
            getSystem().initialize(applicationContext, configuration);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals("PSC base URL is required for the selected authentication system",
                    scve.getMessage());
        }
    }

    public void testInitializeEntryPoint() throws Exception {
        doValidInitialize();

        assertTrue("Wrong entry point type", getSystem().entryPoint() instanceof CasProcessingFilterEntryPoint);
        CasProcessingFilterEntryPoint entryPoint = (CasProcessingFilterEntryPoint) getSystem().entryPoint();
        assertEquals("Wrong CAS URL", EXPECTED_SERVICE_URL, entryPoint.getLoginUrl());
        assertCorrectServiceProperties(entryPoint.getServiceProperties());
    }

    private void assertCorrectServiceProperties(ServiceProperties serviceProps) {
        assertEquals("Wrong return URL", EXPECTED_APP_URL + "j_acegi_cas_security_check",
                serviceProps.getService());
        assertFalse("Should not send renews", serviceProps.isSendRenew());
    }

    public void testInitializeAuthManager() throws Exception {
        doValidInitialize();
        assertTrue("Wrong type", getSystem().authenticationManager() instanceof ProviderManager);
        ProviderManager manager = (ProviderManager) getSystem().authenticationManager();
        assertEquals("Wrong number of providers", 2, manager.getProviders().size());
        assertTrue("First provider is not CAS provider",
                manager.getProviders().get(0) instanceof CasAuthenticationProvider);
        CasAuthenticationProvider provider = (CasAuthenticationProvider) manager.getProviders().get(0);
        CasProxyTicketValidator validator = ((CasProxyTicketValidator) provider.getTicketValidator());

        assertTrue("Wrong kind of authorities populator",
                provider.getCasAuthoritiesPopulator() instanceof DaoCasAuthoritiesPopulator);

        assertEquals("Wrong ticket validation URL", "http://etc:5443/cas/proxyValidate",
                validator.getCasValidate());
        assertTrue("Trust store should not be set", StringUtils.isBlank(validator.getTrustStore()));
        assertCorrectServiceProperties(validator.getServiceProperties());
        assertTrue("Proxy tickets should be allowed",
                provider.getCasProxyDecider() instanceof AcceptAnyCasProxy);
    }

    public void testInitializeLogoutFilter() throws Exception {
        doValidInitialize();
        assertTrue("Wrong filter type", getSystem().logoutFilter() instanceof LogoutFilter);
        // can't really test anything else because no properties are exposed.
    }

    @Override
    public CasAuthenticationSystem getSystem() {
        return system;
    }
}
