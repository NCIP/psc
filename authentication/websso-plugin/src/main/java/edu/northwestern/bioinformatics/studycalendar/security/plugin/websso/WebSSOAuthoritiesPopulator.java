/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.plugin.websso;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUserDetailsService;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.providers.cas.CasAuthoritiesPopulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Saurabh Agrawal
 * @author Kruttik Aggarwal
 * @author Rhett Sutphin
 */
public class WebSSOAuthoritiesPopulator implements CasAuthoritiesPopulator {
    private final Logger log = LoggerFactory.getLogger(getClass());

    public static final String ATTRIBUTE_DELIMITER = "$";
    public static final String KEY_VALUE_PAIR_DELIMITER = "^";

    private static final Pattern USERNAME_FROM_GRID_IDENTITY = Pattern.compile("CN=([^/]+)");

    private static final String FIRST_NAME_RESPONSE_KEY = "CAGRID_SSO_FIRST_NAME";
    private static final String LAST_NAME_RESPONSE_KEY = "CAGRID_SSO_LAST_NAME";
    private static final String GRID_IDENTITY_RESPONSE_KEY = "CAGRID_SSO_GRID_IDENTITY";
    private static final String DELEGATION_SERVICE_EPR_RESPONSE_KEY = "CAGRID_SSO_DELEGATION_SERVICE_EPR";

    public static final String GRID_FIRST_NAME_USER_ATTRIBUTE = "cagrid.sso.name.first";
    public static final String GRID_LAST_NAME_USER_ATTRIBUTE = "cagrid.sso.name.last";
    public static final String GRID_IDENTITY_USER_ATTRIBUTE = "cagrid.grid-identity";
    public static final String GRID_EPR_XML_USER_ATTRIBUTE = "cagrid.delegated-credential.xml";
    public static final String DELEGATED_CREDENTIAL_USER_ATTRIBUTE = "cagrid.delegated-credential.value";

    private static final Map<String, String> COPY_KEY_ATTRIBUTES_MAP = new MapBuilder<String, String>().
        put(FIRST_NAME_RESPONSE_KEY, GRID_FIRST_NAME_USER_ATTRIBUTE).
        put(LAST_NAME_RESPONSE_KEY, GRID_LAST_NAME_USER_ATTRIBUTE).
        put(GRID_IDENTITY_RESPONSE_KEY, GRID_IDENTITY_USER_ATTRIBUTE).
        put(DELEGATION_SERVICE_EPR_RESPONSE_KEY, GRID_EPR_XML_USER_ATTRIBUTE).
        toMap();

    private PscUserDetailsService pscUserDetailsService;
    private String hostCertificate, hostKey;

    /**
     * Obtains the granted authorities for the specified user.<P>May throw any
     * <code>AuthenticationException</code> or return <code>null</code> if the authorities are unavailable.</p>
     *
     * @param casUserId as obtained from the webSSO CAS validation service
     * @return the details of the indicated user (at minimum the granted authorities and the username)
     */
    public PscUser getUserDetails(String casUserId) throws AuthenticationException {
        Map<String, String> responseAttributes = parseResponse(casUserId);
        String username = extractUsername(responseAttributes, casUserId);

        log.debug("Getting details for user {} from provided user details service", username);
        PscUser user = pscUserDetailsService.loadUserByUsername(username);
        copySimpleAttributes(responseAttributes, user);

        String epr = responseAttributes.get(DELEGATION_SERVICE_EPR_RESPONSE_KEY);
        try {
            user.setAttribute(DELEGATED_CREDENTIAL_USER_ATTRIBUTE,
                createDelegatedCredentialAcquirer(epr).acquire());
        } catch (Exception e) {
            log.error("Could not retrieve user credential from CDS service using reference \n" + epr, e);
            throw new BadCredentialsException("Failed to resolve delegated credential", e);
        }
        return user;
    }

    private Map<String, String> parseResponse(String casUserId) {
        Map<String, String> attributeMap = new HashMap<String, String>();
        StringTokenizer stringTokenizer = new StringTokenizer(casUserId, ATTRIBUTE_DELIMITER);
        while (stringTokenizer.hasMoreTokens()) {
            String attributeKeyValuePair = stringTokenizer.nextToken();
            attributeMap.put(attributeKeyValuePair.substring(0, attributeKeyValuePair.indexOf(KEY_VALUE_PAIR_DELIMITER)),
                    attributeKeyValuePair.substring(attributeKeyValuePair.indexOf(KEY_VALUE_PAIR_DELIMITER) + 1, attributeKeyValuePair.length()));
        }
        return attributeMap;
    }

    private String extractUsername(Map<String, String> responseAttributes, String originalResponse) {
        String gridIdentity = responseAttributes.get(GRID_IDENTITY_RESPONSE_KEY);

        if (gridIdentity == null) {
            log.warn("WebSSO server returned a successful authentication without a grid identity.");
            log.debug("- Response: \"{}\"", originalResponse);
            log.debug("- Parsed: {}", responseAttributes);
            throw new BadCredentialsException("No grid identity in \"" + originalResponse + '"');
        }

        Matcher usernameMatch = USERNAME_FROM_GRID_IDENTITY.matcher(gridIdentity);
        if (usernameMatch.find()) {
            return usernameMatch.group(1);
        } else {
            throw new BadCredentialsException(
                "Unable to extract username from grid identity " + gridIdentity + "; no CN");
        }
    }

    private void copySimpleAttributes(Map<String, String> responseAttributes, PscUser target) {
        for (String responseKey : COPY_KEY_ATTRIBUTES_MAP.keySet()) {
            target.setAttribute(COPY_KEY_ATTRIBUTES_MAP.get(responseKey), responseAttributes.get(responseKey));
        }
    }

    protected DelegatedCredentialAcquirer createDelegatedCredentialAcquirer(String xml) {
        return new DelegatedCredentialAcquirer(xml, hostCertificate, hostKey);
    }

    ////// CONFIGURATION

    @Required
    public void setPscUserDetailsService(PscUserDetailsService pscUserDetailsService) {
        this.pscUserDetailsService = pscUserDetailsService;
    }

    public void setHostCertificate(String hostCertificate) {
        this.hostCertificate = hostCertificate;
    }

    public void setHostKey(String hostKey) {
        this.hostKey = hostKey;
    }
}


