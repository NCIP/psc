package edu.northwestern.bioinformatics.studycalendar.utils;

import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createUser;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;
import gov.nih.nci.cabig.ctms.tools.spring.ControllerUrlResolver;
import gov.nih.nci.cabig.ctms.web.chrome.Section;
import gov.nih.nci.cabig.ctms.web.chrome.Task;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.easymock.EasyMock.expect;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static java.util.Arrays.asList;
import java.util.Map;

/**
 * @author John Dzak
 */
public class SecureSectionInterceptorTest extends WebTestCase {
    private Section section0, section1;
    private Task task0, task1;
    private ControllerUrlResolver resolver;
    private SecureSectionInterceptor interceptor;
    private UserDao userDao;
    private User siteCoord;
    private DefaultListableBeanFactory beanFactory;

    protected void setUp() throws Exception {
        super.setUp();

        siteCoord = createUser("site coord", Role.SITE_COORDINATOR);
        SecurityContextHolderTestHelper.setSecurityContext(siteCoord.getName() , EMPTY);

        userDao = registerDaoMockFor(UserDao.class);
        resolver = registerMockFor(ControllerUrlResolver.class);
        beanFactory = new DefaultListableBeanFactory ();


        interceptor = new SecureSectionInterceptor();
        interceptor.setUserDao(userDao);
        interceptor.postProcessBeanFactory(beanFactory);

        task0 = new Task();
        task0.setLinkName("siteCoordinatorController");
        task0.setUrlResolver(resolver);

        task1 = new Task();
        task1.setLinkName("subjectCoordinatorController");
        task1.setUrlResolver(resolver);

        section0 = new Section();
        section0.setTasks(asList(task0));
        section0.setPathMapping("/**");

        section1 = new Section();
        section1.setTasks(asList(task1));
        section1.setPathMapping("/**");

        interceptor.setSections(asList(section0, section1));

        request.setAttribute(WebUtils.INCLUDE_SERVLET_PATH_ATTRIBUTE, "");
        request.setAttribute(WebUtils.INCLUDE_CONTEXT_PATH_ATTRIBUTE, "/psc");
    }

    @SuppressWarnings({"unchecked"})
    public void testUserHasAccessToSection() throws Exception {
        expect(userDao.getByName("site coord")).andReturn(siteCoord);

        replayMocks();

        interceptor.preHandle(request, response, null);
        verifyMocks();

        assertTrue(true);
        // TODO: 1) create list of sections that the user role is allowed to view (make dependent on if there are any tasks viewable)
        // TODO: 2) inside the section create the tasks the user is allowed to view
//        assertEquals("User should have access to section", 1, ((List<Section>) request.getAttribute("sections")).size());
//        assertSame("User should have access to section", section0, ((List<Section>) request.getAttribute("sections")).get(0));
    }

    public void testUserDoesNotHaveAccessToSection() {
       assertTrue(true);
    }

    public void testPostProcessFactory() {
        assertTrue(true);
        Map controllerRolesMap = interceptor.getControllerRolesMap();
        // TODO: Test building of controller roles map
    }

    ////// Helper Methods
    private void registerControllerBean(String name, Class clazz) {
        String beanName = name + "Controller";
        beanFactory.registerBeanDefinition(beanName, createBeanDef(clazz));
        beanFactory.registerAlias(beanName, '/' + name);
    }

    private BeanDefinition createBeanDef(Class clazz) {
        return new RootBeanDefinition(clazz);
    }

    @AccessControl(roles={Role.SITE_COORDINATOR})
    private class SiteCoordinatorController extends TestingController {}

    @AccessControl(roles={Role.STUDY_COORDINATOR})
    private class SubjectCoordinatorController extends TestingController {}

    private static class TestingController implements Controller {
        public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
            throw new UnsupportedOperationException("handleRequest not implemented");
        }
    }
}
