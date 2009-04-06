package edu.northwestern.bioinformatics.studycalendar.osgi.hostservices.internal;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.osgi.hostservices.HostBeans;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import org.acegisecurity.userdetails.UserDetailsService;
import org.osgi.framework.BundleContext;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Bridge between the OSGi layer and beans initialized in the host application context.
 * There is an instance
 *
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "RawUseOfParameterizedType" })
public class HostBeansImpl implements HostBeans {
    private static final Map<Class, String> EXPOSED_BEANS = new MapBuilder<Class, String>().
        put(DataSource.class, "dataSource").
        put(UserDetailsService.class, "pscUserDetailsService").
        toMap();

    private Collection<DeferredBeanInvoker> invokers;

    public HostBeansImpl() {
        invokers = new ArrayList<DeferredBeanInvoker>();
    }

    public void registerServices(BundleContext context) {
        for (Class serviceInterface : EXPOSED_BEANS.keySet()) {
            context.registerService(
                serviceInterface.getName(), createDeferredBeanProxy(serviceInterface), null);
        }
    }

    public void setHostApplicationContext(ApplicationContext hostContext) {
        for (DeferredBeanInvoker invoker : invokers) {
            invoker.setBean(hostContext.getBean(invoker.getBeanName()));
        }
    }

    private synchronized Object createDeferredBeanProxy(Class serviceInterface) {
        DeferredBeanInvoker invoker = new DeferredBeanInvoker(EXPOSED_BEANS.get(serviceInterface));
        invokers.add(invoker);
        return Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class[] { serviceInterface },
            invoker);
    }

    private static class DeferredBeanInvoker implements InvocationHandler {
        private String beanName;
        private Object bean;

        private DeferredBeanInvoker(String beanName) {
            this.beanName = beanName;
        }

        public String getBeanName() {
            return beanName;
        }

        public synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (bean == null) {
                throw new StudyCalendarSystemException(
                    "Cannot invoke method on host bean %s because it is not available yet", beanName);
            } else {
                return method.invoke(bean, args);
            }
        }

        public synchronized void setBean(Object bean) {
            this.bean = bean;
        }
    }
}
