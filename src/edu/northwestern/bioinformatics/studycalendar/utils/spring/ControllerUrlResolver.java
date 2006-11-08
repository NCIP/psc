package edu.northwestern.bioinformatics.studycalendar.utils.spring;

/**
 * @author Rhett Sutphin
 */
public interface ControllerUrlResolver {
    ResolvedControllerReference resolve(String controllerBeanName);
}
