/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.internal;

import edu.northwestern.bioinformatics.studycalendar.security.AuthenticationSystemConfiguration;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.context.SecurityContextImpl;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Processes the "Authorization" header if present and validates the given credentials.
 * If the credentials are valid, the appropriate {@link org.acegisecurity.Authentication} is
 * added to the security context.  Otherwise (i.e., if the credentials are invalid or there's no
 * Authorization header), nothing is done.  If the specific request requires authorization, it is
 * the responsibility of the API implementation (i.e., the Restlet bits) to challenge the user.
 * <p>
 * PSC supports one or two credential types, depending on the selected authentication system:
 * <ul>
 *   <li>HTTP BASIC: the standard username+password mechanism from RFC 1945</li>
 *   <li>psc_token: an opaque token that is verified in an authentication system-specific manner</li>
 * </ul>
 *
 * @author Rhett Sutphin
 */
public class ApiAuthenticationFilter implements Filter {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private AuthenticationSystemConfiguration authenticationSystemConfiguration;

    public void doFilter(
        ServletRequest request, ServletResponse response, FilterChain chain
    ) throws IOException, ServletException {
        String authorizationHeader = ((HttpServletRequest) request).getHeader("Authorization");
        Authentication original = SecurityContextHolder.getContext().getAuthentication();
        if (authorizationHeader == null ||
            processAuthorization(authorizationHeader, (HttpServletResponse) response)) {
            chain.doFilter(request, response);

            if (original == null) {
                SecurityContextHolder.clearContext();
            } else {
                SecurityContextHolder.getContext().setAuthentication(original);
            }
        }
    }

    /**
     * Analyzes the provided authorization header and attempts to authenticate the provided
     * credentials.  As noted in the class description, the filter chain will continue almost all
     * the time, whether the credentials are valid or not.
     *
     * @return should the filter chain continue?
     * @throws IOException
     */
    private boolean processAuthorization(
        String authorizationHeader, HttpServletResponse response
    ) throws IOException {
        SecurityContextHolder.clearContext();
        if (authorizationHeader.startsWith("Basic")) {
            return processBasicAuthorization(authorizationHeader, response);
        } else if (authorizationHeader.startsWith("psc_token")) {
            return processPscTokenAuthorization(authorizationHeader, response);
        } else {
            log.warn("Never-supported authentication scheme presented: {}", authorizationHeader);
            return true;
        }
    }

    private String extractAuthorizationHeaderParameterValue(String authorizationHeader) {
        String[] headerParts = authorizationHeader.split(" ", 2);
        if (headerParts.length > 1) {
            return headerParts[1];
        } else {
            return "";
        }
    }

    private boolean processBasicAuthorization(
        String authorizationHeader, HttpServletResponse servletResponse
    ) throws IOException {
        String encodedCreds = extractAuthorizationHeaderParameterValue(authorizationHeader);
        String decodedCreds = new String(Base64.decodeBase64(encodedCreds.getBytes()));

        int colon = decodedCreds.indexOf(':');
        if (colon < 0) {
            log.warn("Invalidly-formatted basic credentials submitted");
            return true;
        }
        String username = decodedCreds.substring(0, colon);
        String pass = decodedCreds.substring(colon + 1);

        Authentication request =
            getAuthenticationSystem().createUsernamePasswordAuthenticationRequest(username, pass);
        return verifyCredentials(request, servletResponse, "Basic");
    }

    private boolean processPscTokenAuthorization(
        String authorizationHeader, HttpServletResponse response
    ) throws IOException {
        String token = extractAuthorizationHeaderParameterValue(authorizationHeader);
        if (token.length() == 0) {
            return true;
        }

        return verifyCredentials(
            getAuthenticationSystem().createTokenAuthenticationRequest(token), response, "psc_token");
    }

    private boolean verifyCredentials(
        Authentication request, HttpServletResponse servletResponse, String schemeName
    ) throws IOException {
        if (request != null) {
            try {
                Authentication auth =
                    getAuthenticationSystem().authenticationManager().authenticate(request);
                setAuthentic(auth);
            } catch (AuthenticationException e) {
                log.debug("Failure encountered when processing authentication for API; failure deferred to guard.",
                    e);
            }
            return true;
        } else {
            servletResponse.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, String.format(
                "%s credentials are not supported with the configured authentication system",
                schemeName));
            return false;
        }
    }

    private void setAuthentic(Authentication authentication) {
        SecurityContext context = new SecurityContextImpl();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }

    // Uses DI
    public void init(FilterConfig filterConfig) throws ServletException { }
    public void destroy() { }

    ////// CONFIGURATION

    private AuthenticationSystem getAuthenticationSystem() {
        return this.authenticationSystemConfiguration.getAuthenticationSystem();
    }

    public void setAuthenticationSystemConfiguration(AuthenticationSystemConfiguration authenticationSystemConfiguration) {
        this.authenticationSystemConfiguration = authenticationSystemConfiguration;
    }
}
