package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.Finder;
import org.springframework.beans.factory.BeanFactory;

/**
 * @author Rhett Sutphin
 */
public class PscBeanNameRouter extends BeanNameRouter {
    @Override
    protected Finder createFinder(BeanFactory factory, String beanName) {
        return new AuthorizingFinder(factory, beanName);
    }
}
