package edu.northwestern.bioinformatics.studycalendar.web.osgi;

import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.Membrane;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Bundle;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public class OsgiLayerTools {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private Membrane membrane;
    private BundleContext bundleContext;

    public void updateConfiguration(Dictionary<?, ?> newProps, String servicePid, String serviceInTargetBundle) {
        ServiceReference cmRef = bundleContext.getServiceReference(ConfigurationAdmin.class.getName());
        if (cmRef == null) {
            throw new StudyCalendarSystemException(
                "OSGi CM service not available.  Unable to update %s.", servicePid);
        } else {
            Bundle targetBundle = bundleContext.getServiceReference(serviceInTargetBundle).getBundle();
            log.debug("Updating configuration for bundle {} at {}", targetBundle, targetBundle.getLocation());
            ConfigurationAdmin cm = (ConfigurationAdmin) membrane.farToNear(bundleContext.getService(cmRef));
            try {
                Configuration targetConfig = cm.getConfiguration(servicePid, targetBundle.getLocation());
                log.trace("Updating {} with {}", servicePid, newProps == null ? "<no properties>" : newProps);
                targetConfig.update(newProps);
            } catch (IOException e) {
                throw new StudyCalendarSystemException(
                    "I/O problem while acquiring configuration %s to update", servicePid, e);
            }
        }
    }

    public Object getRequiredService(String serviceName) {
        ServiceReference ref = bundleContext.getServiceReference(serviceName);
        if (ref == null) {
            throw new StudyCalendarSystemException(
                "Service %s not available in the OSGi layer", serviceName);
        }
        return membrane.farToNear(bundleContext.getService(ref));
    }

    ////// CONFIGURATION

    public void setMembrane(Membrane membrane) {
        this.membrane = membrane;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}
