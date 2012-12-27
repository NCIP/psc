/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.plugin.cas;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.direct.CasDirectAuthenticationProvider;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.direct.CasDirectUsernamePasswordAuthenticationToken;
import org.acegisecurity.Authentication;
import org.acegisecurity.providers.ProviderManager;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.providers.anonymous.AnonymousAuthenticationProvider;
import org.acegisecurity.providers.cas.CasAuthenticationProvider;
import org.acegisecurity.providers.cas.populator.DaoCasAuthoritiesPopulator;
import org.acegisecurity.providers.cas.proxy.AcceptAnyCasProxy;
import org.acegisecurity.providers.cas.ticketvalidator.CasProxyTicketValidator;
import org.acegisecurity.ui.cas.CasProcessingFilter;
import org.acegisecurity.ui.cas.ServiceProperties;
import org.acegisecurity.ui.logout.LogoutFilter;
import org.apache.commons.lang.StringUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.Filter;
import javax.servlet.FilterChain;

/**
 * @author Rhett Sutphin
 */
public class CasAuthenticationSystemTest extends CasBasedAuthenticationSystemTestCase {
    private CasAuthenticationSystem system;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        system = new CasAuthenticationSystem();
        system.setBundleContext(bundleContext);
    }

    public void testServiceUrlRequired() throws Exception {
        configuration.set(CasAuthenticationSystem.APPLICATION_URL, EXPECTED_APP_URL);
        try {
            getSystem().validate(configuration);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals("Service URL is required for the selected authentication system",
                    scve.getMessage());
        }
    }

    public void testApplicationUrlRequired() throws Exception {
        configuration.set(CasAuthenticationSystem.SERVICE_URL, EXPECTED_SERVICE_URL);
        try {
            getSystem().validate(configuration);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals("PSC base URL is required for the selected authentication system",
                    scve.getMessage());
        }
    }

    public void testInitializeEntryPoint() throws Exception {
        doValidInitialize();

        assertTrue("Wrong entry point type", getSystem().entryPoint() instanceof NoJsessionidEntryPoint);
        NoJsessionidEntryPoint entryPoint = (NoJsessionidEntryPoint) getSystem().entryPoint();
        assertEquals("Wrong CAS URL", expectedLoginUrl(), entryPoint.getLoginUrl());
        assertCorrectServiceProperties(entryPoint.getServiceProperties());
    }

    private void assertCorrectServiceProperties(ServiceProperties serviceProps) {
        assertEquals("Wrong return URL", expectedServiceUrl(), serviceProps.getService());
        assertFalse("Should not send renews", serviceProps.isSendRenew());
    }

    public void testInitializeCasAuthProvider() throws Exception {
        configuration.set(CasAuthenticationSystem.ALLOW_DIRECT_CAS, true);
        doValidInitialize();
        assertTrue("Wrong type", getSystem().authenticationManager() instanceof ProviderManager);
        ProviderManager manager = (ProviderManager) getSystem().authenticationManager();
        assertEquals("Wrong number of providers", 3, manager.getProviders().size());
        assertTrue("Second provider is not CAS provider",
                manager.getProviders().get(1) instanceof CasAuthenticationProvider);
        CasAuthenticationProvider provider = (CasAuthenticationProvider) manager.getProviders().get(1);
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

    public void testInitializeCasDirectAuthProvider() throws Exception {
        configuration.set(CasAuthenticationSystem.ALLOW_DIRECT_CAS, true);
        doValidInitialize();
        assertTrue("Wrong type", getSystem().authenticationManager() instanceof ProviderManager);
        ProviderManager manager = (ProviderManager) getSystem().authenticationManager();
        assertEquals("Wrong number of providers", 3, manager.getProviders().size());
        assertTrue("First provider is not CAS direct provider",
                manager.getProviders().get(0) instanceof CasDirectAuthenticationProvider);
        CasDirectAuthenticationProvider provider = (CasDirectAuthenticationProvider) manager.getProviders().get(0);

        assertEquals("Wrong service URL", expectedServiceUrl(), provider.getServiceUrl());
        assertEquals("Wrong login URL", expectedLoginUrl(), provider.getLoginUrl());
    }
    
    public void testAllowDirectCasDefaultsToFalse() throws Exception {
        assertFalse(configuration.get(CasAuthenticationSystem.ALLOW_DIRECT_CAS));
    }

    public void testCasDirectAuthProviderNotIncludedWhenDisabled() throws Exception {
        configuration.set(CasAuthenticationSystem.ALLOW_DIRECT_CAS, false);
        doValidInitialize();
        assertTrue("Wrong type", getSystem().authenticationManager() instanceof ProviderManager);
        ProviderManager manager = (ProviderManager) getSystem().authenticationManager();
        assertEquals("Wrong number of providers", 2, manager.getProviders().size());
        assertTrue("Wrong first provider: " + manager.getProviders().get(0),
            manager.getProviders().get(0) instanceof CasAuthenticationProvider);
        assertTrue("Wrong second provider: " + manager.getProviders().get(1),
            manager.getProviders().get(1) instanceof AnonymousAuthenticationProvider);
    }

    public void testInitializeLogoutFilter() throws Exception {
        doValidInitialize();
        Filter actual = getSystem().logoutFilter();
        assertTrue("Wrong filter type", actual instanceof LogoutFilter);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/auth/logout");
        // Expect filter chain not continued
        FilterChain filterChain = registerMockFor(FilterChain.class);
        replayMocks();
        actual.doFilter(request, new MockHttpServletResponse(), filterChain);
        verifyMocks();
    }

    public void testTokenAuthRequestReturnsTheWeirdThingThatCasProcessingFilterExpects() throws Exception {
        Authentication actual = getSystem().createTokenAuthenticationRequest("PT-foo");
        assertTrue("Wrong type of Authentication", actual instanceof UsernamePasswordAuthenticationToken);
        assertEquals("Wrong principal", CasProcessingFilter.CAS_STATELESS_IDENTIFIER, actual.getPrincipal());
        assertEquals("Wrong credentials", "PT-foo", actual.getCredentials());
    }

    public void testUsernamePasswordAuthGivesCasDirectTokenWhenEnabled() throws Exception {
        configuration.set(CasAuthenticationSystem.ALLOW_DIRECT_CAS, true);
        doValidInitialize();
        Authentication actual = getSystem().createUsernamePasswordAuthenticationRequest("someone", "something");
        assertTrue("Wrong type of Authentication: " + actual,
            actual instanceof CasDirectUsernamePasswordAuthenticationToken);
        assertEquals("Wrong principal", "someone", actual.getPrincipal());
        assertEquals("Wrong credentials", "something", actual.getCredentials());
    }

    public void testUsernamePasswordAuthGivesNullWhenDisabled() throws Exception {
        configuration.set(CasAuthenticationSystem.ALLOW_DIRECT_CAS, false);
        doValidInitialize();
        Authentication actual = getSystem().createUsernamePasswordAuthenticationRequest("someone", "something");
        assertNull(actual);
    }

    private String expectedLoginUrl() {
        return EXPECTED_SERVICE_URL + "/login";
    }

    private String expectedServiceUrl() {
        return EXPECTED_APP_URL + "auth/cas_security_check";
    }

    @Override
    public CasAuthenticationSystem getSystem() {
        return system;
    }
}
