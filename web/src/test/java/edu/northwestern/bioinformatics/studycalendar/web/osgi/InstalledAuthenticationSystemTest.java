package edu.northwestern.bioinformatics.studycalendar.web.osgi;

import edu.northwestern.bioinformatics.studycalendar.security.CompleteAuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.security.plugin.AuthenticationSystem;
import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;
import gov.nih.nci.cabig.ctms.web.filters.FilterAdapter;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.context.SecurityContextImpl;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import static org.easymock.EasyMock.*;
import org.osgi.framework.BundleContext;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.osgi.mock.MockServiceReference;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public class InstalledAuthenticationSystemTest extends WebTestCase {
    private InstalledAuthenticationSystem filter;
    private MockCompleteAuthenticationSystem completeAuthenticationSystem;
    private SecurityContextImpl securityContext;
    private BundleContext bundleContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        completeAuthenticationSystem = new MockCompleteAuthenticationSystem();
        securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(
            "Walter", "S", new GrantedAuthority[] { new GrantedAuthorityImpl("FOO") }));

        bundleContext = registerNiceMockFor(BundleContext.class);
        expectCompleteAuthenticationSystemRetrieved();

        filter = new InstalledAuthenticationSystem();
        filter.setBundleContext(bundleContext);
        filter.setMembrane(new TransparentMembrane());

        SecurityContextHolder.clearContext();
    }

    public void testSecurityContextCopiedFromCompleteAuthenticationSystem() throws Exception {
        final boolean[] chainContinued = { false };

        replayMocks();

        filter.doFilter(request, response, new FilterChain() {
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
        filter.doFilter(request, response, new MockFilterChain());

        verifyMocks();
        assertNotSame("Embedded security context still available", securityContext, SecurityContextHolder.getContext());
        assertSame("Old security context not restored", oldContext, SecurityContextHolder.getContext());
    }

    private void expectCompleteAuthenticationSystemRetrieved() {
        MockServiceReference sr = new MockServiceReference();
        expect(bundleContext.getServiceReference(CompleteAuthenticationSystem.class.getName())).andReturn(sr);
        expect(bundleContext.getService(sr)).andReturn(completeAuthenticationSystem);
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
    }
}
