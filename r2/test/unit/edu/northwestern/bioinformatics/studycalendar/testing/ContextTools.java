package edu.northwestern.bioinformatics.studycalendar.testing;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Rhett Sutphin
 */
public class ContextTools {
    public static ApplicationContext createDeployedApplicationContext() {
        return new ClassPathXmlApplicationContext(new String[] {
            "classpath:/applicationContext.xml",
            "classpath*:/applicationContext-*.xml"
        });
    }

    private ContextTools() { }
}
