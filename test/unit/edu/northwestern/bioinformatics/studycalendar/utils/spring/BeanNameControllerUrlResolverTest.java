package edu.northwestern.bioinformatics.studycalendar.utils.spring;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Rhett Sutphin
 */
public class BeanNameControllerUrlResolverTest extends StudyCalendarTestCase {
    private static final String SERVLET_NAME = "pub";


    private BeanNameControllerUrlResolver resolver;
    private DefaultListableBeanFactory beanFactory;

    protected void setUp() throws Exception {
        super.setUp();
        resolver = new BeanNameControllerUrlResolver();
        resolver.setServletName(SERVLET_NAME);

        beanFactory = new DefaultListableBeanFactory();
    }

    public void testResolve() throws Exception {
        beanFactory.registerBeanDefinition("test", new RootBeanDefinition(TestingController.class));
        beanFactory.registerAlias("test", "/zippo/test");

        doProcess();

        ResolvedControllerReference ref = resolver.resolve("test");
        assertEquals("/pub/zippo/test", ref.getUrl());
        assertEquals(TestingController.class, ref.getControllerClass());
    }

    private void doProcess() {
        resolver.postProcessBeanFactory(beanFactory);
    }

    ////// TEST CLASSES

    private static class TestingController implements Controller {
        public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
            throw new UnsupportedOperationException("handleRequest not implemented");
        }
    }
}
