package edu.northwestern.bioinformatics.studycalendar.core;

import edu.northwestern.bioinformatics.studycalendar.database.StudyCalendarDbTestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.orm.hibernate3.support.OpenSessionInViewInterceptor;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public abstract class DaoTestCase extends StudyCalendarDbTestCase {

    protected MockHttpServletRequest request = new MockHttpServletRequest();
    protected MockHttpServletResponse response = new MockHttpServletResponse();
    protected WebRequest webRequest = new ServletWebRequest(request);
    private boolean shouldFlush = true;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        beginSession();
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            endSession();
        } finally {
            super.tearDown();
        }
    }

    @Override
    public void runBare() throws Throwable {
        setUp();
        try {
            runTest();
        }
        catch (Throwable throwable) {
            shouldFlush = false;
            throw throwable;
        }
        finally {
            tearDown();
        }
    }

    private void beginSession() {
        log.info("-- beginning DaoTestCase interceptor session --");
        for (OpenSessionInViewInterceptor interceptor : interceptors()) {
            interceptor.preHandle(webRequest);
        }
    }

    private void endSession() {
        log.info("--    ending DaoTestCase interceptor session --");
        for (OpenSessionInViewInterceptor interceptor : reverseInterceptors()) {
            if (shouldFlush) {
                interceptor.postHandle(webRequest, null);
            }
            interceptor.afterCompletion(webRequest, null);
        }
    }

    protected void interruptSession() {
        endSession();
        log.info("-- interrupted DaoTestCase session --");
        beginSession();
    }

    private List<OpenSessionInViewInterceptor> interceptors() {
        return Arrays.asList((OpenSessionInViewInterceptor) getApplicationContext().getBean(
            "openSessionInViewInterceptor"));
    }

    private List<OpenSessionInViewInterceptor> reverseInterceptors() {
        List<OpenSessionInViewInterceptor> interceptors = interceptors();
        Collections.reverse(interceptors);
        return interceptors;
    }

    @Override
    protected DataSource getDataSource() {
        return (DataSource) getApplicationContext().getBean("dataSource");
    }

    public static ApplicationContext getApplicationContext() {
        return StudyCalendarTestCase.getDeployedApplicationContext();
    }
}
