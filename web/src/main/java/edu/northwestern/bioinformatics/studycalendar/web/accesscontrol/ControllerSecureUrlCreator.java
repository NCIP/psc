package edu.northwestern.bioinformatics.studycalendar.web.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.core.osgi.OsgiLayerTools;
import edu.northwestern.bioinformatics.studycalendar.security.FilterSecurityInterceptorConfigurer;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.LegacyModeSwitch;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBasedDictionary;
import gov.nih.nci.cabig.ctms.tools.spring.ControllerUrlResolver;
import gov.nih.nci.cabig.ctms.tools.spring.ResolvedControllerReference;
import org.acegisecurity.GrantedAuthority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.mvc.Controller;

import java.util.Collection;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

/**
 * @author John Dzak
 * @author Rhett Sutphin
 */
@Deprecated // need to remove the acegi filter that depends on the url list, too
public class ControllerSecureUrlCreator implements BeanFactoryPostProcessor, Ordered, FactoryBean {
    private static Logger log = LoggerFactory.getLogger(ControllerSecureUrlCreator.class);
    private ControllerUrlResolver urlResolver;
    private Map<String, GrantedAuthority[]> pathRoleMap;
    private OsgiLayerTools osgiLayerTools;
    private LegacyModeSwitch legacyModeSwitch;
    private ControllerRequiredAuthorityExtractor controllerRequiredAuthorityExtractor;

    // Must occur after BeanNameControllerUrlResolver
    public int getOrder() { return 3; }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        pathRoleMap = new TreeMap<String, GrantedAuthority[]>(new PathComparator());
        String[] controllerNames = beanFactory.getBeanNamesForType(Controller.class, false, false);
        for (String controllerName : controllerNames) {
            ResolvedControllerReference controller = urlResolver.resolve(controllerName);
            GrantedAuthority[] authorities = getAllowedAuthorities(beanFactory, controllerName, controller);

            String url = controller.getUrl(true);
            if (log.isDebugEnabled()) {
                log.debug("Controller {} ({}) requires one of the roles {}",
                    new Object[] { controller.getControllerClass(), url, authorities });
            }
            pathRoleMap.put(new ApacheAntPattern(url).toString(), authorities);
        }

        Dictionary upd = createOsgiDictionary();
        String servicePid = FilterSecurityInterceptorConfigurer.SERVICE_PID;

        osgiLayerTools.updateConfiguration(upd, servicePid);
    }

    private GrantedAuthority[] getAllowedAuthorities(ConfigurableListableBeanFactory beanFactory, String controllerName, ResolvedControllerReference controller) {
        if (legacyModeSwitch.isOn()) {
            return controllerRequiredAuthorityExtractor.
                getAllowedAuthoritiesForController(
                    (Controller) beanFactory.getBean(controllerName, controller.getControllerClass()));
        } else {
            // In the new auth mode, authorization is handled elsewhere
            return PscRole.values();
        }
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    private Dictionary createOsgiDictionary() {
        Collection<String> serializedMap = new Vector<String>();
        for (Map.Entry<String, GrantedAuthority[]> pair : pathRoleMap.entrySet()) {
            StringBuilder v = new StringBuilder(pair.getKey()).append('|');
            for (GrantedAuthority role : pair.getValue()) {
                v.append(role.getAuthority()).append(' ');
            }
            v.deleteCharAt(v.length() - 1);
            serializedMap.add(v.toString());
        }

        Dictionary<String, Object> props = new MapBasedDictionary<String, Object>();
        props.put(FilterSecurityInterceptorConfigurer.PATH_ROLE_MAP_KEY, serializedMap);
        return props;
    }

    public Map<String, GrantedAuthority[]> getPathRoleMap() {
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
    public void setOsgiLayerTools(OsgiLayerTools osgiLayerTools) {
        this.osgiLayerTools = osgiLayerTools;
    }

    @Required
    public void setControllerRequiredAuthorityExtractor(ControllerRequiredAuthorityExtractor controllerRequiredAuthorityExtractor) {
        this.controllerRequiredAuthorityExtractor = controllerRequiredAuthorityExtractor;
    }

    @Required
    public void setLegacyModeSwitch(LegacyModeSwitch legacyModeSwitch) {
        this.legacyModeSwitch = legacyModeSwitch;
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
