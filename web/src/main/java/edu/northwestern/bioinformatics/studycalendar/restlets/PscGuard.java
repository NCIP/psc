package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.web.osgi.InstalledAuthenticationSystem;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.context.SecurityContextImpl;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.engine.security.AuthenticatorHelper;
import org.restlet.security.Guard;
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
 * Authorization is handled in {@link AbstractPscResource}.
 *
 * @author Rhett Sutphin
 */
public class PscGuard extends Guard {
    private final Logger log = LoggerFactory.getLogger(getClass());

    public static final ChallengeScheme PSC_TOKEN
        = new ChallengeScheme("HTTP_psc_token", "psc_token", "Token-based pluggable authentication for PSC");
    public static final String AUTH_TOKEN_ATTRIBUTE_KEY = "pscAuthenticationToken";
    private static final String DIRECT_AUTH_ATTRIBUTE_KEY = "pscDirectlyAuthenticated";

    private Pattern except;
    private InstalledAuthenticationSystem installedAuthenticationSystem;
    private ApplicationSecurityManager applicationSecurityManager;

    public PscGuard() {
        super(null, ChallengeScheme.HTTP_BASIC, "PSC");
    }

    @Override
    public int doHandle(Request request, Response response) {
        if (doesNotRequireAuthentication(request)) {
            accept(request, response);
            return CONTINUE;
        }
        Authentication sessionAuth = SecurityContextHolder.getContext().getAuthentication();
        if (sessionAuth != null && sessionAuth.isAuthenticated()) {
            setCurrentAuthenticationToken(request, sessionAuth);
            accept(request, response);
            return CONTINUE;
        } else {
            try {
                return super.doHandle(request, response);
            } catch (UnimplementedScheme unimplementedScheme) {
                response.setEntity(unimplementedScheme.getMessage(), MediaType.TEXT_PLAIN);
                response.setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
                return STOP;
            }
        }
    }

    private boolean doesNotRequireAuthentication(Request request) {
        Reference ref = request.getResourceRef().getRelativeRef(request.getRootRef());
        log.debug("Checking for guard exceptions against {}", ref);
        return except != null && except.matcher(ref.toString()).matches();
    }

    @Override
    // Mostly copied from Restlet's AuthenticationUtils.authenticate.
    // This is a roundabout implementation (since I'm overriding this
    // method anyway, delegating to an AuthenticationHelper sort of obfuscates
    // things), but once Restlet supports multiple challenge schemes
    // it will be easier to upgrade.
    public int authenticate(Request request) {
        int result = Guard.AUTHENTICATION_MISSING;

        // An authentication scheme has been defined,
        // the request must be authenticated
        ChallengeResponse cr = request.getChallengeResponse();

        if (cr != null) {
            if (this.supportsScheme(cr.getScheme())) {
                AuthenticatorHelper helper = Engine.getInstance()
                        .findHelper(cr.getScheme(), false, true);

                if (helper != null) {
                    result = helper.authenticate(cr, request, this);
                } else {
                    throw new IllegalArgumentException("Challenge scheme "
                            + cr.getScheme()
                            + " not supported by the Restlet engine.");
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

    public boolean supportsScheme(ChallengeScheme scheme) {
        return ChallengeScheme.HTTP_BASIC.equals(scheme)
            || PscGuard.PSC_TOKEN.equals(scheme);
    }

    @Override
    public boolean checkSecret(Request request, String identifier, char[] secret) {
        return authenticate(request, getAuthenticationSystem()
            .createUsernamePasswordAuthenticationRequest(identifier, new String(secret)));
    }

    public boolean checkToken(Request request, String credentials) {
        return authenticate(request, getAuthenticationSystem()
            .createTokenAuthenticationRequest(credentials));
    }

    protected boolean authenticate(Request request, Authentication token) {
        if (token == null) {
            throw new UnimplementedScheme(request.getChallengeResponse().getScheme());
        }

        try {
            Authentication auth = getAuthenticationManager().authenticate(token);
            if (auth == null) {
                return false;
            } else {
                setCurrentAuthenticationToken(request, auth);
                setAcegiSecurityContext(request, auth);
                return auth.isAuthenticated();
            }
        } catch (AuthenticationException ae) {
            log.debug("Authentication using injected authentication provider failed", ae);
            return false;
        }
    }

    private void setAcegiSecurityContext(Request request, Authentication auth) {
        SecurityContextHolder.setContext(new SecurityContextImpl());
        SecurityContextHolder.getContext().setAuthentication(auth);
        request.getAttributes().put(DIRECT_AUTH_ATTRIBUTE_KEY, true);
    }

    @Override
    protected void afterHandle(Request request, Response response) {
        clearAcegiSecurityContext(request);
    }

    private void clearAcegiSecurityContext(Request request) {
        Boolean locallySet = (Boolean) request.getAttributes().get(DIRECT_AUTH_ATTRIBUTE_KEY);
        if (locallySet != null && locallySet) {
            SecurityContextHolder.clearContext();
        }
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

    public Pattern getExcept() {
        return except;
    }

    public void setExcept(Pattern except) {
        this.except = except;
    }

    protected AuthenticationManager getAuthenticationManager() {
        return getAuthenticationSystem().authenticationManager();
    }

    protected AuthenticationSystem getAuthenticationSystem() {
        return installedAuthenticationSystem.getAuthenticationSystem();
    }

    @Required
    public void setInstalledAuthenticationSystem(InstalledAuthenticationSystem installedAuthenticationSystem) {
        this.installedAuthenticationSystem = installedAuthenticationSystem;
    }

    @Required
    public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
        this.applicationSecurityManager = applicationSecurityManager;
    }

    ///// INNER

    private static final class UnimplementedScheme extends StudyCalendarSystemException {
        public UnimplementedScheme(ChallengeScheme scheme) {
            super("%s authentication is not supported with the configured authentication system", scheme.getTechnicalName());
        }
    }
}
