package edu.northwestern.bioinformatics.studycalendar.testing;

import edu.nwu.bioinformatics.commons.testing.CoreTestCase;
import edu.nwu.bioinformatics.commons.ComparisonUtils;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.easymock.classextension.EasyMock;
import org.easymock.IArgumentMatcher;
import org.springframework.context.ApplicationContext;
import org.apache.commons.beanutils.PropertyUtils;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.utils.DayRange;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;

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

    ////// MOCK REGISTRATION AND HANDLING

    protected <T> T registerMockFor(Class<T> forClass) {
        return registered(EasyMock.createMock(forClass));
    }

    protected <T> T registerMockFor(Class<T> forClass, Method... methodsToMock) {
        return registered(EasyMock.createMock(forClass, methodsToMock));
    }

    protected <T extends StudyCalendarDao> T registerDaoMockFor(Class<T> forClass) {
        List<Method> methods = new LinkedList<Method>(Arrays.asList(forClass.getMethods()));
        for (Iterator<Method> iterator = methods.iterator(); iterator.hasNext();) {
            Method method = iterator.next();
            if ("domainClass".equals(method.getName())) {
                iterator.remove();
            }
        }
        return registerMockFor(forClass, methods.toArray(new Method[methods.size()]));
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

    protected static <T> T matchByProperties(T template) {
        EasyMock.reportMatcher(new PropertyMatcher<T>(template));
        return null;
    }

    protected static void assertDayRange(Integer expectedStart, Integer expectedEnd, DayRange actual) {
        assertEquals("Wrong start day", expectedStart, actual.getStartDay());
        assertEquals("Wrong end day", expectedEnd, actual.getEndDay());
    }


    protected void tearDown() throws Exception {
        super.tearDown();
        ApplicationSecurityManager.removeUserSession();
    }

    /**
     * Easymock matcher that compares two objects on their property values
     */
    private static class PropertyMatcher<T> implements IArgumentMatcher {
        private T template;
        private Map<String, Object> templateProperties;

        public PropertyMatcher(T template) {
            this.template = template;
            try {
                templateProperties = PropertyUtils.describe(template);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        public boolean matches(Object argument) {
            try {
                Map<String, Object> argumentProperties = PropertyUtils.describe(argument);
                for (Map.Entry<String, Object> entry : templateProperties.entrySet()) {
                    Object argProp = argumentProperties.get(entry.getKey());
                    Object templProp = entry.getValue();
                    if (!ComparisonUtils.nullSafeEquals(templProp, argProp)) {
                        throw new AssertionError("Argument's " + entry.getKey()
                            + " property doesn't match template's: " + templProp + " != " + argProp);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

            return true;
        }


        public void appendTo(StringBuffer buffer) {
            buffer.append(template).append(" (by properties)");
        }
    }
}
