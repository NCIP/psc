package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRoleUse;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUserDetailsService;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.VisibleStudyParameters;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserStudySubjectAssignmentRelationship;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.VisibleAuthorizationInformation;
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
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataAccessException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Rhett Sutphin
 */
public class PscUserService implements PscUserDetailsService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final static Comparator<User> CSM_USER_BY_PSC_USER_ORDER =
        new Comparator<User>() {
            public int compare(User o1, User o2) {
                return createRolelessUser(o1).compareTo(createRolelessUser(o2));
            }
        };

    private AuthorizationManager csmAuthorizationManager;
    private SuiteRoleMembershipLoader suiteRoleMembershipLoader;
    private CsmHelper csmHelper;

    private StudyDao studyDao;
    private SiteDao siteDao;
    private StudySubjectAssignmentDao studySubjectAssignmentDao;
    private StudySiteDao studySiteDao;

    public PscUser getProvisionableUser(String username) {
        User user = loadCsmUser(username);
        if (user == null) return null;
        return createProvisionableUser(user);
    }

    public PscUser loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException, DisabledException {
        PscUser pscUser = getAuthorizableUser(username);
        if (pscUser == null) {
            throw new UsernameNotFoundException("Unknown user " + username);
        } else if (!pscUser.isActive()) {
            throw new LockedException(username + " is not an active account");
        }
        return pscUser;
    }

    public PscUser getAuthorizableUser(String username) {
        User user = loadCsmUser(username);
        if (user == null) return null;
        return createAuthorizableUser(user);
    }

    private User loadCsmUser(String username) {
        return csmAuthorizationManager.getUser(username);
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
            users.add(createRolelessUser(csmUser));
        }
        Collections.sort(users);
        return users;
    }

    /**
     * Returns a list of CSM users whose names match the given text.  Matching is against username
     * and first & last names.  It is case-sensitive due to limitations in CSM.
     * <p>
     * If you want full {@link PscUser}s, pass the result to {#getPscUsers}.
     * <p>
     * If the search text is null, all users are returned.
     */
    @SuppressWarnings({ "unchecked" })
    public List<User> search(String text) {
        List<User> result;
        if (text == null) {
            result = csmAuthorizationManager.getObjects(new UserSearchCriteria(new User()));
        } else {
            Set<User> matches = new LinkedHashSet<User>();
            matches.addAll(searchBy("loginName", text));
            matches.addAll(searchBy("lastName", text));
            matches.addAll(searchBy("firstName", text));
            result = new ArrayList<User>(matches);
        }
        Collections.sort(result, CSM_USER_BY_PSC_USER_ORDER);
        return result;
    }

    @SuppressWarnings({ "unchecked" })
    private List<User> searchBy(String property, String searchText) {
        searchText = '%' + searchText + '%';
        User u = new User();
        {
            BeanWrapper bw = new BeanWrapperImpl(u);
            bw.setPropertyValue(property, searchText);
        }
        return csmAuthorizationManager.getObjects(new UserSearchCriteria(u));
    }

    /**
     * Gets PscUser instances corresponding to the given CSM users.  The CSM users must be real,
     * loaded instances.  (Particularly, their IDs must be set and correct.)
     * <p>
     * This loads the full role membership collection for each user, so beware of using it on
     * long lists of users.
     */
    public List<PscUser> getPscUsers(Collection<User> csmUsers, boolean includePartialMemberships) {
        List<PscUser> users = new ArrayList<PscUser>(csmUsers.size());
        for (User csmUser : csmUsers) {
            users.add(
                includePartialMemberships ?
                    createProvisionableUser(csmUser) :
                    createAuthorizableUser(csmUser)
            );
        }
        return users;
    }

    private PscUser createProvisionableUser(User user) {
        return new PscUser(
            user,
            prepareMemberships(
                suiteRoleMembershipLoader.getProvisioningRoleMemberships(user.getUserId()))
        );
    }

    private PscUser createAuthorizableUser(User user) {
        return new PscUser(
            user,
            prepareMemberships(suiteRoleMembershipLoader.getRoleMemberships(user.getUserId())));
    }

    private static PscUser createRolelessUser(User o1) {
        return new PscUser(o1, Collections.<SuiteRole, SuiteRoleMembership>emptyMap());
    }

    private Map<SuiteRole, SuiteRoleMembership> prepareMemberships(
        Map<SuiteRole, SuiteRoleMembership> input
    ) {
        TreeMap<SuiteRole, SuiteRoleMembership> result =
            new TreeMap<SuiteRole, SuiteRoleMembership>(PscRole.ORDER);
        result.putAll(input);
        return result;
    }

    /**
     * Finds all the users in the system who have the role "collegialRole" and who
     * have overlapping scope in that role with the given user in any of the specified
     * roles.  If no candidate roles are specified, the collegial role is used.
     * <p>
     * For example, say you have these users (simplified by omitting study scope):
     * <ul>
     *   <li><tt>alice</tt>, a Data Reader for NU</li>
     *   <li><tt>betsy</tt>, a SSCM for NU</li>
     *   <li><tt>cally</tt>, a Data Reader for Mayo</li>
     *   <li><tt>darlene</tt>, an SSCM and Data Reader for NU</li>
     * </ul>
     * You would have the following results:
     * <ul>
     *   <li><code>getColleaguesOf(alice, DATA_READER)</code> => <tt>[alice, darlene]</tt></li>
     *   <li><code>getColleaguesOf(alice, SSCM, DATA_READER)</code> => <tt>[betsy, darlene]</tt></li>
     *   <li><code>getColleaguesOf(betsy, DATA_READER)</code> => <tt>[betsy]</tt></li>
     *   <li><code>getColleaguesOf(betsy, SSCM)</code> => <tt>[darlene]</tt></li>
     *   <li><code>getColleaguesOf(betsy, DATA_READER, SSCM)</code> => <tt>[alice, darlene]</tt></li>
     *   <li><code>getColleaguesOf(cally, DATA_READER)</code> => <tt>[cally]</tt></li>
     * </ul>
     */
    public List<PscUser> getColleaguesOf(
        PscUser primaryUser, PscRole collegialRole, PscRole... primaryRoles
    ) {
        if (primaryRoles.length == 0) primaryRoles = new PscRole[] { collegialRole };

        List<SuiteRoleMembership> primaryMemberships =
            new ArrayList<SuiteRoleMembership>(primaryRoles.length);
        for (PscRole primaryRole : primaryRoles) {
            SuiteRoleMembership srm = primaryUser.getMembership(primaryRole);
            if (srm != null) primaryMemberships.add(srm);
        }
        if (primaryMemberships.isEmpty()) return Collections.emptyList();

        Collection<User> csmUsers = getCsmUsers(collegialRole);
        List<PscUser> candidates = getPscUsers(csmUsers, false);
        CANDIDATES: for (Iterator<PscUser> it = candidates.iterator(); it.hasNext();) {
            PscUser candidate = it.next();
            SuiteRoleMembership candidateMembership = candidate.getMembership(collegialRole);
            if (candidateMembership != null) {
                for (SuiteRoleMembership primaryMembership : primaryMemberships) {
                    if (candidateMembership.intersect(primaryMembership) != null) {
                        continue CANDIDATES;
                    }
                }
            }
            it.remove();
        }
        Collections.sort(candidates);

        return candidates;
    }

    /**
     * Finds all the users who have participation memberships that are provisionable
     * by the given study team admin.
     *
     * @see PscRole#valuesProvisionableByStudyTeamAdministrator()
     */
    public List<PscUser> getTeamMembersFor(PscUser studyTeamAdmin) {
        SuiteRoleMembership staMembership =
            studyTeamAdmin.getMembership(PscRole.STUDY_TEAM_ADMINISTRATOR);
        if (staMembership == null) return Collections.emptyList();

        Set<User> csmUsers = new LinkedHashSet<User>();
        for (PscRole role : PscRole.valuesProvisionableByStudyTeamAdministrator()) {
            csmUsers.addAll(getCsmUsers(role));
        }
        List<PscUser> candidates = getPscUsers(csmUsers, true);
        CANDIDATES: for (Iterator<PscUser> it = candidates.iterator(); it.hasNext();) {
            PscUser candidate = it.next();
            for (PscRole spRole : PscRole.valuesProvisionableByStudyTeamAdministrator()) {
                SuiteRoleMembership candidateMembership = candidate.getMembership(spRole);
                if (candidateMembership != null &&
                    candidateMembership.intersect(staMembership) != null) {
                    continue CANDIDATES;
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
        List<StudySubjectAssignment> assignments =
            studySubjectAssignmentDao.getAssignmentsInIntersection(
                getVisibleStudyIds(user, PscRoleUse.SUBJECT_MANAGEMENT.roles()),
                getVisibleSiteIds(user, PscRoleUse.SUBJECT_MANAGEMENT.roles()));
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
    public List<UserStudySubjectAssignmentRelationship> getManagedAssignments(
        PscUser user, PscUser viewer
    ) {
        return getManagedAssignments(user, viewer, false);
    }

    private List<UserStudySubjectAssignmentRelationship> getManagedAssignments(
        PscUser manager, PscUser viewer, boolean includeAll
    ) {
        List<StudySubjectAssignment> managed = studySubjectAssignmentDao.
            getAssignmentsByManagerCsmUserId(manager.getCsmUser().getUserId().intValue());
        List<UserStudySubjectAssignmentRelationship> result =
            new ArrayList<UserStudySubjectAssignmentRelationship>(managed.size());
        for (StudySubjectAssignment assignment : managed) {
            UserStudySubjectAssignmentRelationship managerRel =
                new UserStudySubjectAssignmentRelationship(manager, assignment);
            if (includeAll || managerRel.getCanUpdateSchedule()) {
                UserStudySubjectAssignmentRelationship viewerRel
                    = new UserStudySubjectAssignmentRelationship(viewer, assignment);
                if (includeAll || viewerRel.isVisible()) result.add(viewerRel);
            } else {
                log.warn(
                    "The designated primary manager ({}) for assignment id={} can no longer manage it.",
                    manager.getUsername(), assignment.getId());
            }
        }
        return result;
    }

    /**
     * Returns all the assignments for which the user is the designated manager whether or not the
     * user can still manage (or even see) them.
     */
    public List<UserStudySubjectAssignmentRelationship> getDesignatedManagedAssignments(
        PscUser user
    ) {
        return getManagedAssignments(user, user, true);
    }

    /**
     * Returns the internal IDs for the StudySites which the user can see in the
     * given roles.  If no roles are specified, it will use all the user's site
     * participation roles. If the user has access to all the study sites in
     * the system, it may return null.
     */
    public Collection<Integer> getVisibleStudySiteIds(PscUser user, PscRole... roles) {
        PscRole[] effective = roles.length == 0 ? PscRoleUse.SITE_PARTICIPATION.roles() : roles;
        return studySiteDao.getIntersectionIds(
            getVisibleStudyIds(user, effective), getVisibleSiteIds(user, effective));
    }

    /**
     * Returns the internal IDs for the StudySubjectAssignments which the user
     * can see in the given roles.  If no roles are specified, it will use all
     * the user's roles which can see subject data. If the user has access to
     * all the assignments in the system, it may return null.
     */
    public Collection<Integer> getVisibleAssignmentIds(PscUser user, PscRole... roles) {
        PscRole[] effective = roles.length == 0 ? PscRoleUse.SUBJECT_MANAGEMENT.roles() : roles;
        return studySubjectAssignmentDao.getAssignmentIdsInIntersection(
            getVisibleStudyIds(user, effective), getVisibleSiteIds(user, effective));
    }

    private Collection<Integer> getVisibleStudyIds(PscUser user, PscRole... roles) {
        return studyDao.getVisibleStudyIds(user.getVisibleStudyParameters(roles));
    }

    private Collection<Integer> getVisibleSiteIds(PscUser user, PscRole... roles) {
        return siteDao.getVisibleSiteIds(user.getVisibleSiteParameters(roles));
    }

    /**
     * Returns the authorization roles and scopes which a user is permitted to view for
     * <em>other users</em>.
     */
    // TODO: support STA, too. Also, merge results for users with more than one applicable role.
    @SuppressWarnings({ "unchecked" })
    public VisibleAuthorizationInformation getVisibleAuthorizationInformationFor(PscUser user) {
        VisibleAuthorizationInformation info = new VisibleAuthorizationInformation();

        if (user.getMembership(PscRole.USER_ADMINISTRATOR) != null) {
            SuiteRoleMembership ua = user.getMembership(PscRole.USER_ADMINISTRATOR);
            info.setSites(ua.isAllSites() ? siteDao.getAll() : (List<Site>) ua.getSites());
            VisibleStudyParameters provisionable = new VisibleStudyParameters();
            if (ua.isAllSites()) {
                provisionable.forAllManagingSites().forAllParticipatingSites();
                info.setRoles(Arrays.asList(SuiteRole.values()));
            } else {
                provisionable.forManagingSiteIdentifiers(ua.getSiteIdentifiers()).
                    forParticipatingSiteIdentifiers(ua.getSiteIdentifiers());
                List<SuiteRole> nonGlobal = new ArrayList<SuiteRole>(Arrays.asList(SuiteRole.values()));
                for (Iterator<SuiteRole> it = nonGlobal.iterator(); it.hasNext();) {
                    SuiteRole role = it.next();
                    if (!role.isScoped()) it.remove();
                }
                info.setRoles(nonGlobal);
            }
            info.setStudiesForTemplateManagement(
                studyDao.getVisibleStudiesForTemplateManagement(provisionable));
            info.setStudiesForSiteParticipation(
                studyDao.getVisibleStudiesForSiteParticipation(provisionable));
        } else if (user.getMembership(PscRole.SYSTEM_ADMINISTRATOR) != null) {
            info.setRoles(Arrays.asList(
                SuiteRole.USER_ADMINISTRATOR, SuiteRole.SYSTEM_ADMINISTRATOR));
            info.setSites(Collections.<Site>emptyList());
        }

        return info;
    }

    ////// CONFIGURATION

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

    @Required
    public void setStudySiteDao(StudySiteDao studySiteDao) {
        this.studySiteDao = studySiteDao;
    }
}
