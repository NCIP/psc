package edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import gov.nih.nci.security.SecurityServiceProvider;
import gov.nih.nci.security.UserProvisioningManager;
import gov.nih.nci.security.authorization.domainobjects.Group;
import gov.nih.nci.security.authorization.domainobjects.ProtectionElement;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroup;
import gov.nih.nci.security.authorization.domainobjects.ProtectionGroupRoleContext;
import gov.nih.nci.security.authorization.domainobjects.Role;
import gov.nih.nci.security.authorization.domainobjects.User;
import gov.nih.nci.security.dao.RoleSearchCriteria;
import gov.nih.nci.security.dao.SearchCriteria;
import gov.nih.nci.security.dao.UserSearchCriteria;
import gov.nih.nci.security.dao.ProtectionGroupSearchCriteria;
import gov.nih.nci.security.exceptions.CSObjectNotFoundException;

/**
 * @author Padmaja Vedula
 */

public class StudyCalendarAuthorizationManager {
	public static final String APPLICATION_CONTEXT_NAME = "study_calendar";
	public static final String BASE_SITE_PG = "BaseSitePG";
    public static final String ASSIGNED_USERS = "ASSIGNED_USERS";
    public static final String AVAILABLE_USERS = "AVAILABLE_USERS";
    private static Log log = LogFactory.getLog(LoginCheckInterceptor.class);
     
  
    public void assignProtectionElementsToUsers(List<String> userIds, String protectionElementObjectId) throws Exception
	{
    	UserProvisioningManager provisioningManager = null;
    	boolean protectionElementPresent = false;	
			
		try { 
			provisioningManager = getProvisioningManager();
			provisioningManager.getProtectionElement(protectionElementObjectId);
			protectionElementPresent = true;
		} catch (CSObjectNotFoundException ex){
			ProtectionElement newProtectionElement = new ProtectionElement();
			newProtectionElement.setObjectId(protectionElementObjectId);
			newProtectionElement.setProtectionElementName(protectionElementObjectId);
			provisioningManager.createProtectionElement(newProtectionElement);
			provisioningManager.setOwnerForProtectionElement(protectionElementObjectId, userIds.toArray(new String[0]));
		}
		if (protectionElementPresent)
		{
			if (log.isDebugEnabled()) {
				log.debug(" The given Protection Element: " + provisioningManager.getProtectionElement(protectionElementObjectId).getProtectionElementName()+ "is present in Database");
			}
			for (String userId : userIds)
			{
				String userName = getUserObject(userId).getLoginName();
				if (!(provisioningManager.checkOwnership((String)userName, protectionElementObjectId)))
				{
					if (log.isDebugEnabled()) {
						log.debug(" Given Protection Element: " + provisioningManager.getProtectionElement(protectionElementObjectId).getProtectionElementName()+ "is not owned by " + userName);
					}
					provisioningManager.setOwnerForProtectionElement((String)userName, protectionElementObjectId, provisioningManager.getProtectionElement(protectionElementObjectId).getAttribute());
				} else {
					if (log.isDebugEnabled()) {
						log.debug(" Given Protection Element: " + provisioningManager.getProtectionElement(protectionElementObjectId).getProtectionElementName()+ "is owned by " + userName);
					}
				}
			
			}
		}
	}
						
	public Map getUsers(String groupName, String protectionElementObjectId) throws Exception {
		HashMap<String, List> usersMap = new HashMap<String, List>();
		List<User> usersForRequiredGroup = getUsersForGroup(groupName);
        usersMap = (HashMap) getUserListsForProtectionElement(usersForRequiredGroup, protectionElementObjectId);
				
		return usersMap;
	}
    
	
	private Map getUserListsForProtectionElement(List<User> users, String protectionElementObjectId) throws Exception {
		UserProvisioningManager provisioningManager = null;
		HashMap<String, List> userHashMap = new HashMap<String, List>();
		List<User> assignedUsers = new ArrayList<User>();
		List<User> availableUsers = new ArrayList<User>();
		provisioningManager = getProvisioningManager();
		
		for (User user : users)
		{
			String userName = user.getLoginName();
			if (provisioningManager.checkOwnership(userName, protectionElementObjectId))
			{
				assignedUsers.add(user);
			} else {
				availableUsers.add(user);
			}
		}
		userHashMap.put(ASSIGNED_USERS, assignedUsers);
		userHashMap.put(AVAILABLE_USERS, availableUsers);
		return userHashMap;
	}
	
