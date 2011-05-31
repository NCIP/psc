package edu.northwestern.bioinformatics.studycalendar.osgi.authorization;

import edu.northwestern.bioinformatics.studycalendar.osgi.OsgiLayerIntegratedTestCase;
import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteAuthorizationSource;
import gov.nih.nci.security.AuthorizationManager;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.osgi.framework.ServiceReference;

import java.io.IOException;

import static edu.northwestern.bioinformatics.studycalendar.osgi.OsgiLayerIntegratedTestHelper.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author Rhett Sutphin
 */
@RunWith(JUnit4.class)
public class AuthorizationPluginsIntegratedTest extends OsgiLayerIntegratedTestCase {
    private static final String MOCK_PLUGIN_SYMBOLIC_NAME =
        "edu.northwestern.bioinformatics.psc-authorization-mock-plugin";

    @Test @Ignore
    public void pluginLayerGivesTheCsmAuthorizationManagerByDefault() throws Exception {
        assertThat(getCurrentAuthorizationManager().getClass().getName(),
            is("gov.nih.nci.security.provisioning.AuthorizationManagerImpl"));
    }

    @Test @Ignore
    public void pluginLayerGivesAnAuthorizationSocketIfAnAuthorizationPluginIsAvailable() throws Exception {
        startBundle(MOCK_PLUGIN_SYMBOLIC_NAME, SuiteAuthorizationSource.class.getName());

        assertThat(getCurrentAuthorizationManager().getClass().getName(),
            is("gov.nih.nci.cabig.ctms.suite.authorization.socket.internal.SuiteAuthorizationSocket"));
    }

    @Test @Ignore
    public void pluginLayerGivesTheCsmAuthorizationManagerIfAllAuthorizationPluginsAreDisabled() throws Exception {
        startBundle(MOCK_PLUGIN_SYMBOLIC_NAME, SuiteAuthorizationSource.class.getName());
        stopBundle(MOCK_PLUGIN_SYMBOLIC_NAME);

        assertThat(getCurrentAuthorizationManager().getClass().getName(),
            is("gov.nih.nci.security.provisioning.AuthorizationManagerImpl"));
    }

    private Object getCurrentAuthorizationManager() throws IOException {
        ServiceReference sr = getBundleContext().
            getServiceReference(AuthorizationManager.class.getName());
        assertThat("No registered service", sr, is(not(nullValue())));

        Object service = getBundleContext().getService(sr);
        assertThat("Service not found", service, is(not(nullValue())));
        return service;
    }
}
