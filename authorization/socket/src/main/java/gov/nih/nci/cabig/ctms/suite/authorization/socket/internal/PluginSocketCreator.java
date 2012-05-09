package gov.nih.nci.cabig.ctms.suite.authorization.socket.internal;

import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteAuthorizationSource;
import gov.nih.nci.security.AuthorizationManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * Declarative Services component used to automatically create and register
 * {@link SuiteAuthorizationSocket} instances as {@link SuiteAuthorizationSource}s are found.
 *
 * @author Rhett Sutphin
 */
public class PluginSocketCreator {
    private final Logger log = LoggerFactory.getLogger(getClass());

    // map from SuiteAuthorizationSource ref to the registered Socket's reg
    private Map<ServiceReference, ServiceRegistration> adapters;
    private BundleContext bundleContext;

    @SuppressWarnings({ "UnusedDeclaration" })
    protected void activate(BundleContext context) {
        log.debug("Activating");

        this.bundleContext = context;
        this.adapters = new HashMap<ServiceReference, ServiceRegistration>();

        log.debug("Activated");
    }

    @SuppressWarnings({ "UnusedDeclaration" })
    protected synchronized void deactivate(int reason) {
        log.debug("Deactivating with reason code {}", reason);

        this.bundleContext = null;

        if (this.adapters != null && !this.adapters.isEmpty()) {
            log.info("Deactivating while {} socket(s) are live; unregistering sockets",
                this.adapters.size());
            for (ServiceRegistration registration : this.adapters.values()) {
                destroyRegistration(registration);
            }
        }
        this.adapters = null;

        log.debug("Deactivated");
    }

    @SuppressWarnings({ "UnusedDeclaration", "RawUseOfParameterizedType", "unchecked" })
    protected void createSocket(ServiceReference reference) {
        if (bundleContext == null) {
            log.warn(
                "createSocket called for {} before activation or after deactivation. No changes made.",
                reference);
            return;
        }

        Object service = bundleContext.getService(reference);
        if (service instanceof SuiteAuthorizationSource) {
            log.info("Registering authorization socket for {}", service);
            Dictionary props = new Hashtable();

            Integer sourceRank = (Integer) reference.getProperty(Constants.SERVICE_RANKING);
            if (sourceRank != null) {
                props.put(Constants.SERVICE_RANKING, sourceRank);
            }

            String sourcePid = (String) reference.getProperty(Constants.SERVICE_PID);
            if (sourcePid != null) {
                props.put(Constants.SERVICE_PID, "SuiteAuthorizationSocket for " + sourcePid);
            }

            ServiceRegistration socketReg = bundleContext.registerService(
                AuthorizationManager.class.getName(),
                new SuiteAuthorizationSocket((SuiteAuthorizationSource) service),
                props);
            adapters.put(reference, socketReg);
        }
    }

    @SuppressWarnings({ "UnusedDeclaration" })
    protected synchronized void destroySocket(ServiceReference reference) {
        if (adapters == null) {
            log.debug(
                "destroySocket called after deactivation or before activation. No changes made.");
            return;
        }
        ServiceRegistration socketReg = adapters.get(reference);
        if (socketReg != null) {
            destroyRegistration(socketReg);
        }
    }

    protected void destroyRegistration(ServiceRegistration socketReg) {
        socketReg.unregister();
    }
}
