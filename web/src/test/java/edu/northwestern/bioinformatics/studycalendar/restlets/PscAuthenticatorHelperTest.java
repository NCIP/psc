package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.Parameter;
import org.restlet.engine.Engine;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.engine.security.AuthenticatorUtils;

/**
 * @author Rhett Sutphin
 */
public class PscAuthenticatorHelperTest extends RestletTestCase {
    private PscAuthenticatorHelper helper;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        helper = new PscAuthenticatorHelper();
        helper.afterPropertiesSet();
    }

    public void testWrittenChallenge() throws Exception {
        String actual = helper.formatRequest(
            new ChallengeRequest(PscAuthenticator.HTTP_PSC_TOKEN, "PSC-7"), response, null);

        assertEquals("psc_token realm=\"PSC-7\"", actual);
    }

    public void testIsFindableForRequestAfterRegistration() throws Exception {
        assertNotNull(Engine.getInstance().findHelper(PscAuthenticator.HTTP_PSC_TOKEN, false, true));
    }

    public void testIsFindableForResponseAfterRegistration() throws Exception {
        assertNotNull(Engine.getInstance().findHelper(PscAuthenticator.HTTP_PSC_TOKEN, true, false));
    }

    // This test verifies that the default behavior in Restlet is sufficient for psc_token.
    public void testParsedResponseHasTokenAsRawValue() throws Exception {
        request.getHeaders().
            add(new Parameter(HeaderConstants.HEADER_AUTHORIZATION, "psc_token ABC-FOO-42"));

        ChallengeResponse actual =
            AuthenticatorUtils.parseResponse(request, "psc_token ABC-FOO-42", request.getHeaders());
        assertEquals("ABC-FOO-42", actual.getRawValue());
    }
}
