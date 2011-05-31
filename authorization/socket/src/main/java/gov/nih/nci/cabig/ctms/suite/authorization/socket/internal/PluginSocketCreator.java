package gov.nih.nci.cabig.ctms.suite.authorization.socket.internal;

import gov.nih.nci.cabig.ctms.CommonsSystemException;
import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteAuthorizationSource;
import gov.nih.nci.security.AuthorizationManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class PluginSocketCreator implements ServiceListener {
    private final Logger log = LoggerFactory.getLogger(getClass());

    // map from SuiteAuthorizationSource ref to the registered Socket's reg
    private Map<ServiceReference, ServiceRegistration> adapters;
    private final BundleContext bundleContext;

    public PluginSocketCreator(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        adapters = new HashMap<ServiceReference, ServiceRegistration>();
    }

    /**
     * Creates sockets for any sources that existed before the creator was registered.
     */
    public void init() {
        ServiceReference[] existingSourceRefs;
        try {
            existingSourceRefs = bundleContext.getAllServiceReferences(SuiteAuthorizationSource.class.getName(), null);
        } catch (InvalidSyntaxException e) {
            throw new CommonsSystemException("No filter provided, so this shouldn't be possible", e);
        }

        if (existingSourceRefs != null) {
            for (ServiceReference sourceRef : existingSourceRefs) register(sourceRef);
        }
    }

    /**
     * Create a socket when a new source is registered and removes the socket when the source is
     * unregistered.
     */
    public void serviceChanged(ServiceEvent serviceEvent) {
        switch (serviceEvent.getType()) {
            case ServiceEvent.REGISTERED:
                register(serviceEvent);
                break;
            case ServiceEvent.UNREGISTERING:
                unregister(serviceEvent);
                break;
        }
    }

    private void register(ServiceEvent registerEvent) {
        register(registerEvent.getServiceReference());
    }

    @SuppressWarnings( { "RawUseOfParameterizedType", "unchecked" })
    private void register(ServiceReference sourceRef) {
        Object service = bundleContext.getService(sourceRef);
        if (service instanceof SuiteAuthorizationSource) {
            log.info("Registering authorization socket for {}", service);
            Integer sourceRank = (Integer) sourceRef.getProperty(Constants.SERVICE_RANKING);
            Dictionary props = new Hashtable();
            if (sourceRank != null) {
                props.put(Constants.SERVICE_RANKING, sourceRank);
            }
            ServiceRegistration socketReg = bundleContext.registerService(
                AuthorizationManager.class.getName(),
                new SuiteAuthorizationSocket((SuiteAuthorizationSource) service),
                props);
            adapters.put(sourceRef, socketReg);
        }
    }

    private void unregister(ServiceEvent serviceEvent) {
        ServiceRegistration socketReg = adapters.get(serviceEvent.getServiceReference());
        if (socketReg != null) {
            socketReg.unregister();
        }
    }
}
