package edu.northwestern.bioinformatics.studycalendar.web.tools;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.LegacyModeSwitch;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ControllerRequiredAuthorityExtractor;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.cabig.ctms.web.chrome.Section;
import gov.nih.nci.cabig.ctms.web.chrome.Task;
import gov.nih.nci.security.authorization.domainobjects.User;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.EMPTY;

/**
 * @author John Dzak
 * @author Rhett Sutphin
 */
public class SecureSectionInterceptorTest extends WebTestCase {
    private Task workloadTask;
    private SecureSectionInterceptor interceptor;
    private PscUser alice, eve;
    private DefaultListableBeanFactory beanFactory;
    private Section managementSection, subjectSection;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        alice = createUser("alice", PscRole.STUDY_SUBJECT_CALENDAR_MANAGER);
        eve = createUser("eve", PscRole.STUDY_TEAM_ADMINISTRATOR, PscRole.DATA_READER);
        SecurityContextHolderTestHelper.setSecurityContext(alice, EMPTY);

        beanFactory = new DefaultListableBeanFactory();

        interceptor = new SecureSectionInterceptor();
        interceptor.setApplicationSecurityManager(applicationSecurityManager);
        ControllerRequiredAuthorityExtractor extractor = new ControllerRequiredAuthorityExtractor();
        extractor.setLegacyModeSwitch(new LegacyModeSwitch(false));
        interceptor.setControllerRequiredAuthorityExtractor(extractor);
        interceptor.postProcessBeanFactory(beanFactory);

        workloadTask = new Task();
        workloadTask.setLinkName("workloadController");

        Task scheduleTask = new Task();
        scheduleTask.setLinkName("scheduleController");

        managementSection = new Section();
        managementSection.setDisplayName("Management");
        managementSection.setTasks(asList(workloadTask));
        managementSection.setPathMapping("/**");

        subjectSection = new Section();
        subjectSection.setDisplayName("Subjects");
        subjectSection.setTasks(asList(scheduleTask));
        subjectSection.setPathMapping("/**");

        interceptor.setSections(asList(managementSection, subjectSection));

        request.setAttribute(WebUtils.INCLUDE_CONTEXT_PATH_ATTRIBUTE, "/psc");

        // Bean Name: siteCoordController    Alias: /workloadController
        registerControllerBean("workload", WorkloadController.class);

        // Bean Name: subjCoordController   Alias: /scheduleController
        registerControllerBean("schedule", ScheduleController.class);
    }

    private PscUser createUser(String username, PscRole... roles) {
        User csmUser = new User();
        csmUser.setLoginName(username);
        Map<SuiteRole, SuiteRoleMembership> srms = new LinkedHashMap<SuiteRole, SuiteRoleMembership>();
        for (PscRole role : roles) {
            srms.put(role.getSuiteRole(), new SuiteRoleMembership(role.getSuiteRole(), null, null));
        }
        return new PscUser(csmUser, srms);
    }

    @SuppressWarnings({"unchecked"})
    public void testPreHandleWhenForCoordinator() throws Exception {
        doPreHandle();

        List<Section> actualSections = (List<Section>) request.getAttribute("sections");

        assertEquals("Wrong number of sections", 1, actualSections.size());
        assertEquals("User should have access to subjects", subjectSection, actualSections.get(0));
    }

    @SuppressWarnings({"unchecked"})
    public void testPreHandleWhenForStudyCoordinatorAndAdmin() throws Exception {
        SecurityContextHolderTestHelper.setSecurityContext(eve, EMPTY);

        doPreHandle();

        List<Section> actualSections = (List<Section>) request.getAttribute("sections");

        assertEquals("Wrong number of sections", 2, actualSections.size());
        assertEquals("User should have access to the management section", managementSection, actualSections.get(0));
        assertEquals("User should have access to the subject section", subjectSection, actualSections.get(1));
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

    public static class WorkloadController extends TestingController {
        public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
            return ResourceAuthorization.createCollection(PscRole.STUDY_TEAM_ADMINISTRATOR);
        }
    }

    public static class ScheduleController extends TestingController {
        public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
            return ResourceAuthorization.createCollection(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER, PscRole.DATA_READER);
        }
    }

    public abstract static class TestingController implements Controller, PscAuthorizedHandler {
        public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
            throw new UnsupportedOperationException("handleRequest not implemented");
        }
    }
}
