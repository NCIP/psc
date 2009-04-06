package edu.northwestern.bioinformatics.studycalendar.web.tools;

import edu.northwestern.bioinformatics.studycalendar.osgi.hostservices.HostBeans;
import edu.northwestern.bioinformatics.studycalendar.osgi.hostservices.impl.HostBeansImpl;
import edu.northwestern.bioinformatics.studycalendar.security.StubAuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.mock.MockBundleContext;
import org.springframework.osgi.mock.MockServiceReference;

/**
 * @author Rhett Sutphin
*/
public class PscTestingBundleContext extends MockBundleContext {
    private static final MockServiceReference AUTH_SYSTEM_REF = new MockServiceReference(
        new String[] { AuthenticationSystem.class.getName() });
    private static final MockServiceReference HOST_BEANS_REF = new MockServiceReference(
        new String[] { HostBeans.class.getName() });

    @Override
    public ServiceReference getServiceReference(String s) {
        if (s.equals(AuthenticationSystem.class.getName())) {
            return AUTH_SYSTEM_REF;
        } else if (s.equals(HostBeans.class.getName())) {
            return HOST_BEANS_REF;
        } else {
            return null;
        }
    }

    @Override
    public Object getService(ServiceReference serviceReference) {
        if (serviceReference == AUTH_SYSTEM_REF) {
            return new StubAuthenticationSystem();
        } else if (serviceReference == HOST_BEANS_REF) {
            return new HostBeansImpl();
        } else {
            return null;
        }
    }
}
