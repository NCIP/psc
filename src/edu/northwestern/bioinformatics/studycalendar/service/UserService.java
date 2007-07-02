package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import gov.nih.nci.security.UserProvisioningManager;
import gov.nih.nci.security.authorization.domainobjects.Group;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Transactional
public class UserService {

    private UserDao userDao;
    private UserProvisioningManager userProvisioningManager;
    public static final String STUDY_CALENDAR_APPLICATION_ID = "2";

    public User createUser(User user) throws Exception {
        gov.nih.nci.security.authorization.domainobjects.User csmUser = createCsmUser(user);

        if(csmUser.getUserId() == null || StringUtils.isBlank(csmUser.getUserId().toString()))
            return null;

        assignCsmGroups(csmUser.getUserId().toString(), user.getRoles());
        user.setCsmUserId(csmUser.getUserId());
        userDao.save(user);

        return user;
    }

    private gov.nih.nci.security.authorization.domainobjects.User createCsmUser(User user) throws Exception {
        gov.nih.nci.security.authorization.domainobjects.User csmUser =
                new gov.nih.nci.security.authorization.domainobjects.User();
        csmUser.setLoginName(user.getName());
        csmUser.setFirstName("");   // Attribute can't be null
        csmUser.setLastName("");    // Attribute can't be null
        userProvisioningManager.createUser(csmUser);
        return csmUser;
    }

    private void assignCsmGroups(String userId, Set<Role> roles) throws Exception {
        List<String> csmRoles = rolesToCsmGroups(roles);
        if(csmRoles.size() > 0) {
            userProvisioningManager.assignGroupsToUser(userId, csmRoles.toArray(new String[csmRoles.size()]));
        }
    }

    private List<String> rolesToCsmGroups(Set<Role> roles) throws Exception{
        List csmGroupsForUser = new ArrayList<String>();
        if(roles != null) {
            Set<Group> allCsmGroups = getAllCsmGroups();

            for(Role role: roles) {
                for(Group group: allCsmGroups) {
                    if(isGroupEqualToRole(group, role)) {
                        csmGroupsForUser.add(group.getGroupId().toString());
                    }
                }
            }
        }
        return csmGroupsForUser;
    }

    private Set<Group> getAllCsmGroups() throws Exception {
        Set<Group> groups = userProvisioningManager.getApplicationById(STUDY_CALENDAR_APPLICATION_ID).getGroups();
        return groups == null ? new HashSet<Group>() : groups;
    }

    private boolean isGroupEqualToRole(Group group, Role role) {
        return group.toString().equals(role.getCode());
    }

    @SuppressWarnings("unchecked")
    public User getUserByName(String userName) {
        List<User> usersResults = userDao.getByName(userName);
        return usersResults.size() > 0 ? usersResults.iterator().next() : null;
    }


    ////// CONFIGURATION
    @Required
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Required
    public void setUserProvisioningManager(UserProvisioningManager userProvisioningManager) {
        this.userProvisioningManager = userProvisioningManager;
    }
}
