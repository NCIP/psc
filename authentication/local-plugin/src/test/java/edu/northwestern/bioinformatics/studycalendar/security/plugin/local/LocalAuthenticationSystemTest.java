package edu.northwestern.bioinformatics.studycalendar.security.plugin.local;

import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationTestCase;
import gov.nih.nci.cabig.ctms.tools.configuration.Configuration;
import org.acegisecurity.providers.ProviderManager;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.dao.DataAccessException;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.FilterChain;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Rhett Sutphin
 */
public class LocalAuthenticationSystemTest extends AuthenticationTestCase {
    private LocalAuthenticationSystem system;
    private Configuration configuration;
    private StaticApplicationContext applicationContext;

    public void setUp() throws Exception {
        super.setUp();
        configuration = blankConfiguration();

        applicationContext = new StaticApplicationContext();
        applicationContext.registerSingleton("pscUserDetailsService", FakeUserDetailsService.class);
        applicationContext.registerSingleton("dataSource", FakeDataSource.class);
        applicationContext.registerSingleton("defaultLogoutFilter", FakeFilter.class);
        applicationContext.refresh();
        system = new LocalAuthenticationSystem();
    }

    public void testInitializeAuthManager() throws Exception {
        replayMocks();
        system.initialize(applicationContext, configuration);
        assertTrue("Wrong type", system.authenticationManager() instanceof ProviderManager);
        ProviderManager manager = (ProviderManager) system.authenticationManager();
        assertEquals("Wrong number of providers", 2, manager.getProviders().size());
        assertTrue("First provider is not PSC-local provider",
                manager.getProviders().get(0) instanceof PscAuthenticationProvider);
    }

    private static class FakeUserDetailsService implements UserDetailsService {
        public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException, DataAccessException {
            throw new UnsupportedOperationException("loadUserByUsername not implemented");
        }
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

    private static class FakeFilter implements Filter {
        public void init(FilterConfig filterConfig) throws ServletException {
            throw new UnsupportedOperationException("init not implemented");
        }

        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
            throw new UnsupportedOperationException("doFilter not implemented");
        }

        public void destroy() {
            throw new UnsupportedOperationException("destroy not implemented");
        }
    }
}
