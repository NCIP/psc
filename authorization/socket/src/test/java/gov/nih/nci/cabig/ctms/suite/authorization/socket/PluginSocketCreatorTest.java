package gov.nih.nci.cabig.ctms.suite.authorization.socket;

import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteAuthorizationSource;
import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteUser;
import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteUserRoleLevel;
import gov.nih.nci.cabig.ctms.suite.authorization.plugin.SuiteUserSearchOptions;
import gov.nih.nci.cabig.ctms.suite.authorization.socket.internal.PluginSocketCreator;
import gov.nih.nci.cabig.ctms.suite.authorization.socket.internal.SuiteAuthorizationSocket;
import gov.nih.nci.security.AuthorizationManager;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceRegistration;
import org.springframework.osgi.mock.MockServiceReference;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author Rhett Sutphin
 */
public class PluginSocketCreatorTest {
    private RegisteringMockBundleContext bundleContext;
    private PluginSocketCreator creator;

    @Before
    public void before() throws Exception {
        bundleContext  = new RegisteringMockBundleContext();
        creator = new PluginSocketCreator(bundleContext);
    }

    @Test
    public void initRegistersSocketsForExistingSources() throws Exception {
        bundleContext.registerService(
            new String[] { SuiteAuthorizationSource.class.getName() },
            new MockAuthorizationSource(), null);

        creator.init();
        assertThat(bundleContext.getRegistrations().size(), is(2));
        RegisteringMockBundleContext.UnregisterableMockServiceRegistration actual =
            bundleContext.getRegistrations().get(1);
        assertThat(actual.getInterfaces(), hasItems(AuthorizationManager.class.getName()));
    }

    @Test
    public void initDoesNothingWithNoExistingSources() throws Exception {
        creator.init();
        assertThat(bundleContext.getRegistrations().size(), is(0));
    }

    @Test
    public void itRegistersAnAuthorizationManagerServiceWhenANewSourceShowsUp() throws Exception {
        registerMockAuthorizationSource();

        assertThat(bundleContext.getRegistrations().size(), is(2));
        RegisteringMockBundleContext.UnregisterableMockServiceRegistration actual =
            bundleContext.getRegistrations().get(1);
        assertThat(actual.getInterfaces(), hasItems(AuthorizationManager.class.getName()));
    }

    @Test
    public void itRegistersASocketInstanceWhenANewSourceShowsUp() throws Exception {
        registerMockAuthorizationSource();

        assertThat(bundleContext.getRegistrations().size(), is(2));
        RegisteringMockBundleContext.UnregisterableMockServiceRegistration actual =
            bundleContext.getRegistrations().get(1);
        assertThat(actual.getService(), instanceOf(SuiteAuthorizationSocket.class));
    }

    @Test
    public void itCarriesForwardTheServiceRankingForTheSourceWhenOneIsProvided() throws Exception {
        ServiceRegistration sourceReg = bundleContext.registerService(
            new String[] { SuiteAuthorizationSource.class.getName() },
            new MockAuthorizationSource(),
            new MapBuilder<String, Object>().put(Constants.SERVICE_RANKING, 17).toDictionary());
        creator.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, sourceReg.getReference()));

        assertThat(bundleContext.getRegistrations().size(), is(2));
        RegisteringMockBundleContext.UnregisterableMockServiceRegistration actual =
            bundleContext.getRegistrations().get(1);
        assertThat((Integer) actual.getReference().getProperty(Constants.SERVICE_RANKING), is(17));
    }

    @Test
    public void itDoesNothingWhenAnotherServiceShowsUp() throws Exception {
        ServiceRegistration sourceReg = bundleContext.registerService(
            new String[] { List.class.getName() },
            new ArrayList(), null);
        creator.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, sourceReg.getReference()));

        assertThat(bundleContext.getRegistrations().size(), is(1));
    }

    @Test
    public void itUnregistersTheSocketWhenTheSourceVanishes() throws Exception {
        ServiceRegistration reg = registerMockAuthorizationSource();

        creator.serviceChanged(new ServiceEvent(ServiceEvent.UNREGISTERING, reg.getReference()));
        assertThat(bundleContext.getRegistrations().size(), is(1));
    }

    @Test
    public void isDoesNothingWhenAnUnknownServiceIsUnregistered() throws Exception {
        MockServiceReference oldSource = new MockServiceReference(
            new String[] { SuiteAuthorizationSource.class.getName() });

        creator.serviceChanged(new ServiceEvent(ServiceEvent.UNREGISTERING, oldSource));
        assertThat(bundleContext.getRegistrations().size(), is(0));
    }

    private ServiceRegistration registerMockAuthorizationSource() {
        ServiceRegistration sourceReg = bundleContext.registerService(
            new String[] { SuiteAuthorizationSource.class.getName() },
            new MockAuthorizationSource(), null);
        creator.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, sourceReg.getReference()));

        return sourceReg;
    }

    private static class MockAuthorizationSource implements SuiteAuthorizationSource {
        public SuiteUser getUser(String username, SuiteUserRoleLevel desiredDetail) {
            throw new UnsupportedOperationException("getUser not implemented");
            // return null;
        }

        public SuiteUser getUser(int id, SuiteUserRoleLevel desiredDetail) {
            throw new UnsupportedOperationException("getUser not implemented");
            // return null;
        }

        public Collection<SuiteUser> getUsersByRole(SuiteRole role) {
            throw new UnsupportedOperationException("getUsersByRole not implemented");
            // return null;
        }

        public Collection<SuiteUser> searchUsers(SuiteUserSearchOptions criteria) {
            throw new UnsupportedOperationException("searchUsers not implemented");
            // return null;
        }
    }
}
