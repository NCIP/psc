package edu.northwestern.bioinformatics.studycalendar.web.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import static edu.northwestern.bioinformatics.studycalendar.domain.Role.*;
import edu.northwestern.bioinformatics.studycalendar.security.FilterSecurityInterceptorConfigurer;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBasedDictionary;
import edu.northwestern.bioinformatics.studycalendar.web.osgi.OsgiLayerTools;
import edu.northwestern.bioinformatics.studycalendar.web.osgi.TransparentMembrane;
import gov.nih.nci.cabig.ctms.tools.spring.BeanNameControllerUrlResolver;
import static org.easymock.EasyMock.*;
import org.easymock.classextension.EasyMock;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.osgi.mock.MockBundle;
import org.springframework.osgi.mock.MockServiceReference;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Map;
import java.util.Vector;

@SuppressWarnings({ "RawUseOfParameterizedType", "unchecked" })
public class ControllerSecureUrlCreatorTest extends StudyCalendarTestCase {
    private static final String PREFIX = "prefix";

    private ControllerSecureUrlCreator creator;
    private DefaultListableBeanFactory beanFactory;
    private BeanNameControllerUrlResolver resolver;
    private BundleContext bundleContext;
    private ConfigurationAdmin configurationAdmin;
    private Configuration osgiConfiguration;
    private OsgiLayerTools osgiLayerTools;

    private static final MockBundle SOCKET_BUNDLE = new MockBundle("edu.northwestern.bioinformatics.psc-authentication-socket") {
            @Override
            public String getLocation() {
                return "somewhere";
            }
        };

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        bundleContext = registerMockFor(BundleContext.class);
        configurationAdmin = registerMockFor(ConfigurationAdmin.class);
        osgiConfiguration = registerMockFor(Configuration.class);
        expect(configurationAdmin.getConfiguration(FilterSecurityInterceptorConfigurer.SERVICE_PID, SOCKET_BUNDLE.getLocation())).
            andStubReturn(osgiConfiguration);
        osgiConfiguration.update((Dictionary) notNull());
        expectLastCall().asStub();
        osgiLayerTools = new OsgiLayerTools();
        osgiLayerTools.setBundleContext(bundleContext);
        osgiLayerTools.setMembrane(new TransparentMembrane());

        resolver = new BeanNameControllerUrlResolver();
        resolver.setServletName(PREFIX);

        creator = new ControllerSecureUrlCreator();
        creator.setUrlResolver(resolver);
        creator.setOsgiLayerTools(osgiLayerTools);

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
        doProcess();

