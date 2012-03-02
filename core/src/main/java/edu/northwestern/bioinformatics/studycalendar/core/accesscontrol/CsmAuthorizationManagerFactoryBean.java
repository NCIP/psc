package edu.northwestern.bioinformatics.studycalendar.core.accesscontrol;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;
import gov.nih.nci.security.dao.AuthorizationDAO;
import gov.nih.nci.security.provisioning.AuthorizationManagerImpl;
import gov.nih.nci.security.AuthorizationManager;

/**
 * @author Jalpa Patel
 */
public class CsmAuthorizationManagerFactoryBean implements FactoryBean {
    private String applicationName;
    private AuthorizationDAO authorizationDao;

    public Object getObject() throws Exception {
        AuthorizationManagerImpl authorizationManagerImpl = new AuthorizationManagerImpl(applicationName);
        authorizationManagerImpl.setAuthorizationDAO(authorizationDao);
        return authorizationManagerImpl;
    }

    public Class getObjectType() {
        return AuthorizationManager.class;
    }

    public boolean isSingleton() {
        return true;
    }

    @Required
    public void setApplicationName(String applicationName) {

        this.applicationName = applicationName;
    }

    @Required
    public void setAuthorizationDao(AuthorizationDAO authorizationDao) {
        this.authorizationDao = authorizationDao;
    }
}
