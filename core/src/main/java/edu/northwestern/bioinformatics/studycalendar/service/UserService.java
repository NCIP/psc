package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import edu.northwestern.bioinformatics.studycalendar.service.dataproviders.SiteConsumer;
import gov.nih.nci.security.AuthorizationManager;
import gov.nih.nci.security.exceptions.CSObjectNotFoundException;
import gov.nih.nci.security.exceptions.CSTransactionException;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

@Deprecated
@Transactional
public class UserService implements Serializable {
    private UserDao userDao;
    private AuthorizationManager authorizationManager;
    private SiteConsumer siteConsumer;

    public User saveUser(User user, String password, final String emailAddress) {
        if (user == null)
            return null;

        if (user.getCsmUserId() == null) {
            try {
                gov.nih.nci.security.authorization.domainobjects.User csmUser = createCsmUser(user, password, emailAddress);
                user.setCsmUserId(csmUser.getUserId());
                if (csmUser.getUserId() == null) {
                    throw new StudyCalendarSystemException("CSM user did not get an ID on create");
                }
            } catch (CSTransactionException e) {
                throw new StudyCalendarSystemException("CSM user creation failed", e);
            }
        } else {
            try {
                gov.nih.nci.security.authorization.domainobjects.User csmUser = authorizationManager.getUserById(user.getCsmUserId().toString());
                csmUser.setPassword(password);
                //for editing emailAddress
                if (! csmUser.getEmailId().equals(emailAddress)) {
                    csmUser.setEmailId(emailAddress);
                }
                authorizationManager.modifyUser(csmUser);
            } catch (CSObjectNotFoundException e) {
                throw new StudyCalendarSystemException(
                        "%s references CSM user with id %d but CSM reports no such user exists",
                        user.getName(), user.getCsmUserId(), e);
            } catch (CSTransactionException e) {
                throw new StudyCalendarSystemException("CSM user update failed", e);
            }
        }

        userDao.save(user);
        return user;
    }

    private gov.nih.nci.security.authorization.domainobjects.User createCsmUser(User user, String password, final String emailAddress) throws CSTransactionException {
        gov.nih.nci.security.authorization.domainobjects.User csmUser =
                new gov.nih.nci.security.authorization.domainobjects.User();
        csmUser.setLoginName(user.getName());
        csmUser.setPassword(password);
        // These attributes can't be null. Oracle treats an empty string as NULL.
        csmUser.setFirstName(".");
        csmUser.setLastName(".");
        csmUser.setEmailId(emailAddress);
        authorizationManager.createUser(csmUser);
        return csmUser;
    }

    /**
     * Returns the user, fully initialized.
     * @param username
     */
    public User getUserByName(String username) {
        User user = userDao.getByName(username);
        if (user != null) {
            Hibernate.initialize(user);
            for (UserRole role : user.getUserRoles()) {
                Hibernate.initialize(role.getSites());
                Hibernate.initialize(role.getStudySites());
            }
            siteConsumer.refresh(user.getAllSites());
        }
        return user;
    }

    ////// CONFIGURATION

    @Required
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Required
    public void setAuthorizationManager(AuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
    }

    @Required
    public void setSiteConsumer(SiteConsumer siteConsumer) {
        this.siteConsumer = siteConsumer;
    }
}