	public UserProvisioningManager getProvisioningManager() throws Exception {
		return SecurityServiceProvider.getUserProvisioningManager(APPLICATION_CONTEXT_NAME);
		
	}
	
    public User getUserObject(String id) throws Exception {
    	UserProvisioningManager provisioningManager = null;
    	provisioningManager = getProvisioningManager();
    	User user = null;
      	user = provisioningManager.getUserById(id);
      	return user;
    }
    
    public void createProtectionGroup(String newProtectionGroup, String parentPG) throws Exception {
    	UserProvisioningManager provisioningManager = null;
    	provisioningManager = getProvisioningManager();
    	if (parentPG != null) {
    		ProtectionGroup parentGroupSearch = new ProtectionGroup();
    		parentGroupSearch.setProtectionGroupName(parentPG);
            SearchCriteria protectionGroupSearchCriteria = new ProtectionGroupSearchCriteria(parentGroupSearch);
    		List parentGroupList = provisioningManager.getObjects(protectionGroupSearchCriteria);
    		
    		if (parentGroupList.size() > 0) {
    			ProtectionGroup parentProtectionGroup = (ProtectionGroup) parentGroupList.get(0);
    			ProtectionGroup requiredProtectionGroup = new ProtectionGroup();
    			requiredProtectionGroup.setProtectionGroupName(newProtectionGroup);
    			requiredProtectionGroup.setParentProtectionGroup(parentProtectionGroup);
    			provisioningManager.createProtectionGroup(requiredProtectionGroup);
    			if (log.isDebugEnabled()) {
					log.debug("new protection group created " + newProtectionGroup);
				}
    		}
    	}
    }
    
    /**
     * Method to retrieve all site protection groups
     * 
     */
    
    public List getSites() throws Exception {
    	UserProvisioningManager provisioningManager = null;
    	provisioningManager = getProvisioningManager();	
    	List<ProtectionGroup> siteList = new ArrayList<ProtectionGroup>() ;
    	
		ProtectionGroup parentGroupSearch = new ProtectionGroup();
		parentGroupSearch.setProtectionGroupName(BASE_SITE_PG);
	    SearchCriteria protectionGroupSearchCriteria = new ProtectionGroupSearchCriteria(parentGroupSearch);
		List parentGroupList = provisioningManager.getObjects(protectionGroupSearchCriteria);
			
		if (parentGroupList.size() > 0) {
			ProtectionGroup parentProtectionGroup = (ProtectionGroup) parentGroupList.get(0);
			ProtectionGroup protectionGroup = new ProtectionGroup();
	        SearchCriteria pgSearchCriteria = new ProtectionGroupSearchCriteria(protectionGroup);
			List<ProtectionGroup> pgList = provisioningManager.getObjects(pgSearchCriteria);
			
			if (pgList.size() > 0) {
				for (ProtectionGroup requiredProtectionGroup : pgList) {
					   if (requiredProtectionGroup.getParentProtectionGroup().getProtectionGroupId() == parentProtectionGroup.getProtectionGroupId()) {	
						   siteList.add(requiredProtectionGroup);
					   }
				}
			}
		}
		return siteList;
    }
    
