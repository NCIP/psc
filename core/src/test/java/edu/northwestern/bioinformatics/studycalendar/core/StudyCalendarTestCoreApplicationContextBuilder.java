package edu.northwestern.bioinformatics.studycalendar.core;

import static edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarApplicationContextBuilder.DEPLOYED_CONFIG_LOCATIONS;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Rhett Sutphin
 */
public class StudyCalendarTestCoreApplicationContextBuilder {
    private static String[] SUPPLEMENTAL_CONFIG_LOCATIONS = {
        "classpath:applicationContext-core-testing-osgi.xml"
    };

    private static final StaticApplicationContextHelper helper = new StaticApplicationContextHelper() {
        @Override
        protected ApplicationContext createApplicationContext() {
            return StudyCalendarTestCoreApplicationContextBuilder.createApplicationContext();
        }
    };

    public static ApplicationContext createApplicationContext() {
        int coreCount = DEPLOYED_CONFIG_LOCATIONS.length;
        String[] loc = new String[coreCount + SUPPLEMENTAL_CONFIG_LOCATIONS.length];
        System.arraycopy(
            DEPLOYED_CONFIG_LOCATIONS,     0, loc, 0,         coreCount);
        System.arraycopy(
            SUPPLEMENTAL_CONFIG_LOCATIONS, 0, loc, coreCount, SUPPLEMENTAL_CONFIG_LOCATIONS.length);
        return new ClassPathXmlApplicationContext(loc);
    }

    public static ApplicationContext getApplicationContext() {
        return helper.getApplicationContext();
    }
}
