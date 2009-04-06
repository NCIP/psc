package edu.northwestern.bioinformatics.studycalendar.web.tools;

import edu.northwestern.bioinformatics.studycalendar.osgi.hostservices.HostBeans;
import edu.northwestern.bioinformatics.studycalendar.osgi.hostservices.internal.HostBeansImpl;
import edu.northwestern.bioinformatics.studycalendar.security.StubAuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.test.PscTestingBundleContext;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.memory.InMemoryDaoImpl;

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
        addService(UserDetailsService.class, new InMemoryDaoImpl());
    }
}
