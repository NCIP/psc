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
import gov.nih.nci.security.authorization.domainobjects.User;
import gov.nih.nci.security.dao.ProtectionGroupSearchCriteria;
import gov.nih.nci.security.dao.SearchCriteria;
import gov.nih.nci.security.dao.UserSearchCriteria;
import gov.nih.nci.security.exceptions.CSObjectNotFoundException;

/**
 * @author Padmaja Vedula
 */

public class StudyCalendarAuthorizationManager {
    public static final String ASSIGNED_USERS = "ASSIGNED_USERS";
    public static final String AVAILABLE_USERS = "AVAILABLE_USERS";
    private static Log log = LogFactory.getLog(LoginCheckInterceptor.class);
    UserProvisioningManager provisioningManager = null;
    
    public StudyCalendarAuthorizationManager(String contextName) throws Exception {
    	provisioningManager = getProvisioningManager(contextName);
    }
       
    public void assignProtectionElementsToUsers(List<String> userIds, String protectionElementObjectId) throws Exception
	{
		boolean protectionElementPresent = false;	
						
		try { 
			provisioningManager.getProtectionElement(protectionElementObjectId);
			protectionElementPresent = true;
		} catch (CSObjectNotFoundException ex){
			ProtectionElement newProtectionElement = new ProtectionElement();
			newProtectionElement.setObjectId(protectionElementObjectId);
			newProtectionElement.setProtectionElementName(protectionElementObjectId);
			provisioningManager.createProtectionElement(newProtectionElement);
			provisioningManager.setOwnerForProtectionElement(protectionElementObjectId, (String[])userIds.toArray());
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
		List<User> usersForRequiredGroup = new ArrayList<User>(); 
						
		User user = new User();
        SearchCriteria userSearchCriteria = new UserSearchCriteria(user);
		List<User> userList = provisioningManager.getObjects(userSearchCriteria);
		if (userList.size() > 0)
		{
			
		   for (User requiredUser : userList) {
			   //User requiredUser = (User) userList.get(i);
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
           usersMap = (HashMap) getUserLists(usersForRequiredGroup, protectionElementObjectId);
				
		}	
			
		return usersMap;
	}
    
	
	private Map getUserLists(List<User> users, String protectionElementObjectId) throws Exception {
		HashMap<String, List> userHashMap = new HashMap<String, List>();
		List<User> assignedUsers = new ArrayList<User>();
		List<User> availableUsers = new ArrayList<User>();
		for (User user : users)
		{
			//User user = (User)users.get(i); 
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
	
	public UserProvisioningManager getProvisioningManager(String contextName) throws Exception {
		return SecurityServiceProvider.getUserProvisioningManager(contextName);
		
	}
	
    public User getUserObject(String id) throws Exception {
    	User user = null;
      	user = provisioningManager.getUserById(id);
      	return user;
    }
    
    public void createProtectionGroup(String newProtectionGroup, String parentPG) throws Exception {
      	
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
}
