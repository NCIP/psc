/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.core.osgi;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.Membrane;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Constants;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class OsgiLayerTools {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private Membrane membrane;
    private BundleContext bundleContext;

    public void updateConfiguration(Dictionary<?, ?> newProps, String servicePid) {
        Bundle targetBundle = findBundleWithPid(servicePid);
        if (targetBundle == null) {
            log.warn("No managed service with PID {} is available to configure", servicePid);
            return;
        }
        updateConfiguration(newProps, targetBundle, servicePid);
    }

    public void updateConfiguration(Dictionary<?, ?> newProps, Bundle targetBundle, String servicePid) {
        ServiceReference cmRef = bundleContext.getServiceReference(ConfigurationAdmin.class.getName());
        if (cmRef == null) {
            throw new StudyCalendarSystemException(
                "OSGi CM service not available.  Unable to update %s.", servicePid);
        }
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

    private Bundle findBundleWithPid(String servicePid) {
        try {
            ServiceReference[] refs = bundleContext.getServiceReferences(ManagedService.class.getName(), null);
            if (refs != null) {
                for (ServiceReference ref : refs) {
                    if (servicePid.equals(ref.getProperty(Constants.SERVICE_PID))) {
                        return ref.getBundle();
                    }
                }
            }
        } catch (InvalidSyntaxException e) {
            throw new StudyCalendarError("Not possible -- no filter provided", e);
        }
        return null;
    }

    public <T> T getRequiredService(Class<T> serviceType) {
        return getService(serviceType, true);
    }

    public <T> T getOptionalService(Class<T> serviceType) {
        return getService(serviceType, false);
    }

    @SuppressWarnings({ "unchecked" })
    private <T> T getService(Class<T> serviceType, boolean required) {
        ServiceReference ref = bundleContext.getServiceReference(serviceType.getName());
        if (ref == null) {
            if (required) {
                throw new StudyCalendarSystemException(
                    "Service %s not available in the OSGi layer", serviceType.getName());
            } else {
                return null;
            }
        }
        return (T) membrane.farToNear(bundleContext.getService(ref));
    }

    @SuppressWarnings({ "unchecked" })
    public <T> List<T> getServices(Class<T> serviceType) {
        ServiceReference[] refs;
        try {
            refs = bundleContext.getServiceReferences(serviceType.getName(), null);
        } catch (InvalidSyntaxException e) {
            // There's no filter string, so...
            throw new StudyCalendarError("This should not be possible", e);
        }

        if (refs == null) {
            return Collections.emptyList();
        } else {
            List<T> services = new ArrayList<T>(refs.length);
            for (ServiceReference ref : refs) {
                Object service = bundleContext.getService(ref);
                if (service == null) {
                    log.warn("One of the service references for {} pointed to an invalid service (ServiceReference: {})",
                        serviceType.getName(), ref);
                } else {
                    services.add((T) membrane.farToNear(service));
                }
            }
            return services;
        }
    }

    ////// CONFIGURATION

    @Required
    public void setMembrane(Membrane membrane) {
        this.membrane = membrane;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}
