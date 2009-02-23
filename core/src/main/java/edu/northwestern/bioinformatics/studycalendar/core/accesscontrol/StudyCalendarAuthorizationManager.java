package edu.northwestern.bioinformatics.studycalendar.core.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import gov.nih.nci.security.UserProvisioningManager;
import gov.nih.nci.security.authorization.domainobjects.Group;
import gov.nih.nci.security.authorization.domainobjects.ProtectionElement;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroupRoleContext;
import gov.nih.nci.security.authorization.domainobjects.Role;
import gov.nih.nci.security.authorization.domainobjects.User;
import gov.nih.nci.security.dao.GroupSearchCriteria;
import gov.nih.nci.security.dao.ProtectionGroupSearchCriteria;
import gov.nih.nci.security.dao.RoleSearchCriteria;
import gov.nih.nci.security.dao.SearchCriteria;
import gov.nih.nci.security.dao.UserSearchCriteria;
import gov.nih.nci.security.exceptions.CSObjectNotFoundException;
import gov.nih.nci.security.exceptions.CSTransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Facade which provides PSC-specific access to CSM.  No business logic should be included
 * here.
 *
 * @author Padmaja Vedula
 * @author Rhett Sutphin
 * @see edu.northwestern.bioinformatics.studycalendar.service.AuthorizationService
 */

// TODO: None of these methods should throw checked exceptions

public class StudyCalendarAuthorizationManager implements Serializable {
    public static final String APPLICATION_CONTEXT_NAME = "study_calendar";
    public static final String ASSIGNED_USERS = "ASSIGNED_USERS";
    public static final String AVAILABLE_USERS = "AVAILABLE_USERS";
    public static final String ASSIGNED_PGS = "ASSIGNED_PGS";
    public static final String AVAILABLE_PGS = "AVAILABLE_PGS";
    public static final String ASSIGNED_PES = "ASSIGNED_PES";
    public static final String AVAILABLE_PES = "AVAILABLE_PES";
    public static final String SUBJECT_COORDINATOR_GROUP = "SUBJECT_COORDINATOR";
    public static final String SITE_COORDINATOR_GROUP = "SITE_COORDINATOR";
    
    private static Logger log = LoggerFactory.getLogger(StudyCalendarAuthorizationManager.class);

    private UserProvisioningManager userProvisioningManager;

    public User getUserObject(String id) throws Exception {
        return userProvisioningManager.getUserById(id);
    }

