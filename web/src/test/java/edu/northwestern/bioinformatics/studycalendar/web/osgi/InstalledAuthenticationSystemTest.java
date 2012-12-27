/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.osgi;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarUserException;
import edu.northwestern.bioinformatics.studycalendar.core.osgi.OsgiLayerTools;
import edu.northwestern.bioinformatics.studycalendar.security.CompleteAuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.tools.configuration.DictionaryConfiguration;
import edu.northwestern.bioinformatics.studycalendar.tools.configuration.RawDataConfiguration;
import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;
import gov.nih.nci.cabig.ctms.tools.configuration.DefaultConfigurationProperties;
import gov.nih.nci.cabig.ctms.web.filters.FilterAdapter;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.context.SecurityContextImpl;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import static org.easymock.EasyMock.expect;
import org.springframework.mock.web.MockFilterChain;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public class InstalledAuthenticationSystemTest extends WebTestCase {
    private InstalledAuthenticationSystem installedSystem;
    private MockCompleteAuthenticationSystem completeAuthenticationSystem;
    private SecurityContextImpl securityContext;
    private OsgiLayerTools osgiLayerTools;
    private RawDataConfiguration storedAuthenticationSystemConfiguration;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        completeAuthenticationSystem = new MockCompleteAuthenticationSystem();
        securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(
            "Walter", "S", new GrantedAuthority[] { new GrantedAuthorityImpl("FOO") }));

        osgiLayerTools = registerNiceMockFor(OsgiLayerTools.class);
        expectCompleteAuthenticationSystemRetrieved();
        storedAuthenticationSystemConfiguration
            = new DictionaryConfiguration(DefaultConfigurationProperties.empty());

        installedSystem = new InstalledAuthenticationSystem();
        installedSystem.setOsgiLayerTools(osgiLayerTools);
        installedSystem.setStoredAuthenticationSystemConfiguration(storedAuthenticationSystemConfiguration);

        SecurityContextHolder.clearContext();
    }

    public void testSecurityContextCopiedFromCompleteAuthenticationSystem() throws Exception {
        final boolean[] chainContinued = { false };

        replayMocks();

        installedSystem.doFilter(request, response, new FilterChain() {
            public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) throws IOException, ServletException {
                assertTrue("Chain continued before the authentication system was run",
                    completeAuthenticationSystem.wasDone());
                assertSame("Security context not available",
                    securityContext, SecurityContextHolder.getContext());
                chainContinued[0] = true;
            }
        });
        verifyMocks();
        assertTrue("Filter did not continue the chain", chainContinued[0]);
    }

    public void testSecurityContextResetAfterExecution() throws Exception {
        SecurityContext oldContext = new SecurityContextImpl();
        SecurityContextHolder.setContext(oldContext);

        replayMocks();
        installedSystem.doFilter(request, response, new MockFilterChain());

        verifyMocks();
        assertNotSame("Embedded security context still available", securityContext, SecurityContextHolder.getContext());
        assertSame("Old security context not restored", oldContext, SecurityContextHolder.getContext());
    }

    private void expectCompleteAuthenticationSystemRetrieved() {
        expect(osgiLayerTools.getRequiredService(CompleteAuthenticationSystem.class)).
            andReturn(completeAuthenticationSystem);
    }

    private class MockCompleteAuthenticationSystem extends FilterAdapter implements CompleteAuthenticationSystem {
        private boolean done = false;

        @Override
        public void doFilter(
            ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain
        ) throws IOException, ServletException {
            done = true;
            filterChain.doFilter(servletRequest, servletResponse);
        }

        public boolean wasDone() { return done; }
        
        public AuthenticationSystem getCurrentAuthenticationSystem() {
            throw new UnsupportedOperationException("getCurrentAuthenticationSystem not implemented");
        }

        public SecurityContext getCurrentSecurityContext() {
            return done ? securityContext : null;
        }

        public StudyCalendarUserException getLastAuthenticationUpdateError() {
            throw new UnsupportedOperationException("getLastAuthenticationUpdateError not implemented");
        }
    }
}
