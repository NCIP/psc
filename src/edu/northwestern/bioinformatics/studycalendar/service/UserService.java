package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import gov.nih.nci.security.UserProvisioningManager;
import gov.nih.nci.security.dao.SearchCriteria;
import gov.nih.nci.security.dao.GroupSearchCriteria;
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

    public User saveUser(User user) throws Exception {
        if(user == null)
            return null;

        if(user.getCsmUserId() == null) {
            gov.nih.nci.security.authorization.domainobjects.User csmUser = createCsmUser(user);
            user.setCsmUserId(csmUser.getUserId());
            if(csmUser.getUserId() == null)
                throw new StudyCalendarSystemException("Csm User Id is null");
        }

        assignCsmGroups(user.getCsmUserId().toString(), user.getRoles());
        userDao.save(user);
        return user;
    }

    private gov.nih.nci.security.authorization.domainobjects.User createCsmUser(User user) throws Exception {
        gov.nih.nci.security.authorization.domainobjects.User csmUser =
                new gov.nih.nci.security.authorization.domainobjects.User();
        csmUser.setLoginName(user.getName());
        csmUser.setPassword(user.getPassword());
        csmUser.setFirstName("");   // Attribute can't be null
        csmUser.setLastName("");    // Attribute can't be null
        userProvisioningManager.createUser(csmUser);
        return csmUser;
    }

    private void assignCsmGroups(String userId, Set<Role> roles) throws Exception {
        List<String> csmRoles = rolesToCsmGroups(roles);
        String[] strCsmRoles = csmRoles.toArray(new String[csmRoles.size()]);
        if(csmRoles.size() > 0) {
            userProvisioningManager.assignGroupsToUser(userId, strCsmRoles);
        }
    }

    private List<String> rolesToCsmGroups(Set<Role> roles) throws Exception{
        List csmGroupsForUser = new ArrayList<String>();
        if(roles != null) {
            List<Group> allCsmGroups = getAllCsmGroups();

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

    @SuppressWarnings("unchecked")
    private List<Group> getAllCsmGroups() throws Exception {
        SearchCriteria searchCriteria = new GroupSearchCriteria(new Group());
        List<Group> groups = userProvisioningManager.getObjects(searchCriteria);
        if(groups == null) {
            throw new StudyCalendarSystemException("Get Csm Groups is null");
        }
        return groups;
    }

    protected boolean isGroupEqualToRole(Group group, Role role) {
        return group.getGroupName().equals(role.getCode());
    }

    @SuppressWarnings("unchecked")
    public User getUserByName(String userName) {
        List<User> usersResults = userDao.getByName(userName);
        return usersResults.size() > 0 ? usersResults.iterator().next() : null;
    }

    public List<User> getAllUsers() throws Exception {
        return userDao.getAll();
    }

    public User getUserById(int id)  throws Exception {
        return userDao.getById(id);
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
