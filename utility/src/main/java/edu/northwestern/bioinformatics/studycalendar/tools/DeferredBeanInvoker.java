/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * An {@link InvocationHandler} which assists in creating proxies that immediately provide
 * some interface although the concrete implementation is not available yet.
 * <p>
 * This is used in PSC to smooth over gaps between legacy DI-oriented objects and OSGi-exported
 * services.
 *
 * @author Rhett Sutphin
 */
public class DeferredBeanInvoker implements InvocationHandler {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private String className;
    private Object bean;

    public DeferredBeanInvoker(String className) {
        this.className = className;
    }

    public synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("toString".equals(method.getName())) {
            return String.format("DeferredBean for %s", bean);
        } else if (bean == null) {
            Object rv = defaultReturnValue(method);
            log.debug(
                "Cannot invoke method {} on service {} because it is not available yet.  Returning default value {}.",
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
