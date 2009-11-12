package edu.northwestern.bioinformatics.studycalendar.security.plugin.cas.direct;

import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
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
            boolean loginSucceeded = executeDirectAuthentication(authentication);
            if (loginSucceeded) {
                String username = getUsername(authentication);
                return new CasDirectUsernamePasswordAuthenticationToken(
                    username, "[REMOVED PASSWORD]",
                    userDetailsService.loadUserByUsername(username).getAuthorities()
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

    protected String getUsername(Authentication authentication) {
        return (String) authentication.getPrincipal();
    }

    protected boolean executeDirectAuthentication(Authentication authentication) throws IOException {
        LoginFormReader form = new LoginFormReader(loginFacade.getForm());
        return loginFacade.postCredentials(new MapBuilder<String, String>().
            put("username", (String) authentication.getPrincipal()).
            put("password", (String) authentication.getCredentials()).
            put("lt", form.getLoginTicket()).
            toMap());
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
