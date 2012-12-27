/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.plugin.insecure;

import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import junit.framework.TestCase;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.springframework.osgi.mock.MockBundleContext;

import java.util.Dictionary;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "RawUseOfParameterizedType" })
public class ActivatorTest extends TestCase {
    private ServiceRegistrationRecordingBundleContext bundleContext;
    private Activator activator;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        bundleContext = new ServiceRegistrationRecordingBundleContext();
        activator = new Activator();
    }

    public void testRegisteredServiceIsAFactory() throws Exception {
        activator.start(bundleContext);
        assertTrue("Service not a factory", bundleContext.getLastService() instanceof ServiceFactory);
        assertTrue("Factory is for wrong type",
            ((ServiceFactory) bundleContext.getLastService()).getService(null, null) instanceof InsecureAuthenticationSystem);
    }

    public void testRegisteredServicePropertiesIncludeName() throws Exception {
        activator.start(bundleContext);
        assertEquals(
            "Wrong name",
            bundleContext.getLastServiceProperties().get(AuthenticationSystem.ServiceKeys.NAME),
            "Insecure");
    }

    public void testRegisteredServicePropertiesIncludeBehavior() throws Exception {
        activator.start(bundleContext);
        assertEquals(
            "Wrong behavior",
            bundleContext.getLastServiceProperties().get(AuthenticationSystem.ServiceKeys.BEHAVIOR_DESCRIPTION),
            new InsecureAuthenticationSystem().behaviorDescription());
    }

    private static class ServiceRegistrationRecordingBundleContext extends MockBundleContext {
        private String lastServiceClass;
        private Object lastService;
        private Dictionary lastServiceProperties;

        @Override
        public ServiceRegistration registerService(String interfaceClass, Object service, Dictionary properties) {
            this.lastServiceClass = interfaceClass;
            this.lastService = service;
            this.lastServiceProperties = properties;
            return super.registerService(interfaceClass, service, properties);
        }

        public String getLastServiceClass() {
            return lastServiceClass;
        }

        public Object getLastService() {
            return lastService;
        }

        public Dictionary getLastServiceProperties() {
            return lastServiceProperties;
        }
    }
}
