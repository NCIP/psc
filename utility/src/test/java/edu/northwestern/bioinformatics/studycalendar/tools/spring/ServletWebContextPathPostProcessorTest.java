package edu.northwestern.bioinformatics.studycalendar.tools.spring;

import junit.framework.TestCase;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Collections;

/**
 * @author Rhett Sutphin
 */
public class ServletWebContextPathPostProcessorTest extends TestCase {
    private static final String CONTEXT_PATH = "/frob";
    private static final String SCHEME = "http";
    private static final String SERVER_NAME = "testServer";
    private static final int SERVER_PORT = 1111;
    private static final String APPLICATION_PATH = "http://testServer:1111/frob";

    private ServletWebContextPathPostProcessor processor;
    private ConfigurableListableBeanFactory target;
    private MockHttpServletRequest request;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        request = new MockHttpServletRequest();
        request.setContextPath(CONTEXT_PATH);

        processor = new ServletWebContextPathPostProcessor();

        DefaultListableBeanFactory parent = new DefaultListableBeanFactory();
        parent.registerSingleton("parentAware", new TestContextPathAwareItem());

        target = new DefaultListableBeanFactory(parent);
        target.registerSingleton("someMap", Collections.singletonMap("foo", "baz"));
        target.registerSingleton("contextAware", new TestContextPathAwareItem());
        target.registerSingleton("applicationAware", new TestApplicationPathAwareItem());
    }

    public void testContextPathSetAfterFirstRequestSet() throws Exception {
        processor.postProcessBeanFactory(target);
        processor.registerRequest(request);

        TestContextPathAwareItem item = (TestContextPathAwareItem) target.getBean("contextAware");
        assertEquals("Context path not set", CONTEXT_PATH, item.getContextPath());
    }

    public void testAncestorsContextPathsSet() throws Exception {
        processor.postProcessBeanFactory(target);
        processor.registerRequest(request);

        TestContextPathAwareItem item = (TestContextPathAwareItem) target.getBean("parentAware");
        assertEquals("Context path not set for parent", CONTEXT_PATH, item.getContextPath());
    }

    public void testContextPathOnlySetOnce() throws Exception {
        MockHttpServletRequest request2 = new MockHttpServletRequest();
        request2.setContextPath("/different");

        processor.postProcessBeanFactory(target);
        processor.registerRequest(request);
        processor.registerRequest(request2);

        TestContextPathAwareItem item = (TestContextPathAwareItem) target.getBean("contextAware");
        assertEquals("Context path overridden", CONTEXT_PATH, item.getContextPath());
    }

    public void testApplicationPathSetAfterFirstRequestSet() throws Exception {
        request.setScheme(SCHEME);
        request.setServerName(SERVER_NAME);
        request.setServerPort(SERVER_PORT);

        processor.postProcessBeanFactory(target);
        processor.registerRequest(request);
        TestApplicationPathAwareItem item = (TestApplicationPathAwareItem) target.getBean("applicationAware");
        assertEquals("Application path not set", APPLICATION_PATH, item.getApplicationPath());
    }

    private static class TestContextPathAwareItem implements WebContextPathAware {
        private String contextPath;

        public String getContextPath() {
            return contextPath;
        }

        public void setWebContextPath(String contextPath) {
            this.contextPath = contextPath;
        }
    }

    private static class TestApplicationPathAwareItem implements ApplicationPathAware {
        private String applicationPath;

        public String getApplicationPath() {
            return applicationPath;
        }

        public void setApplicationPath(String applicationPath) {
            this.applicationPath = applicationPath;
        }
    }
}
