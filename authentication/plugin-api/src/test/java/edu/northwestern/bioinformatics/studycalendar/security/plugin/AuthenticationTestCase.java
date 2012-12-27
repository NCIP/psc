/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.plugin;

import edu.northwestern.bioinformatics.studycalendar.mocks.osgi.PscTestingBundleContext;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUserDetailsService;
import gov.nih.nci.cabig.ctms.testing.MockRegistry;
import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;
import gov.nih.nci.cabig.ctms.tools.configuration.DefaultConfigurationProperties;
import gov.nih.nci.cabig.ctms.tools.configuration.TransientConfiguration;
import junit.framework.TestCase;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public abstract class AuthenticationTestCase extends TestCase {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private MockRegistry mocks;
    protected StaticPscUserDetailsService userDetailsService;
    protected DataSource dataSource;
    protected PscTestingBundleContext bundleContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mocks = new MockRegistry(log);

        userDetailsService = new StaticPscUserDetailsService();
        dataSource = new FakeDataSource();

        bundleContext = new PscTestingBundleContext();
        bundleContext.addService(DataSource.class, dataSource);
        bundleContext.addService(PscUserDetailsService.class, userDetailsService);
    }

    protected <T> T registerMockFor(Class<T> clazz, Method... methods) {
        return getMocks().registerMockFor(clazz, methods);
    }

    protected <T> T registerNiceMockFor(Class<T> clazz, Method... methods) {
        return getMocks().registerNiceMockFor(clazz, methods);
    }

    protected void replayMocks() {
        getMocks().replayMocks();
    }

    protected void verifyMocks() {
        getMocks().verifyMocks();
    }

    protected void resetMocks() {
        getMocks().resetMocks();
    }

    protected MockRegistry getMocks() {
        return mocks;
    }

    public static Configuration blankConfiguration() {
        return new TransientConfiguration(DefaultConfigurationProperties.empty());
    }

    private static class FakeDataSource implements DataSource {
        public Connection getConnection() throws SQLException {
            throw new UnsupportedOperationException("getConnection not implemented");
        }

        public Connection getConnection(String username, String password) throws SQLException {
            throw new UnsupportedOperationException("getConnection not implemented");
        }

        public PrintWriter getLogWriter() throws SQLException {
            throw new UnsupportedOperationException("getLogWriter not implemented");
        }

        public void setLogWriter(PrintWriter out) throws SQLException {
            throw new UnsupportedOperationException("setLogWriter not implemented");
        }

        public void setLoginTimeout(int seconds) throws SQLException {
            throw new UnsupportedOperationException("setLoginTimeout not implemented");
        }

        public int getLoginTimeout() throws SQLException {
            throw new UnsupportedOperationException("getLoginTimeout not implemented");
        }

        //// JDBC4 methods

        public <T> T unwrap(Class<T> iface) throws SQLException {
            throw new UnsupportedOperationException("unwrap not implemented");
        }

        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            throw new UnsupportedOperationException("isWrapperFor not implemented");
        }
    }

    protected static class StaticPscUserDetailsService implements PscUserDetailsService {
        private Map<String, PscUser> users = new HashMap<String, PscUser>();

        public PscUser loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
            PscUser u = users.get(username);
            if (u == null) throw new UsernameNotFoundException(username);
            return u;
        }

        public void addUser(String username, PscRole... roles) {
            users.put(username, AuthorizationObjectFactory.createPscUser(username, roles));
        }
    }
}