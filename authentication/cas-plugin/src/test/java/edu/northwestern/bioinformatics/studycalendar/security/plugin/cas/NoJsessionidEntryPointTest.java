/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.plugin.cas;

import junit.framework.TestCase;
import org.acegisecurity.ui.cas.ServiceProperties;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * @author Rhett Sutphin
 */
public class NoJsessionidEntryPointTest extends TestCase {
    private static final String LOGIN_URL = "https://security-server/cas";
    private static final String SERVICE = "http://localhost/psc-tests";

    private NoJsessionidEntryPoint entryPoint;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        request = new MockHttpServletRequest();
        response = new SessionAppendingMockHttpServletResponse();

        entryPoint = new NoJsessionidEntryPoint();
        entryPoint.setLoginUrl(LOGIN_URL);
        ServiceProperties sp = new ServiceProperties();
        sp.setService(SERVICE);
        entryPoint.setServiceProperties(sp);
    }

    public void testRedirectedUrlDoesNotIncludeJsessionid() throws Exception {
        commence();

        assertFalse("jsessionid present in response URL: " + response.getRedirectedUrl(),
            response.getRedirectedUrl().contains("jsessionid"));
    }

    public void testRedirectedUrlDoesIncludeService() throws Exception {
        commence();

        assertTrue("service not present in response URL: " + response.getRedirectedUrl(),
            response.getRedirectedUrl().contains("service=" + URLEncoder.encode(SERVICE, "UTF-8")));
    }

    private void commence() throws IOException, ServletException {
        entryPoint.commence(request, response, null);
    }

    private static class SessionAppendingMockHttpServletResponse extends MockHttpServletResponse {
        @Override
        public String encodeURL(String s) {
            return s + ";jsessionid=foo";
        }
    }
}
