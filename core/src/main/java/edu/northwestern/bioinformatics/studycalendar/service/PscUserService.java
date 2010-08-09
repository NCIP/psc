package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.LegacyModeSwitch;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRoleUse;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUserDetailsService;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserStudySubjectAssignmentRelationship;
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

    private StudyDao studyDao;
    private SiteDao siteDao;
    private StudySubjectAssignmentDao studySubjectAssignmentDao;

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

    ////// VISIBLE DOMAIN INSTANCES

    /**
     * Get all the assignments the user can see.
     */
    public List<UserStudySubjectAssignmentRelationship> getVisibleAssignments(PscUser user) {
        Collection<Integer> sites = siteDao.getVisibleSiteIds(
            user.getVisibleSiteParameters(PscRoleUse.SUBJECT_MANAGEMENT.roles()));
        Collection<Integer> studies = studyDao.getVisibleStudyIds(
            user.getVisibleStudyParameters(PscRoleUse.SUBJECT_MANAGEMENT.roles()));
        List<StudySubjectAssignment> assignments =
            studySubjectAssignmentDao.getAssignmentsInIntersection(sites, studies);
        List<UserStudySubjectAssignmentRelationship> result =
            new ArrayList<UserStudySubjectAssignmentRelationship>(assignments.size());
        for (StudySubjectAssignment assignment : assignments) {
            UserStudySubjectAssignmentRelationship rel = new UserStudySubjectAssignmentRelationship(user, assignment);
            // this condition is a belt+suspenders thing
            if (rel.isVisible()) result.add(rel);
        }
        return result;
    }

    /**
     * Returns all the assignments for which the user is the designated manager and which the user
     * can still manage.
     */
    public List<UserStudySubjectAssignmentRelationship> getManagedAssignments(PscUser user) {
        List<StudySubjectAssignment> managed = studySubjectAssignmentDao.
            getAssignmentsByManagerCsmUserId(user.getCsmUser().getUserId().intValue());
        List<UserStudySubjectAssignmentRelationship> result =
            new ArrayList<UserStudySubjectAssignmentRelationship>(managed.size());
        for (StudySubjectAssignment assignment : managed) {
            UserStudySubjectAssignmentRelationship rel =
                new UserStudySubjectAssignmentRelationship(user, assignment);
            if (rel.getCanUpdateSchedule()) {
                result.add(rel);
            } else {
                log.warn(
                    "The designated primary manager ({}) for assignment id={} can no longer manage it.",
                    user.getUsername(), assignment.getId());
            }
        }
        return result;
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

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }

    @Required
    public void setStudySubjectAssignmentDao(StudySubjectAssignmentDao studySubjectAssignmentDao) {
        this.studySubjectAssignmentDao = studySubjectAssignmentDao;
    }
}
