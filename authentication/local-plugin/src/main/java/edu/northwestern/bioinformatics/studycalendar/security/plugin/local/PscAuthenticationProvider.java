/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.plugin.local;

import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.Authentication;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.providers.dao.AbstractUserDetailsAuthenticationProvider;

/**
 * @author Jalpa Patel
 */
public class PscAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private UserDetailsService userDetailsService;
    private PscAuthenticationHelper pscAuthenticationHelper;

    public Authentication authenticate(Authentication authentication) {
      Boolean valid = pscAuthenticationHelper.authenticate(authentication);
        if(!valid){
            throw new BadCredentialsException("Invalid username or password");
        }
        UserDetails user = userDetailsService.loadUserByUsername(authentication.getPrincipal().toString());
        return new UsernamePasswordAuthenticationToken(user, authentication.getCredentials(), user.getAuthorities());
    }

    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {
    }

    protected UserDetails retrieveUser(String userName, UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken) throws AuthenticationException {
        return userDetailsService.loadUserByUsername(userName);
    }

    public UserDetailsService getUserDetailsService() {
        return userDetailsService;
    }

    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public PscAuthenticationHelper getPscAuthenticationHelper() {
        return pscAuthenticationHelper;
    }

    public void setPscAuthenticationHelper(PscAuthenticationHelper pscAuthenticationHelper) {
        this.pscAuthenticationHelper = pscAuthenticationHelper;
    }
}
