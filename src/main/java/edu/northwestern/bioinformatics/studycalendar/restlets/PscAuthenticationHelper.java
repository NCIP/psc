package edu.northwestern.bioinformatics.studycalendar.restlets;

import com.noelios.restlet.Engine;
import com.noelios.restlet.authentication.AuthenticationHelper;
import org.restlet.Guard;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.Parameter;
import org.restlet.data.Request;
import org.restlet.util.Series;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Rhett Sutphin
 */
public class PscAuthenticationHelper extends AuthenticationHelper implements InitializingBean {
    public PscAuthenticationHelper() {
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
    public void formatCredentials(StringBuilder sb, ChallengeResponse challenge, Request request, Series<Parameter> httpHeaders) {
        sb.append(challenge.getCredentials());
    }

    ////// LIFECYCLE

    public void afterPropertiesSet() throws Exception {
        Engine.getInstance().getRegisteredAuthentications().add(this);
    }
}
