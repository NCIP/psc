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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * @author Rhett Sutphin
 */
public class PscUserDetailsServiceImpl implements PscUserDetailsService {
    private UserService userService;
    private PlatformTransactionManager transactionManager;
    private AuthorizationManager authorizationManager;
    private SuiteRoleMembershipLoader suiteRoleMembershipLoader;

    public PscUser loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException, DisabledException {
        User user = loadCsmUser(username);
        return new PscUser(
            user,
            suiteRoleMembershipLoader.getRoleMemberships(user.getUserId()),
            loadLegacyUser(username)
        );
    }

    private User loadCsmUser(String username) {
        User user = authorizationManager.getUser(username);
        if (user == null) throw new UsernameNotFoundException("Unknown user " + username);
        return user;
    }

    private edu.northwestern.bioinformatics.studycalendar.domain.User loadLegacyUser(String userName) {
        // This explicit transaction demarcation shouldn't be necessary
        // However, annotating with @Transactional(readOnly=true) was not stopping hibernate from flushing.
        TransactionStatus transactionStatus = transactionManager.getTransaction(readOnlyTransactionDef());
        try {
            return actuallyLoadUser(userName);
        } finally {
            transactionManager.rollback(transactionStatus);
        }
    }

    private edu.northwestern.bioinformatics.studycalendar.domain.User actuallyLoadUser(String userName) {
        edu.northwestern.bioinformatics.studycalendar.domain.User user =
            userService.getUserByName(userName);
        if (user == null) throw new UsernameNotFoundException("Unknown user " + userName);
        if (!user.getActiveFlag()) throw new DisabledException("User is disabled " +userName);
        return user;
    }

    private DefaultTransactionDefinition readOnlyTransactionDef() {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setReadOnly(true);
        return def;
    }

    ////// CONFIGURATION

    @Required @Deprecated
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Required @Deprecated
    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Required
    public void setAuthorizationManager(AuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
    }

    @Required
    public void setSuiteRoleMembershipLoader(SuiteRoleMembershipLoader suiteRoleMembershipLoader) {
        this.suiteRoleMembershipLoader = suiteRoleMembershipLoader;
    }
}
