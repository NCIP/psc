/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.osgi.authorization;

import edu.northwestern.bioinformatics.studycalendar.osgi.OsgiLayerIntegratedTestCase;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.DefaultMembrane;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembershipLoader;
import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteAuthorizationSource;
import gov.nih.nci.security.AuthorizationManager;
import gov.nih.nci.security.authorization.domainobjects.User;
import gov.nih.nci.security.dao.UserSearchCriteria;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

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
    private static final String SOCKET_SYMBOLIC_NAME =
        "edu.northwestern.bioinformatics.psc-authorization-socket";
    private static final String DEFAULT_CSM_SYMBOLIC_NAME =
        "edu.northwestern.bioinformatics.psc-authorization-default-csm";
    private static final String AUTHORIZATION_SERVICES_SYMBOLIC_NAME =
        "edu.northwestern.bioinformatics.psc-authorization-auxiliary-services";

    @Before
    public void before() throws Exception {
        waitForService(DEFAULT_CSM_SYMBOLIC_NAME, AuthorizationManager.class.getName());
    }

    @After
    public void after() throws BundleException, IOException {
        stopBundle(MOCK_PLUGIN_SYMBOLIC_NAME);
    }

    @Test
    public void pluginLayerGivesTheCsmAuthorizationManagerByDefault() throws Exception {
        assertThat(getCurrentAuthorizationManager().toString(),
            containsString("gov.nih.nci.security.provisioning.AuthorizationManagerImpl"));
    }

    @Test
    public void pluginLayerGivesAnAuthorizationSocketIfAnAuthorizationPluginIsAvailable() throws Exception {
        startBundle(MOCK_PLUGIN_SYMBOLIC_NAME, SuiteAuthorizationSource.class.getName());
        waitForService(SOCKET_SYMBOLIC_NAME, AuthorizationManager.class.getName());

        assertThat(getCurrentAuthorizationManager().getClass().getName(),
            is("gov.nih.nci.cabig.ctms.suite.authorization.socket.internal.SuiteAuthorizationSocket"));
    }

    @Test
    public void pluginLayerGivesTheCsmAuthorizationManagerIfAllAuthorizationPluginsAreDisabled() throws Exception {
        startBundle(MOCK_PLUGIN_SYMBOLIC_NAME, SuiteAuthorizationSource.class.getName());
        stopBundle(MOCK_PLUGIN_SYMBOLIC_NAME);

        assertThat(getCurrentAuthorizationManager().toString(),
            containsString("gov.nih.nci.security.provisioning.AuthorizationManagerImpl"));
    }

    @Test
    public void anAuthorizationPluginCanBeQueriedForUsers() throws Exception {
        startBundle(MOCK_PLUGIN_SYMBOLIC_NAME, SuiteAuthorizationSource.class.getName());
        waitForService(SOCKET_SYMBOLIC_NAME, AuthorizationManager.class.getName());

        assertThat(
            getWrappedAuthorizationManager().getObjects(new UserSearchCriteria(new User())).size(),
            is(1));
    }

    @Test
    public void pluginLayerGivesASuiteRoleMembershipLoaderByDefault() throws Exception {
        waitForService(AUTHORIZATION_SERVICES_SYMBOLIC_NAME, SuiteRoleMembershipLoader.class.getName());

        assertThat(getWrappedSuiteRoleMembershipLoader(),
            instanceOf(SuiteRoleMembershipLoader.class));
    }

    @Test
    public void pluginLayerGivesASuiteRoleMembershipLoaderBasedOnCsmByDefault() throws Exception {
        ServiceReference srmlRef = getSuiteRoleMembershipLoaderReference();

        assertThat((String) srmlRef.getProperty("authorizationManagerService"),
            equalTo("edu.northwestern.bioinformatics.psc-authorization-default-csm.AUTHORIZATION_MANAGER"));
    }

    @Test
    public void pluginLayerGivesASuiteRoleMembershipLoaderBasedAnAuthorizationPluginIfPresent() throws Exception {
        startBundle(MOCK_PLUGIN_SYMBOLIC_NAME, SuiteAuthorizationSource.class.getName());
        waitForService(SOCKET_SYMBOLIC_NAME, AuthorizationManager.class.getName());
        waitForService(AUTHORIZATION_SERVICES_SYMBOLIC_NAME, SuiteRoleMembershipLoader.class.getName());

        ServiceReference srmlRef = getSuiteRoleMembershipLoaderReference();
        assertThat((String) srmlRef.getProperty("authorizationManagerService"),
            equalTo("SuiteAuthorizationSocket for edu.northwestern.bioinformatics.psc-authorization-mock-plugin.SOURCE"));
    }

    @Test
    public void suiteRoleMembershipLoaderFromAuthorizationPluginIsUsable() throws Exception {
        startBundle(MOCK_PLUGIN_SYMBOLIC_NAME, SuiteAuthorizationSource.class.getName());
        waitForService(SOCKET_SYMBOLIC_NAME, AuthorizationManager.class.getName());
        waitForService(AUTHORIZATION_SERVICES_SYMBOLIC_NAME, SuiteRoleMembershipLoader.class.getName());

        Map<SuiteRole,SuiteRoleMembership> actual = getWrappedSuiteRoleMembershipLoader().getRoleMemberships(1);
        assertNotNull("No memberships found for id=1", actual);
        assertTrue(actual.get(SuiteRole.STUDY_QA_MANAGER).isAllSites());
    }

    private Object getCurrentAuthorizationManager() throws IOException {
        ServiceReference sr = getBundleContext().
            getServiceReference(AuthorizationManager.class.getName());
        assertThat("No AuthorizationManager registered", sr, is(not(nullValue())));

        Object service = getBundleContext().getService(sr);
        assertThat("AuthorizationManager ref not resolvable", service, is(not(nullValue())));
        return service;
    }

    private AuthorizationManager getWrappedAuthorizationManager() throws IOException {
        return (AuthorizationManager) createCsmMembrane().farToNear(getCurrentAuthorizationManager());
    }

    private ServiceReference getSuiteRoleMembershipLoaderReference() throws IOException {
        ServiceReference srmlRef =
            getBundleContext().getServiceReference(SuiteRoleMembershipLoader.class.getName());
        assertThat("No SuiteRoleMembershipLoader available", srmlRef, is(notNullValue()));
        return srmlRef;
    }

    private Object getCurrentSuiteRoleMembershipLoader() throws IOException {
        ServiceReference srmlRef = getSuiteRoleMembershipLoaderReference();
        Object service = getBundleContext().getService(srmlRef);
        assertThat("SuiteRoleMembershipLoader ref not resolvable", service, is(notNullValue()));
        return service;
    }

    private SuiteRoleMembershipLoader getWrappedSuiteRoleMembershipLoader() throws IOException {
        return (SuiteRoleMembershipLoader) createCsmMembrane().
            farToNear(getCurrentSuiteRoleMembershipLoader());
    }

    private DefaultMembrane createCsmMembrane() {
        DefaultMembrane membrane = new DefaultMembrane(getClass().getClassLoader(),
            "gov.nih.nci.security",
            "gov.nih.nci.security.dao",
            "gov.nih.nci.security.exceptions",
            "gov.nih.nci.security.authorization.domainobjects",
            "gov.nih.nci.security.authorization.jaas",
            "gov.nih.nci.cabig.ctms.suite.authorization"
        );
        membrane.setProxyConstructorParameters(
            Collections.singletonMap(SuiteRoleMembership.class.getName(), Arrays.asList(null, null, null)));
        return membrane;
    }
}
