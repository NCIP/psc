package edu.northwestern.bioinformatics.studycalendar.web.tools;

import edu.northwestern.bioinformatics.studycalendar.osgi.hostservices.HostBeans;
import edu.northwestern.bioinformatics.studycalendar.osgi.hostservices.internal.HostBeansImpl;
import edu.northwestern.bioinformatics.studycalendar.security.CompleteAuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.FilterSecurityInterceptorConfigurer;
import edu.northwestern.bioinformatics.studycalendar.security.StubAuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.acegi.PscUserDetailsService;
import edu.northwestern.bioinformatics.studycalendar.security.internal.CompleteAuthenticationSystemImpl;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.service.PscUserDetailsServiceImpl;
import edu.northwestern.bioinformatics.studycalendar.test.PscTestingBundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.IOException;
import java.util.Dictionary;

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
        addService(PscUserDetailsService.class, new PscUserDetailsServiceImpl());
        addService(ConfigurationAdmin.class, new MockConfigurationAdmin());
        addService(FilterSecurityInterceptorConfigurer.class, new FilterSecurityInterceptorConfigurer());
        addService(CompleteAuthenticationSystem.class, new CompleteAuthenticationSystemImpl());
    }

    private static class MockConfigurationAdmin implements ConfigurationAdmin {
        public Configuration createFactoryConfiguration(String s) throws IOException {
            return new MockConfiguration();
        }

        public Configuration createFactoryConfiguration(String s, String s1) throws IOException {
            return new MockConfiguration();
        }

        public Configuration getConfiguration(String s, String s1) throws IOException {
            return new MockConfiguration();
        }

        public Configuration getConfiguration(String s) throws IOException {
            return new MockConfiguration();
        }

        public Configuration[] listConfigurations(String s) throws IOException, InvalidSyntaxException {
            return new Configuration[] { new MockConfiguration() };
        }
    }

    private static class MockConfiguration implements Configuration {
        public String getPid() {
            throw new UnsupportedOperationException("getPid not implemented");
        }

        public Dictionary getProperties() {
            throw new UnsupportedOperationException("getProperties not implemented");
        }

        public void update(Dictionary dictionary) throws IOException {
            // do nothing
        }

        public void delete() throws IOException {
            // do nothing
        }

        public String getFactoryPid() {
            throw new UnsupportedOperationException("getFactoryPid not implemented");
        }

        public void update() throws IOException {
            // do nothing
        }

        public void setBundleLocation(String s) {
            throw new UnsupportedOperationException("setBundleLocation not implemented");
        }

        public String getBundleLocation() {
            throw new UnsupportedOperationException("getBundleLocation not implemented");
        }
    }
}
