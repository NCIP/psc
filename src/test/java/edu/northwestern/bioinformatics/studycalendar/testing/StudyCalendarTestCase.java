package edu.northwestern.bioinformatics.studycalendar.testing;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.context.ApplicationContext;

/**
 * @author Rhett Sutphin
 */
public abstract class StudyCalendarTestCase extends AbstractTestCase {
    private static ApplicationContext applicationContext = null;
    private static Throwable acLoadingFailure = null;

    static {
        SLF4JBridgeHandler.install();
    }

    public static ApplicationContext getDeployedApplicationContext() {
        synchronized (StudyCalendarTestCase.class) {
            if (applicationContext == null && acLoadingFailure == null) {
                try {
                    applicationContext = ContextTools.createDeployedApplicationContext();
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

    ////// MOCK REGISTRATION AND HANDLING


    protected void tearDown() throws Exception {
        super.tearDown();
        ApplicationSecurityManager.removeUserSession();
    }

}
