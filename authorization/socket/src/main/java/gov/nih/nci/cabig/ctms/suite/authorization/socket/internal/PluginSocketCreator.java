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
        this.bundleContext = context;
        this.adapters = new HashMap<ServiceReference, ServiceRegistration>();
    }

    @SuppressWarnings({ "UnusedDeclaration" })
    protected void deactivate() {
        this.bundleContext = null;
        this.adapters = null;
    }

    @SuppressWarnings({ "UnusedDeclaration", "RawUseOfParameterizedType", "unchecked" })
    protected void createSocket(ServiceReference reference) {
        Object service = bundleContext.getService(reference);
        if (service instanceof SuiteAuthorizationSource) {
            log.info("Registering authorization socket for {}", service);
            Integer sourceRank = (Integer) reference.getProperty(Constants.SERVICE_RANKING);
            Dictionary props = new Hashtable();
            if (sourceRank != null) {
                props.put(Constants.SERVICE_RANKING, sourceRank);
            }
            ServiceRegistration socketReg = bundleContext.registerService(
                AuthorizationManager.class.getName(),
                new SuiteAuthorizationSocket((SuiteAuthorizationSource) service),
                props);
            adapters.put(reference, socketReg);
        }
    }

    @SuppressWarnings({ "UnusedDeclaration" })
    protected void destroySocket(ServiceReference reference) {
        ServiceRegistration socketReg = adapters.get(reference);
        if (socketReg != null) {
            socketReg.unregister();
        }
    }
}
