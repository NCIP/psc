package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.Router;
import org.restlet.Finder;
import org.restlet.resource.Resource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.BeanFactory;

/**
 * Restlet {@link Router} which behaves like Spring's
 * {@link org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping}.
 *
 * @author Rhett Sutphin
 */
public class BeanNameRouter extends Router implements BeanFactoryPostProcessor {
    public void postProcessBeanFactory(ConfigurableListableBeanFactory factory) throws BeansException {
        String[] names = factory.getBeanNamesForType(Resource.class, true, true);
        for (String name : names) {
            String uri = resolveUrl(name, factory);
            if (uri != null) {
                attach(uri, createFinder(factory, name));
            }
        }
    }

    /**
     * Overide this to change the type of Finder created for each resource bean.
     * Default is {@link SpringBeanFinder}.
     */
    protected Finder createFinder(BeanFactory factory, String beanName) {
        return new SpringBeanFinder(factory, beanName);
    }

    /**
     * Uses this first alias for this bean that starts with '/'. This mimics the behavior
     * of {@link org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping}.
     */
    private String resolveUrl(String resourceName, ConfigurableListableBeanFactory factory) {
        for (String alias : factory.getAliases(resourceName)) {
            if (alias.startsWith("/")) return alias;
        }
        return null;
    }
}
