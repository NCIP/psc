/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.Response;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.Parameter;
import org.restlet.engine.Engine;
import org.restlet.engine.http.header.ChallengeWriter;
import org.restlet.engine.security.AuthenticatorHelper;
import org.restlet.util.Series;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public class PscAuthenticatorHelper extends AuthenticatorHelper implements InitializingBean {
    public PscAuthenticatorHelper() {
        // This helper claims to be a client-side helper even though it is only used on a server
        // because Restlet's AuthenticatorUtils requires this behavior to avoid a useless warning.
        // See #1467 for more info.
        super(PscAuthenticator.HTTP_PSC_TOKEN, true, true);
    }

    @Override
    public void formatRawRequest(
        ChallengeWriter cw, ChallengeRequest challenge,
        Response response, Series<Parameter> httpHeaders
    ) throws IOException {
        if (challenge.getRealm() != null) {
            cw.appendQuotedChallengeParameter("realm", challenge.getRealm());
        }
    }

    ////// LIFECYCLE

    public void afterPropertiesSet() throws Exception {
        Engine.getInstance().getRegisteredAuthenticators().add(this);
    }
}
