package edu.northwestern.bioinformatics.studycalendar.security.plugin;

import edu.northwestern.bioinformatics.studycalendar.test.PscTestingBundleContext;
import gov.nih.nci.cabig.ctms.testing.MockRegistry;
import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;
import gov.nih.nci.cabig.ctms.tools.configuration.DefaultConfigurationProperties;
import gov.nih.nci.cabig.ctms.tools.configuration.TransientConfiguration;
import junit.framework.TestCase;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.memory.InMemoryDaoImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Rhett Sutphin
 */
public abstract class AuthenticationTestCase extends TestCase {
    private final Log log = LogFactory.getLog(getClass());
    private MockRegistry mocks;
    protected UserDetailsService userDetailsService;
    protected DataSource dataSource;
    protected PscTestingBundleContext bundleContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mocks = new MockRegistry(log);

        userDetailsService = new InMemoryDaoImpl();
        dataSource = new FakeDataSource();

        bundleContext = new PscTestingBundleContext();
        bundleContext.addService(DataSource.class, dataSource);
        bundleContext.addService(UserDetailsService.class, userDetailsService);
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
    }
}