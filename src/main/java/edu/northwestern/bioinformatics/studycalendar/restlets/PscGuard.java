package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.Guard;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.Reference;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.AuthenticationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.regex.Pattern;

/**
 * Authentication piece of the API security implementation.  There is a single
 * instance of this guard for the entire API router.  It performs authentication
 * and then puts the Acegi authentication token as a request attribute under the key
 * {@link #AUTH_TOKEN_ATTRIBUTE_KEY}.
 * <p>
 * Authorization is handled by {@link AuthorizingFinder} based on resources
 * implementing {@link AuthorizedResource}.
 *  
 *
 * @author Rhett Sutphin
 */
public class PscGuard extends Guard {
    private final Logger log = LoggerFactory.getLogger(getClass());

    public static final String AUTH_TOKEN_ATTRIBUTE_KEY = "pscAuthenticationToken";

    private Pattern except;
    private AuthenticationManager authenticationManager;

    public PscGuard() {
        super(null, ChallengeScheme.HTTP_BASIC, "PSC");
    }

    @Override
    public void doHandle(Request request, Response response) {
        if (doesNotRequireAuthentication(request)) {
            accept(request, response);
        } else {
            super.doHandle(request, response);
        }
    }

    private boolean doesNotRequireAuthentication(Request request) {
        Reference ref = request.getResourceRef().getRelativeRef(request.getRootRef());
        log.debug("Checking for guard exceptions against {}", ref);
        return except != null && except.matcher(ref.toString()).matches();
    }

    @Override // largely copied from Guard
    public int authenticate(Request request) {
        int result = 0;

        // An authentication scheme has been defined,
        // the request must be authenticated
        ChallengeResponse cr = request.getChallengeResponse();

        if (cr != null) {
            if (getScheme().equals(cr.getScheme())) {
                // The challenge schemes are compatible
                String identifier = request.getChallengeResponse().getIdentifier();
                char[] secret = request.getChallengeResponse().getSecret();

                // Check the credentials
                if ((identifier != null) && (secret != null)) {
                    Authentication auth = authenticate(identifier, new String(secret));
                    if (auth == null) {
                        result = -1;
                    } else {
                        result = auth.isAuthenticated() ? 1 : -1;
                        request.getAttributes().put(AUTH_TOKEN_ATTRIBUTE_KEY, auth);
                    }
                }
            } else {
                // The challenge schemes are incompatible, we need to
                // challenge the client
            }
        } else {
            // No challenge response found, we need to challenge the client
        }

        return result;
    }

    protected Authentication authenticate(String identifier, String secret) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(identifier, secret);
        try {
            return authenticationManager.authenticate(token);
        } catch (AuthenticationException ae) {
            log.debug("Authentication using injected authentication provider failed", ae);
            return null;
        }
    }

    ////// CONFIGURATION

    public Pattern getExcept() {
        return except;
    }

    public void setExcept(Pattern except) {
        this.except = except;
    }

    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    @Required
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }
}
