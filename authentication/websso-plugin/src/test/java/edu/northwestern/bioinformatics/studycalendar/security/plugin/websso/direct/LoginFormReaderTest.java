/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.plugin.websso.direct;

import edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.direct.CasDirectException;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class LoginFormReaderTest extends TestCase {
    public void testCanExtractLoginTicketFromWebSSOLoginPage() throws Exception {
        LoginFormReader reader = readerFor("websso-login-selected-credential-provider.html");
        assertEquals("Login ticket not found",
            "_c3DAB613F-A64B-E80E-CDCE-F90B531B76D2_k37DB76AA-C765-1E0E-D291-39D116DECC9D",
            reader.getLoginTicket());
    }

    public void testCanExtractAuthenticationServices() throws Exception {
        LoginFormReader reader = readerFor("websso-login-selected-credential-provider.html");
        Map<String, String> actual = reader.getAuthenticationServices();
        assertEquals("Wrong number of organizations found: " + actual, 2, actual.size());
        assertEquals("Missing 'Dorian'", "https://fenchurch-cagrid.local:8443/wsrf/services/cagrid/Dorian", actual.get("Dorian"));
        assertEquals("Missing 'Other Dorian'", "https://fenchurch-cagrid.local:8444/wsrf/services/cagrid/Dorian2", actual.get("Other Dorian"));
    }

    public void testErrorIfNoAuthenticationServicesPresent() throws Exception {
        LoginFormReader reader = readerFor("websso-login-initial.html");
        try {
            reader.getAuthenticationServices();
            fail("Exception not thrown");
        } catch (CasDirectException e) {
            assertEquals(
                "The WebSSO login page does not include a list of authenticationServiceURLs.  Cannot use direct login.",
                e.getMessage());
        }
    }

    public void testErrorForOrgsIfNotAWebSSOLoginPage() throws Exception {
        LoginFormReader reader = readerFor("random.html");
        try {
            reader.getAuthenticationServices();
            fail("Exception not thrown");
        } catch (CasDirectException e) {
            assertEquals(
                "The WebSSO login page does not include a list of authenticationServiceURLs.  Cannot use direct login.",
                e.getMessage());
        }
    }

    private LoginFormReader readerFor(String filename) throws IOException {
        InputStream stream = getClass().getResourceAsStream('/' + filename);
        return new LoginFormReader(IOUtils.toString(stream));
    }
}
