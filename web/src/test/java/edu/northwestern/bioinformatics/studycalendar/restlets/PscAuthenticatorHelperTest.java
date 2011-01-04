package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.data.ChallengeRequest;

/**
 * @author Rhett Sutphin
 */
public class PscAuthenticatorHelperTest extends RestletTestCase {
    public void testWrittenChallenge() throws Exception {
        String actual = new PscAuthenticatorHelper().formatRequest(
            new ChallengeRequest(PscAuthenticator.HTTP_PSC_TOKEN, "PSC-7"), response, null);

        assertEquals("psc_token realm=\"PSC-7\"", actual);
    }
}
