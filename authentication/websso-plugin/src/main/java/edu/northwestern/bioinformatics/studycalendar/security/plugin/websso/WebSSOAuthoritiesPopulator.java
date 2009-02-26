package edu.northwestern.bioinformatics.studycalendar.security.plugin.websso;


import org.acegisecurity.AuthenticationException;
import org.acegisecurity.providers.cas.CasAuthoritiesPopulator;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;


/**
 * @author Saurabh Agrawal
 */
public class WebSSOAuthoritiesPopulator implements CasAuthoritiesPopulator {

    private UserDetailsService userDetailsService;
    public static final String ATTRIBUTE_DELIMITER = "$";
    public static final String KEY_VALUE_PAIR_DELIMITER = "^";

    public static final String CCTS_USER_ID_KEY = "CAGRID_SSO_EMAIL_ID";
    public static final String CAGRID_SSO_FIRST_NAME = "CAGRID_SSO_FIRST_NAME";
    public static final String CAGRID_SSO_LAST_NAME = "CAGRID_SSO_LAST_NAME";
    public static final String CAGRID_SSO_GRID_IDENTITY = "CAGRID_SSO_GRID_IDENTITY";

    private final Logger log = LoggerFactory.getLogger(getClass());


    /**
     * Obtains the granted authorities for the specified user.<P>May throw any
     * <code>AuthenticationException</code> or return <code>null</code> if the authorities are unavailable.</p>
     *
     * @param casUserId as obtained from the webSSO CAS validation service
     * @return the details of the indicated user (at minimum the granted authorities and the username)
     * @throws org.acegisecurity.AuthenticationException
     *          DOCUMENT ME!
     */
    public UserDetails getUserDetails(String casUserId) throws AuthenticationException {

        Map<String, String> attributeMap = new HashMap<String, String>();
        StringTokenizer stringTokenizer = new StringTokenizer(casUserId, ATTRIBUTE_DELIMITER);
        while (stringTokenizer.hasMoreTokens()) {
            String attributeKeyValuePair = stringTokenizer.nextToken();
            attributeMap.put(attributeKeyValuePair.substring(0, attributeKeyValuePair.indexOf(KEY_VALUE_PAIR_DELIMITER)),
                    attributeKeyValuePair.substring(attributeKeyValuePair.indexOf(KEY_VALUE_PAIR_DELIMITER) + 1, attributeKeyValuePair.length()));
        }


        String gridIdentity = attributeMap.get(CAGRID_SSO_GRID_IDENTITY);

        String userName = "";
        if (gridIdentity != null) {
            userName = gridIdentity.substring(gridIdentity.indexOf("/CN=") + 4, gridIdentity.length());
        } else {
            log.error(CAGRID_SSO_GRID_IDENTITY + " is null");
        }
        log.debug("Getting details for user with userId:" + userName + " from provided user details service");


        //        //assuming CSM userid is the email address
        //        WebSSOUser user = new WebSSOUser(userDetailsService.loadUserByUsername(attributeMap.get(CCTS_USER_ID_KEY)));
        WebSSOUser user = new WebSSOUser(userDetailsService.loadUserByUsername(userName));


        user.setGridId(userName);
        user.setFirstName(attributeMap.get(CAGRID_SSO_FIRST_NAME));
        user.setLastName(attributeMap.get(CAGRID_SSO_LAST_NAME));

        return user;
    }

    @Required
    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }


}


