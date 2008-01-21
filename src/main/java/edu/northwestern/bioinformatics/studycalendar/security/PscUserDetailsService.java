package edu.northwestern.bioinformatics.studycalendar.security;

import gov.nih.nci.security.acegi.csm.authorization.CSMUserDetailsService;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.acegisecurity.userdetails.User;
import org.acegisecurity.userdetails.UserDetailsService;
import org.springframework.dao.DataAccessException;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;

public class PscUserDetailsService implements UserDetailsService {
    private UserDao userDao;

    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException, DataAccessException {
        return userDao.getByName(userName);
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }
}
