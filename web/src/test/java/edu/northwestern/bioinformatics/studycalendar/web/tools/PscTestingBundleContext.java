package edu.northwestern.bioinformatics.studycalendar.web.tools;

import org.springframework.osgi.mock.MockBundleContext;
import org.springframework.osgi.mock.MockServiceReference;
import org.osgi.framework.ServiceReference;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.StubAuthenticationSystem;

/**
 * @author Rhett Sutphin
*/
public class PscTestingBundleContext extends MockBundleContext {
    private static final MockServiceReference AUTH_SYSTEM_REF = new MockServiceReference(
        new String[] { AuthenticationSystem.class.getName() });

    @Override
    public ServiceReference getServiceReference(String s) {
        if (s.equals(AuthenticationSystem.class.getName())) {
            return AUTH_SYSTEM_REF;
        } else {
            return super.getServiceReference(s);
        }
    }

    @Override
    public Object getService(ServiceReference serviceReference) {
        if (serviceReference == AUTH_SYSTEM_REF) {
            return new StubAuthenticationSystem();
        } else {
            return super.getService(serviceReference);
        }
    }
}
