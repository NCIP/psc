package edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.direct;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Encapsulates uses of the not-very-mockable HTTPClient library for this module.
 *
 * @author Rhett Sutphin
 */
public class DirectLoginHttpFacade {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private HttpClient httpClient;
    private String loginUrl, serviceUrl;

    /**
     * Contacts the CAS server and GETs its login form, returning the content as a string.
     */
    public String getForm() throws IOException {
        log.trace("GETting {} to obtain login ticket", loginUrl);
        GetMethod get = initMethod(new GetMethod(loginUrl));
        get.getParams().setParameter("service", serviceUrl);
        try {
            httpClient.executeMethod(get);
            if (get.getStatusCode() == HttpStatus.SC_OK) {
                return get.getResponseBodyAsString();
            } else {
                throw new CasDirectException("Retrieving the login form %s failed: %s",
                    loginUrl, get.getStatusLine());
            }
        } finally {
            get.releaseConnection();
        }
    }

    /**
     * POSTs the given credentials to the CAS server.  Returns true if the response is a redirect
     * to the requested serviceUrl, false otherwise.
     *
     * @return true when the credentials are accepted, otherwise false
     */
    public boolean postCredentials(
        String username, String password, String loginTicket
    ) throws IOException {
        log.trace("POSTing to {} for direct CAS authentication", loginUrl);

        PostMethod post = initMethod(new PostMethod(loginUrl));
        post.setParameter("username", username);
        post.setParameter("password", password);
        post.setParameter("lt", loginTicket);
        post.setParameter("service", serviceUrl);

        try {
            httpClient.executeMethod(post);
            if (300 <= post.getStatusCode() && post.getStatusCode() <= 399) {
                String redirectedTo = post.getResponseHeader("Location").getValue();
                if (redirectedTo.startsWith(serviceUrl)) {
                    log.trace("- Success");
                    return true;
                } else {
                    log.trace("- Received a redirect to the wrong place: {} but expected {}", redirectedTo, serviceUrl);
                }
            } else {
                log.trace("- auth failed: {}", post.getStatusLine());
            }
        } finally {
            post.releaseConnection();
        }

        return false;
    }

    private <T extends HttpMethodBase> T initMethod(T method) {
        method.setFollowRedirects(false);
        method.setRequestHeader("User-Agent", "PSC-DirectCAS");
        return method;
    }

    ////// CONFIGURATION

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }
}
