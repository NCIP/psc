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
public class PSCAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private UserDetailsService userDetailsService;
    private StudyCalendarAuthenticationManager studyCalendarAuthenticationManager;

    public Authentication authenticate(Authentication authentication) {
      Boolean valid = studyCalendarAuthenticationManager.authenticate(authentication);
        if(valid == false){
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

    public StudyCalendarAuthenticationManager getStudyCalendarAuthenticationManager() {
        return studyCalendarAuthenticationManager;
    }

    public void setStudyCalendarAuthenticationManager(StudyCalendarAuthenticationManager studyCalendarAuthenticationManager) {
        this.studyCalendarAuthenticationManager = studyCalendarAuthenticationManager;
    }
}
