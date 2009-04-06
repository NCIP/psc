package edu.northwestern.bioinformatics.studycalendar.web.osgi;

import edu.northwestern.bioinformatics.studycalendar.osgi.hostservices.HostBeans;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.Membrane;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * This is the host-side initializer for the OSGi-side HostBeans service.
 *
 * @author Rhett Sutphin
 */
public class HostBeansInitializer implements ApplicationContextAware, InitializingBean {
    private ApplicationContext applicationContext;
    private BundleContext bundleContext;

    public void afterPropertiesSet() throws Exception {
        ServiceReference ref = bundleContext.getServiceReference(HostBeans.class.getName());
        if (ref != null) {
            Membrane membrane = Membrane.get(
                Thread.currentThread().getContextClassLoader(),
                "org.springframework.context",
                "org.acegisecurity.userdetails",
                "org.acegisecurity",
                "javax.sql",
                "edu.northwestern.bioinformatics.studycalendar.osgi.hostservices"
            );
            HostBeans beans = (HostBeans) membrane.farToNear(bundleContext.getService(ref));
            beans.setHostApplicationContext(applicationContext);
        }
    }

    ////// CONFIGURATION

    @Required
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Required
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}
