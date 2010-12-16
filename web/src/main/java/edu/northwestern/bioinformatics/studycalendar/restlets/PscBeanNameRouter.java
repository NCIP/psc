package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.ext.spring.SpringBeanRouter;
import org.restlet.resource.Finder;
import org.springframework.beans.factory.BeanFactory;

/**
 * @author Rhett Sutphin
 */
public class PscBeanNameRouter extends SpringBeanRouter {
    @Override
    protected Finder createFinder(BeanFactory factory, String beanName) {
        return new AuthorizingFinder(this, factory, beanName);
    }
}
