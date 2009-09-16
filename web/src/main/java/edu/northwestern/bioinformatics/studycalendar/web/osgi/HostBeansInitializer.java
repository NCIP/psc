package edu.northwestern.bioinformatics.studycalendar.web.osgi;

import edu.northwestern.bioinformatics.studycalendar.osgi.hostservices.HostBeans;
import edu.northwestern.bioinformatics.studycalendar.security.acegi.PscUserDetailsService;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.Membrane;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import javax.sql.DataSource;

/**
 * This is the host-side initializer for the OSGi-side HostBeans service.
 *
 * @author Rhett Sutphin
 */
public class HostBeansInitializer implements InitializingBean {
    private BundleContext bundleContext;
    private Membrane membrane;

    private DataSource dataSource;
    private PscUserDetailsService userDetailsService;

    public void afterPropertiesSet() throws Exception {
        ServiceReference ref = bundleContext.getServiceReference(HostBeans.class.getName());
        if (ref != null) {
            HostBeans beans = (HostBeans) membrane.farToNear(bundleContext.getService(ref));
            beans.setDataSource(dataSource);
            beans.setPscUserDetailsService(userDetailsService);
        }
    }

    ////// CONFIGURATION

    @Required
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
}
