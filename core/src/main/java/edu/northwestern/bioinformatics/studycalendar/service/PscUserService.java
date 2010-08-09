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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

    /**
     * Returns a list of all the users in the system.  For performance reasons, their role
     * memberships are not included.
     */
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

    /**
     * Gets PscUser instances corresponding to the given CSM users.  The CSM users must be real,
     * loaded instances.  (Particularly, their IDs must be set and correct.)
     * <p>
     * This loads the full role membership collection for each user, so beware of using it on
     * long lists of users.
     */
    public List<PscUser> getPscUsers(Collection<User> csmUsers) {
        List<PscUser> users = new ArrayList<PscUser>(csmUsers.size());
        for (User csmUser : csmUsers) {
            users.add(new PscUser(
                csmUser, suiteRoleMembershipLoader.getRoleMemberships(csmUser.getUserId())));
        }
        return users;
    }

    /**
     * Finds all the users in the system who share some role membership scope with the
     * given user in the given role.
     */
    public List<PscUser> getColleaguesOf(PscUser user, PscRole collegialRole) {
        SuiteRoleMembership primaryMembership = user.getMembership(collegialRole);
        if (primaryMembership == null) return Collections.emptyList();

        Collection<User> csmUsers = getCsmUsers(collegialRole);
        List<PscUser> candidates = getPscUsers(csmUsers);
        for (Iterator<PscUser> it = candidates.iterator(); it.hasNext();) {
            PscUser candidate = it.next();
            SuiteRoleMembership candidateMembership = candidate.getMembership(collegialRole);
            if (candidateMembership != null &&
                candidateMembership.intersect(primaryMembership) != null) {
                continue;
            }
            it.remove();
        }
        Collections.sort(candidates);

        return candidates;
    }

    /**
     * Finds all the users who have participation memberships that are manageable
     * by the given study team admin.  "Manageable by" means the memberships are for
     * study-scoped roles and their site scopes overlap with the admin's site scope.
     */
    public List<PscUser> getTeamMembersFor(PscUser studyTeamAdmin) {
        SuiteRoleMembership staMembership =
            studyTeamAdmin.getMembership(PscRole.STUDY_TEAM_ADMINISTRATOR);
        if (staMembership == null) return Collections.emptyList();

        Set<User> csmUsers = new LinkedHashSet<User>();
        for (PscRole role : PscRoleUse.SITE_PARTICIPATION.roles()) {
            if (role.isStudyScoped()) csmUsers.addAll(getCsmUsers(role));
        }
        List<PscUser> candidates = getPscUsers(csmUsers);
        CANDIDATES: for (Iterator<PscUser> it = candidates.iterator(); it.hasNext();) {
            PscUser candidate = it.next();
            for (PscRole spRole : PscRoleUse.SITE_PARTICIPATION.roles()) {
                if (spRole.isStudyScoped()) {
                    SuiteRoleMembership candidateMembership = candidate.getMembership(spRole);
                    if (candidateMembership != null &&
                        candidateMembership.intersect(staMembership) != null) {
                        continue CANDIDATES;
                    }
                }
            }
            it.remove();
        }
        Collections.sort(candidates);
        return candidates;
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
