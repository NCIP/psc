/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.test.restfulapi;

import edu.northwestern.bioinformatics.studycalendar.security.csm.internal.DefaultCsmAuthorizationManagerFactory;
import gov.nih.nci.security.AuthorizationManager;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;

import javax.sql.DataSource;

/**
 * @author Rhett Sutphin
 */
public class DefaultCsmAuthorizationManagerFactoryBean implements FactoryBean {
    private DataSource dataSource;
    private AuthorizationManager authorizationManager;

    public Object getObject() throws Exception {
        if (authorizationManager == null) createAuthorizationManager();
        return authorizationManager;
    }

    private void createAuthorizationManager() {
        authorizationManager = new DefaultCsmAuthorizationManagerFactory(dataSource).create();
    }

    public Class getObjectType() {
        return AuthorizationManager.class;
    }

    public boolean isSingleton() {
        return true;
    }

    ////// CONFIGURATION

    @Required
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
