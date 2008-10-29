package edu.northwestern.bioinformatics.studycalendar.test;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Rhett Sutphin
 */
public class StudyCalendarTestHelper {
    public static final String[] DEPLOYED_CONFIG_LOCATIONS = new String[] {
        "classpath*:/applicationContext*.xml"
    };

    private static ApplicationContext applicationContext = null;
    private static Throwable acLoadingFailure = null;

    /**
     * Creates a new copy of the application context, as it is created in the deployed application.
     */
    public static ApplicationContext createDeployedApplicationContext() {
        return new ClassPathXmlApplicationContext(DEPLOYED_CONFIG_LOCATIONS);
    }

    /**
     * Returns a statically-cached (i.e., at the JVM level) copy of the application context
     * created by {@link #createDeployedApplicationContext()}.
     * <p> 
     * If loading the application context fails for any reason, it caches the failure and does
     * not retry the load.  This makes running a partially failing suite much faster.
     */
    public static ApplicationContext getDeployedApplicationContext() {
        synchronized (StudyCalendarTestHelper.class) {
            if (applicationContext == null && acLoadingFailure == null) {
                try {
                    applicationContext = createDeployedApplicationContext();
                } catch (RuntimeException e) {
                    acLoadingFailure = e;
                    throw e;
                }
            } else if (acLoadingFailure != null) {
                throw new StudyCalendarSystemException("Application context loading already failed; will not retry.", acLoadingFailure);
            }
            return applicationContext;
        }
    }

    // static class
    private StudyCalendarTestHelper() { }
}
