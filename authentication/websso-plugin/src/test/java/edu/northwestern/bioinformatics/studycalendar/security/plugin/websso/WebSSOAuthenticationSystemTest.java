/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.plugin.websso;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.CasAuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.CasBasedAuthenticationSystemTestCase;
import gov.nih.nci.cabig.caaers.web.security.cas.CaaersCasProxyTicketValidator;
import org.acegisecurity.providers.ProviderManager;
import org.acegisecurity.providers.cas.CasAuthenticationProvider;

import java.io.File;

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
    private File expectedHostKey, expectedHostCert;

    private WebSSOAuthenticationSystem system;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        system = new WebSSOAuthenticationSystem();
        system.setBundleContext(bundleContext);

        expectedHostKey = File.createTempFile("host", "key");
        expectedHostCert = File.createTempFile("host", "cert");
    }

    @Override
    @SuppressWarnings({ "ResultOfMethodCallIgnored" })
    protected void tearDown() throws Exception {
        expectedHostKey.delete();
        expectedHostCert.delete();
        super.tearDown();
    }

    public void testInitializeAuthManager() throws Exception {
        setMinimumValidConfiguration();
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
        setMinimumValidConfiguration();
        configuration.set(WebSSOAuthenticationSystem.HOST_KEY, null);
        assertValidationFailure("Host key is required for the selected authentication system");
    }

    public void testHostCertRequired() throws Exception {
        setMinimumValidConfiguration();
        configuration.set(WebSSOAuthenticationSystem.HOST_CERT, null);
        assertValidationFailure("Host certificate is required for the selected authentication system");
    }

    public void testHostKeyMustBeReadable() throws Exception {
        setMinimumValidConfiguration();
        configuration.set(WebSSOAuthenticationSystem.HOST_KEY, "/an/invalid/file");
        assertValidationFailure("Host key '/an/invalid/file' is not readable");
    }

    public void testHostCertMustBeReadable() throws Exception {
        setMinimumValidConfiguration();
        configuration.set(WebSSOAuthenticationSystem.HOST_CERT, "/an/invalid/file");
        assertValidationFailure("Host certificate '/an/invalid/file' is not readable");
    }

    private void assertValidationFailure(String expectedMessage) {
        try {
            getSystem().validate(configuration);
            fail("Validation exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            assertEquals(expectedMessage,
                    scve.getMessage());
        }
    }

    private void setMinimumValidConfiguration() {
        configuration.set(CasAuthenticationSystem.SERVICE_URL, EXPECTED_SERVICE_URL);
        configuration.set(CasAuthenticationSystem.APPLICATION_URL, EXPECTED_APP_URL);
        configuration.set(WebSSOAuthenticationSystem.HOST_CERT, expectedHostCert.getAbsolutePath());
        configuration.set(WebSSOAuthenticationSystem.HOST_KEY, expectedHostKey.getAbsolutePath());
    }

    @Override
    public CasAuthenticationSystem getSystem() {
        return system;
    }
}