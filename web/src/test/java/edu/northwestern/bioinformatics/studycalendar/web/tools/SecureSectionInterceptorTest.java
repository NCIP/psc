package edu.northwestern.bioinformatics.studycalendar.web.tools;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createUser;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import gov.nih.nci.cabig.ctms.web.chrome.Section;
import gov.nih.nci.cabig.ctms.web.chrome.Task;
import static org.apache.commons.lang.StringUtils.EMPTY;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static java.util.Arrays.asList;
import java.util.List;

/**
 * @author John Dzak
 */
public class SecureSectionInterceptorTest extends WebTestCase {
    private Task task0;
    private SecureSectionInterceptor interceptor;
    private User siteCoord, studyCoordAndAdmin;
    private DefaultListableBeanFactory beanFactory;
    private Section section0, section1;

    protected void setUp() throws Exception {
        super.setUp();

        siteCoord = createUser("site coord", Role.SITE_COORDINATOR);
        studyCoordAndAdmin = createUser("study coord", Role.STUDY_COORDINATOR, Role.STUDY_ADMIN);
        SecurityContextHolderTestHelper.setSecurityContext(siteCoord, EMPTY);

        beanFactory = new DefaultListableBeanFactory ();

        interceptor = new SecureSectionInterceptor();
        interceptor.setApplicationSecurityManager(applicationSecurityManager);
        interceptor.postProcessBeanFactory(beanFactory);

        task0 = new Task();
        task0.setLinkName("siteCoordController");

        Task task1 = new Task();
        task1.setLinkName("subjCoordController");

        section0 = new Section();
        section0.setDisplayName("Section 0");
        section0.setTasks(asList(task0));
        section0.setPathMapping("/**");

        section1 = new Section();
        section1.setDisplayName("Section 1");
        section1.setTasks(asList(task1));
        section1.setPathMapping("/**");

        interceptor.setSections(asList(section0, section1));

        request.setAttribute(WebUtils.INCLUDE_CONTEXT_PATH_ATTRIBUTE, "/psc");

        // Bean Name: siteCoordController    Alias: /siteCoordController
        registerControllerBean("siteCoord", SiteCoordinatorController.class);

        // Bean Name: subjCoordController   Alias: /subjCoordController
        registerControllerBean("subjCoord", SubjectCoordinatorController.class);
    }

    public void testGetAllowedRoles() {
        List<Role> roles = interceptor.getAllowedRoles(new SiteCoordinatorController());
        assertEquals("Wrong number of roles", 1, roles.size());
        assertSame("Wrong number of roles", Role.SITE_COORDINATOR, roles.get(0));
    }

    @SuppressWarnings({"unchecked"})
    public void testPreHandleWhenForSiteCoordinator() throws Exception {
        doPreHandle();

        List<Section> actualSections = (List<Section>) request.getAttribute("sections");

        assertEquals("Wrong number of sections", 1, actualSections.size());

        assertEquals("User should have access to section 0", section0, actualSections.get(0));
    }

    @SuppressWarnings({"unchecked"})
    public void testPreHandleWhenForStudyCoordinatorAndAdmin() throws Exception {
        SecurityContextHolderTestHelper.setSecurityContext(studyCoordAndAdmin, EMPTY);

        doPreHandle();

        List<Section> actualSections = (List<Section>) request.getAttribute("sections");

        assertEquals("Wrong number of sections", 1, actualSections.size());

        assertEquals("User should have access to section 1 only", section1, actualSections.get(0));
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

    private void doPreHandle() throws Exception {
        replayMocks();
        interceptor.postProcessBeanFactory(beanFactory);
        interceptor.preHandle(request, response, null);
        verifyMocks();
    }

    ////// Inner Classes
    @AccessControl(roles={Role.SITE_COORDINATOR})
    public static class SiteCoordinatorController extends TestingController {}

    @AccessControl(roles={Role.STUDY_COORDINATOR, Role.STUDY_ADMIN})
    public static class SubjectCoordinatorController extends TestingController {}

    public static class TestingController implements Controller {
        public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
            throw new UnsupportedOperationException("handleRequest not implemented");
        }
    }
}