        Role[] defs = actualRolesForPath("/prefix/single/**");
        assertEquals("Wrong number of roles", 1, defs.length);
        assertEquals("Wrong Role", STUDY_COORDINATOR, defs[0]);
    }

    public void testMultiGroupRegistered() throws Exception {
        registerControllerBean("multi", MultiGroupController.class);
        doProcess();

        Role[] defs = actualRolesForPath("/prefix/multi/**");
        assertEquals("Wrong Protection Group Size", 2, defs.length);

        assertEquals("Wrong Role", STUDY_COORDINATOR, defs[0]);
        assertEquals("Wrong Role", SUBJECT_COORDINATOR, defs[1]);
    }

    public void testNoGroupAllowsAll() throws Exception {
        registerControllerBean("zero", NoGroupController.class);
        doProcess();

        Role[] roles = actualRolesForPath("/prefix/zero/**");
        assertEquals("Wrong number of roles", 5, roles.length);

        assertEquals("Wrong role 0", STUDY_COORDINATOR,    roles[0]);
        assertEquals("Wrong role 1", STUDY_ADMIN,          roles[1]);
        assertEquals("Wrong role 2", SYSTEM_ADMINISTRATOR, roles[2]);
        assertEquals("Wrong role 3", SUBJECT_COORDINATOR,  roles[3]);
        assertEquals("Wrong role 4", SITE_COORDINATOR,     roles[4]);
    }

    public void testMapResolvesPathsLongestFirst() throws Exception {
        registerControllerBean("long/plus", SingleGroupController.class);
        registerControllerBean("long", MultiGroupController.class);
        registerControllerBean("long/plus/more", NoGroupController.class);
        doProcess();

        Map<String, Role[]> actual = actualPathMap();
        Role[] noGroup = actual.get("/prefix/long/plus/more/**");
        assertEquals("Wrong number of groups: " + Arrays.asList(noGroup), 5, noGroup.length);
        Role[] singleGroup = actual.get("/prefix/long/plus/**");
        assertEquals("Wrong number of groups: " + Arrays.asList(singleGroup), 1, singleGroup.length);
        Role[] multiGroup = actual.get("/prefix/long/**");
        assertEquals("Wrong number of groups: " + Arrays.asList(multiGroup), 2, multiGroup.length);
    }

    public void testMapResolvesSeparatePathsWithSameLength() throws Exception {
        registerControllerBean("pear", SingleGroupController.class);
        registerControllerBean("pome", MultiGroupController.class);
        doProcess();

        Role[] singleGroup = actualRolesForPath("/prefix/pear/**");
        assertNotNull("Could not resolve pear", singleGroup);
        assertEquals("Wrong number of groups: " + Arrays.asList(singleGroup), 1, singleGroup.length);

        Role[] multiGroup = actualRolesForPath("/prefix/pome/**");
        assertNotNull("Could not resolve pome", multiGroup);
        assertEquals("Wrong number of groups: " + Arrays.asList(multiGroup), 2, multiGroup.length);
    }

    public void testProcessingFailsIfConfigurationAdminServiceNotAvailable() throws Exception {
        try {
            doProcess(null);
            fail("Exception not thrown");
        } catch (StudyCalendarSystemException scse) {
            assertEquals("OSGi CM service not available.  Unable to update edu.northwestern.bioinformatics.studycalendar.security.filter-security-configurer.",
                scse.getMessage());
        }
    }

    public void testProcessingUpdatesConfigurationAtEnd() throws Exception {
        registerControllerBean("pear", SingleGroupController.class);
        registerControllerBean("pome", MultiGroupController.class);
        EasyMock.reset(osgiConfiguration);
        osgiConfiguration.update(new MapBasedDictionary(Collections.singletonMap(
            FilterSecurityInterceptorConfigurer.PATH_ROLE_MAP_KEY,
            new Vector(Arrays.asList(
                "/prefix/pome/**|STUDY_COORDINATOR SUBJECT_COORDINATOR",
                "/prefix/pear/**|STUDY_COORDINATOR"
            )
        ))));
        doProcess();
    }

    public void testMapExposedAsFactoryBeanResult() throws Exception {
        registerControllerBean("pear", SingleGroupController.class);
        registerControllerBean("pome", MultiGroupController.class);
        doProcess();

        assertNotNull(creator.getObject());
        Map<String, Role[]> actual = (Map<String, Role[]>) creator.getObject();
        assertSame(actual, actualPathMap());
        assertEquals(2, actual.size());
    }

    public void testFactoryBeanForSingleton() throws Exception {
        doProcess();
        assertTrue(creator.isSingleton());
    }

    public void testFactoryBeanForMap() throws Exception {
        doProcess();
        assertEquals(Map.class, creator.getObjectType());
    }

    private Role[] actualRolesForPath(String path) {
        assertNotNull("Map is null", actualPathMap());
        Role[] roles = actualPathMap().get(path);
        assertNotNull("No roles for " + path, roles);
        return roles;
    }

    private Map<String, Role[]> actualPathMap() {
        return creator.getPathRoleMap();
    }

    private void doProcess() {
        doProcess(configurationAdmin);
    }

    private void doProcess(Object expectedCMService) {
        ServiceReference cmSR = expectedCMService == null ? null : new MockServiceReference();
        expect(bundleContext.getServiceReference(ConfigurationAdmin.class.getName())).
            andReturn(expectedCMService == null ? null : cmSR);
        if (cmSR != null) {
            expect(bundleContext.getServiceReference(FilterSecurityInterceptorConfigurer.class.getName())).
                andReturn(new MockServiceReference(SOCKET_BUNDLE));
            expect(bundleContext.getService(cmSR)).andReturn(expectedCMService);
        }

        replayMocks();
        resolver.postProcessBeanFactory(beanFactory);
        creator.postProcessBeanFactory(beanFactory);
        verifyMocks();
    }

    private static class TestingController implements Controller {
        public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
            throw new UnsupportedOperationException("handleRequest not implemented");
        }
    }

    @AccessControl(roles = STUDY_COORDINATOR)
    public static class SingleGroupController extends TestingController { }

    @AccessControl(roles = { STUDY_COORDINATOR, SUBJECT_COORDINATOR })
    public static class MultiGroupController extends TestingController { }

    public static class NoGroupController extends TestingController { }

}
