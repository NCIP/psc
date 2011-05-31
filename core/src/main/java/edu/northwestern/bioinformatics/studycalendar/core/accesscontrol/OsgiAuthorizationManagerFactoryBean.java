package edu.northwestern.bioinformatics.studycalendar.core.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.core.osgi.OsgiLayerTools;
import gov.nih.nci.security.AuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

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
        log.debug("Returning proxy authorization manager {}", proxy);
        return proxy;
    }

    private synchronized void createProxy() {
        if (proxy != null) return;

        proxy = (AuthorizationManager) Proxy.newProxyInstance(
            getClass().getClassLoader(), new Class<?>[] { AuthorizationManager.class },
            new OsgiDelegatedAuthorizationManagerHandler(osgiLayerTools));
    }

    ////// CONFIGURATION

    @Required
    public void setOsgiLayerTools(OsgiLayerTools osgiLayerTools) {
        this.osgiLayerTools = osgiLayerTools;
    }

    ////// INNER CLASSES

    private static class OsgiDelegatedAuthorizationManagerHandler implements InvocationHandler {
        private final OsgiLayerTools osgiLayerTools;

        public OsgiDelegatedAuthorizationManagerHandler(OsgiLayerTools osgiLayerTools) {
            this.osgiLayerTools = osgiLayerTools;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return method.invoke(
                osgiLayerTools.getRequiredService(AuthorizationManager.class),
                args);
        }
    }
}
