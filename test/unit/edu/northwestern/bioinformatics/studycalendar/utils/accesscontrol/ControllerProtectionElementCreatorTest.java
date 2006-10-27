package edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup.*;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * @author Rhett Sutphin
 */
public class ControllerProtectionElementCreatorTest extends StudyCalendarTestCase {
    private static final String PREFIX = "/prefix";

    private ControllerProtectionElementCreator creator;
    private StudyCalendarAuthorizationManager studyCalendarAuthorizationManager;
    private DefaultListableBeanFactory beanFactory;

    protected void setUp() throws Exception {
        super.setUp();
        studyCalendarAuthorizationManager = registerMockFor(StudyCalendarAuthorizationManager.class);

        creator = new ControllerProtectionElementCreator();
        creator.setStudyCalendarAuthorizationManager(studyCalendarAuthorizationManager);
        creator.setUrlPrefix(PREFIX);

        beanFactory = new DefaultListableBeanFactory();
    }

    private void registerControllerBean(String name, Class clazz) {
        String beanName = name + "Controller";
        beanFactory.registerBeanDefinition(beanName, createBeanDef(clazz));
        beanFactory.registerAlias(beanName, '/' + name);
    }

    private BeanDefinition createBeanDef(Class clazz) {
        return new RootBeanDefinition(clazz);
    }

    public void testSingleGroupRegistered() throws Exception {
        registerControllerBean("single", SingleGroupController.class);
        studyCalendarAuthorizationManager.registerUrl(PREFIX + "/single", Arrays.asList(BASE.csmName()));
        doProcess();
    }

    public void testMultiGroupRegistered() throws Exception {
        registerControllerBean("multi", MultiGroupController.class);
        studyCalendarAuthorizationManager.registerUrl(PREFIX + "/multi", Arrays.asList(CREATE_STUDY.csmName(), ASSIGN_PARTICIPANT.csmName()));
        doProcess();
    }

    public void testNoGroupIgnored() throws Exception {
        registerControllerBean("zero", NoGroupController.class);
        doProcess();
    }
    
    public void testNullPrefixWorks() throws Exception {
        creator.setUrlPrefix(null);
        registerControllerBean("single", SingleGroupController.class);
        studyCalendarAuthorizationManager.registerUrl("/single", Arrays.asList(BASE.csmName()));
        doProcess();
    }

    private void doProcess() {
        replayMocks();
        creator.postProcessBeanFactory(beanFactory);
        verifyMocks();
    }

    ////// TEST CLASSES

    private static class TestingController implements Controller {
        public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
            throw new UnsupportedOperationException("handleRequest not implemented");
        }
    }

    @AccessControl(protectionGroups = BASE)
    public static class SingleGroupController extends TestingController { }

    @AccessControl(protectionGroups = { CREATE_STUDY, ASSIGN_PARTICIPANT })
    public static class MultiGroupController extends TestingController { }

    public static class NoGroupController extends TestingController { }
}