    public void createProtectionGroup(String newProtectionGroup) {
        try {
            ProtectionGroup requiredProtectionGroup = new ProtectionGroup();
            requiredProtectionGroup.setProtectionGroupName(newProtectionGroup);
            userProvisioningManager.createProtectionGroup(requiredProtectionGroup);
        } catch (CSTransactionException e) {
            throw new StudyCalendarSystemException("Creating PG failed", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("new protection group created " + newProtectionGroup);
        }
    }
    
    /**
     * Method to retrieve all site protection groups
     * 
     */
    @SuppressWarnings({ "unchecked" })
    public List<ProtectionGroup> getSites() throws Exception {
        List<ProtectionGroup> siteList = new ArrayList<ProtectionGroup>() ;
        ProtectionGroup protectionGroup = new ProtectionGroup();
        SearchCriteria pgSearchCriteria = new ProtectionGroupSearchCriteria(protectionGroup);
        List<ProtectionGroup> pgList = userProvisioningManager.getObjects(pgSearchCriteria);

        if (pgList.size() > 0) {
            for (ProtectionGroup requiredProtectionGroup : pgList) {
                if (isSitePG(requiredProtectionGroup)) {
                    siteList.add(requiredProtectionGroup);
                }
            }
        }

        return siteList;
    }
    
    /**
     * Method to retrieve a site protection group
     * @param name
     * @return null or site Protection Group
     */
    @SuppressWarnings({ "unchecked" })
    public ProtectionGroup getPGByName(String name) {
        ProtectionGroup requiredProtectionGroup = null;

        ProtectionGroup protectionGroupSearch = new ProtectionGroup();
        protectionGroupSearch.setProtectionGroupName(name);
        SearchCriteria protectionGroupSearchCriteria = new ProtectionGroupSearchCriteria(protectionGroupSearch);
        List<ProtectionGroup> protectionGroupList = userProvisioningManager.getObjects(protectionGroupSearchCriteria);

        if (protectionGroupList.size() > 0) {
            requiredProtectionGroup = protectionGroupList.get(0);

        }
        return requiredProtectionGroup;
    }

    @SuppressWarnings({ "unchecked" })
    public List<User> getUsersForGroup(String groupName) {
        List<User> usersForRequiredGroup = new ArrayList<User>();
        User user = new User();
        SearchCriteria userSearchCriteria = new UserSearchCriteria(user);
        List<User> userList = userProvisioningManager.getObjects(userSearchCriteria);
        for (User requiredUser : userList) {
            Set<Group> userGroups = getGroups(requiredUser);
            for (Group userGroup : userGroups) {
                if (userGroup.getGroupName().equals(groupName)) {
                    usersForRequiredGroup.add(requiredUser);
                    break;
                }
            }
        }
        return usersForRequiredGroup;
    }

    public void assignProtectionGroupsToUsers(String userId, ProtectionGroup protectionGroup, String roleName) throws Exception {
        assignProtectionGroupsToUsers(asList(userId), protectionGroup, roleName);
    }

    @SuppressWarnings({ "unchecked" })
    public void assignProtectionGroupsToUsers(List<String> userIds, ProtectionGroup protectionGroup, String roleName) throws Exception {
        if (protectionGroup == null) return;
        
        Role role = new Role();
        role.setName(roleName);
        SearchCriteria roleSearchCriteria = new RoleSearchCriteria(role);
        List<Role> roleList = userProvisioningManager.getObjects(roleSearchCriteria);
        if (roleList.size() > 0) {
            Role accessRole = roleList.get(0);
            String[] roleIds = new String[] {accessRole.getId().toString()};

            for (String userId : userIds) {
                userProvisioningManager.assignUserRoleToProtectionGroup(userId, roleIds, protectionGroup.getProtectionGroupId().toString());
            }
        }
    }

    @SuppressWarnings({ "unchecked" })
    public void assignProtectionGroupsToUsers(List<String> userIds, ProtectionGroup protectionGroup, String[] roleNames) throws Exception {
        if (protectionGroup == null) return;

        List<Role> roleList = new ArrayList<Role>();
        for (String roleStr : roleNames ) {
            Role searchRole = new Role();
            searchRole.setName(roleStr);
            SearchCriteria roleSearchCriteria = new RoleSearchCriteria(searchRole);
            roleList.addAll(userProvisioningManager.getObjects(roleSearchCriteria));
        }

        if (roleList.size() > 0) {
            String[] roleIds = new String[roleList.size()];
            Iterator<Role> role = roleList.iterator();
            for (int i = 0; i < roleIds.length; i++) {
                roleIds[i] = role.next().getId().toString();
            }

            for (String userId : userIds) {
                userProvisioningManager.assignUserRoleToProtectionGroup(userId, roleIds, protectionGroup.getProtectionGroupId().toString());
            }
        }
    }
    
    public void removeProtectionGroupUsers(List<String> userIds, ProtectionGroup protectionGroup) throws Exception
    {
        if (protectionGroup == null) return;

        if (!((userIds.size() == 1) && (userIds.get(0).length() == 0))) {
            for (String userId : userIds) {
                userProvisioningManager.removeUserFromProtectionGroup(protectionGroup.getProtectionGroupId().toString(), userId);
            }
        }
    }

    public void registerUrl(String url, List<String> protectionGroups) {
        if (log.isDebugEnabled()) log.debug("Attempting to register PE for " + url + " in " + protectionGroups);

        ProtectionElement element = getOrCreateProtectionElement(url);

        syncProtectionGroups(element, protectionGroups);
    }

    private ProtectionElement getOrCreateProtectionElement(String objectId) {
        ProtectionElement element = null;
        try {
            element = userProvisioningManager.getProtectionElement(objectId);
            log.debug("PE for " + objectId + " found");
        } catch (CSObjectNotFoundException e) {
            log.debug("PE for " + objectId + " not found");
            // continue
        }
        if (element == null) {
            element = new ProtectionElement();
            element.setObjectId(objectId);
            element.setProtectionElementName(objectId);
            element.setProtectionElementDescription("Autogenerated PE for " + objectId);
            try {
                userProvisioningManager.createProtectionElement(element);
            } catch (CSTransactionException e) {
                throw new StudyCalendarSystemException("Creating PE for " + objectId + " failed", e);
            }
            try {
                element = userProvisioningManager.getProtectionElement(element.getObjectId());
            } catch (CSObjectNotFoundException e) {
                throw new StudyCalendarSystemException("Reloading just-created PE for " + element.getObjectId() + " failed", e);
            }
        }
        return element;
    }

    @SuppressWarnings({ "unchecked" })
    private void syncProtectionGroups(ProtectionElement element, List<String> desiredProtectionGroups) {
        Set<ProtectionGroup> existingGroups;
        try {
            existingGroups = userProvisioningManager.getProtectionGroups(element.getProtectionElementId().toString());
        } catch (CSObjectNotFoundException e) {
            throw new StudyCalendarError("Could not find groups for just-created/loaded PE", e);
        }
        // if they're all the same, we don't need to do anything
        if (existingGroups.size() == desiredProtectionGroups.size()) {
            List<String> existingNames = new ArrayList<String>(existingGroups.size());
            for (ProtectionGroup existingGroup : existingGroups) existingNames.add(existingGroup.getProtectionGroupName());
            if (log.isDebugEnabled()) log.debug(element.getObjectId() + " currently in " + desiredProtectionGroups);
            if (existingNames.containsAll(desiredProtectionGroups)) {
                log.debug("Sync requires no changes");
                return;
            }
        }

        if (log.isDebugEnabled()) log.debug("Setting groups for " + element.getObjectId() + " to " + desiredProtectionGroups);
        // accumulate IDs from names
        // Seriously -- there's no way to look them up by name
        List<ProtectionGroup> allGroups = userProvisioningManager.getProtectionGroups();
        List<String> desiredGroupIds = new ArrayList<String>(desiredProtectionGroups.size());
        for (ProtectionGroup group : allGroups) {
            if (desiredProtectionGroups.contains(group.getProtectionGroupName())) {
                desiredGroupIds.add(group.getProtectionGroupId().toString());
            }
        }
        // warn about missing groups, if any
        if (desiredGroupIds.size() != desiredProtectionGroups.size()) {
            List<String> missingGroups = new LinkedList<String>(desiredProtectionGroups);
            for (ProtectionGroup group : allGroups) {
                String name = group.getProtectionGroupName();
                if (missingGroups.contains(name)) missingGroups.remove(name);
            }
            log.warn("Requested protection groups included one or more that don't exist:  " + missingGroups + ".  These groups were skipped.");
        }

        try {
            userProvisioningManager.assignToProtectionGroups(
                element.getProtectionElementId().toString(), desiredGroupIds.toArray(new String[0]));
        } catch (CSTransactionException e) {
            throw new StudyCalendarSystemException("Assigning PE " + element.getProtectionElementName() + " to groups " + desiredProtectionGroups + " failed", e);
        }
    }

    public List<ProtectionGroup> getSitePGsForUser(String userName) {
        return getSitePGs(userProvisioningManager.getUser(userName));
    }

    public List<ProtectionGroup> getStudySitePGsForUser(String userName) {
        User user = userProvisioningManager.getUser(userName);
        List<ProtectionGroup> studySitePGs = new ArrayList<ProtectionGroup>();

        for (Group group : getGroups(user)) {
            if (group.getGroupName().equals(SUBJECT_COORDINATOR_GROUP)) {
                Set<ProtectionGroupRoleContext> pgRoleContexts = getProtectionGroupRoleContexts(user);
                for (ProtectionGroupRoleContext pgrc : pgRoleContexts) {
                    if (isStudySitePG(pgrc.getProtectionGroup())) {
                        studySitePGs.add(pgrc.getProtectionGroup());
                    }
                }
           }
        }
        return studySitePGs;
    }

    @SuppressWarnings({ "unchecked" })
    public void removeProtectionGroup(String protectionGroupName) throws Exception {
        ProtectionGroup pg = new ProtectionGroup();
        pg.setProtectionGroupName(protectionGroupName);
        SearchCriteria pgSearchCriteria = new ProtectionGroupSearchCriteria(pg);
        List<ProtectionGroup> pgList = userProvisioningManager.getObjects(pgSearchCriteria);
        if (pgList.size() > 0) {
        	userProvisioningManager.removeProtectionGroup(pgList.get(0).getProtectionGroupId().toString());
        }
        
    }
    
    @SuppressWarnings({ "unchecked" })
    public void createAndAssignPGToUser(List<String> userIds, String protectionGroupName, String roleName) throws Exception {
        ProtectionGroup pg = new ProtectionGroup();
        pg.setProtectionGroupName(protectionGroupName);
        SearchCriteria pgSearchCriteria = new ProtectionGroupSearchCriteria(pg);
        List<ProtectionGroup> pgList = userProvisioningManager.getObjects(pgSearchCriteria);
        if (pgList.size() <= 0) {
            ProtectionGroup requiredProtectionGroup = new ProtectionGroup();
            requiredProtectionGroup.setProtectionGroupName(protectionGroupName);
            userProvisioningManager.createProtectionGroup(requiredProtectionGroup);
        }
        Role role = new Role();
        role.setName(roleName);
        SearchCriteria roleSearchCriteria = new RoleSearchCriteria(role);
        List<Role> roleList = userProvisioningManager.getObjects(roleSearchCriteria);
        if (roleList.size() > 0) {
            Role accessRole = roleList.get(0);
            String[] roleIds = new String[] {accessRole.getId().toString()};
            if (!((userIds.size() == 1) && (userIds.get(0).length() == 0))) {
                for (String userId : userIds)
                {
                    userProvisioningManager.assignUserRoleToProtectionGroup(userId, roleIds, getPGByName(protectionGroupName).getProtectionGroupId().toString());
                }
            }
        }
    }

    @SuppressWarnings({ "unchecked" })
    public boolean isUserPGAssigned(String pgName, String userId) throws Exception {
        Set<ProtectionGroupRoleContext> pgRoleContext = userProvisioningManager.getProtectionGroupRoleContextForUser(userId);
        List<ProtectionGroupRoleContext> pgRoleContextList = new ArrayList<ProtectionGroupRoleContext> (pgRoleContext);
        if (pgRoleContextList.size() != 0) {
            for (ProtectionGroupRoleContext pgrc : pgRoleContextList) {
                if (pgrc.getProtectionGroup().getProtectionGroupName().equals(pgName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void assignCsmGroups(edu.northwestern.bioinformatics.studycalendar.domain.User user, Set<UserRole> userRoles) throws Exception {
        List<String> csmRoles = rolesToCsmGroups(userRoles);
        String[] strCsmRoles = csmRoles.toArray(new String[csmRoles.size()]);
        userProvisioningManager.assignGroupsToUser(user.getCsmUserId().toString(), strCsmRoles);
    }

    private List<String> rolesToCsmGroups(Set<UserRole> userRoles) throws Exception{
        List<String> csmGroupsForUser = new ArrayList<String>();
        if(userRoles != null) {
            List<Group> allCsmGroups = getAllCsmGroups();

            for(UserRole userRole: userRoles) {
                for(Group group: allCsmGroups) {
                    if(isGroupEqualToRole(group, userRole.getRole())) {
                        csmGroupsForUser.add(group.getGroupId().toString());
                    }
                }
            }
        }
        return csmGroupsForUser;
    }

    ////// INTERNAL HELPERS

    protected boolean isGroupEqualToRole(Group group, edu.northwestern.bioinformatics.studycalendar.domain.Role role) {
        return group.getGroupName().equals(role.getCode());
    }

    @SuppressWarnings("unchecked")
    private List<Group> getAllCsmGroups() throws Exception {
        SearchCriteria searchCriteria = new GroupSearchCriteria(new Group());
        List<Group> groups = userProvisioningManager.getObjects(searchCriteria);
        if(groups == null) {
            throw new StudyCalendarSystemException("Get Csm Groups is null");
        }
        return groups;
    }
    
    private List<ProtectionGroup> getSitePGs(User user) {
        List<ProtectionGroup> sites = new ArrayList<ProtectionGroup>();
        Set<ProtectionGroupRoleContext> pgRoleContext = getProtectionGroupRoleContexts(user);
        if (pgRoleContext.size() != 0) {
            for (ProtectionGroupRoleContext pgrc : pgRoleContext) {
                 if (isSitePG(pgrc.getProtectionGroup())) {
                     sites.add(pgrc.getProtectionGroup());
                 }
            }
        }
        return sites;
    }

    @SuppressWarnings({ "unchecked" })
    private Set<Group> getGroups(User user) {
        try {
            return userProvisioningManager.getGroups(user.getUserId().toString());
        } catch (CSObjectNotFoundException e) {
            throw new StudyCalendarSystemException("Could not get groups for " + user.getLoginName(), e);
        }
    }

    @SuppressWarnings({ "unchecked" })
    private Set<ProtectionGroupRoleContext> getProtectionGroupRoleContexts(User user) {
        try {
            return userProvisioningManager.getProtectionGroupRoleContextForUser(user.getUserId().toString());
        } catch (CSObjectNotFoundException e) {
            throw new StudyCalendarSystemException("Could not find PGRCs for " + user.getLoginName(), e);
        }
    }

    private boolean isSitePG(ProtectionGroup protectionGroup) {
        return protectionGroup.getProtectionGroupName().startsWith(Site.class.getName());
    }

    private boolean isStudySitePG(ProtectionGroup protectionGroup) {
        return protectionGroup.getProtectionGroupName().startsWith(StudySite.class.getName());
    }

    ////// CONFIGURATION
    
    public void setUserProvisioningManager(UserProvisioningManager userProvisioningManager) {
        this.userProvisioningManager = userProvisioningManager;
    }

}


