package edu.northwestern.bioinformatics.studycalendar.utils.spring;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.web.servlet.mvc.Controller;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;

/**
 * @author Rhett Sutphin
*/
public class ResolvedControllerReference {
    private String beanName;
    private String className;
    private String url;

    public ResolvedControllerReference(String beanName, String className, String url) {
        this.beanName = beanName;
        this.className = className;
        this.url = url;
    }

    /**
     * The context-relative URL for the configured controller.
     */
    public String getUrl() {
        return url;
    }

    public Class<? extends Controller> getControllerClass() {
        try {
            return (Class<? extends Controller>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new StudyCalendarSystemException("Class for controller bean " + beanName + " not found", e);
        }
    }
}
