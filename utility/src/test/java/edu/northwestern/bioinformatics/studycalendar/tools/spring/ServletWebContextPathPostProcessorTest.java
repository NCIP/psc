package edu.northwestern.bioinformatics.studycalendar.tools.spring;

import junit.framework.TestCase;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.mock.web.MockServletContext;

import java.util.Collections;

/**
 * @author Rhett Sutphin
 */
public class ServletWebContextPathPostProcessorTest extends TestCase {
    private static final String CONTEXT_PATH = "/frob";

    private ServletWebContextPathPostProcessor processor;
    private ConfigurableListableBeanFactory target;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServletContext servletContext = new MockServletContext();
        servletContext.setContextPath(CONTEXT_PATH);

        processor = new ServletWebContextPathPostProcessor();
        processor.setServletContext(servletContext);

        DefaultListableBeanFactory parent = new DefaultListableBeanFactory();
        parent.registerSingleton("parentAware", new TestContextPathAwareItem());

        target = new DefaultListableBeanFactory(parent);
        target.registerSingleton("someMap", Collections.singletonMap("foo", "baz"));
        target.registerSingleton("aware", new TestContextPathAwareItem());
    }

    public void testContextPathSet() throws Exception {
        processor.postProcessBeanFactory(target);

        TestContextPathAwareItem item = (TestContextPathAwareItem) target.getBean("aware");
        assertEquals("Context path not set", CONTEXT_PATH, item.getContextPath());
    }

    public void testAncestorsContextPathsSet() throws Exception {
        processor.postProcessBeanFactory(target);

        TestContextPathAwareItem item = (TestContextPathAwareItem) target.getBean("parentAware");
        assertEquals("Context path not set for parent", CONTEXT_PATH, item.getContextPath());
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
