/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.core.osgi;

import edu.northwestern.bioinformatics.studycalendar.mocks.osgi.PscTestingBundleContext;
import edu.northwestern.bioinformatics.studycalendar.security.csm.internal.DefaultCsmAuthorizationManagerFactory;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembershipLoader;
import gov.nih.nci.security.AuthorizationManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import javax.sql.DataSource;

/**
 * @author Rhett Sutphin
 */
public class CoreTestingBundleContext extends PscTestingBundleContext implements InitializingBean {
    private DataSource dataSource;

    public void reset() {
        testingDetails.clear();
        AuthorizationManager authorizationManager =
            new DefaultCsmAuthorizationManagerFactory(dataSource).create();
        addService(AuthorizationManager.class, authorizationManager);

        SuiteRoleMembershipLoader srmLoader = new SuiteRoleMembershipLoader();
        srmLoader.setAuthorizationManager(authorizationManager);
        addService(SuiteRoleMembershipLoader.class, srmLoader);
    }

    public void afterPropertiesSet() throws Exception {
        reset();
    }

    ////// CONFIGURATION

    @Required
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
