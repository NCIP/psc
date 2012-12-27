/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.authorization.auxiliary.internal;

import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembershipLoader;
import gov.nih.nci.security.AuthorizationManager;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import java.util.Dictionary;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author Rhett Sutphin
 */
public class SuiteRoleMembershipLoaderRegistererTest {
    private SuiteRoleMembershipLoaderRegisterer registerer;

    private RegisteringMockBundleContext bundleContext;
    private AuthorizationManager managerA, managerB;

    @Before
    public void before() throws Exception {
        bundleContext = new RegisteringMockBundleContext();

        managerA = EasyMock.createMock(AuthorizationManager.class);
        managerB = EasyMock.createMock(AuthorizationManager.class);

        registerer = new SuiteRoleMembershipLoaderRegisterer();
        registerer.activate(bundleContext);
    }

    @Test
    public void itRegistersANewSRMLForTheFirstAuthorizationManager() throws Exception {
        registerAuthManager(managerA);

        assertThat(bundleContext.getRegistrations().size(), is(2));
        ServiceReference[] actual =
            bundleContext.getAllServiceReferences(SuiteRoleMembershipLoader.class.getName(), null);
        assertThat(actual.length, is(1));
    }

    @Test
    public void itUnregistersTheFirstSRMLWhenAHigherRankedAuthorizationManagerAppears() throws Exception {
        registerAuthManager(managerA, "A", 0);
        registerAuthManager(managerB, "B", 1);

        assertThat(bundleContext.getRegistrations().size(), is(3));
        ServiceReference actual =
            bundleContext.getAllServiceReferences(SuiteRoleMembershipLoader.class.getName(), null)[0];
        assertThat((String) actual.getProperty(Constants.SERVICE_PID),
            is("SuiteRoleMembershipLoader for B"));
    }

    @Test
    public void itKeepsTheFirstSRMLWhenALowerRankedAuthorizationManagerAppears() throws Exception {
        registerAuthManager(managerA, "A", 0);
        registerAuthManager(managerB, "B", -1);

        assertThat(bundleContext.getRegistrations().size(), is(3));
        ServiceReference actual =
            bundleContext.getAllServiceReferences(SuiteRoleMembershipLoader.class.getName(), null)[0];
        assertThat((String) actual.getProperty(Constants.SERVICE_PID),
            is("SuiteRoleMembershipLoader for A"));
    }

    @Test
    public void itSwitchesToALowerRankedAuthorizationManagerWhenAHigherRankedOneDisappears() throws Exception {
        ServiceRegistration aReg = registerAuthManager(managerA, "A", 0);
        registerAuthManager(managerB, "B", -1);
        registerer.changeLoader(aReg.getReference());

        assertThat(bundleContext.getRegistrations().size(), is(3));
        ServiceReference actual =
            bundleContext.getAllServiceReferences(SuiteRoleMembershipLoader.class.getName(), null)[0];
        assertThat((String) actual.getProperty(Constants.SERVICE_PID),
            is("SuiteRoleMembershipLoader for B"));
    }

    @Test
    public void itRemovesTheSRMLWhenTheLastAuthorizationManagerDisappears() throws Exception {
        ServiceRegistration aReg = registerAuthManager(managerA, "A", 0);
        ServiceRegistration bReg = registerAuthManager(managerB, "B", -1);
        registerer.changeLoader(aReg.getReference());
        registerer.changeLoader(bReg.getReference());

        assertThat(bundleContext.getAllServiceReferences(
            SuiteRoleMembershipLoader.class.getName(), null), is(nullValue()));
    }

    @Test
    public void itRecordsThePidOfTheSourceService() throws Exception {
        registerAuthManager(managerA, "A", 0);

        ServiceReference actual =
            bundleContext.getAllServiceReferences(SuiteRoleMembershipLoader.class.getName(), null)[0];
        assertThat((String) actual.getProperty("authorizationManagerService"), is("A"));
    }

    private ServiceRegistration registerAuthManager(AuthorizationManager manager) {
        return registerAuthManager(manager, null);
    }

    private ServiceRegistration registerAuthManager(AuthorizationManager manager, String pid, int rank) {
        return registerAuthManager(manager,
            new MapBuilder<String, Object>().
                put(Constants.SERVICE_PID, pid).
                put(Constants.SERVICE_RANKING, rank).
                toDictionary());
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    private ServiceRegistration registerAuthManager(AuthorizationManager manager, Dictionary properties) {
        ServiceRegistration sourceReg = bundleContext.registerService(
            new String[] { AuthorizationManager.class.getName() },
            manager, properties);
        registerer.createLoader(sourceReg.getReference());

        return sourceReg;
    }
}
