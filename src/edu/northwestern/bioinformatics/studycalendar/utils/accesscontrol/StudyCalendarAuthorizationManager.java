package edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private static final String APPLICATION_CONTEXT_STRING = "study_calendar";
    public static final String ASSIGNED_USERS = "ASSIGNED_USERS";
    public static final String AVAILABLE_USERS = "AVAILABLE_USERS";
    private static Log log = LogFactory.getLog(LoginCheckInterceptor.class);
    
    public StudyCalendarAuthorizationManager() {
    	
    }
       
    public void assignProtectionElementsToUsers(ArrayList userIds, String protectionElementObjectId) throws Exception
	{
		boolean protectionElementPresent = false;	
		UserProvisioningManager provisioningManager = null;
	
		provisioningManager = getProvisioningManager();
				
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
		if ((protectionElementPresent == true))
		{
			if (log.isDebugEnabled()) {
				log.debug(" The given Protection Element: " + provisioningManager.getProtectionElement(protectionElementObjectId).getProtectionElementName()+ "is present in Database");
			}
			for (int i =0; i< userIds.size();i++)
			{
				String userName = getUserObject((String)userIds.get(i)).getLoginName();
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
						
	public HashMap getUsers(String groupName, String protectionElementObjectId) throws Exception {
		HashMap<String, List> usersMap = new HashMap<String, List>();
		UserProvisioningManager provisioningManager = null;
		ArrayList<User> usersForRequiredGroup = new ArrayList<User>(); 
		provisioningManager = getProvisioningManager();
				
		User user = new User();
        SearchCriteria userSearchCriteria = new UserSearchCriteria(user);
		List userList = provisioningManager.getObjects(userSearchCriteria);
		if (userList.size() > 0)
		{
			
		   for (int i=0; i <userList.size(); i++) {
			   User requiredUser = (User) userList.get(i);
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
			   usersMap = getUserLists(usersForRequiredGroup, protectionElementObjectId);
				
		}	
			
		return usersMap;
	}
    
	
	private HashMap getUserLists(ArrayList users, String protectionElementObjectId) throws Exception {
		UserProvisioningManager provisioningManager = null;
		
		provisioningManager = getProvisioningManager();
		HashMap<String, List> userHashMap = new HashMap<String, List>();
		List<User> assignedUsers = new ArrayList<User>();
		List<User> availableUsers = new ArrayList<User>();
		for (int i =0; i< users.size();i++)
		{
			User user = (User)users.get(i); 
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
		return SecurityServiceProvider.getUserProvisioningManager(APPLICATION_CONTEXT_STRING);
		
	}
	
    public User getUserObject(String id) throws Exception {
    	User user = null;
      	user = getProvisioningManager().getUserById(id);
      	return user;
    }
    
    public void createProtectionGroup(String newProtectionGroup, String parentPG) throws Exception {
    	UserProvisioningManager provisioningManager = getProvisioningManager();
    	
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
    			System.out.println("newProtection group created " + requiredProtectionGroup.getProtectionGroupName());
    		}
    	}
    }
}
