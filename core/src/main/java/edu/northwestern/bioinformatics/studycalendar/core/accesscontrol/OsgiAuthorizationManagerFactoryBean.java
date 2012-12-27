/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.core.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.core.osgi.OsgiLayerTools;
import gov.nih.nci.security.AuthorizationManager;
import gov.nih.nci.security.exceptions.CSTransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * A factory which produces a singleton proxy AuthorizationManager that delegates
 * to the best version found in the OSGi plugin layer for each call.
 *
 * @author Rhett Sutphin
 */
public class OsgiAuthorizationManagerFactoryBean implements FactoryBean {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private OsgiLayerTools osgiLayerTools;
    private AuthorizationManager proxy;

    @SuppressWarnings( { "RawUseOfParameterizedType" })
    public Class getObjectType() {
        return AuthorizationManager.class;
    }

    public boolean isSingleton() {
        return true;
    }

    public Object getObject() throws Exception {
        if (proxy == null) createProxy();
        return proxy;
    }

    private synchronized void createProxy() {
        if (proxy != null) return;

        proxy = (AuthorizationManager) Proxy.newProxyInstance(
            getClass().getClassLoader(),
            new Class<?>[] { AuthorizationManager.class, PossiblyReadOnlyAuthorizationManager.class },
            new OsgiDelegatedAuthorizationManagerHandler(osgiLayerTools));
    }

    ////// CONFIGURATION

    @Required
    public void setOsgiLayerTools(OsgiLayerTools osgiLayerTools) {
        this.osgiLayerTools = osgiLayerTools;
    }

    ////// INNER CLASSES

    private static class OsgiDelegatedAuthorizationManagerHandler implements InvocationHandler {
        private final Logger log = LoggerFactory.getLogger(getClass());

        private final OsgiLayerTools osgiLayerTools;
        private Method isReadOnlyMethod;
        private Map<AuthorizationManager, Boolean> reviewedForReadOnly;

        public OsgiDelegatedAuthorizationManagerHandler(OsgiLayerTools osgiLayerTools) {
            this.osgiLayerTools = osgiLayerTools;
            reviewedForReadOnly = new IdentityHashMap<AuthorizationManager, Boolean>();
            try {
                isReadOnlyMethod = PossiblyReadOnlyAuthorizationManager.class.getMethod("isReadOnly");
            } catch (NoSuchMethodException e) {
                throw new StudyCalendarError("It does exist.", e);
            }
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.equals(isReadOnlyMethod)) {
                return isReadOnly(currentAuthorizationManager());
            } else {
                return invokeMethodOnAuthorizationManager(method, args);
            }
        }

        private Object invokeMethodOnAuthorizationManager(Method method, Object[] args) throws Throwable {
            try {
                return method.invoke(currentAuthorizationManager(), args);
            } catch (InvocationTargetException ite) {
                throw ite.getCause();
            }
        }

        private boolean isReadOnly(AuthorizationManager authorizationManager) {
            if (!reviewedForReadOnly.containsKey(authorizationManager)) {
                synchronized (this) {
                    reviewedForReadOnly.
                        put(authorizationManager, checkReadOnly(authorizationManager));
                }
            }
            return reviewedForReadOnly.get(authorizationManager);
        }

        private Boolean checkReadOnly(AuthorizationManager authorizationManager) {
            log.debug("Determining if {} is a read-only authorization manager", authorizationManager);
            try {
                authorizationManager.removeRole("foobarquux");
                log.debug("- It threw no exception, so it is not read-only");
                return false;
            } catch (CSTransactionException cst) {
                log.debug("- It threw a declared exception, so it is not read-only");
                return false;
            } catch (UnsupportedOperationException uoe) {
                log.debug("- It threw UnsupportedOperationException, so it is read-only");
                return true;
            } catch (RuntimeException re) {
                log.debug("- It threw an unexpected exception, so it is not read-only");
                return false;
            }
        }

        private AuthorizationManager currentAuthorizationManager() {
            return osgiLayerTools.getRequiredService(AuthorizationManager.class);
        }
    }
}
