package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.utils.NamedComparator;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import gov.nih.nci.security.UserProvisioningManager;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.io.Serializable;

@Transactional
public class UserService implements Serializable {

    private UserDao userDao;
    private UserProvisioningManager userProvisioningManager;
    public static final String STUDY_CALENDAR_APPLICATION_ID = "2";

    public User saveUser(User user, String password) throws Exception {
        if(user == null)
            return null;

        if(user.getCsmUserId() == null) {
            gov.nih.nci.security.authorization.domainobjects.User csmUser = createCsmUser(user, password);
            user.setCsmUserId(csmUser.getUserId());
            if(csmUser.getUserId() == null)
                throw new StudyCalendarSystemException("Csm User Id is null");
        } else {
            gov.nih.nci.security.authorization.domainobjects.User csmUser = userProvisioningManager.getUserById(user.getCsmUserId().toString());
            csmUser.setPassword(password);
            userProvisioningManager.modifyUser(csmUser);
        }

        return saveUser(user);
    }

    public User saveUser(User user) throws Exception {
        if (user == null) return null;

        userDao.save(user);
        return user;
    }

    private gov.nih.nci.security.authorization.domainobjects.User createCsmUser(User user, String password) throws Exception {
        gov.nih.nci.security.authorization.domainobjects.User csmUser =
                new gov.nih.nci.security.authorization.domainobjects.User();
        csmUser.setLoginName(user.getName());
        csmUser.setPassword(password);
        csmUser.setFirstName("");   // Attribute can't be null
        csmUser.setLastName("");    // Attribute can't be null
        userProvisioningManager.createUser(csmUser);
        return csmUser;
    }

    @SuppressWarnings("unchecked")
    public User getUserByName(String userName) {
        User usersResults = userDao.getByName(userName);
        return usersResults;
    }

    public List<User> getAllUsers() throws Exception {
        return userDao.getAll();
    }

    public User getUserById(int id)  throws Exception {
        return userDao.getById(id);
    }

    public List<User> getSubjectCoordinatorsForSites(List<Site> sites) {
        List<User> users = userDao.getAllSubjectCoordinators();
        List<User> associatedUsers = new ArrayList<User>();
        for (User user : users) {
            for (Site site : sites) {
                UserRole userRole = user.getUserRole(Role.SUBJECT_COORDINATOR);
                if (userRole != null && userRole.getSites().contains(site)) {
                    associatedUsers.add(user);
                    break;
                }
            }
        }
        return associatedUsers;
    }

    public List<User> getSiteCoordinatorsAssignableUsers(User siteCoordinator) {
        List<Site> sites = new ArrayList<Site>();
        List<User> assignableUsers = new ArrayList<User>();
        if (siteCoordinator != null) {
            UserRole userRole = siteCoordinator.getUserRole(Role.SITE_COORDINATOR);

            sites.addAll(userRole.getSites());

            assignableUsers = getSubjectCoordinatorsForSites(sites);
            Collections.sort(assignableUsers, new NamedComparator());
        }

        return assignableUsers;
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
