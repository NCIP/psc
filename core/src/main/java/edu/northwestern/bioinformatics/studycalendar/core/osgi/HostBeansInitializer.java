package edu.northwestern.bioinformatics.studycalendar.core.osgi;

import edu.northwestern.bioinformatics.studycalendar.osgi.hostservices.HostBeans;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUserDetailsService;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.Membrane;
import gov.nih.nci.security.AuthorizationManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.apache.felix.cm.PersistenceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * This is the host-side initializer for the OSGi-side HostBeans service.
 *
 * @author Rhett Sutphin
 */
public class HostBeansInitializer implements InitializingBean {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private BundleContext bundleContext;
    private Membrane membrane;

    private DataSource dataSource;
    private PscUserDetailsService userDetailsService;
    private PersistenceManager persistenceManager;
    private AuthorizationManager authorizationManager;

    public void afterPropertiesSet() throws Exception {
        if (bundleContext != null) {
            ServiceReference ref = bundleContext.getServiceReference(HostBeans.class.getName());
            if (ref != null) {
                HostBeans beans = (HostBeans) membrane.farToNear(bundleContext.getService(ref));
                beans.setDataSource(dataSource);
                beans.setPscUserDetailsService(userDetailsService);
                beans.setPersistenceManager(persistenceManager);
                beans.setAuthorizationManager(authorizationManager);
            }
        } else {
            log.debug("No bundleContext set");
            log.info("Since the OSGi layer was not started, the HostBeans bridge will not be initialized.");
        }
    }

    ////// CONFIGURATION

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Required
    public void setMembrane(Membrane membrane) {
        this.membrane = membrane;
    }

    ////// DEFERRED BEANS

    @Required
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Required
    public void setPscUserDetailsService(PscUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Required
    public void setPersistenceManager(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    @Required
    public void setCsmAuthorizationManager(AuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
    }
}
