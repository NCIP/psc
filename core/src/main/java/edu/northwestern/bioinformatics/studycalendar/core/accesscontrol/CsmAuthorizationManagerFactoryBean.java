package edu.northwestern.bioinformatics.studycalendar.core.accesscontrol;

import gov.nih.nci.security.AuthorizationManager;
import gov.nih.nci.security.dao.AuthorizationDAO;
import gov.nih.nci.security.exceptions.CSConfigurationException;
import gov.nih.nci.security.provisioning.AuthorizationManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;

/**
 * Works around CSM 4.2's insistence on reading an external configuration file even in an
 * IoC context.  It takes advantage of the fact that, if a session factory already exists for a given
 * CSM context name, CSM will not try to read the external file.  The session factory is created in
 * applicationContext-authorization.xml.  This class ensures that the AuthorizationManager instance
 * isn't created until after the session factory.
 *
 * @author Rhett Sutphin
 */
public class CsmAuthorizationManagerFactoryBean implements FactoryBean {
    private String applicationName;
    private AuthorizationDAO authorizationDao;

    public Object getObject() throws Exception {
        AuthorizationManagerImpl m = new AuthorizationManagerImpl(applicationName);
        m.setAuthorizationDAO(authorizationDao);
        return m;
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
