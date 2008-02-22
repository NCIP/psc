package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.ext.spring.SpringFinder;
import org.restlet.resource.Resource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

/**
 * An alternative to {@link SpringFinder} which uses spring's BeanFactory mechanism
 * to load a prototype bean by name.
 *
 * @author Rhett Sutphin
 */
public class SpringBeanFinder extends SpringFinder implements BeanFactoryAware {
    private BeanFactory beanFactory;
    private String beanName;

    public SpringBeanFinder() {
    }

    public SpringBeanFinder(BeanFactory beanFactory, String beanName) {
        setBeanFactory(beanFactory);
        setBeanName(beanName);
    }

    @Override
    public Resource createResource() {
        Object resource = getBeanFactory().getBean(getBeanName());
        if (resource == null) {
            throw new NullPointerException("No bean named "+ getBeanName());
        }
        if (!(resource instanceof Resource)) {
            throw new ClassCastException(getBeanName() + " does not resolve to an instance of " + Resource.class.getName());
        }
        return (Resource) resource;
    }

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }
}
