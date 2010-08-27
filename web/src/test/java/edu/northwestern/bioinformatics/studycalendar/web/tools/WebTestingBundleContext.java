package edu.northwestern.bioinformatics.studycalendar.web.tools;

import edu.northwestern.bioinformatics.studycalendar.mocks.osgi.MockConfigurationAdmin;
import edu.northwestern.bioinformatics.studycalendar.mocks.osgi.PscTestingBundleContext;
import edu.northwestern.bioinformatics.studycalendar.osgi.hostservices.HostBeans;
import edu.northwestern.bioinformatics.studycalendar.osgi.hostservices.internal.HostBeansImpl;
import edu.northwestern.bioinformatics.studycalendar.security.CompleteAuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.StubAuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUserDetailsService;
import edu.northwestern.bioinformatics.studycalendar.security.internal.CompleteAuthenticationSystemImpl;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.service.PscUserService;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * @author Rhett Sutphin
*/
public class WebTestingBundleContext extends PscTestingBundleContext {
    public WebTestingBundleContext() {
        reset();
    }

    public void reset() {
        testingDetails.clear();
        addService(AuthenticationSystem.class, new StubAuthenticationSystem());
        addService(HostBeans.class, new HostBeansImpl());
        addService(PscUserDetailsService.class, new PscUserService());
        addService(ConfigurationAdmin.class, new MockConfigurationAdmin());
        addService(CompleteAuthenticationSystem.class, new CompleteAuthenticationSystemImpl());
    }
}
