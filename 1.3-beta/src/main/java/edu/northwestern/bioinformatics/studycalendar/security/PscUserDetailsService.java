package edu.northwestern.bioinformatics.studycalendar.security;

import gov.nih.nci.security.acegi.csm.authorization.CSMUserDetailsService;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.acegisecurity.userdetails.User;
import org.springframework.dao.DataAccessException;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;

public class PscUserDetailsService extends CSMUserDetailsService {
    private UserDao userDao;

    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException, DataAccessException {
        UserDetails userDetails =  super.loadUserByUsername(userName);

        edu.northwestern.bioinformatics.studycalendar.domain.User pscUser = userDao.getByName(userName);

        if (pscUser != null) {
            userDetails =
                    new User(userDetails.getUsername(), userDetails.getPassword(), pscUser.getActiveFlag(),
                            userDetails.isAccountNonExpired(), userDetails.isCredentialsNonExpired(),
                            pscUser.getActiveFlag(), userDetails.getAuthorities());
        }

        return userDetails;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }
}
