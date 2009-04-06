package edu.northwestern.bioinformatics.studycalendar.osgi.hostservices.internal;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.osgi.hostservices.HostBeans;
import org.acegisecurity.userdetails.UserDetailsService;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Bridge between the OSGi layer and beans initialized in the host application context.
 * There is an instance
 *
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "RawUseOfParameterizedType" })
public class HostBeansImpl implements HostBeans {
    private static final Logger log = LoggerFactory.getLogger(HostBeansImpl.class);

    private static final Collection<Class<?>> EXPOSED_BEANS = Arrays.asList(
        DataSource.class, UserDetailsService.class
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
                serviceInterface.getName(), createDeferredBeanProxy(serviceInterface), null);
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

    public void setUserDetailsService(UserDetailsService userDetailsService) {
        invokers.get(UserDetailsService.class).setBean(userDetailsService);
    }

    private static class DeferredBeanInvoker implements InvocationHandler {
        private String className;
        private Object bean;

        private DeferredBeanInvoker(String className) {
            this.className = className;
        }

        public synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (bean == null) {
                throw new StudyCalendarSystemException(
                    "Cannot invoke method on host bean %s because it is not available yet", className);
            } else {
                log.trace("Invoking {} on {}@{}", new Object[] { method, bean.getClass(), System.identityHashCode(bean) });
                return method.invoke(bean, args);
            }
        }

        public synchronized void setBean(Object bean) {
            this.bean = bean;
        }
    }
}
