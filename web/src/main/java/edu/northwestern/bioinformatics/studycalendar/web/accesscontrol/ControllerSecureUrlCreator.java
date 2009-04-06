package edu.northwestern.bioinformatics.studycalendar.web.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.security.FilterSecurityInterceptorConfigurer;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.Membrane;
import gov.nih.nci.cabig.ctms.tools.spring.ControllerUrlResolver;
import gov.nih.nci.cabig.ctms.tools.spring.ResolvedControllerReference;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.mvc.Controller;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

/**
 * @author John Dzak
 */
public class ControllerSecureUrlCreator implements BeanFactoryPostProcessor, Ordered, FactoryBean {
    private static Logger log = LoggerFactory.getLogger(ControllerSecureUrlCreator.class);
    private ControllerUrlResolver urlResolver;
    private Map<String, Role[]> pathRoleMap;
    private BundleContext bundleContext;
    private Membrane membrane;

    // Must occur after BeanNameControllerUrlResolver
    public int getOrder() { return 3; }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        pathRoleMap = new TreeMap<String, Role[]>(new PathComparator());
        String[] controllerNames = beanFactory.getBeanNamesForType(Controller.class, false, false);
        for (String controllerName : controllerNames) {
            ResolvedControllerReference controller = urlResolver.resolve(controllerName);
            Role[] groupNames = getRequiredRoles(controller);

            String url = controller.getUrl(true);
            if (log.isDebugEnabled()) {
                log.debug("Controller {} ({}) requires one of the roles {}",
                    new Object[] { controller.getControllerClass(), url, groupNames });
            }
            pathRoleMap.put(new ApacheAntPattern(url).toString(), groupNames);
        }

        ServiceReference cmRef = bundleContext.getServiceReference(ConfigurationAdmin.class.getName());
        if (cmRef == null) {
            throw new StudyCalendarSystemException("OSGi CM service not available.  Unable to update secure URL map.");
        } else {
            Bundle fsicBundle = bundleContext.getServiceReference(
                FilterSecurityInterceptorConfigurer.class.getName()).getBundle();
            log.debug("Updating configuration for bundle {} at {}", fsicBundle, fsicBundle.getLocation());
            ConfigurationAdmin cm = (ConfigurationAdmin) membrane.farToNear(bundleContext.getService(cmRef));
            try {
                Configuration fsicConfig = cm.getConfiguration(
                    FilterSecurityInterceptorConfigurer.SERVICE_PID, fsicBundle.getLocation());
                Dictionary upd = createOsgiDictionary();
                log.trace("Updating {} with {}", FilterSecurityInterceptorConfigurer.SERVICE_PID, upd);
                fsicConfig.update(upd);
            } catch (IOException e) {
                throw new StudyCalendarSystemException("IO problem while acquiring configuration to update", e);
            }
        }
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    private Dictionary createOsgiDictionary() {
        Collection<String> serializedMap = new Vector<String>();
        for (Map.Entry<String, Role[]> pair : pathRoleMap.entrySet()) {
            StringBuilder v = new StringBuilder(pair.getKey()).append('|');
            for (Role role : pair.getValue()) {
                v.append(role.csmGroup()).append(' ');
            }
            v.deleteCharAt(v.length() - 1);
            serializedMap.add(v.toString());
        }

        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(FilterSecurityInterceptorConfigurer.PATH_ROLE_MAP_KEY, serializedMap);
        return props;
    }

    private Role[] getRequiredRoles(ResolvedControllerReference controller) {
        Class<? extends Controller> clazz = controller.getControllerClass();
        AccessControl ac = clazz.getAnnotation(AccessControl.class);
        Role[] roles = Role.values();
        if (ac != null) {
             roles = ac.roles();
        }
        return roles;
    }

    public Map<String, Role[]> getPathRoleMap() {
        return pathRoleMap;
    }

    public Object getObject() throws Exception {
        return getPathRoleMap();
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public Class getObjectType() {
        return Map.class;
    }

    public boolean isSingleton() {
        return true;
    }

    ////// CONFIGURATION

    @Required
    public void setUrlResolver(ControllerUrlResolver urlResolver) {
        this.urlResolver = urlResolver;
    }

    @Required
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Required
    public void setMembrane(Membrane membrane) {
        this.membrane = membrane;
    }

    private class ApacheAntPattern {
        String ANT_SUFFIX = "/**";
        String url;

        public ApacheAntPattern(String url) {
            this.url = url;
        }

        @Override
        public String toString() {
            return url + ANT_SUFFIX;
        }
    }

    /**
     * Orders strings in descending order by length, then descending by content.  Used to
     * put the longest paths first for comparisons.
     */
    public static class PathComparator implements Comparator<String> {
        public int compare(String o1, String o2) {
            int lengthDiff = o2.length() - o1.length();
            if (lengthDiff != 0) return lengthDiff;
            return o2.compareTo(o1);
        }
    }
}
