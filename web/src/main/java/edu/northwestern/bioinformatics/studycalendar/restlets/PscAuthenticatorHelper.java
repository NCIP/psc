package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.Request;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.Parameter;
import org.restlet.engine.Engine;
import org.restlet.engine.http.header.ChallengeWriter;
import org.restlet.engine.security.AuthenticatorHelper;
import org.restlet.security.Guard;
import org.restlet.util.Series;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Rhett Sutphin
 */
public class PscAuthenticatorHelper extends AuthenticatorHelper implements InitializingBean {
    public PscAuthenticatorHelper() {
        super(PscGuard.PSC_TOKEN, false, true);
    }

    @Override
    public int authenticate(ChallengeResponse cr, Request request, Guard guard) {
        if (cr.getCredentials() == null) return Guard.AUTHENTICATION_MISSING;

        if (!(guard instanceof PscGuard)) {
            throw new IllegalArgumentException(getClass().getSimpleName() + " only works with " + PscGuard.class.getSimpleName());
        }
        PscGuard pscGuard = (PscGuard) guard;

        return pscGuard.checkToken(request, cr.getCredentials())
            ? Guard.AUTHENTICATION_VALID
            : Guard.AUTHENTICATION_INVALID;
    }

    @Override
    public void formatRawResponse(ChallengeWriter cw, ChallengeResponse challenge, Request request, Series<Parameter> httpHeaders) {
        cw.append(challenge.getRawValue());
    }

    ////// LIFECYCLE

    public void afterPropertiesSet() throws Exception {
        Engine.getInstance().getRegisteredAuthenticators().add(this);
    }
}
