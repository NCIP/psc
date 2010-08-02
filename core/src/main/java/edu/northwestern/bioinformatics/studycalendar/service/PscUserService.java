package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.LegacyModeSwitch;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUserDetailsService;
import gov.nih.nci.cabig.ctms.suite.authorization.CsmHelper;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembershipLoader;
import gov.nih.nci.security.AuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.Group;
import gov.nih.nci.security.authorization.domainobjects.User;
import gov.nih.nci.security.dao.UserSearchCriteria;
import gov.nih.nci.security.exceptions.CSObjectNotFoundException;
import org.acegisecurity.DisabledException;
import org.acegisecurity.LockedException;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class PscUserService implements PscUserDetailsService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private UserService userService;
    private PlatformTransactionManager transactionManager;
    private AuthorizationManager csmAuthorizationManager;
    private SuiteRoleMembershipLoader suiteRoleMembershipLoader;
    private LegacyModeSwitch legacyModeSwitch;
    private CsmHelper csmHelper;

    public PscUser getProvisionableUser(String username) {
        User user = loadCsmUser(username);
        if (user == null) return null;
        return new PscUser(
            user, suiteRoleMembershipLoader.getProvisioningRoleMemberships(user.getUserId()),
            legacyModeSwitch.isOn() ? loadLegacyUser(username) : null
        );
    }

    public PscUser loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException, DisabledException {
        User user = loadCsmUser(username);
        if (user == null) throw new UsernameNotFoundException("Unknown user " + username);
        PscUser pscUser = new PscUser(
            user,
            suiteRoleMembershipLoader.getRoleMemberships(user.getUserId()),
            legacyModeSwitch.isOn() ? loadLegacyUser(username) : null
        );
        if (!pscUser.isActive()) {
            throw new LockedException(username + " is not an active account");
        }
        return pscUser;
    }

    private User loadCsmUser(String username) {
        return csmAuthorizationManager.getUser(username);
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

    /**
     * Returns all the users have been designated to have the given role.
     * Be careful: for performance reasons, this method does not filter out scoped users
     * who have only been partially provisioned.
     */
    @SuppressWarnings({ "unchecked" })
    public Collection<User> getCsmUsers(PscRole role) {
        try {
            Group roleGroup = csmHelper.getRoleCsmGroup(role.getSuiteRole());
            return csmAuthorizationManager.getUsers(roleGroup.getGroupId().toString());
        } catch (CSObjectNotFoundException e) {
            log.debug("CSM could not find the group on second load while resolving users for {}", role);
            return Collections.emptySet();
        }
    }

    @SuppressWarnings({"unchecked"})
    public List<PscUser> getAllUsers() {
        List<User> allCsmUsers = csmAuthorizationManager.getObjects(new UserSearchCriteria(new User()));
        List<PscUser> users = new ArrayList<PscUser>(allCsmUsers.size());
        for (User csmUser : allCsmUsers) {
            users.add(new PscUser(csmUser, Collections.<SuiteRole, SuiteRoleMembership>emptyMap()));
        }
        Collections.sort(users);
        return users;
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
    public void setCsmAuthorizationManager(AuthorizationManager authorizationManager) {
        this.csmAuthorizationManager = authorizationManager;
    }

    @Required
    public void setCsmHelper(CsmHelper csmHelper) {
        this.csmHelper = csmHelper;
    }

    @Required
    public void setSuiteRoleMembershipLoader(SuiteRoleMembershipLoader suiteRoleMembershipLoader) {
        this.suiteRoleMembershipLoader = suiteRoleMembershipLoader;
    }

    @Required @Deprecated
    public void setLegacyModeSwitch(LegacyModeSwitch lmSwitch) {
        this.legacyModeSwitch = lmSwitch;
    }
}
