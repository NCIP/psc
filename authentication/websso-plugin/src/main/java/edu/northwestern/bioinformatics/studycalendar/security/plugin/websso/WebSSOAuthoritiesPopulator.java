package edu.northwestern.bioinformatics.studycalendar.security.plugin.websso;


import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.acegisecurity.AuthenticationException;
import org.acegisecurity.providers.cas.CasAuthoritiesPopulator;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.security.acegi.PscUserDetailsService;


/**
 * @author Saurabh Agrawal
 */
public class WebSSOAuthoritiesPopulator implements CasAuthoritiesPopulator {

    private PscUserDetailsService pscUserDetailsService;
    public static final String ATTRIBUTE_DELIMITER = "$";
    public static final String KEY_VALUE_PAIR_DELIMITER = "^";

    public static final String CCTS_USER_ID_KEY = "CAGRID_SSO_EMAIL_ID";
    public static final String CAGRID_SSO_FIRST_NAME = "CAGRID_SSO_FIRST_NAME";
    public static final String CAGRID_SSO_LAST_NAME = "CAGRID_SSO_LAST_NAME";
    public static final String CAGRID_SSO_GRID_IDENTITY = "CAGRID_SSO_GRID_IDENTITY";
    public static final String CAGRID_SSO_DELEGATION_SERVICE_EPR = "CAGRID_SSO_DELEGATION_SERVICE_EPR";
    public static final String USER_DELEGATED_CREDENTIAL = "USER_DELEGATED_CREDENTIAL";
    
    private String hostCertificate;

    private String hostKey;

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


        //assuming CSM userid is the email address
        User user = pscUserDetailsService.loadUserByUsername(userName);

        user.setGridId(userName);
        
        //Copy the websso attributes to the PSC User as transient properties
        addWebSSOAttributes(user, attributeMap);
        
        // Get the delegated credential and store it in the PSC User object as a transient attribute
        // This will be available later in the Authentication object
        //TODO: Uncomment it when OSGI-fying the grid is tabled
/*        try {
            GlobusCredential hostCredential = new GlobusCredential(hostCertificate, hostKey);
            DelegatedCredentialReference delegatedCredentialReference = (DelegatedCredentialReference) Utils
                            .deserializeObject(
                                            new StringReader(attributeMap
                                                            .get(CAGRID_SSO_DELEGATION_SERVICE_EPR)),
                                            DelegatedCredentialReference.class,
                                            CredentialDelegationServiceClient.class
                                                            .getResourceAsStream("client-config.wsdd"));
            log.debug("host certificate path: "+hostCertificate);
            log.debug("host key path: "+hostKey);
            log.debug("delegatedCredentialReference.toString():"+ delegatedCredentialReference.toString());
            log.debug("delegatedCredentialReference.getEndpointReference().toString():" +delegatedCredentialReference.getEndpointReference().toString());
            log.debug("delegatedCredentialReference.getEndpointReference().getAddress().toString(): "+delegatedCredentialReference.getEndpointReference().getAddress().toString());
            log.debug("delegatedCredentialReference.getEndpointReference().getAddress().getHost(): "+delegatedCredentialReference.getEndpointReference().getAddress().getHost());
            log.debug("delegatedCredentialReference.getEndpointReference().getAddress().getPath(): "+delegatedCredentialReference.getEndpointReference().getAddress().getPath());
            DelegatedCredentialUserClient delegatedCredentialUserClient = new DelegatedCredentialUserClient(
                            delegatedCredentialReference, hostCredential);

            GlobusCredential userCredential = delegatedCredentialUserClient.getDelegatedCredential();
            log.debug("Identitiy: "+userCredential.getIdentity());
            log.debug("Issuer: "+userCredential.getIssuer());
            log.debug("Subject: "+userCredential.getSubject());
            user.addAttribute(USER_DELEGATED_CREDENTIAL, userCredential);
        }
        catch (Exception e) {
            // just log it and move on for now. Discuss if a RuntimeException should be
        	//thrown to stop the application from logging in. 
            log.error("Could not retreive user credential from CDS service", e);
        }*/
        return user;
    }

    @Required
    public void setPscUserDetailsService(PscUserDetailsService pscUserDetailsService) {
		this.pscUserDetailsService = pscUserDetailsService;
	}

    private void addWebSSOAttributes(User user , Map<String, String> map){
    	user.addAttribute(CAGRID_SSO_GRID_IDENTITY, map.get(CAGRID_SSO_GRID_IDENTITY));
    	user.addAttribute(CAGRID_SSO_FIRST_NAME, map.get(CAGRID_SSO_FIRST_NAME));
    	user.addAttribute(CAGRID_SSO_LAST_NAME, map.get(CAGRID_SSO_LAST_NAME));
    	user.addAttribute(CAGRID_SSO_DELEGATION_SERVICE_EPR, map.get(CAGRID_SSO_DELEGATION_SERVICE_EPR));
    }

	public void setHostCertificate(String hostCertificate) {
		this.hostCertificate = hostCertificate;
	}

	public void setHostKey(String hostKey) {
		this.hostKey = hostKey;
	}

}


