/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.plugin.websso.direct;

import edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.direct.CasDirectAuthenticationProvider;
import org.acegisecurity.Authentication;
import org.acegisecurity.BadCredentialsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public class WebSSODirectAuthenticationProvider extends CasDirectAuthenticationProvider {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    protected boolean executeDirectAuthentication(
        edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.direct.DirectLoginHttpFacade baseHttp, Authentication authentication
    ) throws IOException {
        DirectLoginHttpFacade http = (DirectLoginHttpFacade) baseHttp;
        QualifiedUsername user = new QualifiedUsername(authentication);
        log.debug("Attempting direct login for {}", user);

        http.start();
        LoginFormReader reader = new LoginFormReader(
            http.selectCredentialProvider(user.getCredentialProvider()));
        String authenticationServiceUrl =
            reader.getAuthenticationServices().get(user.getAuthenticationServiceName());
        if (authenticationServiceUrl == null) {
            log.debug("No authentication system named {} found.  Found: {}",
                user.getAuthenticationServiceName(), reader.getAuthenticationServices());
            throw new BadCredentialsException(String.format(
                "The WebSSO server does not know of an authentication service named \"%s\"",
                user.getAuthenticationServiceName()));
        }
        http.selectAuthenticationService(authenticationServiceUrl);
        return http.postCredentials(user.getUsername(), (String) authentication.getCredentials());
    }

    @Override
    protected DirectLoginHttpFacade createLoginFacade() {
        return new DirectLoginHttpFacade(getLoginUrl(), getServiceUrl());
    }

    @Override
    protected String getUsername(Authentication authentication) {
        return new QualifiedUsername(authentication).getUsername();
    }

    private static class QualifiedUsername {
        private String credentialProvider, authenticationServiceName, username;

        private QualifiedUsername(Authentication authentication) {
            String src = (String) authentication.getPrincipal();
            String[] bits = src.split("\\\\");
            if (bits.length != 3) {
                throw new BadCredentialsException(
                    "The principal must be of the form credential-provider\\authentication-service-name\\username");
            }
            credentialProvider = bits[0];
            authenticationServiceName = bits[1];
            username = bits[2];
        }

        public String getCredentialProvider() {
            return credentialProvider;
        }

        public String getAuthenticationServiceName() {
            return authenticationServiceName;
        }

        public String getUsername() {
            return username;
        }

        @Override
        public String toString() {
            return new StringBuilder(getClass().getSimpleName()).
                append("[credentialProvider=").append(getCredentialProvider()).
                append("; authenticationServiceName=").append(getAuthenticationServiceName()).
                append("; username=").append(getUsername()).
                append(']').toString();
        }
    }
}
