package edu.northwestern.bioinformatics.studycalendar.testing;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.context.ApplicationContext;

import java.util.Comparator;

/**
 * @author Rhett Sutphin
 */
// TODO: re-merge this with AbstractTestCase
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

    public static <T extends Comparable<T>> void assertOrder(T first, T second) {
        assertNegative(first.compareTo(second));
        assertPositive(second.compareTo(first));
    }

    public static <T> void assertOrder(Comparator<T> comparator, T first, T second) {
        assertNegative(comparator.compare(first, second));
        assertPositive(comparator.compare(second, first));
    }

    ////// MOCK REGISTRATION AND HANDLING


    protected void tearDown() throws Exception {
        super.tearDown();
        ApplicationSecurityManager.removeUserSession();
    }

}