    public List getUsersForGroup(String groupName) throws Exception {
    	UserProvisioningManager provisioningManager = null;
		List<User> usersForRequiredGroup = new ArrayList<User>(); 
		provisioningManager = getProvisioningManager();
		User user = new User();
        SearchCriteria userSearchCriteria = new UserSearchCriteria(user);
		List<User> userList = provisioningManager.getObjects(userSearchCriteria);
		if (userList.size() > 0)
		{
			
		   for (User requiredUser : userList) {
			   try {
				   Set groups = provisioningManager.getGroups(requiredUser.getUserId().toString());
				   Set<Group> userGroups = groups;
				   if (userGroups.size() > 0) {	
					   Group requiredGroup = (Group) userGroups.toArray()[0];
					   if (groupName.equals(requiredGroup.getGroupName())) {
						   usersForRequiredGroup.add(requiredUser);
					   }
				   }
			   } catch (CSObjectNotFoundException cse){
				   throw cse;
			   }
		   
		   }
		}
		return usersForRequiredGroup;
    }
    
    /**
     * Method to retrieve users who have the given protection group assigned to them.
     * (can be used for retrieving site coordinators for site protection groups)
     * @param group
     * @param protectionGroupName
     * @return
     * @throws Exception
     */
    public Map getUserPGLists(String group, String protectionGroupName) throws Exception {
    	HashMap<String, List> usersMap = new HashMap<String, List>();
		List<User> usersForRequiredGroup = getUsersForGroup(group);
        usersMap = (HashMap) getUserListsForProtectionGroup(usersForRequiredGroup, protectionGroupName);
				
		return usersMap;
    	
    }
    
    /**
     * 
     * @param users
     * @param protectionGroupName
     * @return
     * @throws Exception
     */
    
    private Map getUserListsForProtectionGroup(List<User> users, String protectionGroupName) throws Exception {
		UserProvisioningManager provisioningManager = null;
		HashMap<String, List> userHashMap = new HashMap<String, List>();
		List<User> assignedUsers = new ArrayList<User>();
		List<User> availableUsers = new ArrayList<User>();
		provisioningManager = getProvisioningManager();
		for (User user : users)
		{
			String userName = user.getLoginName();
			Set<ProtectionGroupRoleContext> pgRoleContext = provisioningManager.getProtectionGroupRoleContextForUser(userName);
			List<ProtectionGroupRoleContext> pgRoleContextList = new ArrayList(pgRoleContext);
			for (ProtectionGroupRoleContext pgrc : pgRoleContextList)
			{
				if (pgrc.getProtectionGroup().getProtectionGroupName().equals(protectionGroupName)) {
					assignedUsers.add(user);
				} else {
					availableUsers.add(user);
			    }
			}
		}
		userHashMap.put(ASSIGNED_USERS, assignedUsers);
		userHashMap.put(AVAILABLE_USERS, availableUsers);
		return userHashMap;
	}
    
    public void assignProtectionGroupsToUsers(List<String> userIds, ProtectionGroup protectionGroup, String roleName) throws Exception
	{
    	UserProvisioningManager provisioningManager = null;
		provisioningManager = getProvisioningManager();
		Role role = new Role();
		role.setName(roleName);
		SearchCriteria roleSearchCriteria = new RoleSearchCriteria(role);
		List roleList = provisioningManager.getObjects(roleSearchCriteria);
		if (roleList.size() > 0) {
			Role accessRole = (Role) roleList.get(0);
			String[] roleIds = new String[] {accessRole.getId().toString()};

			for (String userId : userIds)
			{
				provisioningManager.assignUserRoleToProtectionGroup(userId, roleIds, protectionGroup.getProtectionGroupId().toString());
			}
		}
	}
    
    public void removeProtectionGroupUsers(List<String> userIds, ProtectionGroup protectionGroup) throws Exception
    {
    	UserProvisioningManager provisioningManager = null;
    	provisioningManager = getProvisioningManager();
    	
    	for (String userId : userIds)
    	{
    		provisioningManager.removeUserFromProtectionGroup(protectionGroup.getProtectionGroupId().toString(), userId);
    	}
    }
    
}


