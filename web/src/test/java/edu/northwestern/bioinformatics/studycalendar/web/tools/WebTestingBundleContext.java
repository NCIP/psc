package edu.northwestern.bioinformatics.studycalendar.web.tools;

import edu.northwestern.bioinformatics.studycalendar.mocks.osgi.MockConfigurationAdmin;
import edu.northwestern.bioinformatics.studycalendar.mocks.osgi.PscTestingBundleContext;
import edu.northwestern.bioinformatics.studycalendar.osgi.hostservices.HostBeans;
import edu.northwestern.bioinformatics.studycalendar.osgi.hostservices.internal.HostBeansImpl;
import edu.northwestern.bioinformatics.studycalendar.security.CompleteAuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.StubAuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUserDetailsService;
import edu.northwestern.bioinformatics.studycalendar.security.csm.internal.DefaultCsmAuthorizationManagerFactory;
import edu.northwestern.bioinformatics.studycalendar.security.internal.CompleteAuthenticationSystemImpl;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.service.PscUserService;
import gov.nih.nci.security.AuthorizationManager;
import org.osgi.service.cm.ConfigurationAdmin;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import javax.sql.DataSource;

/**
 * @author Rhett Sutphin
*/
public class WebTestingBundleContext extends PscTestingBundleContext implements InitializingBean {
    private DataSource dataSource;

    public void reset() {
        testingDetails.clear();
        addService(AuthenticationSystem.class, new StubAuthenticationSystem());
        addService(HostBeans.class, new HostBeansImpl());
        addService(PscUserDetailsService.class, new PscUserService());
        addService(ConfigurationAdmin.class, new MockConfigurationAdmin());
        addService(CompleteAuthenticationSystem.class, new CompleteAuthenticationSystemImpl());
        addService(AuthorizationManager.class,
            new DefaultCsmAuthorizationManagerFactory(dataSource).create());
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
