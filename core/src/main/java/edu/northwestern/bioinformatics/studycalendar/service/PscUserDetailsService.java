package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.domain.User;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.springframework.dao.DataAccessException;
import org.acegisecurity.DisabledException;

/**
 * Implementation of Acegi's {@UserDetailsService} for PSC.
 */
public class PscUserDetailsService implements UserDetailsService {
    private UserService userService;

    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException, DataAccessException, DisabledException {
        User user = userService.getUserByName(userName);
        if (user == null) throw new UsernameNotFoundException("Unknown user " + userName);
        if (!user.getActiveFlag()) throw new DisabledException("User is disabled " +userName);
        return user;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
