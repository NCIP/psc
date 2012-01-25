package edu.northwestern.bioinformatics.studycalendar.osgi.plugininstaller.internal;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

/**
 * Waits for the fileinstall service factory to become available.
 *
 * @author Rhett Sutphin
 */
public class PluginInstallerPreqFinder implements ServiceListener {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private ServiceReference cmRef, fiRef;
    private boolean pluginInstallerStarted;

    private BundleContext context;

    public PluginInstallerPreqFinder(BundleContext context) {
        this.context = context;
        cmRef = context.getServiceReference(ConfigurationAdmin.class.getName());
        if (cmRef != null) {
            log.debug("Found CM service at construction: {}", cmRef);
        }

        try {
            ServiceReference[] possibleFiRefs =
                context.getAllServiceReferences(ManagedServiceFactory.class.getName(),
                    String.format("(%s=%s)", Constants.SERVICE_PID, PluginInstaller.FILEINSTALL_FACTORY_PID));
            if (possibleFiRefs != null && possibleFiRefs.length == 1) {
                fiRef = possibleFiRefs[0];
                log.debug("Found fileinstall service at construction: {}", fiRef);
            }
        } catch (InvalidSyntaxException e) {
            throw new Error("This should not be possible", e);
        }

        considerStartingPluginInstaller();
    }

    public void serviceChanged(ServiceEvent event) {
        if (pluginInstallerStarted) { return; }

        if (isAboutCmService(event)) {
            cmRef = serviceReferenceIfAvailable(event);
            log.debug("CM service changed; ref now {}", cmRef);
        } else if (isAboutFileinstallService(event)) {
            fiRef = serviceReferenceIfAvailable(event);
            log.debug("Fileinstall service changed; ref now {}", fiRef);
        }

        considerStartingPluginInstaller();
    }

    private void considerStartingPluginInstaller() {
        if (fiRef != null && cmRef != null) {
            try {
                new PluginInstaller(
                    (ConfigurationAdmin) context.getService(cmRef), fiRef.getBundle()
                ).startWatchers();
                pluginInstallerStarted = true;
            } catch (IOException e) {
                log.error("Could not start deployer plugin installer service", e);
            }
        }
    }

    private ServiceReference serviceReferenceIfAvailable(ServiceEvent event) {
        return (event.getType() != ServiceEvent.UNREGISTERING) ? event.getServiceReference() : null;
    }

    private boolean isAboutFileinstallService(ServiceEvent event) {
        return PluginInstaller.FILEINSTALL_FACTORY_PID.
            equals(event.getServiceReference().getProperty(Constants.SERVICE_PID));
    }

    private boolean isAboutCmService(ServiceEvent event) {
        String[] interfaces =
            (String[]) event.getServiceReference().getProperty(Constants.OBJECTCLASS);
        return Arrays.asList(interfaces).contains(ConfigurationAdmin.class.getName());
    }
}
