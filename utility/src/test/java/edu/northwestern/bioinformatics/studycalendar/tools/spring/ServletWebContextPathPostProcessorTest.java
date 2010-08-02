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
        target.registerSingleton("aware", new TestContextPathAwareItem());
    }

    public void testContextPathSetAfterFirstRequestSet() throws Exception {
        processor.postProcessBeanFactory(target);
        processor.registerRequest(request);

        TestContextPathAwareItem item = (TestContextPathAwareItem) target.getBean("aware");
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

        TestContextPathAwareItem item = (TestContextPathAwareItem) target.getBean("aware");
        assertEquals("Context path overridden", CONTEXT_PATH, item.getContextPath());
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
}
