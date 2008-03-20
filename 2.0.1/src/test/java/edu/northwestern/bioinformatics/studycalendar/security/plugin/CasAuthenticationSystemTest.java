package edu.northwestern.bioinformatics.studycalendar.security.plugin;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.tools.configuration.MockConfiguration;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;
import org.acegisecurity.providers.ProviderManager;
import org.acegisecurity.providers.cas.CasAuthenticationProvider;
import org.acegisecurity.providers.cas.populator.DaoCasAuthoritiesPopulator;
import org.acegisecurity.providers.cas.proxy.AcceptAnyCasProxy;
import org.acegisecurity.providers.cas.ticketvalidator.CasProxyTicketValidator;
import org.acegisecurity.ui.cas.CasProcessingFilterEntryPoint;
import org.acegisecurity.ui.cas.ServiceProperties;
import org.acegisecurity.ui.logout.LogoutFilter;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.ApplicationContext;

/**
 * @author Rhett Sutphin
 */
public class CasAuthenticationSystemTest extends StudyCalendarTestCase {
    private static final String EXPECTED_SERVICE_URL = "http://etc:5443/cas";
    private static final String EXPECTED_APP_URL = "http://psc.etc/";

    private CasAuthenticationSystem system;
    private Configuration configuration;
    private ApplicationContext applicationContext;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        system = new CasAuthenticationSystem();
        configuration = new MockConfiguration();
        applicationContext = getDeployedApplicationContext();
    }

    public void testServiceUrlRequired() throws Exception {
        configuration.set(CasAuthenticationSystem.APPLICATION_URL, EXPECTED_APP_URL);
        try {
            system.initialize(applicationContext, configuration);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals("Service URL is required for the selected authentication system",
                scve.getMessage());
        }
    }

    public void testApplicationUrlRequired() throws Exception {
        configuration.set(CasAuthenticationSystem.SERVICE_URL, EXPECTED_SERVICE_URL);
        try {
            system.initialize(applicationContext, configuration);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals("PSC base URL is required for the selected authentication system",
                scve.getMessage());
        }
    }

    public void testInitializeEntryPoint() throws Exception {
        doValidInitialize();

        assertTrue("Wrong entry point type", system.entryPoint() instanceof CasProcessingFilterEntryPoint);
        CasProcessingFilterEntryPoint entryPoint = (CasProcessingFilterEntryPoint) system.entryPoint();
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
        assertTrue("Wrong type", system.authenticationManager() instanceof ProviderManager);
        ProviderManager manager = (ProviderManager) system.authenticationManager();
        assertEquals("Wrong number of providers", 2, manager.getProviders().size());
        assertTrue("First provider is not CAS provider",
            manager.getProviders().get(0) instanceof CasAuthenticationProvider);
        CasAuthenticationProvider provider = (CasAuthenticationProvider) manager.getProviders().get(0);
        assertTrue("Wrong kind of authorities populator",
            provider.getCasAuthoritiesPopulator() instanceof DaoCasAuthoritiesPopulator);
        CasProxyTicketValidator validator = ((CasProxyTicketValidator) provider.getTicketValidator());
        assertEquals("Wrong ticket validation URL", "http://etc:5443/cas/proxyValidate",
            validator.getCasValidate());
        assertTrue("Trust store should not be set", StringUtils.isBlank(validator.getTrustStore()));
        assertCorrectServiceProperties(validator.getServiceProperties());
        assertTrue("Proxy tickets should be allowed",
            provider.getCasProxyDecider() instanceof AcceptAnyCasProxy);
    }

    public void testInitializeLogoutFilter() throws Exception {
        doValidInitialize();
        assertTrue("Wrong filter type", system.logoutFilter() instanceof LogoutFilter);
        // can't really test anything else because no properties are exposed.
    }

    private void doValidInitialize() {
        configuration.set(CasAuthenticationSystem.SERVICE_URL, EXPECTED_SERVICE_URL);
        configuration.set(CasAuthenticationSystem.APPLICATION_URL, EXPECTED_APP_URL);
        system.initialize(applicationContext, configuration);
    }
}
