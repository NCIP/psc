package edu.northwestern.bioinformatics.studycalendar.core;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarApplicationContextBuilder.DEPLOYED_CONFIG_LOCATIONS;

/**
 * @author Rhett Sutphin
 */
public class StudyCalendarTestCoreApplicationContextBuilder {
    private static String[] TEST_CONFIG_LOCATIONS = new String[DEPLOYED_CONFIG_LOCATIONS.length];
    static {
        for (int i = 0; i < DEPLOYED_CONFIG_LOCATIONS.length; i++) {
            String deployedConfigLocation = DEPLOYED_CONFIG_LOCATIONS[i];
            if (deployedConfigLocation.contains("core-osgi")) {
                TEST_CONFIG_LOCATIONS[i] = "classpath:applicationContext-core-testing-osgi.xml";
            } else {
                TEST_CONFIG_LOCATIONS[i] = deployedConfigLocation;
            }
        }
    }

    private static final StaticApplicationContextHelper helper = new StaticApplicationContextHelper() {
        @Override
        protected ApplicationContext createApplicationContext() {
            return StudyCalendarTestCoreApplicationContextBuilder.createApplicationContext();
        }
    };

    public static ApplicationContext createApplicationContext() {
        return new ClassPathXmlApplicationContext(TEST_CONFIG_LOCATIONS, true);
    }

    public static ApplicationContext getApplicationContext() {
        return helper.getApplicationContext();
    }
}
