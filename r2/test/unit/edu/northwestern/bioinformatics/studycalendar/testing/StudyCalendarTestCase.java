package edu.northwestern.bioinformatics.studycalendar.testing;

import edu.nwu.bioinformatics.commons.testing.CoreTestCase;

import junit.framework.TestCase;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.HashSet;

import org.easymock.classextension.EasyMock;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author Rhett Sutphin
 */
public abstract class StudyCalendarTestCase extends CoreTestCase {
    private static ApplicationContext applicationContext = null;
    protected Set<Object> mocks = new HashSet<Object>();

    public static ApplicationContext getDeployedApplicationContext() {
        synchronized (StudyCalendarTestCase.class) {
            if (applicationContext == null) {
                applicationContext = ContextTools.createDeployedApplicationContext();
            }
            return applicationContext;
        }
    }

    public static void assertEqualArrays(Object[] expected, Object[] actual) {
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < actual.length; i++) {
            assertEquals("Mismatch at index " + i, expected[i], actual[i]);
        }
    }

    ////// MOCK REGISTRATION AND HANDLING

    protected <T> T registerMockFor(Class<T> forClass) {
        return registered(EasyMock.createMock(forClass));
    }

    protected <T> T registerMockFor(Class<T> forClass, Method[] methodsToMock) {
        return registered(EasyMock.createMock(forClass, methodsToMock));
    }

    protected void replayMocks() {
        for (Object mock : mocks) EasyMock.replay(mock);
    }

    protected void verifyMocks() {
        for (Object mock : mocks) EasyMock.verify(mock);
    }

    protected void resetMocks() {
        for (Object mock : mocks) EasyMock.reset(mock);
    }

    private <T> T registered(T mock) {
        mocks.add(mock);
        return mock;
    }
}
