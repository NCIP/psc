package edu.northwestern.bioinformatics.studycalendar.osgi.hostservices.internal;

import edu.northwestern.bioinformatics.studycalendar.osgi.hostservices.HostBeans;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUserDetailsService;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import gov.nih.nci.security.AuthorizationManager;
import org.apache.felix.cm.PersistenceManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    private static final Collection<Class<?>> EXPOSED_BEANS = Arrays.asList(
        DataSource.class, PscUserDetailsService.class, PersistenceManager.class, AuthorizationManager.class
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

    public void setDataSource(DataSource dataSource) {
        invokers.get(DataSource.class).setBean(dataSource);
    }

    public void setPscUserDetailsService(PscUserDetailsService userDetailsService) {
        invokers.get(PscUserDetailsService.class).setBean(userDetailsService);
    }

    public void setPersistenceManager(PersistenceManager persistenceManager) {
        invokers.get(PersistenceManager.class).setBean(persistenceManager);
    }

    public void setAuthorizationManager(AuthorizationManager authorizationManager) {
        invokers.get(AuthorizationManager.class).setBean(authorizationManager);
    }

    private static class DeferredBeanInvoker implements InvocationHandler {
        private String className;
        private Object bean;

        private DeferredBeanInvoker(String className) {
            this.className = className;
        }

        public synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if ("toString".equals(method.getName())) {
                return String.format("DeferredBean for %s", bean);
            } else if (bean == null) {
                Object rv = defaultReturnValue(method);
                log.debug(
                    "Cannot invoke method {} on host bean {} because it is not available yet.  Returning default value {}.",
                    new Object[] { method.getName(), className, rv });
                return rv;
            } else {
                log.trace("Invoking {} on {}@{}", new Object[] { method, bean.getClass(), System.identityHashCode(bean) });
                try {
                    return method.invoke(bean, args);
                } catch (InvocationTargetException ite) {
                    throw ite.getCause();
                }
            }
        }

        private Object defaultReturnValue(Method method) {
            if (method.getReturnType().isPrimitive()) {
                if (method.getReturnType() == Boolean.TYPE) {
                    return false;
                } else {
                    return 0;
                }
            } else {
                return null;
            }
        }

        public synchronized void setBean(Object bean) {
            this.bean = bean;
        }
    }
}
