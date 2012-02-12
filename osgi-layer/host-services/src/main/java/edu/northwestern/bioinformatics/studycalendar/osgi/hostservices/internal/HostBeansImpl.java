package edu.northwestern.bioinformatics.studycalendar.osgi.hostservices.internal;

import edu.northwestern.bioinformatics.studycalendar.osgi.hostservices.HostBeans;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUserDetailsService;
import edu.northwestern.bioinformatics.studycalendar.tools.DeferredBeanInvoker;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import org.apache.felix.cm.PersistenceManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Bridge between the OSGi layer and beans initialized in the host application context.
 *
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "RawUseOfParameterizedType" })
public class HostBeansImpl implements HostBeans {
    private static final Logger log = LoggerFactory.getLogger(HostBeansImpl.class);

    @SuppressWarnings({ "unchecked" })
    private static final Collection<Class<?>> EXPOSED_BEANS = Arrays.asList(
        PscUserDetailsService.class, PersistenceManager.class
    );

    private Map<Class<?>, DeferredBeanInvoker> invokers;

    public HostBeansImpl() {
        invokers = createInvokers();
    }

    private Map<Class<?>, DeferredBeanInvoker> createInvokers() {
        Map<Class<?>, DeferredBeanInvoker> map = new HashMap<Class<?>, DeferredBeanInvoker>();
        for (Class serviceInterface : EXPOSED_BEANS) {
            map.put(serviceInterface, new DeferredBeanInvoker(serviceInterface.getName()));
        }
        return map;
    }

    public void registerServices(BundleContext context) {
        for (Class<?> serviceInterface : invokers.keySet()) {
            context.registerService(
                serviceInterface.getName(), createDeferredBeanProxy(serviceInterface),
                new MapBuilder<String, Object>().put(Constants.SERVICE_RANKING, Integer.MIN_VALUE).toDictionary());
        }
    }

    private synchronized Object createDeferredBeanProxy(Class serviceInterface) {
        return Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class[] { serviceInterface },
            invokers.get(serviceInterface));
    }

    public void setPscUserDetailsService(PscUserDetailsService userDetailsService) {
        invokers.get(PscUserDetailsService.class).setBean(userDetailsService);
    }

    public void setPersistenceManager(PersistenceManager persistenceManager) {
        invokers.get(PersistenceManager.class).setBean(persistenceManager);
    }
}
