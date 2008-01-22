package edu.northwestern.bioinformatics.studycalendar.testing;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Rhett Sutphin
 */
public class ContextTools {
    public static final String[] DEPLOYED_CONFIG_LOCATIONS = new String[] {
        "classpath*:/applicationContext*.xml"
    };

    public static ApplicationContext createDeployedApplicationContext() {
        return new ClassPathXmlApplicationContext(DEPLOYED_CONFIG_LOCATIONS);
    }

    private ContextTools() { }
}
