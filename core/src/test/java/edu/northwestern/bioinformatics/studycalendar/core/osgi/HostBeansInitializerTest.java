package edu.northwestern.bioinformatics.studycalendar.core.osgi;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.osgi.hostservices.HostBeans;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUserDetailsService;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.Membrane;
import org.osgi.framework.BundleContext;
import org.springframework.osgi.mock.MockServiceReference;

import static org.easymock.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class HostBeansInitializerTest extends StudyCalendarTestCase {
    private HostBeansInitializer initializer;
    private HostBeans hostBeans;

    private BundleContext bundleContext;
    private Membrane membrane;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        initializer = new HostBeansInitializer();
        hostBeans = registerNiceMockFor(HostBeans.class);
        bundleContext = registerMockFor(BundleContext.class);
        membrane = registerMockFor(Membrane.class);

        initializer.setBundleContext(bundleContext);
        initializer.setMembrane(membrane);
    }

    public void testUserDetailsServicePassedOnOnInit() throws Exception {
        PscUserDetailsService uds = registerMockFor(PscUserDetailsService.class);
        initializer.setPscUserDetailsService(uds);

        expectGetHostBeansFromBundleContext();
        hostBeans.setPscUserDetailsService(uds); // expect

        replayMocks();
        initializer.afterPropertiesSet();
        verifyMocks();
    }

    private void expectGetHostBeansFromBundleContext() {
        MockServiceReference sr = new MockServiceReference();
        expect(bundleContext.getServiceReference(HostBeans.class.getName())).andReturn(sr);
        expect(bundleContext.getService(sr)).andReturn(hostBeans);
        expect(membrane.farToNear(hostBeans)).andReturn(hostBeans);
    }
}
