package edu.northwestern.bioinformatics.studycalendar.utils.spring;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.BeansException;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.core.Ordered;

import java.util.Map;
import java.util.HashMap;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;

/**
 * @author Rhett Sutphin
 */
public class BeanNameControllerUrlResolver implements ControllerUrlResolver, BeanFactoryPostProcessor, Ordered {
    private String servletName;
    private Map<String, ResolvedControllerReference> controllers = new HashMap<String, ResolvedControllerReference>();

    public int getOrder() { return 0; }

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String[] controllerNames = beanFactory.getBeanNamesForType(Controller.class, false, false);
        for (String controllerName : controllerNames) {
            controllers.put(controllerName, createResolvedReference(controllerName, beanFactory));
        }
    }

    public ResolvedControllerReference resolve(String controllerBeanName) {
        return controllers.get(controllerBeanName);
    }

    protected ResolvedControllerReference createResolvedReference(String controllerName, ConfigurableListableBeanFactory beanFactory) {
        BeanDefinition def = beanFactory.getBeanDefinition(controllerName);
        return new ResolvedControllerReference(controllerName, def.getBeanClassName(), resolveUrl(controllerName, beanFactory));
    }

    /**
     * Uses this first alias for this bean that starts with '/'.   This is based on the behavior
     * of {@link org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping}.
     */
    private String resolveUrl(String controllerName, ConfigurableListableBeanFactory beanFactory) {
        String[] aliases = beanFactory.getAliases(controllerName);
        for (String alias : aliases) {
            if (alias.startsWith("/")) return '/' + servletName + alias;
        }
        throw new StudyCalendarSystemException(
            "The controller bean " + controllerName + " does not have a URL mapping");
    }

    ////// CONFIGURATION

    public void setServletName(String servletName) {
        this.servletName = servletName;
    }
}
