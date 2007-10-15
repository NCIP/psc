package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import gov.nih.nci.security.UserProvisioningManager;
import gov.nih.nci.security.authorization.domainobjects.Group;
import gov.nih.nci.security.dao.GroupSearchCriteria;
import gov.nih.nci.security.dao.SearchCriteria;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Transactional
public class UserService {

    private UserDao userDao;
    private UserProvisioningManager userProvisioningManager;
    public static final String STUDY_CALENDAR_APPLICATION_ID = "2";
    private SiteService siteService;

    public User saveUser(User user) throws Exception {
        if(user == null)
            return null;

        if(user.getCsmUserId() == null) {
            gov.nih.nci.security.authorization.domainobjects.User csmUser = createCsmUser(user);
            user.setCsmUserId(csmUser.getUserId());
            if(csmUser.getUserId() == null)
                throw new StudyCalendarSystemException("Csm User Id is null");
        }

        assignCsmGroups(user.getCsmUserId().toString(), user.getUserRoles());
        assignCsmUserToSite(user.getCsmUserId().toString(), user.getName(), user.getUserRoles());
        userDao.save(user);
        return user;
    }

    private void assignCsmUserToSite(String csmUserId, String userName, Set<UserRole> userRoles) throws Exception {
        for (Site site : siteService.getSitesForUser(userName)) {
            siteService.removeAllSiteRoles(site, csmUserId);
        }

        for (UserRole userRole : userRoles) {
            for (Site site : userRole.getSites()) {
                if (SiteService.PARTICIPANT_COORDINATOR_ACCESS_ROLE.equals(userRole.getRole().csmGroup())) {
                    siteService.assignParticipantCoordinators(site, csmUserId);
                } else if (SiteService.RESEARCH_ASSOCIATE_ACCESS_ROLE.equals(userRole.getRole().csmGroup())) {
                    siteService.assignSiteResearchAssociates(site, csmUserId);
                } else if (SiteService.SITE_COORDINATOR_ACCESS_ROLE.equals(userRole.getRole().csmGroup())) {
                    siteService.assignSiteCoordinators(site, csmUserId);
                }
            }
        }

    }

    private gov.nih.nci.security.authorization.domainobjects.User createCsmUser(User user) throws Exception {
        gov.nih.nci.security.authorization.domainobjects.User csmUser =
                new gov.nih.nci.security.authorization.domainobjects.User();
        csmUser.setLoginName(user.getName());
        csmUser.setPassword(user.getPlainTextPassword());
        csmUser.setFirstName("");   // Attribute can't be null
        csmUser.setLastName("");    // Attribute can't be null
        userProvisioningManager.createUser(csmUser);
        return csmUser;
    }

    private void assignCsmGroups(String userId, Set<UserRole> userRoles) throws Exception {
        List<String> csmRoles = rolesToCsmGroups(userRoles);
        String[] strCsmRoles = csmRoles.toArray(new String[csmRoles.size()]);
        if(csmRoles.size() > 0) {
            userProvisioningManager.assignGroupsToUser(userId, strCsmRoles);
        }
    }

    private List<String> rolesToCsmGroups(Set<UserRole> userRoles) throws Exception{
        List csmGroupsForUser = new ArrayList<String>();
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
        User usersResults = userDao.getByName(userName);
        return usersResults;
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

    @Required
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }
}
