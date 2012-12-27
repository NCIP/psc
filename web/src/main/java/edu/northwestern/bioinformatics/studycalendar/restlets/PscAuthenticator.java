/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.security.Authenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * This restlet filter collaborates with the {@link edu.northwestern.bioinformatics.studycalendar.security.internal.ApiAuthenticationFilter}
 * servlet filter to handle authentication for the API.  The division of labor is this:
 * <ul>
 *   <li><code>ApiAuthenticationFilter</code>: parses the HTTP Authorization header, verifies the
 *       presented credentials using the configured Acegi {@link org.acegisecurity.AuthenticationManager},
 *       and sets the record of authentication in the Acegi {@link org.acegisecurity.context.SecurityContext}.
 *    <li><code>PscAuthenticator</code> (this class): determines if a particular request requires
 *        authentication, checks the security context to see if the user is authenticated, sends
 *        HTTP auth challenges if not.
 * </ul>
 * If the user is authenticated, it also puts the Acegi authentication token as a request attribute
 * under the key {@link #AUTH_TOKEN_ATTRIBUTE_KEY}.
 * <p>
 * Authorization is handled in {@link AbstractPscResource}.
 *
 * @author Rhett Sutphin
 */
public class PscAuthenticator extends Authenticator {
    private final Logger log = LoggerFactory.getLogger(getClass());

    public static final String AUTH_TOKEN_ATTRIBUTE_KEY = "pscAuthenticationToken";
    public static final ChallengeScheme HTTP_PSC_TOKEN =
        new ChallengeScheme("HTTP_psc_token", "psc_token",
            "Generic token-based authentication for PSC's pluggable authentication systems");
    private static final String PSC_REALM = "PSC";

    private Pattern except;

    public PscAuthenticator() {
        super(null);
    }

    @Override
    protected boolean authenticate(Request request, Response response) {
        if (doesNotRequireAuthentication(request)) {
            return true;
        }
        Authentication auth = getAcegiAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            challenge(response);
            return false;
        } else {
            setCurrentAuthenticationToken(request, auth);
            return true;
        }
    }

    private boolean doesNotRequireAuthentication(Request request) {
        Reference ref = request.getResourceRef().getRelativeRef(request.getRootRef());
        log.debug("Checking for guard exceptions against {}", ref);
        return except != null && except.matcher(ref.toString()).matches();
    }

    private Authentication getAcegiAuthentication() {
        SecurityContext context = SecurityContextHolder.getContext();
        return context == null ? null : context.getAuthentication();
    }

    private void challenge(Response response) {
        response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        response.getChallengeRequests().
            add(new ChallengeRequest(ChallengeScheme.HTTP_BASIC, PSC_REALM));
        response.getChallengeRequests().
            add(new ChallengeRequest(HTTP_PSC_TOKEN, PSC_REALM));
    }

    public static void setCurrentAuthenticationToken(Request request, Authentication auth) {
        if (auth == null) {
            request.getAttributes().remove(AUTH_TOKEN_ATTRIBUTE_KEY);
        } else {
            request.getAttributes().put(AUTH_TOKEN_ATTRIBUTE_KEY, auth);
        }
    }

    public static Authentication getCurrentAuthenticationToken(Request request) {
        return (Authentication) request.getAttributes().get(AUTH_TOKEN_ATTRIBUTE_KEY);
    }

    ////// CONFIGURATION

    public void setExcept(Pattern except) {
        this.except = except;
    }
}
