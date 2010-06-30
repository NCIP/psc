package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.LegacyModeSwitch;
import org.restlet.Finder;
import org.restlet.ext.spring.SpringBeanRouter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Rhett Sutphin
 */
public class PscBeanNameRouter extends SpringBeanRouter {
    private LegacyModeSwitch legacyModeSwitch;

    @Override
    protected Finder createFinder(BeanFactory factory, String beanName) {
        return new AuthorizingFinder(factory, beanName, legacyModeSwitch.isOn());
    }

    @Required @Deprecated
    public void setLegacyModeSwitch(LegacyModeSwitch legacyModeSwitch) {
        this.legacyModeSwitch = legacyModeSwitch;
    }
}
