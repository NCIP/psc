package edu.northwestern.bioinformatics.studycalendar.testing;

import junit.framework.TestCase;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.HashSet;

import org.easymock.classextension.EasyMock;

/**
 * @author Rhett Sutphin
 */
public abstract class StudyCalendarTestCase extends TestCase {
    protected Set<Object> mocks = new HashSet<Object>();

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
