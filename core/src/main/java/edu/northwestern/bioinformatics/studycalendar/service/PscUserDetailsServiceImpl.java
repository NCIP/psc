package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.security.acegi.PscUserDetailsService;
import org.acegisecurity.DisabledException;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of Acegi's {@UserDetailsService} for PSC.
 */
@Transactional(readOnly = true)
public class PscUserDetailsServiceImpl implements PscUserDetailsService {
    private UserService userService;

    public User loadUserByUsername(String userName) throws UsernameNotFoundException, DataAccessException, DisabledException {
        User user = userService.getUserByName(userName);
        if (user == null) throw new UsernameNotFoundException("Unknown user " + userName);
        if (!user.getActiveFlag()) throw new DisabledException("User is disabled " +userName);
        return user;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
