package edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.direct;

import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.AuthenticationServiceException;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.providers.AuthenticationProvider;
import org.acegisecurity.userdetails.UserDetailsService;

import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public class CasDirectAuthenticationProvider implements AuthenticationProvider {
    private DirectLoginHttpFacade loginFacade;
    private UserDetailsService userDetailsService;

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public boolean supports(Class aClass) {
        return CasDirectUsernamePasswordAuthenticationToken.class.isAssignableFrom(aClass);
    }

    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!supports(authentication.getClass())) {
            return null;
        }

        if (authentication.isAuthenticated()) {
            return authentication;
        }

        try {
            LoginFormReader form = new LoginFormReader(loginFacade.getForm());
            boolean loginSucceeded = loginFacade.postCredentials(
                (String) authentication.getPrincipal(),
                (String) authentication.getCredentials(),
                form.getLoginTicket());
            if (loginSucceeded) {
                return new CasDirectUsernamePasswordAuthenticationToken(
                    authentication.getPrincipal(), "[REMOVED PASSWORD]",
                    userDetailsService.loadUserByUsername((String) authentication.getPrincipal()).getAuthorities()
                );
            } else {
                throw new BadCredentialsException(
                    "Credentials are invalid according to direct CAS login");
            }
        } catch (IOException ioe) {
            throw new AuthenticationServiceException("Direct CAS login failed", ioe);
        } catch (CasDirectException cde) {
            throw new AuthenticationServiceException("Direct CAS login failed", cde);
        }
    }

    public void setDirectLoginHttpFacade(DirectLoginHttpFacade directLoginHttpFacade) {
        this.loginFacade = directLoginHttpFacade;
    }

    public DirectLoginHttpFacade getDirectLoginHttpFacade() {
        return loginFacade;
    }

    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
}
