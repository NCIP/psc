package edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.BeansException;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.mvc.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.acegisecurity.intercept.web.FilterSecurityInterceptor;
import org.acegisecurity.intercept.web.PathBasedFilterInvocationDefinitionMap;
import org.acegisecurity.ConfigAttributeDefinition;
import org.acegisecurity.SecurityConfig;
import gov.nih.nci.cabig.ctms.tools.spring.ControllerUrlResolver;
import gov.nih.nci.cabig.ctms.tools.spring.ResolvedControllerReference;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;

/**
 * @author John Dzak
 */
public class ControllerSecureUrlCreator implements BeanFactoryPostProcessor, Ordered {
    private static Logger log = LoggerFactory.getLogger(ControllerSecureUrlCreator.class);
    private FilterSecurityInterceptor filterInvocationInterceptor;
    private ControllerUrlResolver urlResolver;
    private PathBasedFilterInvocationDefinitionMap pathMap;

    // Must occur after BeanNameControllerUrlResolver
    public int getOrder() { return 3; }



    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        pathMap = new PathBasedFilterInvocationDefinitionMap();
        String[] controllerNames = beanFactory.getBeanNamesForType(Controller.class, false, false);
        for (String controllerName : controllerNames) {
            ResolvedControllerReference controller = urlResolver.resolve(controllerName);
            ConfigAttributeDefinition groupNames = getRequiredProtectionGroupNames(controller);
            if (groupNames != null) {
                pathMap.addSecureUrl(new ApacheAntPattern(controller.getUrl(true)).toString(), groupNames);
            }
        }
        filterInvocationInterceptor.setObjectDefinitionSource(pathMap);
    }

    private ConfigAttributeDefinition getRequiredProtectionGroupNames(ResolvedControllerReference controller) {
        Class<? extends Controller> clazz = controller.getControllerClass();
        AccessControl ac = clazz.getAnnotation(AccessControl.class);
        Role[] roles = Role.values();
        if (ac != null) {
             roles = ac.roles();
        }

        ConfigAttributeDefinition def = new ConfigAttributeDefinition();
        for (Role role: roles) {
            def.addConfigAttribute(new SecurityConfig(role.csmGroup()));
        }
        return def;
    }

    private class ApacheAntPattern {
        String ANT_SUFFIX = "/**";
        String url;

        public ApacheAntPattern(String url) {
            this.url = url;
        }

        public String toString() {
            return url + ANT_SUFFIX;
        }
    }

        ////// CONFIGURATION

    @Required
    public void setfilterInvocationInterceptor(FilterSecurityInterceptor filterInvocationInterceptor) {
        this.filterInvocationInterceptor = filterInvocationInterceptor;
    }

    @Required
    public void setUrlResolver(ControllerUrlResolver urlResolver) {
        this.urlResolver = urlResolver;
    }
}
