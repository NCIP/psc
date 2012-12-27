/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.plugin.websso.direct;

import edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.direct.CasDirectException;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Helps WebSSODirectAuthenticationProvider simulate the entire authentication flow for
 * caGrid WebSSO.
 *
 * @author Rhett Sutphin
 */
public class DirectLoginHttpFacade extends edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.direct.DirectLoginHttpFacade {
    private static final String EVENT_ID_PARAMETER = "_eventId";
    private static final String LOGIN_TICKET_PARAMETER = "lt";
    private static final String CREDENTIAL_PROVIDER_PARAMETER = "dorianName";
    private static final String AUTHENTICATION_SERVICE_URL_PARAMETER = "authenticationServiceURL";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private String lt;
    private String dorianName;
    private String authenticationServiceUrl;

    public DirectLoginHttpFacade(String loginUrl, String serviceUrl) {
        super(loginUrl, serviceUrl);
    }

    /**
     * Retrieve the login form and extract the login ticket.  Step 0.
     */
    public void start() throws IOException {
        this.lt = new LoginFormReader(getForm()).getLoginTicket();
    }

    /**
     * Select the specified credential provider.  This is the first step of the authentication
     * process.
     *
     * @return the page content so that the collaborating code can select an authentication service URL
     * @see LoginFormReader
     */
    public String selectCredentialProvider(String credentialProvider) throws IOException {
        this.dorianName = credentialProvider;
        log.trace("POSTing to {} to obtain authentication service URLs for {}", getLoginUrl(), credentialProvider);

        PostMethod post = createLoginPostMethod();
        post.addParameter(EVENT_ID_PARAMETER, "selectDorian");
        post.addParameter(CREDENTIAL_PROVIDER_PARAMETER, this.dorianName);
        post.addParameter(LOGIN_TICKET_PARAMETER, this.lt);

        return doIntermediatePost(post);
    }

    /**
     * Select the authentication service URL.  Step 2.
     */
    public void selectAuthenticationService(String url) throws IOException {
        this.authenticationServiceUrl = url;
        log.trace("POSTing to {} to select authentication service {}", getLoginUrl(), url);

        PostMethod post = createLoginPostMethod();
        post.addParameter(EVENT_ID_PARAMETER, "selectAuthenticationService");
        post.addParameter(CREDENTIAL_PROVIDER_PARAMETER, this.dorianName);
        post.addParameter(LOGIN_TICKET_PARAMETER, this.lt);
        post.addParameter(AUTHENTICATION_SERVICE_URL_PARAMETER, this.authenticationServiceUrl);

        LoginFormReader form = new LoginFormReader(doIntermediatePost(post));

        if (!form.hasUsernameAndPasswordFields()) {
            log.debug(
                "After selecting the authentication system, there are no username & password fields on the page.  Cannot proceed.");
            throw new CasDirectException(
                "After selecting the authentication system, there are no username & password fields on the page.  Cannot proceed.");
        }
    }

    /**
     * Complete authentication by submitting a username and password.  Last step.
     */
    public boolean postCredentials(String username, String password) throws IOException {
        log.trace("POSTing to {} to log in", getLoginUrl());

        return postCredentials(new MapBuilder<String, String>().
            put(EVENT_ID_PARAMETER, "submit").
            put(CREDENTIAL_PROVIDER_PARAMETER, this.dorianName).
            put(LOGIN_TICKET_PARAMETER, this.lt).
            put(AUTHENTICATION_SERVICE_URL_PARAMETER, this.authenticationServiceUrl).
            put("username", username).
            put("password", password).
            // It doesn't seem like this should be needed -- when running in the browser, it
            // is not submitted.  It won't work without it, though.
            put("authenticationServiceProfile", "BasicAuthentication").
            toMap());
    }

    /**
     * Performs a mid-process post and updates the LT from the new response.
     */
    private String doIntermediatePost(PostMethod post) throws IOException {
        try {
            getHttpClient().executeMethod(post);
            if (post.getStatusCode() == HttpStatus.SC_OK) {
                String body = post.getResponseBodyAsString();
                this.lt = new LoginFormReader(body).getLoginTicket();
                return body;
            } else {
                throw new CasDirectException("Retrieving the login form %s failed: %s",
                    getLoginUrl(), post.getStatusLine());
            }
        } finally {
            post.releaseConnection();
        }
    }
}
