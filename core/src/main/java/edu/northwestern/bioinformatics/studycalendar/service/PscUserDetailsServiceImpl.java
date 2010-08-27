package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUserDetailsService;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembershipLoader;
import gov.nih.nci.security.AuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.User;
import org.acegisecurity.DisabledException;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataAccessException;

/**
 * @author Rhett Sutphin
 */
public class PscUserDetailsServiceImpl implements PscUserDetailsService {
    private AuthorizationManager authorizationManager;
    private SuiteRoleMembershipLoader suiteRoleMembershipLoader;

    public PscUser loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException, DisabledException {
        User user = loadCsmUser(username);
        return new PscUser(
            user,
            suiteRoleMembershipLoader.getRoleMemberships(user.getUserId())
        );
    }

    private User loadCsmUser(String username) {
        User user = authorizationManager.getUser(username);
        if (user == null) throw new UsernameNotFoundException("Unknown user " + username);
        return user;
    }

    ////// CONFIGURATION

    @Required
    public void setAuthorizationManager(AuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
    }

    @Required
    public void setSuiteRoleMembershipLoader(SuiteRoleMembershipLoader suiteRoleMembershipLoader) {
        this.suiteRoleMembershipLoader = suiteRoleMembershipLoader;
    }
}
